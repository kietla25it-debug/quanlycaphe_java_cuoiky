package auroracafe.network;
import java.io.Serializable;
public class LoginRequest implements Serializable {
    public final String username;
    public final String password;
    public LoginRequest(String username, String password) { this.username = username; this.password = password; }
}
