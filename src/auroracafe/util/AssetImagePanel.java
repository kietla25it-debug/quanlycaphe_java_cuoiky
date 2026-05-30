package auroracafe.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class AssetImagePanel extends JPanel {
    private final Image image;
    private final int arc;

    public AssetImagePanel(String assetPath, int arc) {
        this.arc = arc;
        Image loaded = null;
        try {
            Path path = Path.of(assetPath);
            if (Files.exists(path)) {
                loaded = new ImageIcon(path.toString()).getImage();
            }
        } catch (Exception ignored) {
        }
        this.image = loaded;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape clip = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc);
        g2.setClip(clip);
        if (image != null) {
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
        g2.dispose();
    }
}
