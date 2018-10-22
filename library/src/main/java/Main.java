import launchit.Launchit;
import launchit.LaunchitConfig;
import launchit.downloader.Downloadable;
import launchit.downloader.errors.DownloadError;
import launchit.downloader.interfaces.DownloaderEventListener;
import launchit.events.GameEvent;
import launchit.formatter.versions.Version;
import launchit.utils.FilesUtils;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;

public class Main implements DownloaderEventListener
{
    Launchit d;
    Version v;

    private void init()
    {
        try {
            d = new LaunchitConfig()
                    .setManifestUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json")
                    .setAssetsServer("http://resources.download.minecraft.net/")
                    .setInstallFolder(FilesUtils.getInstallDir(".test-launcher"))
                    .create();
            v = d.getLocalVersion("1.13.1");
            d.checkForUpdate(v.getId());
            d.getEventBus().register(this);
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
    }

    @Override
    public void downloadFinished(List<DownloadError> errors) {
        System.out.println(errors.size());
        if (errors.size() == 0) {
            d.getGameManager().startGame(v);
        }
    }

    @Subscribe
    public void onGameEvent(GameEvent.Start.Pre event) {
        System.out.println("pre");
        event.setCanceled(true);
    }

    @Subscribe
    public void onGameEvent(GameEvent.Start.Post event) {
        System.out.println("post");
    }

    @Subscribe
    public void onGameEvent(GameEvent.Start event) {
        System.out.println("start");
    }
}
