package launchit.downloader;

import launchit.Launchit;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class LaunchitConfig
{
    private URL manifestUrl;
    private URL assetsServer;
    private File installFolder;

    public LaunchitConfig setManifestUrl(String manifestUrl) throws MalformedURLException {
        this.manifestUrl = new URL(manifestUrl);
        return this;
    }

    public LaunchitConfig setAssetsServer(String server) throws MalformedURLException {
        this.assetsServer = new URL(server);
        return this;
    }

    public LaunchitConfig setInstallFolder(File folder) {
        this.installFolder = folder;
        return this;
    }

    public URL getAssetsServer() {
        return assetsServer;
    }

    public URL getManifestUrl() {
        return manifestUrl;
    }

    public File getInstallFolder() {
        return installFolder;
    }

    public Launchit create()
    {
        return new Launchit(this);
    }
}