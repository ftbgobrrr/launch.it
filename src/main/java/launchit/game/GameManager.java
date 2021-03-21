package launchit.game;

import launchit.Launchit;
import launchit.formatter.versions.Version;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private final Launchit it;
    private final List<GameInstance> instances;

    public GameManager(Launchit it) {
        this.it = it;
        this.instances = new ArrayList<>();
    }

    public GameInstance startGame(Version version) {
        GameInstance instance = new GameInstance(this, version);
        it.getLogger().info("Launching " + instance.getName());
        instances.add(instance);
        instance.start();
        return instance;
    }

    public void stopGame(GameInstance instance)
    {
        instance.getProcess().destroy();
    }

    public Launchit getLaunchit() {
        return it;
    }

    public List<GameInstance> getInstances() {
        return instances;
    }
}
