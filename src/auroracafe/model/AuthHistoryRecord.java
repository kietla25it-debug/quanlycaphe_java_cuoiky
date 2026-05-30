package auroracafe.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuthHistoryRecord implements Serializable {
    private final int id;
    private final int userId;
    private final String fullName;
    private final String username;
    private final String roleName;
    private final LocalDateTime loginAt;
    private LocalDateTime logoutAt;

    public AuthHistoryRecord(int id, int userId, String fullName, String username, String roleName, LocalDateTime loginAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.roleName = roleName;
        this.loginAt = loginAt;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getRoleName() { return roleName; }
    public LocalDateTime getLoginAt() { return loginAt; }
    public LocalDateTime getLogoutAt() { return logoutAt; }
    public void setLogoutAt(LocalDateTime logoutAt) { this.logoutAt = logoutAt; }
    public boolean isOnline() { return logoutAt == null; }
}