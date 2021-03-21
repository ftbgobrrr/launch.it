package launchit.formatter.arguments;

import java.util.List;
import java.util.stream.Collectors;

public class Arguments {

    private List<Argument> game;
    private List<Argument> jvm;

    public List<String> getGameArguments() {
        return getArguments(game);
    }

    public List<ArgRule> getGameArgRules() {
        return getArgRules(game);
    }

    public List<String> getJvmArguments() {
        return getArguments(jvm);
    }

    public List<ArgRule> getJvmArgRules() {
        return getArgRules(jvm);
    }

    private List<String> getArguments(List<Argument> args)
    {
        return args.stream()
                .filter(a -> a.getParam() != null)
                .map(Argument::getParam)
                .collect(Collectors.toList());
    }

    private List<ArgRule> getArgRules(List<Argument> args)
    {
        return args.stream()
                .filter(a -> a.getRule() != null)
                .map(Argument::getRule)
                .collect(Collectors.toList());
    }
}
