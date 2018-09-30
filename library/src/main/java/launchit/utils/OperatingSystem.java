package launchit.utils;

import java.io.File;

public enum OperatingSystem {
    LINUX("linux", new String[]{"linux", "unix"}),
    WINDOWS("windows", new String[]{"win"}),
    OSX("osx", new String[]{"mac"}),
    UNKNOWN("unknown", new String[0]);

    private final String name;
    private final String[] aliases;

    OperatingSystem(String name, String[] aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public static OperatingSystem getCurrentPlatform() {
        String name = System.getProperty("os.name").toLowerCase();
        OperatingSystem[] systems = values();
        for (OperatingSystem os : systems) {
            String[] arrayOfString = os.getAliases();
            for (String alias : arrayOfString) {
                if (name.contains(alias))
                    return os;
            }
        }
        return UNKNOWN;
    }

    public static String getArch() {
        return System.getProperty("sun.arch.data.model");
    }

    public static int getArchMinRam() {
        boolean args = "32".equals(getArch());
        return args ? 512 : 1024;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public String getJavaDir() {
        String separator = System.getProperty("file.separator");
        String path = System.getProperty("java.home") + separator + "bin" + separator;
        if (getCurrentPlatform() == WINDOWS && new File(path + "javaw.exe").isFile())
            return path + "javaw.exe";
        return path + "java";
    }

    public String getName() {
        return this.name;
    }
}