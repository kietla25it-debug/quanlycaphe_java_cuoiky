package auroracafe.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ClientConfig {
    private final String host;
    private final int port;

    public ClientConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() { return host; }
    public int getPort() { return port; }

    public static ClientConfig load(Path path) {
        Properties props = new Properties();
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            } catch (IOException ignored) {
            }
        }
        String host = props.getProperty("host", "localhost").trim();
        int port = parseInt(props.getProperty("port", "9000"), 9000);
        return new ClientConfig(host.isBlank() ? "localhost" : host, port);
    }

    private static int parseInt(String raw, int fallback) {
        try { return Integer.parseInt(raw.trim()); } catch (Exception e) { return fallback; }
    }
}
