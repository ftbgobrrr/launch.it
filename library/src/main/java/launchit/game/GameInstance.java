package launchit.game;

import com.google.common.io.Files;
import launchit.auth.model.Profile;
import launchit.auth.profile.LauncherProfiles;
import launchit.downloader.Downloadable;
import launchit.events.GameEvent;
import launchit.formatter.assets.Asset;
import launchit.formatter.assets.AssetIndex;
import launchit.formatter.libraries.Artifact;
import launchit.formatter.libraries.Library;
import launchit.formatter.rules.ExtractRules;
import launchit.formatter.versions.Version;
import launchit.utils.OperatingSystem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameInstance extends Thread implements Runnable {

    private final Version version;
    private final GameManager manager;
    private final List<String> logs;
    private Process process;

    public GameInstance(GameManager manager, Version version) {
        this.manager = manager;
        this.version = version;
        this.logs = new ArrayList<>();
        this.setName(version.getId() + ":" + this.getId());
    }

    @Override
    public void run() {
        List<String> arguments = new ArrayList<>();
        arguments.add(OperatingSystem.getCurrentPlatform().getJavaDir());
        arguments.addAll(jvmArguments(getVersion().getArguments().getJvmArguments()));
        arguments.add(getVersion().getMainClass());
        arguments.addAll(gameArguments(getVersion().getArguments().getGameArguments()));
        GameEvent.Start.Pre event = new GameEvent.Start.Pre(this, arguments);
        this.getManager().getLaunchit().getEventBus().post(event);
        if (!event.isCanceled()) {
            try {
                ProcessBuilder pb = new ProcessBuilder(arguments);
                pb.directory(this.getManager().getLaunchit().getConfig().getInstallFolder());
                pb.redirectErrorStream(true);
                process = pb.start();
                output(process.getInputStream());
                int error = process.waitFor();
                onGameStop(error);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.manager.getLaunchit().getEventBus().post(new GameEvent.Start.Post(this, arguments));
    }

    private void onGameStop(int error) {
        if (error == 0) {
            this.manager.getLaunchit().getEventBus().post(new GameEvent.Stop(this, error, null));
            return;
        }

        String errorText = null;
        int lines = logs.size() - 1;
        String pattern = "#@!@#";
        while (lines >= 0) {
            String line = logs.get(lines);
            if (line != null) {
                int index = line.lastIndexOf(pattern);
                if (index >= 0 && index < line.length() - pattern.length() - 1) {
                    errorText = line.substring(index + pattern.length()).trim();
                    break;
                }
            }
            --lines;
        }

        File crashReport = null;
        if (errorText != null) {
            crashReport = new File(errorText);
            try {
                Desktop.getDesktop().open(crashReport.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.manager.getLaunchit().getEventBus().post(new GameEvent.Stop(this, error, crashReport));
    }

    private void output(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                this.manager.getLaunchit().getEventBus().post(new GameEvent.Log(this, line, logs));
                logs.add(line);
                System.out.println(line);
            }
        }
    }

    private String getClasspath() {
        try {
            StringBuilder sb = new StringBuilder();
            getVersion().getLibraries()
                    .stream()
                    .filter(Library::matchEnvironement)
                    .forEach(library -> {
                        try {
                            sb.append(library.getLocalFile(this.getManager().getLaunchit()).getCanonicalPath());
                            sb.append(OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS ? ";" : ":");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            sb.append(Version.DownloadType.CLIENT.getLocalFile(this.getManager().getLaunchit(), version).getCanonicalPath());
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private List<String> jvmArguments(List<String> jsonArguments) {
        List<String> args = new ArrayList<>();
        try {
            LauncherProfiles launcherProfiles = this.getManager().getLaunchit().getSessionManager().getLauncherProfiles();
            Profile profile = launcherProfiles.getProfiles().get(launcherProfiles.getSelectedProfile());
            File tempDirFile = Files.createTempDir();
            String tempDir = tempDirFile.getCanonicalPath();
            this.unpackNatives(tempDirFile);
            args.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
            args.add("-Dfml.ignorePatchDiscrepancies=true");
            args.add("-Dminecraft.client.jar=" + Version.DownloadType.CLIENT.getLocalFile(this.getManager().getLaunchit(), version).getCanonicalPath());

            int ram = OperatingSystem.getArchMinRam();
            if (profile.getSettings().getRam() != 0)
                ram = ram < OperatingSystem.getArchMinRam() ? OperatingSystem.getArchMinRam() : profile.getSettings().getRam();
            args.add("-Xmx" + ram + "M");
            args.add("-Xms" + OperatingSystem.getArchMinRam() + "M");
            Map<String, String> map = new HashMap<>();
            StrSubstitutor substitute = new StrSubstitutor(map);
            map.put("natives_directory", tempDir);
            map.put("launcher_name", this.getManager().getLaunchit().getConfig().getLauncherName());
            map.put("launcher_version", "0.0.1");
            map.put("classpath", getClasspath());
            jsonArguments.forEach(arg -> args.add(substitute.replace(arg)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return args;
    }

    private List<String> gameArguments(List<String> jsonArguments) {
        List<String> args = new ArrayList<>();
        try {
            LauncherProfiles launcherProfiles = this.getManager().getLaunchit().getSessionManager().getLauncherProfiles();
            Profile profile = launcherProfiles.getProfiles().get(launcherProfiles.getSelectedProfile());
            Map<String, String> map = new HashMap<>();
            map.put("version_name", getVersion().getId());
            map.put("assets_root", AssetIndex.getLocalAssetsFolder(this.getManager().getLaunchit()).getCanonicalPath());
            map.put("assets_index_name", getVersion().getAssetIndex().getId());
            map.put("game_directory", this.getManager().getLaunchit().getConfig().getInstallFolder().getCanonicalPath());
            map.put("game_assets", reconstructAssets().getCanonicalPath());
            map.put("version_type", getVersion().getType().name().toLowerCase());
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
        File assetsDir = AssetIndex.getLocalAssetsFolder(this.getManager().getLaunchit());
        File indexDir = new File(assetsDir, "indexes");
        File objectDir = new File(assetsDir, "objects");
        String assetVersion = getVersion().getAssetIndex().getId();
        File indexFile = new File(indexDir, assetVersion + ".json");
        File virtualRoot = new File(new File(assetsDir, "virtual"), assetVersion);
        if (!indexFile.isFile())
            return virtualRoot;
        for (Map.Entry<String, Asset> entry : getVersion().getLocalAssetsMap(this.getManager().getLaunchit()).entrySet()) {
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
        Collection<Library> libraries = getVersion().getLibraries();
        for (Library library : libraries) {
            if (!library.matchEnvironement())
                return;
            Artifact artifact = library.getNative(os);
            if (artifact == null)
                continue;
            File file = new File(Library.getLibrariesFolder(this.getManager().getLaunchit()), artifact.getPath());
            ExtractRules extractRules = library.getExtract();
            try (ZipFile zip = new ZipFile(file)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (extractRules != null && !extractRules.shouldExtract(entry.getName()))
                        continue;
                    File targetFile = new File(targetDir, entry.getName());
                    if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
                        //TODO: ERROR unable to create folder
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

    public Version getVersion() {
        return version;
    }

    public GameManager getManager() {
        return manager;
    }

    public Process getProcess() {
        return process;
    }
}
