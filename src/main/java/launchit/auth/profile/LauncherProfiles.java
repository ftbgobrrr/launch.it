package launchit.auth.profile;

import com.google.gson.annotations.Expose;
import launchit.auth.model.Profile;

import java.util.HashMap;
import java.util.Map;

public class LauncherProfiles {

    private @Expose String clientToken;
    private @Expose String selectedProfile;
    private @Expose Map<String, Profile> profiles;

    public Map<String, Profile> getProfiles() {
        return profiles == null ? profiles = new HashMap<>() : profiles;
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getSelectedProfile() {
        return selectedProfile;
    }

    public void setSelectedProfile(String selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }
}
