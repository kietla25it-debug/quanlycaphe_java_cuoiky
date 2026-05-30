package auroracafe.store;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySqlConnectionTest {
    public static void main(String[] args) {
        MySqlConfig config = MySqlConfig.load(Path.of("config", "mysql.properties"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(
                    config.jdbcUrl(false),
                    config.getUsername(),
                    config.getPassword());
                 Statement st = conn.createStatement()) {

                if (config.isAutoCreateSchema()) {
                    st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + config.getDatabase()
                            + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                }

                System.out.println("Ket noi MySQL thanh cong!");
                System.out.println("Database: " + config.getDatabase());
                System.out.println("Host: " + config.getHost() + ":" + config.getPort());
            }
        } catch (Exception e) {
            System.out.println("Ket noi MySQL that bai!");
            e.printStackTrace();
        }
    }
}
