package launchit.launcher.events;

import launchit.launcher.LauncherFile;

public interface ILauncherHandler {

    void endChecking(LauncherFile file, boolean needUpdate);
    void startUpdate(LauncherFile file);
    void updateProgress(LauncherFile file, int current, int total);
    void updateFinished(LauncherFile file, boolean error);

}
