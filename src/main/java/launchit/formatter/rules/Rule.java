package launchit.formatter.rules;

public class Rule {

    public enum Action {
        ALLOW,
        DISALLOW
    }

    private Action action;
    private OsRule os;

    public Action getAction() {
        if (os != null && !os.isCurrentOs())
            return null;
        return action;
    }
}
