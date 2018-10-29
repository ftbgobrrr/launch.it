package launchit.events;

import launchit.events.factory.Event;
import launchit.game.GameInstance;

import java.io.File;
import java.util.List;

public class GameEvent extends Event {

    private final GameInstance instance;

    public GameEvent(GameInstance instance) {
        this.instance = instance;
    }

    public GameInstance getInstance() {
        return instance;
    }

    public static class Start extends GameEvent {

        private final List<String> arguments;

        public Start(GameInstance instance, List<String> arguments) {
            super(instance);
            this.arguments = arguments;
        }

        public List<String> getArguments() {
            return arguments;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    public static class Stop extends GameEvent {

        private final int exitCode;
        private final File crashReport;

        public Stop(GameInstance instance, int exitCode, File crashReport) {
            super(instance);
            this.exitCode = exitCode;
            this.crashReport = crashReport;
        }

        public int getExitCode() {
            return exitCode;
        }

        public File getCrashReport() {
            return crashReport;
        }
    }

    public static class Log extends GameEvent {

        private final List<String> logs;
        private String line;

        public Log(GameInstance instance, String line, List<String> logs) {
            super(instance);
            this.line = line;
            this.logs = logs;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public List<String> getLogs() {
            return logs;
        }
    }
}
