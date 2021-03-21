package launchit.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class PasteUtils {

    private static String pasteURL = "https://hastebin.com/";


    public static String post(String data) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(pasteURL + "documents");

        try {
            post.setEntity(new StringEntity(data));

            HttpResponse response = client.execute(post);
            String result = EntityUtils.toString(response.getEntity());
            JsonObject object = new JsonParser().parse(result).getAsJsonObject();
            if (object.has("key"))
                return pasteURL + object.get("key").getAsString();
            else if (object.has("message") && object.get("message").getAsString().equals("Document exceeds maximum length."))
                return "MAX_LEN";
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPasteURL() {
        return pasteURL;
    }

    public static void setPasteURL(String URL) {
        pasteURL = URL;
    }

}
