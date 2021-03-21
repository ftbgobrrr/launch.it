package launchit.downloader.errors;

import launchit.downloader.Downloadable;

public class DownloadError extends Exception {

    public Downloadable file;

    public DownloadError(String s, Downloadable file) {
        super(s);
        this.file = file;
    }
}
