package launchit.downloader.errors;

import launchit.downloader.Downloadable;

public class ConnectionError extends DownloadError {

    public ConnectionError(Downloadable file) {
        super("Could not connect to server", file);
    }
}
