package auroracafe.client;

import auroracafe.model.AppData;
import auroracafe.network.Request;
import auroracafe.network.Response;
import auroracafe.store.DataStore;

/**
 * DataStore phía CLIENT.
 * Lớp này KHÔNG kết nối MySQL trực tiếp. Mọi thao tác load/save dữ liệu đều gửi qua SocketClient
 * đến server. Server mới là nơi xử lý nghiệp vụ lưu trữ và truy xuất database.
 */
public class RemoteDataStore implements DataStore {
    private final SocketClient client;

    public RemoteDataStore(SocketClient client) {
        this.client = client;
    }

    @Override
    public AppData load() {
        Response res = client.send(new Request("GET_APP_DATA", null));
        if (!res.isSuccess()) {
            throw new IllegalStateException(res.getMessage());
        }
        Object data = res.getData();
        if (!(data instanceof AppData appData)) {
            throw new IllegalStateException("Server trả dữ liệu không hợp lệ.");
        }
        appData.syncCounters();
        return appData;
    }

    @Override
    public void save(AppData data) {
        Response res = client.send(new Request("SAVE_APP_DATA", data));
        if (!res.isSuccess()) {
            throw new IllegalStateException(res.getMessage());
        }
    }
}
