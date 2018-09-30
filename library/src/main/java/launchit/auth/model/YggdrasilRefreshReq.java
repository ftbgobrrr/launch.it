package launchit.auth.model;

public class YggdrasilRefreshReq {
    private String accessToken;
    private String clientToken;

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }
}
