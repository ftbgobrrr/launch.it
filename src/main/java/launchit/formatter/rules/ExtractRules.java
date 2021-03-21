package launchit.formatter.rules;

import java.util.List;

public class ExtractRules {

    private List<String> exclude;

    public boolean shouldExtract(String path) {
        if (this.exclude != null) {
            for (String rule : this.exclude) {
                if (!path.startsWith(rule)) continue;
                return false;
            }
        }
        return true;
    }

}
