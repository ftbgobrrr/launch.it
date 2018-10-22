package launchit.events;

import launchit.downloader.DownloadProgress;
import launchit.downloader.Downloadable;
import launchit.downloader.errors.DownloadError;
import launchit.events.factory.Event;
import launchit.formatter.libraries.Artifact;
import launchit.formatter.versions.Version;

import java.io.File;
import java.util.List;

public class DownloaderEvent extends Event {

    private final Version version;

    /*
        Warning! Versions instances may not be the same between events !
        Use instead Version.getId() to compare
     */
    public DownloaderEvent(Version version) {
        this.version = version;
    }

    public static class Delete extends DownloaderEvent {

        private final List<File> deletedFiles;
        private final int current;
        private final int toDelete;

        public Delete(Version v, List<File> deletedFiles, int current, int toDelete) {
            super(v);
            this.deletedFiles = deletedFiles;
            this.current = current;
            this.toDelete = toDelete;
        }

        public List<File> getDeletedFiles() {
            return deletedFiles;
        }

        public int getCurrent() {
            return current;
        }

        public int toDeleteCount() {
            return toDelete;
        }

        public static class Pre extends Delete {

            private final File file;

            public Pre(Version v, List<File> deletedFiles, File file, int current, int toDelete) {
                super(v, deletedFiles, current, toDelete);
                this.file = file;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }
        }

        public static class Post extends Delete {

            private final File file;

            public Post(Version v, List<File> deletedFiles, File file, int current, int toDelete) {
                super(v, deletedFiles, current, toDelete);
                this.file = file;
            }
        }

        public static class Finished extends Delete {

            public Finished(Version v, List<File> deletedFiles, int current, int toDelete) {
                super(v, deletedFiles, current, toDelete);
            }
        }
    }

    public static class Check extends DownloaderEvent {

        private final List<Downloadable> filesToDownload;
        private final int current;
        private final int toCheck;

        private Check(Version v, List<Downloadable> filesToDownload, int current, int toCheck) {
            super(v);
            this.filesToDownload = filesToDownload;
            this.current = current;
            this.toCheck = toCheck;
        }

        public List<Downloadable> getFilesToDownload() {
            return filesToDownload;
        }

        public int getCurrent() {
            return current;
        }

        public int toCheckCount() {
            return toCheck;
        }

        public static class Pre extends Check {

            private final Artifact artifact;

            public Pre(Version v, List<Downloadable> filesToDownload, Artifact artifact, int current, int toCheck) {
                super(v, filesToDownload, current, toCheck);
                this.artifact = artifact;
            }

            public Artifact getArtifact() {
                return artifact;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }
        }

        public static class Post extends Check {

            private final Artifact artifact;

            public Post(Version v, List<Downloadable> filesToDownload, Artifact artifact, int current, int toCheck) {
                super(v, filesToDownload, current, toCheck);
                this.artifact = artifact;
            }

            public Artifact getArtifact() {
                return artifact;
            }
        }

        public static class Finished extends Check {

            public Finished(Version v, List<Downloadable> filesToDownload, int current, int toDelete) {
                super(v, filesToDownload, current, toDelete);
            }
        }
    }

    public static class Download extends DownloaderEvent {

        private final List<DownloadError> errors;

        public Download(Version version, List<DownloadError> errors) {
            super(version);
            this.errors = errors;
        }

        public List<DownloadError> getErrors() {
            return errors;
        }

        public static class Pre extends Download {

            private final Downloadable downloadable;

            public Pre(Version version, List<DownloadError> errors, Downloadable downloadable) {
                super(version, errors);
                this.downloadable = downloadable;
            }

            public Downloadable getDownloadable() {
                return downloadable;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }
        }

        public static class Post extends Download {

            private final Downloadable downloadable;
            private final DownloadError error;

            public Post(Version version, List<DownloadError> errors, Downloadable downloadable, DownloadError error) {
                super(version, errors);
                this.downloadable = downloadable;
                this.error = error;
            }

            public Downloadable getDownloadable() {
                return downloadable;
            }

            public DownloadError getError() {
                return error;
            }
        }

        public static class Progess extends Download {

            private final Downloadable downloadable;
            private final DownloadProgress progress;

            public Progess(Version version, List<DownloadError> errors, Downloadable downloadable, DownloadProgress progress) {
                super(version, errors);
                this.downloadable = downloadable;
                this.progress = progress;
            }
        }

        public static class Finished extends Download {

            public Finished(Version version, List<DownloadError> errors) {
                super(version, errors);
            }
        }
    }

}
