package launchit.utils;

import launchit.Launchit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

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
            return IOUtils.toString(new URI(u), StandardCharsets.UTF_8) != null;
        } catch (URISyntaxException e) {
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
