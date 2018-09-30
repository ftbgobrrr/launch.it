package launchit.downloader.errors;

import launchit.downloader.Downloadable;

public class InvalidChecksumError extends DownloadError
{
    public InvalidChecksumError(Downloadable file) {
        super("Invalid Checksum", file);
    }
}
