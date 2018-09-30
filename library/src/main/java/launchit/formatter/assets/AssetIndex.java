package launchit.formatter.assets;

import launchit.Launchit;
import launchit.formatter.FileData;

import java.io.File;

public class AssetIndex extends FileData {

    private String id;
    private int totalSize;

    public AssetIndex(String id, int size, int totalSize, String url, String sha1) {
        super(size, url, sha1);
        this.id = id;
        this.totalSize = totalSize;
    }

    public String getId() {
        return id;
    }

    public int getTotalSize() {
        return totalSize;
    }


    //******************************************************************************************************************

    public static File getLocalAssetsFolder(Launchit d) {
        return new File(d.getConfig().getInstallFolder(), "assets");
    }

    public static File getLocalIndexesFolder(Launchit d) {
        return new File(AssetIndex.getLocalAssetsFolder(d), "indexes");
    }

    public static File getLocalObjectsFolder(Launchit d) {
        return new File(AssetIndex.getLocalAssetsFolder(d), "objects");
    }

    public static File getLocalIndexeFolder(Launchit d, AssetIndex index) {
        return new File(AssetIndex.getLocalIndexesFolder(d), index.getId());
    }

    public static File getLocalAssetsIndex(Launchit d, AssetIndex index) {
        return new File(AssetIndex.getLocalIndexeFolder(d, index), String.format("%s.json", index.getId()));
    }
}
