package launchit.auth.model;

public class RefreshReq {
    private String accessToken;
    private String clientToken;

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }
}
