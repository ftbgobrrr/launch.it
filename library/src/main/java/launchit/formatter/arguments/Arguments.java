package launchit.formatter.arguments;

import java.util.List;
import java.util.stream.Collectors;

public class Arguments {

    private List<Argument> game;
    private List<Argument> jvm;

    public List<String> getGameArguments() {
        return game.stream()
                .filter(a -> a.getParam() != null)
                .map(Argument::getParam)
                .collect(Collectors.toList());
    }

    public List<ArgRule> getGameArgRules() {
        return game.stream()
                .filter(a -> a.getRule() != null)
                .map(Argument::getRule)
                .collect(Collectors.toList());
    }
}
