package launchit.formatter.arguments;

public class Argument {

    private String param;
    private ArgRule rule;

    public Argument(String param, ArgRule rule) {
        this.param = param;
        this.rule = rule;
    }

    public String getParam() {
        return param;
    }

    public ArgRule getRule() {
        return rule;
    }
}

