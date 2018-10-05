package launchit.downloader.interfaces;

import launchit.downloader.DownloadProgress;
import launchit.downloader.Downloadable;
import launchit.downloader.errors.DownloadError;
import launchit.formatter.libraries.Artifact;

import java.io.File;
import java.util.List;

public interface DownloaderEventListener extends IFileDownload {

    default void downloadFileStart(Downloadable file) {}
    default void downloadFileEnd(DownloadError error, Downloadable file) {}
    default void downloadFileProgress(Downloadable file, DownloadProgress progress) {}
    default void downloadFinished(List<DownloadError> errors) {}
    default void deleteFileStart(File file, int current, int total) {}
    default void deleteFileEnd(File file, int current, int total) {}
    default void deleteFinished(List<File> deletedFiles) {}
    default void checkFileStart(Artifact file, int current, int toCheck) {}
    default void checkFileEnd(Artifact file, int current, int toCheck) {}
    default void checkFinished(List<Downloadable> filesToDownload) {}

}
