package launchit.auth.model;

public class YggdrasilRefreshRes {
    private String accessToken;
    private String clientToken;
    private YggdrasilProfile selectedProfile;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public YggdrasilProfile getSelectedProfile() {
        return selectedProfile;
    }
}
