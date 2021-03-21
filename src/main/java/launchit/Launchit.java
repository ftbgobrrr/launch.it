package launchit;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.sentry.Sentry;
import launchit.auth.SessionManager;
import launchit.downloader.DownloadProgress;
import launchit.downloader.Downloadable;
import launchit.downloader.errors.DownloadError;
import launchit.events.DownloaderEvent;
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
import launchit.formatter.versions.VersionFile;
import launchit.formatter.versions.VersionType;
import launchit.game.GameManager;
import launchit.launcher.LauncherManager;
import launchit.utils.FilesUtils;
import launchit.utils.OperatingSystem;
import launchit.utils.UrlUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Launchit
{
    private final LaunchitConfig config;
    private final SessionManager sessionManager;
    private final GameManager gameManager;
    private final LauncherManager launcherManager;
    private final Logger logger;
    private ExecutorService executorService;
    private EventBus eventBus;

    protected Launchit(LaunchitConfig config) {
        this.config = config;
        this.logger = Logger.getLogger(config.getLauncherName());
        this.executorService = Executors.newFixedThreadPool(5);
        this.eventBus = EventBus.builder()
                .logNoSubscriberMessages(false)
                .build();
        this.sessionManager = new SessionManager(this, false);
        this.gameManager = new GameManager(this);
        this.launcherManager = new LauncherManager(this);

        Sentry.init(options -> {
            options.setDsn("https://beca9f15cf5c4393953bea2581163733@o543871.ingest.sentry.io/5664930");
            options.setRelease("hennequince-launcher-v2");
            options.setAttachThreads(true);
        });

        Sentry.configureScope(scope -> {
            scope.setContexts("os", OperatingSystem.getCurrentPlatform().getName());
        });
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
            .fromJson(IOUtils.toString(this.getConfig().getManifestUrl(), StandardCharsets.UTF_8), Manifest.class);
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
        return getVersion(IOUtils.toString(new URL(manVersion.getUrl()), StandardCharsets.UTF_8));
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
                    StandardCharsets.UTF_8
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

    public void checkForUpdate(String version) {
        executorService.execute(() -> {
            try {
                getLogger().info("Start checking for updates on version " + version);
                Version local = getLocalVersion(version);
                Version remote = null;
                String remoteJson = null;
                if (UrlUtils.netIsAvailable(this)) {
                    Manifest m = getRemoteManifest();
                    Manifest.ManVersion mV = m.getVersion(version);
                    if (mV == null) {
                        getLogger().severe("Unable to get manifest version of " + version);
                        return;
                    }
                    remoteJson = IOUtils.toString(new URL(mV.getUrl()), StandardCharsets.UTF_8);
                    remote = getVersion(remoteJson);
                    if(local == null)
                        local = remote;
                }

                getLogger().info("Checking files to delete on " + version);
                File librariesFolder = Library.getLibrariesFolder(this);
                File assetsFolder = AssetIndex.getLocalObjectsFolder(this);
                Collection<File> lfs = librariesFolder.exists() ? FileUtils.listFiles(Library.getLibrariesFolder(this), new String[]{ "jar" }, true) : new ArrayList<>();
                Collection<File> assets = assetsFolder.exists() ? FileUtils.listFiles(AssetIndex.getLocalObjectsFolder(this), null, true) : new ArrayList<>();
                IOFileFilter fileFilter = FileFilterUtils.and(
                    FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("libraries", null)),
                    FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("assets", null)),
                    FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("versions", null))
                );
                List<File> files = (List<File>) FileUtils.listFiles(getConfig().getInstallFolder(), TrueFileFilter.INSTANCE, fileFilter);

                Version finalRemote = remote;
                final int filesCount = lfs.size() + assets.size() + files.size();
                final int[] current = {0};
                List<File> deletedFiles = new ArrayList<>();
                Version finalLocal = local;
                lfs.forEach(lib -> {
                    Library remoteLib = finalRemote != null ? finalRemote.getLibrary(this, lib) : null;
                    Library localLib = finalLocal.getLibrary(this, lib);
                    if (remoteLib == null && localLib == null)
                        return;
                    if (localLib != null && finalRemote != null && remoteLib == null)
                    {
                        DownloaderEvent.Delete.Pre event = new DownloaderEvent.Delete.Pre(finalLocal, deletedFiles, lib, current[0], filesCount);
                        this.getEventBus().post(event);
                        if (!event.isCanceled())
                            FileUtils.deleteQuietly(lib);
                        this.getEventBus().post(new DownloaderEvent.Delete.Post(finalLocal, deletedFiles, lib, current[0]++, filesCount));
                    }
                    current[0]++;
                });

                Map<String, Asset> remoteAssetsMap = finalRemote != null ? finalRemote.getRemoteAssetsMap() : null;
                Map<String, Asset> localAssetMap = local.getLocalAssetsMap(this);

                assets.forEach(asset -> {

                    Asset remoteAsset = finalRemote != null ? finalRemote.getAsset(this, remoteAssetsMap, asset) : null;
                    Asset localAsset = finalLocal.getAsset(this, localAssetMap, asset);

                    if (remoteAsset == null && localAsset == null)
                        return;
                    if (localAsset != null && finalRemote != null && remoteAsset == null)
                    {
                        DownloaderEvent.Delete.Pre event = new DownloaderEvent.Delete.Pre(finalLocal, deletedFiles, asset, current[0], filesCount);
                        this.getEventBus().post(event);
                        if (!event.isCanceled())
                            FileUtils.deleteQuietly(asset);
                        this.getEventBus().post(new DownloaderEvent.Delete.Post(finalLocal, deletedFiles, asset, current[0]++, filesCount));
                    }
                    current[0]++;
                });

                files.forEach(file -> {
                    VersionFile remoteFile = finalRemote != null ? finalRemote.getFile(this, file) : null;
                    VersionFile localFile = finalLocal.getFile(this, file);
                    if (remoteFile == null && localFile == null)
                        return;
                    if (localFile != null && finalRemote != null && remoteFile == null)
                    {
                        DownloaderEvent.Delete.Pre event = new DownloaderEvent.Delete.Pre(finalLocal, deletedFiles, file, current[0], filesCount);
                        this.getEventBus().post(event);
                        if (!event.isCanceled())
                            FileUtils.deleteQuietly(file);
                        this.getEventBus().post(new DownloaderEvent.Delete.Post(finalLocal, deletedFiles, file, current[0]++, filesCount));
                    }
                    current[0]++;
                });
                getLogger().info(deletedFiles.size() + " files deleted on on " + version);
                this.getEventBus().post(new DownloaderEvent.Delete.Finished(finalLocal, deletedFiles, current[0], filesCount));

                Version v = local;
                Map<String, Asset> assetMap = v.getLocalAssetsMap(this);
                if (remoteJson != null && remote != null) {
                    try {
                        File localVersionFile = Version.getLocalVersionFile(this, v.getId());
                        String json = new GsonBuilder()
                                            .setPrettyPrinting()
                                            .create()
                                            .toJson(new JsonParser().parse(remoteJson).getAsJsonObject());
                        FileUtils.writeStringToFile(localVersionFile, json, StandardCharsets.UTF_8);
                        v = remote;
                    } catch (IOException e) {
                        e.printStackTrace();
                        getLogger().severe("unable to get remote version file use local instead");
                    }
                }


                if (remoteAssetsMap != null) {
                    try {
                        File localAssetsFile = AssetIndex.getLocalAssetsIndex(this, v.getAssetIndex());
                        Map<String, Object> index = new HashMap<>();
                        index.put("objects", remoteAssetsMap);
                        String json = new GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(index);
                        FileUtils.writeStringToFile(localAssetsFile, json, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        e.printStackTrace();
                        getLogger().severe("unable to get remote assets index local instead");
                    }
                    assetMap = remoteAssetsMap;
                }

                final int toCheck = (int) v.getLibraries().stream()
                                .filter(Library::matchEnvironement)
                                .count()
                            + v.getFiles().size()
                            + assetMap.size()
                            + 1;

                current[0] = 0;
                List<Downloadable> filesToDownload = new ArrayList<>();
                v.getLibraries().stream()
                    .filter(Library::matchEnvironement)
                    .forEach(library -> {
                        Artifact artifact = library.getEnvironmentLibrary();
                        DownloaderEvent.Check.Pre event = new DownloaderEvent.Check.Pre(finalLocal, filesToDownload, artifact, current[0], toCheck);
                        this.getEventBus().post(event);
                        if (!event.isCanceled()) {
                            File localFile = library.getLocalFile(this);
                            if (!localFile.exists() || !FilesUtils.verifyChecksum(localFile, library.getRemoteSha1())) {
                                filesToDownload.add(new Downloadable(FileType.LIBRARY, artifact, localFile));
                                if (localFile.exists())
                                    FileUtils.deleteQuietly(localFile);
                            }
                        }
                        this.getEventBus().post(new DownloaderEvent.Check.Post(finalLocal, filesToDownload, artifact, current[0]++, toCheck));
                    });

                assetMap.forEach((key, asset) -> {
                    Artifact artifact = Asset.toArtifact(asset, this);
                    DownloaderEvent.Check.Pre event = new DownloaderEvent.Check.Pre(finalLocal, filesToDownload, artifact, current[0], toCheck);
                    this.getEventBus().post(event);
                    if (!event.isCanceled()) {
                        File localFile = asset.getLocalFile(this);
                        if (!localFile.exists() || !FilesUtils.verifyChecksum(localFile, asset.getHash())) {

                            filesToDownload.add(new Downloadable(FileType.ASSET, artifact, localFile));
                            if (localFile.exists())
                                FileUtils.deleteQuietly(localFile);
                        }
                    }
                    this.getEventBus().post(new DownloaderEvent.Check.Post(finalLocal, filesToDownload, artifact, current[0]++, toCheck));
                });

                v.getFiles().forEach(file -> {
                    Artifact artifact = file.getDownloads().getArtifact();
                    DownloaderEvent.Check.Pre event = new DownloaderEvent.Check.Pre(finalLocal, filesToDownload, artifact, current[0], toCheck);
                    this.getEventBus().post(event);
                    if (!event.isCanceled()) {
                        File localFile = file.getLocalFile(this);
                        if (!localFile.exists() || !FilesUtils.verifyChecksum(localFile, artifact.getSha1())) {
                            filesToDownload.add(new Downloadable(FileType.OTHER, artifact, localFile));
                            if (localFile.exists())
                                FileUtils.deleteQuietly(localFile);
                        }
                    }
                    this.getEventBus().post(new DownloaderEvent.Check.Post(finalLocal, filesToDownload, artifact, current[0]++, toCheck));
                });
                File localClient = Version.DownloadType.CLIENT.getLocalFile(this, v);
                FileData clientFileData = v.getDownload(Version.DownloadType.CLIENT);
                Artifact clientArtifact = new Artifact(
                        localClient.getAbsolutePath(),
                        clientFileData.getSize(),
                        clientFileData.getUrl(),
                        clientFileData.getSha1()
                );

                DownloaderEvent.Check.Pre event = new DownloaderEvent.Check.Pre(finalLocal, filesToDownload, clientArtifact, current[0], toCheck);
                this.getEventBus().post(event);
                if (!event.isCanceled()) {
                    if (!localClient.exists() || !FilesUtils.verifyChecksum(localClient, clientFileData.getSha1())) {
                        filesToDownload.add(new Downloadable(FileType.CLIENT, clientArtifact, localClient));
                        if (localClient.exists())
                            FileUtils.deleteQuietly(localClient);

                    }
                }
                this.getEventBus().post(new DownloaderEvent.Check.Post(finalLocal, filesToDownload, clientArtifact, current[0]++, toCheck));
                getLogger().info("There is " + filesToDownload.size() + " files to download");
                this.getEventBus().post(new DownloaderEvent.Check.Finished(finalLocal, filesToDownload, current[0], toCheck));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void downloadFiles(Version v, List<Downloadable> files) {
        Thread thread = new Thread(() -> {
            List<DownloadError> errors = new ArrayList<>();
            int total = files.stream()
                    .mapToInt(FileData::getSize)
                    .sum();
            DownloadProgress progress = new DownloadProgress(0, total);
            files.forEach(file -> file.download(this, v, errors, progress));
            this.getEventBus().post(new DownloaderEvent.Download.Finished(v, errors));
        });
        thread.start();
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public LauncherManager getLauncherManager() {
        return launcherManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public LaunchitConfig getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }
}
