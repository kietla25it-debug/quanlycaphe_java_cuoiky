package auroracafe.network;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Request chung để Desktop Client gọi nghiệp vụ ở Server.
 * Client chỉ gửi tên nghiệp vụ + tham số; Server mới thực thi CafeService/AuthService và truy cập DB.
 */
public class ServiceCallRequest implements Serializable {
    private final String method;
    private final Object[] args;

    public ServiceCallRequest(String method, Object... args) {
        this.method = method;
        this.args = args == null ? new Object[0] : args;
    }

    public String getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "ServiceCallRequest{" + "method='" + method + '\'' + ", args=" + Arrays.toString(args) + '}';
    }
}
