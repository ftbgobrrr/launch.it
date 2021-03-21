package launchit.auth.model;

import com.google.gson.annotations.Expose;

import java.util.List;

public class AuthenticateRes {
    private @Expose String accessToken;
    private @Expose String clientToken;
    private @Expose Profile selectedProfile;
    private @Expose List<Profile> availableProfiles;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public Profile getSelectedProfile() {
        return selectedProfile;
    }

    public List<Profile> getAvailableProfiles() {
        return availableProfiles;
    }
}