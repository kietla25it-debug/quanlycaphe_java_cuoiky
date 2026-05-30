package auroracafe.server;

import auroracafe.model.AppData;
import auroracafe.network.*;
import auroracafe.service.AuthService;
import auroracafe.service.CafeService;
import auroracafe.store.DataStore;
import auroracafe.store.FileStore;
import auroracafe.store.MySqlConfig;
import auroracafe.store.MySqlStore;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CafeServer {
    private final int port;
    private final AuthService authService;
    private final CafeService cafeService;
    private final ExecutorService pool;

    public CafeServer(int port, AuthService authService, CafeService cafeService, int maxClients) {
        this.port = port;
        this.authService = authService;
        this.cafeService = cafeService;
        this.pool = Executors.newFixedThreadPool(maxClients);
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ServerLogger.log("SERVER_START port=" + port);
            System.out.println("CafeServer đang chạy tại cổng " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                pool.submit(new ClientHandler(socket, authService, cafeService));
            }
        }
    }

    public static CafeServer createDefault() {
        DataStore store = new FileStore(Path.of("data", "aurora-cafe.dat"));
        AppData data;
        MySqlConfig config = MySqlConfig.load(Path.of("config", "mysql.properties"));
        if (config.isEnabled()) {
            try {
                store = new MySqlStore(config);
                data = store.load();
            } catch (Exception ex) {
                ServerLogger.log("MYSQL_FALLBACK " + ex.getMessage());
                store = new FileStore(Path.of("data", "aurora-cafe.dat"));
                data = store.load();
            }
        } else {
            data = store.load();
        }
        return new CafeServer(9000, new AuthService(data, store), new CafeService(data, store, Path.of("exports", "invoices")), 10);
    }
}
