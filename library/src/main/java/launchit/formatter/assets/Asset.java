package launchit.formatter.assets;

import launchit.Launchit;
import launchit.formatter.libraries.Artifact;
import launchit.utils.UrlUtils;

import java.io.File;

public class Asset {

    public String hash;
    public int size;

    public int getSize() {
        return size;
    }

    public String getHash() {
        return hash;
    }

    //******************************************************************************************************************

    public String getUrl(Launchit d) {
        return UrlUtils.join(d.getConfig().getAssetsServer(), getHash().substring(0, 2), getHash()).toString();
    }

    public File getLocalFile(Launchit d) {
        return new File(AssetIndex.getLocalObjectsFolder(d), getHash().substring(0, 2) + "/" + getHash());
    }

    public static Artifact toArtifact(Asset a, Launchit d)
    {
        return new Artifact(
                a.getHash().substring(0, 2) + "/" + a.getHash(),
                a.getSize(),
                a.getUrl(d),
                a.getHash()
        );
    }
}
