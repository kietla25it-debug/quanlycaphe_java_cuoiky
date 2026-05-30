package auroracafe.network;
import auroracafe.model.Role;
import java.io.Serializable;
public class RegisterRequest implements Serializable {
    public final String fullName;
    public final String username;
    public final String password;
    public final String confirmPassword;
    public final Role role;
    public RegisterRequest(String fullName, String username, String password, String confirmPassword, Role role) {
        this.fullName = fullName; this.username = username; this.password = password; this.confirmPassword = confirmPassword; this.role = role;
    }
}
