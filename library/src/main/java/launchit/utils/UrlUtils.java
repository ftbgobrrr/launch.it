package launchit.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UrlUtils {

    public static URL join(URL u, String ...paths) {
        for (int i = 0; i < paths.length; i++) {
            try {
                u = new URL(u, paths[i] + (i == paths.length - 1 ? "" : '/'));
            } catch (MalformedURLException e) {
                break;
            }
        }
        return u;
    }

    public static boolean netIsAvailable() {
        try {
            final URL url = new URL("http://www.google.com");
            final URLConnection conn = url.openConnection();
            conn.setReadTimeout(3);
            conn.connect();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }
}
