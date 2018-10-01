package launchit.auth.model;

import com.google.gson.annotations.Expose;

public class RefreshRes {
    private @Expose String accessToken;
    private @Expose String clientToken;
    private @Expose Profile selectedProfile;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public Profile getSelectedProfile() {
        return selectedProfile;
    }
}
