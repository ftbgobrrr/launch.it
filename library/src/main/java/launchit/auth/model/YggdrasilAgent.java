package launchit.auth.model;

public class YggdrasilAgent {
    private String name;
    private int version;

    public static YggdrasilAgent getMinecraftAgent() {
        YggdrasilAgent agent = new YggdrasilAgent();
        agent.setName("Minecraft");
        agent.setVersion(1);
        return agent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
