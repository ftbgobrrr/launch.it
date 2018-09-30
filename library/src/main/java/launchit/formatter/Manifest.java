package launchit.formatter;

import launchit.formatter.versions.VersionType;
import java.util.List;
import java.util.Map;

public class Manifest {

    public class ManVersion {

        private String id;
        private VersionType type;
        private String url;
        private String time;
        private String releaseTime;

        public String getId() {
            return id;
        }

        public String getReleaseTime() {
            return releaseTime;
        }

        public String getTime() {
            return time;
        }

        public String getUrl() {
            return url;
        }

        public VersionType getType() {
            return type;
        }
    }

    private Map<VersionType, String> latest;
    private List<ManVersion> versions;

    public List<ManVersion> getVersions() {
        return versions;
    }

    public Map<VersionType, String> getLatest() {
        return latest;
    }


    public ManVersion getVersion(String id) {
        return getVersions()
                .stream()
                .filter(version -> version.getId().equals(id))
                .findFirst()
                .orElseGet(null);
    }
}
