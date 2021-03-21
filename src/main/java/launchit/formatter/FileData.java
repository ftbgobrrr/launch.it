package launchit.formatter;

public class FileData {

    protected int size;
    private String url;
    private String sha1;

    public FileData(int size, String url, String sha1) {
        this.size = size;
        this.url = url;
        this.sha1 = sha1;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSha1() {
        return sha1;
    }

    public String getUrl() {
        return url;
    }
}
