package launchit;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class LaunchitConfig
{
    private URL manifestUrl;
    private URL assetsServer;
    private URL authServer;
    private File installFolder;
    private String launcherName;

    public LaunchitConfig() {
        try {
            authServer = new URL("https://authserver.mojang.com/");
            assetsServer = new URL("http://resources.download.minecraft.net/");
            manifestUrl = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
            launcherName = "LauncherManager";
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public LaunchitConfig setManifestUrl(String manifestUrl) throws MalformedURLException {
        this.manifestUrl = new URL(manifestUrl);
        return this;
    }

    public LaunchitConfig setAssetsServer(String server) throws MalformedURLException {
        this.assetsServer = new URL(server);
        return this;
    }

    public LaunchitConfig setAuthServer(String server) throws MalformedURLException {
        this.authServer = new URL(server);
        return this;
    }

    public LaunchitConfig setLauncherName(String launcherName) {
        this.launcherName = launcherName;
        return this;
    }

    public LaunchitConfig setInstallFolder(File folder) {
        this.installFolder = folder;
        return this;
    }

    public URL getAssetsServer() {
        return assetsServer;
    }

    public URL getAuthServer() {
        return authServer;
    }

    public URL getManifestUrl() {
        return manifestUrl;
    }

    public File getInstallFolder() {
        return installFolder;
    }

    public String getLauncherName() {
        return launcherName;
    }

    public Launchit create()
    {
        return new Launchit(this);
    }
}