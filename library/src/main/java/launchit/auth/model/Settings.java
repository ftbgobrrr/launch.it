package launchit.auth.model;

import com.google.gson.annotations.Expose;

public class Settings {

    private @Expose String version;
    private @Expose int ram;

    public String getVersion() {
        return version;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
