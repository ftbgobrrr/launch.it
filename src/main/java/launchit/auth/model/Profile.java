package launchit.auth.model;

import com.google.gson.annotations.Expose;

public class Profile {

    private @Expose String id;
    private @Expose String name;
    private @Expose boolean isCrack = false;

    private @Expose String accessToken;

    private @Expose Settings settings;

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

    public Settings getSettings() {
        return settings == null ? settings = new Settings() : settings;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCrack(boolean crack) {
        isCrack = crack;
    }

    public boolean isCrack() {
        return isCrack;
    }
}
