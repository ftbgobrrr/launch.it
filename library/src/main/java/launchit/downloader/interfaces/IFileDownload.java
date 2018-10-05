package launchit.downloader.interfaces;

import launchit.downloader.DownloadProgress;
import launchit.downloader.Downloadable;
import launchit.downloader.errors.DownloadError;
import launchit.formatter.libraries.Artifact;

import java.io.File;
import java.util.List;

public interface IFileDownload {

    public void downloadFileStart(Downloadable file);

    public void downloadFileEnd(DownloadError error, Downloadable file);

    public void downloadFileProgress(Downloadable file, DownloadProgress progress);

    public void downloadFinished(List<DownloadError> errors);

    public void deleteFileStart(File file, int current, int total);

    public void deleteFileEnd(File file, int current, int total);

    public void deleteFinished(List<File> deletedFiles);

    public void checkFileStart(Artifact file, int current, int toCheck);

    public void checkFileEnd(Artifact file, int current, int toCheck);

    public void checkFinished(List<Downloadable> filesToDownload);
}


