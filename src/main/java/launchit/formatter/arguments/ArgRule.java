package launchit.formatter.arguments;

import com.google.gson.annotations.JsonAdapter;
import launchit.formatter.adapter.StringToListAdapter;
import launchit.formatter.rules.Rule;

import java.util.List;

public class ArgRule {

    public List<Rule> rules;
    @JsonAdapter(StringToListAdapter.class)
    private List<String> value;
}
