package auroracafe.client;

import auroracafe.model.AppData;
import auroracafe.model.AuthHistoryRecord;
import auroracafe.model.Role;
import auroracafe.model.User;
import auroracafe.network.Request;
import auroracafe.network.Response;
import auroracafe.network.ServiceCallRequest;
import auroracafe.service.AuthService;
import auroracafe.store.DataStore;
import java.util.List;

/**
 * AuthService phía CLIENT.
 * Lớp này chỉ gửi request qua Socket; mọi kiểm tra đăng nhập/đăng ký/ghi lịch sử được xử lý ở SERVER.
 */
public class RemoteAuthService extends AuthService {
    private final SocketClient client;

    public RemoteAuthService(SocketClient client) {
        super(new AppData(), new NoOpDataStore());
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    private <T> T call(String method, Object... args) {
        Response res = client.send(new Request("AUTH_SERVICE", new ServiceCallRequest(method, args)));
        if (!res.isSuccess()) {
            throw new IllegalStateException(res.getMessage());
        }
        return (T) res.getData();
    }

    @Override
    public User login(String username, String password) {
        return call("login", username, password);
    }

    @Override
    public void recordLogout(User user) {
        call("recordLogout", user);
    }

    @Override
    public List<AuthHistoryRecord> getAuthHistory() {
        return call("getAuthHistory");
    }

    @Override
    public String registerPublic(String fullName, String username, String password, String confirmPassword) {
        return call("registerPublic", fullName, username, password, confirmPassword);
    }

    @Override
    public String registerStaff(String fullName, String username, String password, Role role) {
        return call("registerStaff", fullName, username, password, role);
    }

    private static class NoOpDataStore implements DataStore {
        @Override public AppData load() { return new AppData(); }
        @Override public void save(AppData data) { }
    }
}
