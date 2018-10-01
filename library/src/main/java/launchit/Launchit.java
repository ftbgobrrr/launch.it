package launchit;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import launchit.downloader.DownloadProgress;
import launchit.downloader.Downloadable;
import launchit.downloader.LaunchitConfig;
import launchit.downloader.errors.DownloadError;
import launchit.formatter.FileData;
import launchit.formatter.FileType;
import launchit.formatter.Manifest;
import launchit.formatter.adapter.ArgumentSerializer;
import launchit.formatter.adapter.LowerCaseEnumAdapter;
import launchit.formatter.arguments.Argument;
import launchit.formatter.assets.Asset;
import launchit.formatter.assets.AssetIndex;
import launchit.formatter.libraries.Artifact;
import launchit.formatter.libraries.Library;
import launchit.formatter.versions.Version;
import launchit.formatter.versions.VersionType;
import launchit.downloader.interfaces.IFileDownload;
import launchit.utils.FileUtils;
import launchit.utils.UrlUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Launchit
{
    private final LaunchitConfig config;
    private IFileDownload iFileDownload;

    protected Launchit(LaunchitConfig config) {
        this.config = config;
    }


    /**
     * Method that download the versions manifest from the LaunchitConfig::manifestUrl() and return a {@link Manifest} object
     *
     * @return {@link Manifest} the versions manifest from the remote server
     * @throws IOException if the manifest cannot be found
     */
    public Manifest getRemoteManifest() throws IOException {
        return new GsonBuilder()
            .registerTypeAdapterFactory(new LowerCaseEnumAdapter())
            .create()
            .fromJson(IOUtils.toString(this.getConfig().getManifestUrl(), Charsets.UTF_8), Manifest.class);
    }

    public Version getRemoteLatestVersion(Manifest manifest, VersionType type) throws IOException {
        String id = manifest.getLatest().get(type);
        return getRemoteVersion(
                manifest.getVersions()
                    .stream()
                    .filter(v -> v.getId().equals(id) && v.getType().equals(type))
                    .findFirst()
                    .orElseThrow(() -> new Error("Version not found"))
        );
    }

    /**
     *
     * @param manVersion The version reference to find
     * @return a versions
     * @throws IOException if the versions cannot be found
     */
    public Version getRemoteVersion(Manifest.ManVersion manVersion) throws IOException {
        return getVersion(IOUtils.toString(new URL(manVersion.getUrl()), Charsets.UTF_8));
    }

    /**
     *
     * @param id the id of the version
     * @return a version or null if local file not found
     * @throws IOException
     */
    public Version getLocalVersion(String id) throws IOException {
        if (!Version.getLocalVersionFile(this, id).exists())
            return null;
        return getVersion(
                org.apache.commons.io.FileUtils.readFileToString(
                    Version.getLocalVersionFile(this, id),
                    Charsets.UTF_8
                )
        );
    }

    public Version getVersion(String json) {
        return new GsonBuilder()
            .registerTypeAdapterFactory(new LowerCaseEnumAdapter())
            .registerTypeAdapter(new TypeToken<Argument>(){}.getType(), new ArgumentSerializer())
            .enableComplexMapKeySerialization()
            .create()
            .fromJson(
                json,
                Version.class
            );
    }

    public void checkForUpdate(Version version) {

        Thread thread = new Thread(() -> {
            try {
                Version v = version;
                Map<String, Asset> assetMap = v.getLocalAssetsMap(this);
                if (UrlUtils.netIsAvailable()) {
                    Manifest m = getRemoteManifest();
                    Manifest.ManVersion mV = m.getVersion(v.getId());
                    if (mV == null)
                        return;
                    try {
                        File localVersion = Version.getLocalVersionFile(this, v.getId());
                        String versionJson = IOUtils.toString(new URL(mV.getUrl()), Charsets.UTF_8);
                        org.apache.commons.io.FileUtils.writeStringToFile(localVersion, versionJson);
                        v = getVersion(versionJson);
                        File localAssetIndex = AssetIndex.getLocalAssetsIndex(this, v.getAssetIndex());
                        String assetsJson = IOUtils.toString(new URL(v.getAssetIndex().getUrl()), Charsets.UTF_8);
                        org.apache.commons.io.FileUtils.writeStringToFile(localAssetIndex, assetsJson);
                        assetMap = v.getAssetsMap(assetsJson);
                    } catch (IOException e) {
                        e.printStackTrace();
                        //TODO print "unable to get remote files"
                    }
                }

                int toCheck = (int) v.getLibraries().stream()
                                .filter(Library::matchEnvironement)
                                .count()
                            + v.getLocalAssetsMap(this).size()
                            + 1;

                final int[] current = {0};
                List<Downloadable> filesToDownload = new ArrayList<>();
                v.getLibraries().stream()
                    .filter(Library::matchEnvironement)
                    .forEach(library -> {
                        Artifact artifact = library.getEnvironmentLibrary();
                        this.iFileDownload.checkFileStart(artifact, current[0], toCheck);
                        File localFile = library.getLocalFile(this);
                        if (!localFile.exists() || !FileUtils.verifyChecksum(localFile, library.getRemoteSha1()))
                        {
                            this.iFileDownload.checkFileEnd(artifact, current[0]++, toCheck);
                            filesToDownload.add(new Downloadable(FileType.LIBRARY, artifact, localFile));
                            if (localFile.exists())
                                org.apache.commons.io.FileUtils.deleteQuietly(localFile);
                        }

                    });
                assetMap
                    .forEach((key, asset) -> {
                        Artifact artifact = Asset.toArtifact(asset, this);
                        this.iFileDownload.checkFileStart(artifact, current[0], toCheck);
                        File localFile = asset.getLocalFile(this);
                        if (!localFile.exists() || !FileUtils.verifyChecksum(localFile, asset.getHash()))
                        {
                            this.iFileDownload.checkFileEnd(artifact, current[0]++, toCheck);
                            filesToDownload.add(new Downloadable(FileType.ASSET, artifact, localFile));
                            if (localFile.exists())
                                org.apache.commons.io.FileUtils.deleteQuietly(localFile);
                        }
                    });
                File localClient = Version.DownloadType.CLIENT.getLocalFile(this, v);
                FileData clientFileData = v.getDownload(Version.DownloadType.CLIENT);
                Artifact clientArtifact = new Artifact(
                        localClient.getAbsolutePath(),
                        clientFileData.getSize(),
                        clientFileData.getUrl(),
                        clientFileData.getSha1()
                );

                this.iFileDownload.checkFileStart(clientArtifact, current[0], toCheck);
                if (!localClient.exists() || !FileUtils.verifyChecksum(localClient, clientFileData.getSha1())) {
                    filesToDownload.add(new Downloadable(FileType.CLIENT, clientArtifact, localClient));
                    if (localClient.exists())
                        org.apache.commons.io.FileUtils.deleteQuietly(localClient);
                    this.iFileDownload.checkFileEnd(clientArtifact, current[0]++, toCheck);
                }

                this.iFileDownload.checkFinished(filesToDownload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void downloadFiles(List<Downloadable> files, Version v) {
        Thread thread = new Thread(() -> {
            List<DownloadError> errors = new ArrayList<>();
            int total = files.stream()
                    .mapToInt(FileData::getSize)
                    .sum();
            DownloadProgress progress = new DownloadProgress(0, total);
            files.forEach(file -> {
                file.download(this, errors, progress);
            });
            this.getFileListener().downloadFinished(errors);
        });
        thread.start();
    }

    public void setFileListener(IFileDownload iFileDownload) {
        this.iFileDownload = iFileDownload;
    }

    public IFileDownload getFileListener() {
        return iFileDownload;
    }

    public LaunchitConfig getConfig() {
        return config;
    }
}
