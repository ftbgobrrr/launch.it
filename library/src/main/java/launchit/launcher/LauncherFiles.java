package launchit.launcher;

import launchit.utils.OperatingSystem;

import java.util.List;

public class LauncherFiles {

    public LauncherFile launcher;
    public List<LauncherFile> bootloaders;

    public LauncherFile getLauncher() {
        return launcher;
    }

    public List<LauncherFile> getBootloaders() {
        return bootloaders;
    }

    public LauncherFile getBootloader(OperatingSystem os) {
        return bootloaders.stream()
                .filter(b -> b.getArch() == os)
                .findFirst()
                .orElse(null);
    }
}
