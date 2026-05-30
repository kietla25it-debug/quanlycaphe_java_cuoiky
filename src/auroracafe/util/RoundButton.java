package auroracafe.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class RoundButton extends JButton {
    private final Color backgroundColor;
    private final Color hoverColor;
    private final Color selectedColor;
    private boolean hovered;
    private boolean selectedStyle;

    public RoundButton(String text, Color backgroundColor, Color hoverColor) {
        this(text, backgroundColor, hoverColor, hoverColor);
    }

    public RoundButton(String text, Color backgroundColor, Color hoverColor, Color selectedColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.hoverColor = hoverColor;
        this.selectedColor = selectedColor;
        setOpaque(false);
        setFocusPainted(false);
        setForeground(resolveTextColor(backgroundColor));
        setFont(UiTheme.BUTTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 18, 10, 18));
        setContentAreaFilled(false);
        setPreferredSize(new Dimension(160, 42));
        addChangeListener(e -> hovered = getModel().isRollover());
    }

    public void setSelectedStyle(boolean selectedStyle) {
        this.selectedStyle = selectedStyle;
        repaint();
    }

    private Color resolveTextColor(Color background) {
        int luminance = (background.getRed() * 299 + background.getGreen() * 587 + background.getBlue() * 114) / 1000;
        return luminance >= 170 ? UiTheme.TEXT : Color.WHITE;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = backgroundColor;
        if (selectedStyle) {
            fill = selectedColor;
        } else if (getModel().isPressed()) {
            fill = hoverColor.darker();
        } else if (hovered) {
            fill = hoverColor;
        }
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.dispose();
        super.paintComponent(g);
    }
}
