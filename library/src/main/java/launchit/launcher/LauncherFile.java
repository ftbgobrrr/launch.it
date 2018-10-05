package launchit.launcher;

import launchit.utils.OperatingSystem;

public class LauncherFile {

    public enum Type {
        BOOTLOADER,
        LAUNCHER
    }

    private OperatingSystem arch;
    private LauncherFile.Type type;
    private String sha1;
    private String url;

    public String getUrl() {
        return url;
    }

    public String getSha1() {
        return sha1;
    }

    public OperatingSystem getArch() {
        return arch;
    }

    public Type getType() {
        return type;
    }
}
