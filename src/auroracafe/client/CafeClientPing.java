package auroracafe.client;

import auroracafe.network.Request;
import auroracafe.network.Response;

public class CafeClientPing {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9000;
        Response response = new SocketClient(host, port).send(new Request("PING", null));
        System.out.println(response.isSuccess() ? "Kết nối server thành công: " + response.getData() : response.getMessage());
    }
}
