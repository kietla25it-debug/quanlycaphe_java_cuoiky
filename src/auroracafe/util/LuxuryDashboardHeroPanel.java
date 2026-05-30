package auroracafe.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class LuxuryDashboardHeroPanel extends JPanel {
    private final BufferedImage replacement = BrandAssetUtils.loadOriginalImage("assets/dashboard_replacement.png");

    public LuxuryDashboardHeroPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        if (replacement != null) {
            g2.drawImage(replacement, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(new Color(42, 20, 12));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        }
        g2.dispose();
    }
}
