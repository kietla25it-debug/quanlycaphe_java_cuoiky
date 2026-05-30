package auroracafe.client;

import auroracafe.network.Request;
import auroracafe.network.Response;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient {
    private final String host;
    private final int port;
    public SocketClient(String host, int port) { this.host = host; this.port = port; }
    public Response send(Request request) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception e) {
            return Response.fail("Không kết nối được server: " + e.getMessage());
        }
    }
}
