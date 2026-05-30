package auroracafe.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class LuxuryLoginHeroPanel extends JPanel {
    private static final int ARC = 36;
    private final BufferedImage heroImage = BrandAssetUtils.loadOriginalImage("assets/login_luxury.png");

    public LuxuryLoginHeroPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(0, 0, 0, 26));
        g2.fillRoundRect(6, 8, Math.max(0, w - 12), Math.max(0, h - 12), ARC, ARC);

        RoundRectangle2D card = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, ARC, ARC);
        g2.setColor(new Color(35, 17, 9));
        g2.fill(card);

        Shape oldClip = g2.getClip();
        g2.setClip(card);
        if (heroImage != null) {
            drawCoverImage(g2, heroImage, w, h);
        } else {
            g2.setColor(new Color(59, 31, 18));
            g2.fillRect(0, 0, w, h);
        }
        g2.setClip(oldClip);

        g2.setColor(new Color(207, 160, 79));
        g2.setStroke(new BasicStroke(2.4f));
        g2.draw(card);
        g2.setColor(new Color(230, 191, 119, 115));
        g2.draw(new RoundRectangle2D.Double(8, 8, w - 17, h - 17, 30, 30));

        g2.dispose();
    }

    private void drawCoverImage(Graphics2D g2, BufferedImage image, int w, int h) {
        double scale = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
        int drawW = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int drawH = Math.max(1, (int) Math.round(image.getHeight() * scale));
        int x = (w - drawW) / 2;
        int y = (h - drawH) / 2;
        g2.drawImage(image.getScaledInstance(drawW, drawH, Image.SCALE_SMOOTH), x, y, drawW, drawH, null);
    }
}
