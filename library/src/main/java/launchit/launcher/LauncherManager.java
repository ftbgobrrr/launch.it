package launchit.launcher;

import launchit.Launchit;
import launchit.downloader.Downloadable;
import launchit.formatter.Manifest;
import launchit.launcher.events.ILauncherHandler;
import launchit.utils.FilesUtils;
import launchit.utils.OperatingSystem;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class LauncherManager {

    private final Launchit it;
    private ILauncherHandler iLauncherHandler; //TODO: replace by event bus

    public LauncherManager(Launchit it) {
        this.it = it;
    }

    /**
     * NEED NETWORK
     *
     * @param type
     * @param source
     */
    public void checkForUpdate(LauncherFile.Type type, File source) {
        it.getExecutorService().execute(() -> {
            try {
                Manifest manifest = it.getRemoteManifest();
                LauncherFiles launcherFiles = manifest.getLauncher();
                LauncherFile lf = type == LauncherFile.Type.LAUNCHER
                        ? launcherFiles.launcher
                        : launcherFiles.getBootloader(OperatingSystem.getCurrentPlatform());
                if (!source.exists() || !FilesUtils.verifyChecksum(source, lf.getSha1())) {
                    iLauncherHandler.endChecking(lf, true);
                    if (source.exists())
                        FileUtils.deleteQuietly(source);
                    return;
                }
                iLauncherHandler.endChecking(lf, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void update(LauncherFile lf, File dest) {
        it.getExecutorService().execute(() -> {
            int current = 0;
            iLauncherHandler.startUpdate(lf);
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(lf.getUrl()).openConnection(Proxy.NO_PROXY);
                connection.setUseCaches(false);
                connection.setDefaultUseCaches(false);
                connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
                connection.setRequestProperty("Expires", "0");
                connection.setRequestProperty("Pragma", "no-cache");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(30000);

                int status = connection.getResponseCode();
                FilesUtils.ensureFileWritable(dest);
                if (status == 200) {
                    int toDownload = connection.getContentLength();
                    InputStream inputStream = connection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(dest);
                    try {
                        byte[] buffer = new byte[65536];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                            current += read;
                            iLauncherHandler.updateProgress(lf, current, toDownload);
                        }
                    }
                    finally {
                        Downloadable.closeSilently(inputStream);
                        Downloadable.closeSilently(outputStream);
                    }
                    iLauncherHandler.updateFinished(lf, false);
                }
            } catch (IOException e) {
                e.printStackTrace();
                iLauncherHandler.updateFinished(lf, true);
            }
        });
    }

    public void setiLauncherHandler(ILauncherHandler iLauncherHandler) {
        this.iLauncherHandler = iLauncherHandler;
    }

    public File getLauncherFile() {
        return new File(it.getConfig().getInstallFolder(), "launcher.jar");
    }

}
