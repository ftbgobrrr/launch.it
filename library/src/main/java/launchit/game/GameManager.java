package launchit.game;

import com.google.common.io.Files;
import launchit.Launchit;
import launchit.auth.model.Profile;
import launchit.auth.profile.LauncherProfiles;
import launchit.downloader.Downloadable;
import launchit.formatter.assets.Asset;
import launchit.formatter.assets.AssetIndex;
import launchit.formatter.libraries.Artifact;
import launchit.formatter.libraries.Library;
import launchit.formatter.rules.ExtractRules;
import launchit.formatter.versions.Version;
import launchit.game.game.IGameListener;
import launchit.utils.OperatingSystem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameManager {

    private final Launchit it;
    private Version version;
    private LimitedCapacityList<String> logs;
    private List<Runnable> threads;
    private IGameListener gameListener;

    public GameManager(Launchit it) {
        this.it = it;
        logs = new LimitedCapacityList<>(String.class, 10);
        threads = new ArrayList<>();
    }

    public Runnable start(Version version) {
        this.version = version;
        Runnable runable = new Runnable() {
            @Override
            public void run() {
                List<String> arguments = new ArrayList<>();
                arguments.add(OperatingSystem.getCurrentPlatform().getJavaDir());
                arguments.addAll(jvmArguments(version.getArguments().getJvmArguments()));
                arguments.add(version.getMainClass());
                arguments.addAll(gameArguments(version.getArguments().getGameArguments()));

                try {
                    ProcessBuilder pb = new ProcessBuilder(arguments);
                    pb.directory(it.getConfig().getInstallFolder());
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    output(process.getInputStream());
                    int error = process.waitFor();
                    onGameStop(this, error);
                }  catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runable);
        thread.setName("Game");
        thread.start();
        threads.add(runable);
        return runable;
    }

    private void onGameStop(Runnable runnable, int error)
    {
        getGameListener().gameClosedEvent(runnable, error);
        if (error == 0) {
            return;
        }

        String errorText = null;
        String[] sysOut = logs.getItems();
        int lines = sysOut.length - 1;
        while (lines >= 0) {
            String inputStream = sysOut[lines];
            int e = inputStream.lastIndexOf("#@!@#");
            if (e >= 0 && e < inputStream.length() - "#@!@#".length() - 1) {
                errorText = inputStream.substring(e + "#@!@#".length()).trim();
                break;
            }
            --lines;
        }

        if (errorText != null) {
            try {
                Desktop.getDesktop().open(new File(errorText).getParentFile());
            } catch (IOException e) { e.printStackTrace(); }
        }

    }

    private void output(InputStream inputStream) throws IOException {
        BufferedReader br = null;
        try {

            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                logs.add(line + System.getProperty("line.separator"));
                System.out.println(line);
            }
        } finally {
            if (br != null)
                br.close();
        }
    }

    public String getClasspath() {
        try {
            StringBuilder sb = new StringBuilder();
            version.getLibraries()
                    .stream()
                    .filter(Library::matchEnvironement)
                    .forEach(library -> {
                        try {
                            sb.append(library.getLocalFile(it).getCanonicalPath());
                            sb.append(":");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            sb.append(Version.DownloadType.CLIENT.getLocalFile(it, version).getCanonicalPath());
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<String> jvmArguments(List<String> jsonArguments) {
        List<String> args = new ArrayList<>();
        try {
            LauncherProfiles launcherProfiles = it.getSessionManager().getLauncherProfiles();
            Profile profile = launcherProfiles.getProfiles().get(launcherProfiles.getSelectedProfile());
            File tempDirFile = Files.createTempDir();
            String tempDir = tempDirFile.getCanonicalPath();
            this.unpackNatives(tempDirFile);
            args.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
            args.add("-Dfml.ignorePatchDiscrepancies=true");
            args.add("-Dminecraft.client.jar=" + Version.DownloadType.CLIENT.getLocalFile(it, version).getCanonicalPath());

            int ram = OperatingSystem.getArchMinRam();
            if (profile.getSettings().getRam() != 0)
                ram = ram < OperatingSystem.getArchMinRam() ? OperatingSystem.getArchMinRam() : profile.getSettings().getRam();
            args.add("-Xmx" + ram + "M");
            args.add("-Xms" + OperatingSystem.getArchMinRam() + "M");
            Map<String, String> map = new HashMap<>();
            StrSubstitutor substitute = new StrSubstitutor(map);
            map.put("natives_directory", tempDir);
            map.put("launcher_name", it.getConfig().getLauncherName());
            map.put("launcher_version", "0.0.1");
            map.put("classpath", getClasspath());
            jsonArguments.forEach(arg -> args.add(substitute.replace(arg)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return args;
    }

    public List<String> gameArguments(List<String> jsonArguments) {
        List<String> args = new ArrayList<>();
        try {
            LauncherProfiles launcherProfiles = it.getSessionManager().getLauncherProfiles();
            Profile profile = launcherProfiles.getProfiles().get(launcherProfiles.getSelectedProfile());
            Map<String, String> map = new HashMap<>();
            map.put("version_name", version.getId());
            map.put("assets_root", AssetIndex.getLocalAssetsFolder(it).getCanonicalPath());
            map.put("assets_index_name", version.getAssetIndex().getId());
            map.put("game_directory", it.getConfig().getInstallFolder().getCanonicalPath());
            map.put("game_assets", reconstructAssets().getCanonicalPath());
            map.put("version_type", version.getType().name().toLowerCase());
            map.put("user_type", "mojang");
            map.put("auth_access_token", profile.getAccessToken());
            map.put("auth_uuid", profile.getId());
            map.put("auth_player_name", profile.getName());
            map.put("user_properties", "{}");
            StrSubstitutor substitute = new StrSubstitutor(map);
            jsonArguments.forEach(arg -> args.add(substitute.replace(arg)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return args;
    }

    private File reconstructAssets() throws IOException {
        File assetsDir = AssetIndex.getLocalAssetsFolder(it);
        File indexDir = new File(assetsDir, "indexes");
        File objectDir = new File(assetsDir, "objects");
        String assetVersion = version.getAssetIndex().getId();
        File indexFile = new File(indexDir, String.valueOf(assetVersion) + ".json");
        File virtualRoot = new File(new File(assetsDir, "virtual"), assetVersion);
        if (!indexFile.isFile()) {
            //this.log.logWarning("No assets index file " + virtualRoot + "; can't reconstruct assets");
            return virtualRoot;
        }
      //  this.log.logInfo("Reconstructing virtual assets folder at " + virtualRoot);
        for (Map.Entry<String, Asset> entry : version.getLocalAssetsMap(it).entrySet()) {
            File target = new File(virtualRoot, entry.getKey());
            File original = new File(new File(objectDir, entry.getValue().getHash().substring(0, 2)), entry.getValue().getHash());
            if (target.isFile())
                continue;
            FileUtils.copyFile(original, target, false);
        }
        return virtualRoot;
    }

    private void unpackNatives(File targetDir) throws IOException {
        OperatingSystem os = OperatingSystem.getCurrentPlatform();
        Collection<Library> libraries = version.getLibraries();
        for (Library library : libraries) {
            Artifact artifact = library.getNative(os);
            if (artifact == null)
                continue;
            System.out.println(Library.getLibrariesFolder(it) + " " + artifact.getPath());
            File file = new File(Library.getLibrariesFolder(it), artifact.getPath());
            ExtractRules extractRules = library.getExtract();
            try (ZipFile zip = new ZipFile(file)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (extractRules != null && !extractRules.shouldExtract(entry.getName()))
                        continue;
                    File targetFile = new File(targetDir, entry.getName());
                    if (targetFile.getParentFile() != null) {
                        targetFile.getParentFile().mkdirs();
                    }
                    if (entry.isDirectory()) continue;
                    BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));
                    byte[] buffer = new byte[2048];
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                    try {
                        int length;
                        while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                            bufferedOutputStream.write(buffer, 0, length);
                        }
                    } finally {
                        Downloadable.closeSilently(bufferedOutputStream);
                        Downloadable.closeSilently(outputStream);
                        Downloadable.closeSilently(inputStream);
                    }
                }
            }
        }
    }


    public List<Runnable> getThreads() {
        return threads;
    }

    public void setGameListener(IGameListener gameListener) {
        this.gameListener = gameListener;
    }

    public IGameListener getGameListener() {
        return gameListener;
    }
}
