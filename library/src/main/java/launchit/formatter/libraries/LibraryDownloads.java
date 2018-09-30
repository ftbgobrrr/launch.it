package launchit.formatter.libraries;

import java.util.Map;

public class LibraryDownloads {

    private Artifact artifact;
    private Map<String, Artifact> classifiers;

    public Artifact getArtifact() {
        return artifact;
    }

    public Map<String, Artifact> getClassifiers() {
        return classifiers;
    }
}
