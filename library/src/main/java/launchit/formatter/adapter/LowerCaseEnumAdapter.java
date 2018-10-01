package launchit.formatter.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LowerCaseEnumAdapter implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();
        if (!rawType.isEnum())
            return null;

        final Map<String, T> lowercaseToConstant = new HashMap<>();

        for (T constant : rawType.getEnumConstants())
            lowercaseToConstant.put(constant.toString().toLowerCase(Locale.US), constant);

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value == null)
                    out.nullValue();
                else
                    out.value(value.toString().toLowerCase(Locale.US));
            }

            @Override
            public T read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL)
                {
                    in.nextNull();
                    return null;
                }
                return lowercaseToConstant.get(in.nextString());
            }
        };
    }
}
