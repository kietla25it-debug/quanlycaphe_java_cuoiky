package auroracafe.network;

import java.io.Serializable;

public class Request implements Serializable {
    private final String action;
    private final Object data;
    private final String token;

    public Request(String action, Object data) { this(action, data, null); }
    public Request(String action, Object data, String token) {
        this.action = action;
        this.data = data;
        this.token = token;
    }
    public String getAction() { return action; }
    public Object getData() { return data; }
    public String getToken() { return token; }
}
