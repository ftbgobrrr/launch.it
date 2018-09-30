package launchit.auth.model;

import java.util.List;

public class YggdrasilAuthenticateRes {
    private String accessToken;
    private String clientToken;
    private YggdrasilProfile selectedProfile;
    private List<YggdrasilProfile> availableProfiles;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public YggdrasilProfile getSelectedProfile() {
        return selectedProfile;
    }

    public List<YggdrasilProfile> getAvailableProfiles() {
        return availableProfiles;
    }
}