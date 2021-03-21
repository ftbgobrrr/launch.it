package launchit.downloader;

public class DownloadProgress {

    private int progress;
    private int total;

    public DownloadProgress(int progress, int total) {
        this.progress = progress;
        this.total = total;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
