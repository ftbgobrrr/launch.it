package launchit.auth.model;

public class Agent {

    private String name;
    private int version;

    public static Agent getMinecraftAgent() {
        Agent agent = new Agent();
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
