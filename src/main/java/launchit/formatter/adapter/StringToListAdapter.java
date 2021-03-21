package launchit.formatter.adapter;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StringToListAdapter implements JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (json.isJsonNull())
            return null;
        List<String> values = new ArrayList<>();
        if (json.isJsonPrimitive()) {
            values.add(json.getAsJsonPrimitive().getAsString());
            return values;
        }

        if (json.isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray()) {
                values.add(e.getAsJsonPrimitive().getAsString());
            }
        }

        return values;
    }
}
