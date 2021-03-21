package launchit.formatter.adapter;

import com.google.gson.*;
import launchit.formatter.arguments.ArgRule;
import launchit.formatter.arguments.Argument;

import java.lang.reflect.Type;

public class ArgumentSerializer implements JsonDeserializer<Argument> {
    @Override
    public Argument deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull())
            return null;

        if (json.isJsonPrimitive())
            return new Argument(json.getAsJsonPrimitive().getAsString(), null);
        if (json.isJsonObject()) {
            return new Argument(
                    null,
                    new GsonBuilder()
                            .registerTypeAdapterFactory(new LowerCaseEnumAdapter())
                            .create()
                            .fromJson(json.getAsJsonObject(), ArgRule.class)
            );
        }
        return null;
    }
}
