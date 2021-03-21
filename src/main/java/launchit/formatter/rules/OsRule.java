package launchit.formatter.rules;

import launchit.utils.OperatingSystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OsRule {

    private OperatingSystem name;
    private String version;
    private String arch;

    public boolean isCurrentOs() {
        if(this.name != OperatingSystem.getCurrentPlatform())
            return false;

        Matcher matcher;
        Pattern pattern;

        if (this.version != null) {
            pattern = Pattern.compile(this.version);
            matcher = pattern.matcher(System.getProperty("os.version"));
            if (!matcher.matches())
                return false;
        }

        if (this.arch != null) {
            pattern = Pattern.compile(this.arch);
            matcher = pattern.matcher(System.getProperty("os.arch"));
            if (!matcher.matches())
                return false;
        }
        return true;
    }
}
