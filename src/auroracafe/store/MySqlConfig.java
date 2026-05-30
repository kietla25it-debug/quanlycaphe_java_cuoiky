package auroracafe.store;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class MySqlConfig {
    private final boolean enabled;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean autoCreateSchema;

    public MySqlConfig(boolean enabled, String host, int port, String database, String username, String password, boolean autoCreateSchema) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.autoCreateSchema = autoCreateSchema;
    }

    public boolean isEnabled() { return enabled; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isAutoCreateSchema() { return autoCreateSchema; }

    public String jdbcUrl(boolean withDatabase) {
        return "jdbc:mysql://" + host + ":" + port + "/" + (withDatabase ? database : "")
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    }

    public static MySqlConfig load(Path path) {
        Properties props = new Properties();
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            } catch (IOException ignored) {
            }
        }
        return new MySqlConfig(
                Boolean.parseBoolean(props.getProperty("enabled", "true")),
                props.getProperty("host", "localhost"),
                Integer.parseInt(props.getProperty("port", "3306")),
                props.getProperty("database", "cofeManager"),
                props.getProperty("username", "root"),
                props.getProperty("password", "1234"),
                Boolean.parseBoolean(props.getProperty("autoCreateSchema", "true"))
        );
    }
}
