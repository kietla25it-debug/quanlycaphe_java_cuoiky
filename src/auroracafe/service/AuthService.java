package auroracafe.service;

import auroracafe.model.AppData;
import auroracafe.model.AuthHistoryRecord;
import auroracafe.model.Role;
import auroracafe.model.User;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import auroracafe.store.DataStore;
import auroracafe.security.PasswordUtils;

public class AuthService {
    private final AppData data;
    private final DataStore store;

    public AuthService(AppData data, DataStore store) {
        this.data = data;
        this.store = store;
        migratePlainTextPasswords();
    }

    public User login(String username, String password) {
        User user = data.getUsers().stream()
                .filter(User::isActive)
                .filter(u -> u.getUsername().equalsIgnoreCase(username.trim()))
                .filter(u -> PasswordUtils.verify(password, u.getPassword()))
                .findFirst()
                .orElse(null);
        if (user != null) {
            if (!PasswordUtils.isHashed(user.getPassword())) {
                user.setPassword(PasswordUtils.hashPassword(password));
            }
            recordLogin(user);
        }
        return user;
    }

    private void recordLogin(User user) {
        AuthHistoryRecord record = new AuthHistoryRecord(
                data.nextAuthHistoryId(),
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getRole().name(),
                LocalDateTime.now());
        data.getAuthHistoryRecords().add(record);
        store.save(data);
    }

    public void recordLogout(User user) {
        if (user == null) return;
        data.getAuthHistoryRecords().stream()
                .filter(r -> r.getUserId() == user.getId() && r.getLogoutAt() == null)
                .max(Comparator.comparing(AuthHistoryRecord::getLoginAt))
                .ifPresent(record -> record.setLogoutAt(LocalDateTime.now()));
        store.save(data);
    }

    private void migratePlainTextPasswords() {
        boolean changed = false;
        for (User user : data.getUsers()) {
            String stored = user.getPassword();
            if (stored != null && !PasswordUtils.isHashed(stored)) {
                user.setPassword(PasswordUtils.hashPassword(stored));
                changed = true;
            }
        }
        if (changed) {
            store.save(data);
        }
    }

    public List<AuthHistoryRecord> getAuthHistory() {
        return data.getAuthHistoryRecords().stream()
                .sorted(Comparator.comparing(AuthHistoryRecord::getLoginAt).reversed())
                .collect(Collectors.toList());
    }

    public String registerPublic(String fullName, String username, String password, String confirmPassword) {
        if (fullName == null || fullName.isBlank() || username == null || username.isBlank()
                || password == null || password.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            return "Vui lòng nhập đầy đủ thông tin.";
        }
        if (!password.equals(confirmPassword)) {
            return "Mật khẩu nhập lại chưa khớp.";
        }
        if (password.length() < 6) {
            return "Mật khẩu cần ít nhất 6 ký tự.";
        }
        boolean exists = data.getUsers().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username.trim()));
        if (exists) {
            return "Tên đăng nhập đã tồn tại.";
        }
        data.getUsers().add(new User(data.nextUserId(), fullName.trim(), username.trim(), PasswordUtils.hashPassword(password), Role.STAFF, true));
        store.save(data);
        return null;
    }

    public String registerStaff(String fullName, String username, String password, Role role) {
        if (fullName == null || fullName.isBlank() || username == null || username.isBlank() || password == null || password.isBlank()) {
            return "Vui lòng nhập đầy đủ thông tin.";
        }
        if (password.length() < 6) {
            return "Mật khẩu cần ít nhất 6 ký tự.";
        }
        boolean exists = data.getUsers().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username.trim()));
        if (exists) {
            return "Tên đăng nhập đã tồn tại.";
        }
        data.getUsers().add(new User(data.nextUserId(), fullName.trim(), username.trim(), PasswordUtils.hashPassword(password), role, true));
        store.save(data);
        return null;
    }
}
