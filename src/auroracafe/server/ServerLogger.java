package auroracafe.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public final class ServerLogger {
    private static final Path LOG_FILE = Path.of("logs", "server.log");
    private ServerLogger() {}
    public static synchronized void log(String message) {
        try {
            Files.createDirectories(LOG_FILE.getParent());
            Files.writeString(LOG_FILE, "[" + LocalDateTime.now() + "] " + message + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }
}
