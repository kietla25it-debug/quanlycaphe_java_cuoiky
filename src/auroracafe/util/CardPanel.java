package auroracafe.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class CardPanel extends JPanel {
    public CardPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UiTheme.SURFACE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
        g2.setColor(new Color(220, 206, 193, 150));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Insets getInsets() {
        return new Insets(18, 18, 18, 18);
    }
}
