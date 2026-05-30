package auroracafe.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class CoffeeArtPanel extends JPanel {
    private final String title;
    private final String subtitle;
    private final boolean lightMode;
    private final BufferedImage logo = BrandAssetUtils.loadImage("assets/logo_kn_luxury.png");
    public CoffeeArtPanel(String title, String subtitle, boolean lightMode) { this.title = title; this.subtitle = subtitle; this.lightMode = lightMode; setOpaque(false); }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Color top = lightMode ? new Color(255, 246, 236) : new Color(71, 43, 27);
        Color bottom = lightMode ? new Color(246, 227, 204) : new Color(145, 101, 66);
        g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
        drawBeans(g2); drawCup(g2); drawLogo(g2);
        g2.setColor(lightMode ? UiTheme.PRIMARY_DARK : Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22)); g2.drawString(title, 140, 58);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14)); int y = 122; for (String line : subtitle.split("\n")) { g2.drawString(line, 36, y); y += 26; } g2.dispose(); }
    private void drawLogo(Graphics2D g2) { if (logo != null) { Image scaled = BrandAssetUtils.scale(logo, 90, 90); g2.drawImage(scaled, 22, 18, null); return; } }
    private void drawCup(Graphics2D g2) { int w = getWidth(), h = getHeight(), cupW = Math.min(200, w / 3), cupH = Math.min(130, h / 3), x = w - cupW - 55, y = h / 2 - 10; g2.setColor(new Color(255, 251, 245, 245)); g2.fillRoundRect(x, y, cupW, cupH, 30, 30); g2.setColor(new Color(180, 146, 122)); g2.setStroke(new BasicStroke(3f)); g2.drawRoundRect(x, y, cupW, cupH, 30, 30); g2.draw(new Arc2D.Double(x + cupW - 10, y + 24, 50, 58, -70, 240, Arc2D.OPEN)); g2.setColor(new Color(95, 55, 34)); g2.fillRoundRect(x + 12, y + 12, cupW - 24, 24, 16, 16); g2.setColor(new Color(245, 218, 190)); g2.fillOval(x + 25, y + 8, cupW - 52, 18); g2.setColor(new Color(255, 240, 220, 170)); g2.fillOval(x - 16, y + cupH + 18, cupW + 50, 24); java.awt.geom.AffineTransform old = g2.getTransform(); g2.translate(x + 55, y - 30); drawSteam(g2); g2.translate(34, -4); drawSteam(g2); g2.translate(34, 4); drawSteam(g2); g2.setTransform(old); }
    private void drawSteam(Graphics2D g2) { Path2D path = new Path2D.Double(); path.moveTo(4, 36); path.curveTo(-6, 22, 18, 18, 10, 2); path.curveTo(8, -10, -6, -10, 4, -24); g2.setColor(new Color(255, 255, 255, 165)); g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.draw(path); }
    private void drawBeans(Graphics2D g2) { drawBean(g2, 48, getHeight() - 72, 36, 26, new Color(105, 64, 39, 120)); drawBean(g2, 92, getHeight() - 110, 30, 22, new Color(74, 47, 28, 130)); drawBean(g2, 132, getHeight() - 88, 24, 18, new Color(120, 73, 42, 125)); }
    private void drawBean(Graphics2D g2, int x, int y, int w, int h, Color color) { g2.setColor(color); g2.fill(new Ellipse2D.Double(x, y, w, h)); g2.setColor(new Color(255, 255, 255, 120)); g2.draw(new Arc2D.Double(x + w / 2.0 - 4, y + 2, 8, h - 4, 90, 180, Arc2D.OPEN)); }
}
