package auroracafe.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class RemoteImageCache {
    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();
    private RemoteImageCache() {}

    public static ImageIcon get(String url, int width, int height, String title) {
        String key = (url == null ? "" : url) + "|" + width + "|" + height;
        return CACHE.computeIfAbsent(key, k -> load(url, width, height, title));
    }

    private static ImageIcon load(String url, int width, int height, String title) {
        try {
            if (url != null && !url.isBlank()) {
                BufferedImage img;
                if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:")) {
                    img = ImageIO.read(new URL(url));
                } else {
                    Path path = Path.of(url);
                    img = Files.exists(path) ? ImageIO.read(path.toFile()) : null;
                }
                if (img != null) {
                    return new ImageIcon(fitImage(img, width, height));
                }
            }
        } catch (Exception ignored) {}
        return new ImageIcon(createFallback(width, height, title));
    }

    private static BufferedImage fitImage(BufferedImage src, int width, int height) {
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(new Color(247, 242, 236));
        g.fillRoundRect(0, 0, width, height, 22, 22);
        double scale = Math.min((double) width / src.getWidth(), (double) height / src.getHeight());
        int drawW = Math.max(1, (int) Math.round(src.getWidth() * scale));
        int drawH = Math.max(1, (int) Math.round(src.getHeight() * scale));
        int x = (width - drawW) / 2;
        int y = (height - drawH) / 2;
        g.drawImage(src, x, y, drawW, drawH, null);
        g.dispose();
        return out;
    }

    private static BufferedImage createFallback(int width, int height, String title) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(240, 227, 214));
        g.fillRoundRect(0, 0, width, height, 24, 24);
        g.setColor(new Color(116, 77, 50));
        g.fillOval(width / 2 - 28, height / 2 - 28, 56, 56);
        g.setColor(new Color(255, 248, 241));
        g.fillOval(width / 2 - 22, height / 2 - 22, 44, 44);
        g.setColor(new Color(116, 77, 50));
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String t = title == null ? "CAFÉ" : title.substring(0, Math.min(6, title.length())).toUpperCase();
        FontMetrics fm = g.getFontMetrics();
        int tx = Math.max(8, (width - fm.stringWidth(t)) / 2);
        g.drawString(t, tx, height / 2 + 6);
        g.dispose();
        return img;
    }
}
