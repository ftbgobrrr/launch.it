package launchit.auth.model;

import com.google.gson.annotations.Expose;

public class Settings {

    private @Expose String version;
    private @Expose String arguments;
    private @Expose boolean consoleAtStartup;
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

    public String getArguments() {
        return arguments == null ? "" : arguments;
    }

    public void setConsoleAtStartup(boolean consoleAtStartup) {
        this.consoleAtStartup = consoleAtStartup;
    }

    public boolean isConsoleAtStartup() {
        return consoleAtStartup;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
}
