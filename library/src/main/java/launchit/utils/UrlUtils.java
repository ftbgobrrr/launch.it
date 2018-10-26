package launchit.utils;

import launchit.Launchit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UrlUtils {

    public static URL join(URL u, String ...paths)  {
        try {
            return new URL(u, String.join("/", paths));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static boolean netIsAvailable(String u) {
        try {
            final URL url = new URL(u);
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

    public static boolean netIsAvailable() {
        return netIsAvailable("http://www.google.com");
    }

    public static boolean netIsAvailable(Launchit it) {
        return netIsAvailable(it.getConfig().getManifestUrl().toString());
    }
}
