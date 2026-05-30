package auroracafe.model;

import java.io.Serializable;

public class User implements Serializable {
    private final int id;
    private String fullName;
    private String username;
    private String password;
    private Role role;
    private boolean active;

    public User(int id, String fullName, String username, String password, Role role, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
