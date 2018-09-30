package launchit.auth.model;

public class YggdrasilAuthenticateReq {
    private YggdrasilAgent agent;
    private String username;
    private String password;
    private String clientToken;

    public void setAgent(YggdrasilAgent agent) {
        this.agent = agent;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
