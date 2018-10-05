import launchit.Launchit;
import launchit.LaunchitConfig;
import launchit.downloader.Downloadable;
import launchit.downloader.errors.DownloadError;
import launchit.formatter.libraries.Artifact;
import launchit.formatter.versions.Version;
import launchit.downloader.interfaces.DownloaderEventListener;
import launchit.game.GameManager;
import launchit.utils.FilesUtils;

import java.io.IOException;
import java.util.List;

public class Main implements DownloaderEventListener
{
    Launchit d;
    Version v;

    public void init()
    {
        try {
            d = new LaunchitConfig()
                    .setManifestUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json")
                    .setAssetsServer("http://resources.download.minecraft.net/")
                    .setInstallFolder(FilesUtils.getInstallDir(".test-launcher"))
                    .create();
            d.setFileListener(this);
            v = d.getLocalVersion("1.13.1");
            d.checkForUpdate(v.getId());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) 
    {
        Main main = new Main();
        main.init();
    }


    @Override
    public void checkFinished(List<Downloadable> filesToDownload) {
        System.out.println(filesToDownload.size());
        d.downloadFiles(filesToDownload, v);
    }

    @Override
    public void downloadFinished(List<DownloadError> errors) {
        System.out.println(errors.size());
        if (errors.size() == 0) {
            d.getGameManager().start(v);
        }
    }
}
