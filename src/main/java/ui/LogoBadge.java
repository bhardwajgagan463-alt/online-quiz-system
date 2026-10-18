package ui;

import util.Theme;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;

/** App logo: gradient rounded square with a white check icon. */
public class LogoBadge extends JComponent {

    private final int size;

    public LogoBadge(int size) {
        this.size = size;
        setPreferredSize(new Dimension(size, size));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color end = Theme.blend(Theme.PRIMARY, Color.decode("#7C3AED"), 0.7f);
        g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, size, size, end));
        g2.fillRoundRect(0, 0, size, size, size / 3, size / 3);

        int iconSize = size / 2;
        Icons.of(Icons.Type.CHECK, iconSize, Color.WHITE)
                .paintIcon(this, g2, (size - iconSize) / 2, (size - iconSize) / 2);
        g2.dispose();
    }
}