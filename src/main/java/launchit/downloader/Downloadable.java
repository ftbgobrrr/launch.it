package launchit.downloader;

import launchit.Launchit;
import launchit.downloader.errors.ConnectionError;
import launchit.downloader.errors.DownloadError;
import launchit.downloader.errors.InvalidChecksumError;
import launchit.events.DownloaderEvent;
import launchit.formatter.FileType;
import launchit.formatter.libraries.Artifact;
import launchit.formatter.versions.Version;
import launchit.utils.FilesUtils;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Downloadable extends Artifact {

    private FileType type;
    private File localFile;

    public Downloadable(FileType type, File localFile, String path, int size, String url, String sha1) {
        super(path, size, url, sha1);
        this.type = type;
        this.localFile = localFile;
    }

    public Downloadable(FileType type, Artifact artifact, File localFile) {
        this(type, localFile, artifact.getPath(), artifact.getSize(), artifact.getUrl(), artifact.getSha1());
    }

    public FileType getType() {
        return type;
    }

    public File getLocalFile() {
        return localFile;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException ignored) {}
        }
    }

    private void updateExpectedSize(HttpURLConnection connection) {
        if (this.size == 0) {
            this.setSize(connection.getContentLength());
        }
    }

    protected HttpURLConnection makeConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(30000);
        return connection;
    }

    private String copyAndDigest(InputStream inputStream, OutputStream outputStream, Launchit d, Version v, List<DownloadError> errors, DownloadProgress progress) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
        }
        catch (NoSuchAlgorithmException e) {
            Downloadable.closeSilently(inputStream);
            Downloadable.closeSilently(outputStream);
            throw new RuntimeException("Missing Digest.SHA1", e);
        }

        try {
            byte[] buffer = new byte[65536];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
                outputStream.write(buffer, 0, read);
                progress.setProgress(read + progress.getProgress());
                d.getEventBus().post(new DownloaderEvent.Download.Progess(v, errors, this, progress));
            }
        }
        finally {
            Downloadable.closeSilently(inputStream);
            Downloadable.closeSilently(outputStream);
        }

        return String.format("%1$0" + (40) + "x", new BigInteger(1, digest.digest()));
    }

    public void download(Launchit d, Version v, List<DownloadError> errors, DownloadProgress progress) {
        try {
            HttpURLConnection connection = this.makeConnection(new URL(this.getUrl()));
            int status = connection.getResponseCode();
            DownloaderEvent.Download.Pre event = new DownloaderEvent.Download.Pre(v, errors, this);
            d.getEventBus().post(event);
            if (!event.isCanceled()) {
                FilesUtils.ensureFileWritable(getLocalFile());
                if (status == 200) {
                    this.updateExpectedSize(connection);
                    InputStream inputStream = connection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(this.getLocalFile());
                    String digest = this.copyAndDigest(inputStream, outputStream, d, v, errors, progress);
                    if (this.getSha1().equals(digest)) {
                        d.getEventBus().post(new DownloaderEvent.Download.Post(v, errors, this, null));
                        return;
                    }
                    InvalidChecksumError error = new InvalidChecksumError(this);
                    errors.add(error);
                    d.getEventBus().post(new DownloaderEvent.Download.Post(v, errors, this, error));
                }
            }
        } catch (IOException e) {
            ConnectionError error = new ConnectionError(this);
            errors.add(error);
            d.getEventBus().post(new DownloaderEvent.Download.Post(v, errors, this, error));
        }
    }
}
