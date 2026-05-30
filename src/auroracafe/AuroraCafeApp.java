package auroracafe;

import auroracafe.client.ClientConfig;
import auroracafe.client.RemoteAuthService;
import auroracafe.client.RemoteCafeService;
import auroracafe.client.SocketClient;
import auroracafe.network.Request;
import auroracafe.network.Response;
import auroracafe.service.AuthService;
import auroracafe.service.CafeService;
import auroracafe.ui.LoginFrame;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class AuroraCafeApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        ClientConfig clientConfig = ClientConfig.load(Path.of("config", "client.properties"));
        SocketClient socketClient = new SocketClient(clientConfig.getHost(), clientConfig.getPort());
        Response ping = socketClient.send(new Request("PING", null));

        if (!ping.isSuccess()) {
            JOptionPane.showMessageDialog(null,
                    "Không kết nối được Cafe Server.\n\n"
                            + "Để chạy đúng yêu cầu Client/Server, hãy chạy run-server.bat trước, sau đó mới chạy run.bat.\n\n"
                            + "Chi tiết: " + ping.getMessage(),
                    "Lỗi kết nối Server", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ĐÚNG MÔ HÌNH CLIENT/SERVER:
        // Client Swing chỉ gọi RemoteAuthService/RemoteCafeService.
        // Hai lớp remote này gửi request qua SocketClient đến Server.
        // Chỉ Server mới xử lý nghiệp vụ và kết nối MySQL.
        AuthService authService = new RemoteAuthService(socketClient);
        CafeService cafeService = new RemoteCafeService(socketClient);

        SwingUtilities.invokeLater(() -> new LoginFrame(authService,
                user -> new auroracafe.ui.MainFrame(authService, cafeService, user).setVisible(true)).setVisible(true));
    }
}
