package launchit.formatter.versions;

import launchit.Launchit;
import launchit.formatter.assets.AssetIndex;
import launchit.formatter.libraries.Artifact;
import launchit.formatter.libraries.LibraryDownloads;

import java.io.File;

public class VersionFile {

    private String name;
    private LibraryDownloads downloads;

    public String getName() {
        return name;
    }

    public LibraryDownloads getDownloads() {
        return downloads;
    }

    public File getLocalFile(Launchit d) {
        Artifact artifact = downloads.getArtifact();
        if (artifact == null)
            return null;
        return new File(d.getConfig().getInstallFolder(), artifact.getPath());
    }
}
