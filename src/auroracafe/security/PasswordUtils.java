package auroracafe.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Băm mật khẩu theo dạng sha256$salt$hash.
 * Giữ code không cần thư viện ngoài, phù hợp yêu cầu bảo mật cơ bản của đồ án.
 */
public final class PasswordUtils {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIX = "sha256";

    private PasswordUtils() {}

    public static String hashPassword(String plainPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return hashPassword(plainPassword, Base64.getEncoder().encodeToString(salt));
    }

    public static String hashPassword(String plainPassword, String saltBase64) {
        if (plainPassword == null) plainPassword = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            digest.update(salt);
            byte[] hashed = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return PREFIX + "$" + saltBase64 + "$" + Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể băm mật khẩu", e);
        }
    }

    public static boolean isHashed(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(PREFIX + "$") && storedPassword.split("\\$").length == 3;
    }

    public static boolean verify(String plainPassword, String storedPassword) {
        if (storedPassword == null) return false;
        if (!isHashed(storedPassword)) {
            // Hỗ trợ dữ liệu cũ: cho đăng nhập một lần bằng plain text, sau đó AuthService sẽ migrate sang hash.
            return storedPassword.equals(plainPassword);
        }
        String[] parts = storedPassword.split("\\$");
        String calculated = hashPassword(plainPassword, parts[1]);
        return constantTimeEquals(calculated, storedPassword);
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        int diff = x.length ^ y.length;
        for (int i = 0; i < Math.min(x.length, y.length); i++) diff |= x[i] ^ y[i];
        return diff == 0;
    }
}
