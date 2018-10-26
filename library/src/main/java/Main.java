import launchit.Launchit;
import launchit.LaunchitConfig;
import launchit.events.GameEvent;
import launchit.formatter.versions.Version;
import launchit.utils.FilesUtils;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class Main
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
