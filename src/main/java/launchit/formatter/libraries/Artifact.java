package launchit.formatter.libraries;

import launchit.formatter.FileData;

public class Artifact extends FileData {

    private String path;

    public Artifact(String path, int size, String url, String sha1) {
        super(size, url, sha1);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
