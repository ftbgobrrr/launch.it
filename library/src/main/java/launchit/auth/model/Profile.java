package launchit.auth.model;

import com.google.gson.annotations.Expose;

public class Profile {

    private @Expose String id;
    private @Expose String name;

    private String accessToken;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
