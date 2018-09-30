package launchit.formatter.versions;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import launchit.Launchit;
import launchit.formatter.*;
import launchit.formatter.adapter.LowerCaseEnumAdapter;
import launchit.formatter.arguments.Arguments;
import launchit.formatter.assets.Asset;
import launchit.formatter.assets.AssetIndex;
import launchit.formatter.libraries.Library;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Version {

    public enum DownloadType {
        CLIENT,
        SERVER;

        public File getLocalFile(Launchit d, Version v) {
            return new File(Version.getLocalVersionFolder(d, v.getId()), String.format("%s.jar", name().toLowerCase()));
        }
    }

    private Arguments arguments;

    private String id;
    private String assets;
    private String releaseTime;
    private String time;
    private String mainClass;

    private Map<DownloadType, FileData> downloads;
    private List<Library> libraries;

    private int minimumLauncherVersion;
    private VersionType type;
    private AssetIndex assetIndex;

    public String getId() {
        return id;
    }

    public String getAssets() {
        return assets;
    }

    public Map<DownloadType, FileData> getDownloads() {
        return downloads;
    }

    public FileData getDownload(DownloadType type) {
        return getDownloads().get(type);
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public String getTime() {
        return time;
    }

    public String getMainClass() {
        return mainClass;
    }

    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }

    public VersionType getType() {
        return type;
    }

    public AssetIndex getAssetIndex() {
        return assetIndex;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public Arguments getArguments() {
        return arguments;
    }

    //******************************************************************************************************************

    public static File getLocalVersionsFolder(Launchit d) {
        return new File(d.getConfig().getInstallFolder(), "versions");
    }

    public static File getLocalVersionFolder(Launchit d, String version) {
        return new File(Version.getLocalVersionsFolder(d), version);
    }

    public static File getLocalVersionFile(Launchit d, String version) {
        return new File(Version.getLocalVersionFolder(d, version), String.format("%s.json", version));
    }

    public Map<String, Asset> getRemoteAssetsMap() throws IOException {
        return getAssetsMap(IOUtils.toString(new URL(getAssetIndex().getUrl()), Charsets.UTF_8));
    }

    public Map<String, Asset> getLocalAssetsMap(Launchit d) throws IOException {
        if (!AssetIndex.getLocalAssetsIndex(d, getAssetIndex()).exists())
            return null;
        return getAssetsMap(
            FileUtils.readFileToString(
                AssetIndex.getLocalAssetsIndex(d, getAssetIndex()),
                Charsets.UTF_8
            )
        );
    }

    public Map<String, Asset> getAssetsMap(String json) {
        JsonElement elem = new JsonParser().parse(json);
        return new GsonBuilder()
            .registerTypeAdapterFactory(new LowerCaseEnumAdapter())
            .create()
            .fromJson(
                    elem.getAsJsonObject().get("objects"),
                    new TypeToken<Map<String, Asset>>(){}.getType()
            );
    }
}
