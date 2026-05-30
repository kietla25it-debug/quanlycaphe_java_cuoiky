package auroracafe.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class BrandAssetUtils {
    private BrandAssetUtils() {}


    public static BufferedImage loadOriginalImage(String path) {
        try {
            Path p = Path.of(path);
            if (Files.exists(p)) {
                BufferedImage raw = ImageIO.read(p.toFile());
                return raw == null ? null : ensureArgb(raw);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static BufferedImage loadImage(String path) {
        try {
            Path p = Path.of(path);
            if (Files.exists(p)) {
                BufferedImage raw = ImageIO.read(p.toFile());
                if (raw == null) return null;
                BufferedImage prepared = ensureArgb(raw);
                prepared = makeUniformBackgroundTransparent(prepared);
                prepared = cropTransparentBorder(prepared);
                prepared = sharpen(prepared);
                return prepared;
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public static Image scale(BufferedImage image, int width, int height) {
        if (image == null) return null;
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double ratio = Math.min((double) width / image.getWidth(), (double) height / image.getHeight());
        int drawW = Math.max(1, (int) Math.round(image.getWidth() * ratio));
        int drawH = Math.max(1, (int) Math.round(image.getHeight() * ratio));
        int x = (width - drawW) / 2;
        int y = (height - drawH) / 2;
        g2.drawImage(image, x, y, drawW, drawH, null);
        g2.dispose();
        return canvas;
    }

    private static BufferedImage ensureArgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) return src;
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return out;
    }

    private static BufferedImage makeUniformBackgroundTransparent(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] bg = averageCornerColor(src);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;
                if (a > 0 && isBackgroundLike(r, g, b, bg)) {
                    out.setRGB(x, y, 0x00ffffff);
                } else {
                    out.setRGB(x, y, argb);
                }
            }
        }
        return out;
    }

    private static int[] averageCornerColor(BufferedImage src) {
        int sample = Math.max(12, Math.min(src.getWidth(), src.getHeight()) / 18);
        long tr = 0, tg = 0, tb = 0, count = 0;
        int[][] corners = {
                {0, 0},
                {Math.max(0, src.getWidth() - sample), 0},
                {0, Math.max(0, src.getHeight() - sample)},
                {Math.max(0, src.getWidth() - sample), Math.max(0, src.getHeight() - sample)}
        };
        for (int[] c : corners) {
            for (int y = c[1]; y < c[1] + sample && y < src.getHeight(); y++) {
                for (int x = c[0]; x < c[0] + sample && x < src.getWidth(); x++) {
                    int argb = src.getRGB(x, y);
                    int a = (argb >> 24) & 0xff;
                    if (a < 10) continue;
                    tr += (argb >> 16) & 0xff;
                    tg += (argb >> 8) & 0xff;
                    tb += argb & 0xff;
                    count++;
                }
            }
        }
        if (count == 0) return new int[]{245, 235, 220};
        return new int[]{(int) (tr / count), (int) (tg / count), (int) (tb / count)};
    }

    private static boolean isBackgroundLike(int r, int g, int b, int[] bg) {
        int diff = Math.abs(r - bg[0]) + Math.abs(g - bg[1]) + Math.abs(b - bg[2]);
        int brightness = (r + g + b) / 3;
        return brightness > 190 && diff < 70;
    }

    public static BufferedImage cropTransparentBorder(BufferedImage src) {
        if (src == null) return null;
        int minX = src.getWidth();
        int minY = src.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int alpha = (src.getRGB(x, y) >> 24) & 0xff;
                if (alpha > 8) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < minX || maxY < minY) return src;
        int pad = 12;
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(src.getWidth() - 1, maxX + pad);
        maxY = Math.min(src.getHeight() - 1, maxY + pad);
        BufferedImage cropped = new BufferedImage(maxX - minX + 1, maxY - minY + 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = cropped.createGraphics();
        g2.drawImage(src, 0, 0, cropped.getWidth(), cropped.getHeight(), minX, minY, maxX + 1, maxY + 1, null);
        g2.dispose();
        return cropped;
    }

    private static BufferedImage sharpen(BufferedImage src) {
        float[] kernel = {
                0f, -0.2f, 0f,
                -0.2f, 1.8f, -0.2f,
                0f, -0.2f, 0f
        };
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        op.filter(src, dest);
        return dest;
    }
}
