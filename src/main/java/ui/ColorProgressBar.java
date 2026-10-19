package ui;

import util.Theme;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Rounded, colored progress bar (indigo gradient, turns green at 100%). */
public class ColorProgressBar extends JComponent {

    private double fraction = 0;

    public ColorProgressBar() {
        setPreferredSize(new Dimension(100, 10));
    }

    public void setProgress(int value, int max) {
        fraction = max <= 0 ? 0 : Math.min(1.0, (double) value / max);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(Theme.BORDER);                                  // track
        g2.fillRoundRect(0, 0, w, h, h, h);

        int fill = (int) Math.round(w * fraction);
        if (fill > 0) {
            fill = Math.max(fill, h);
            boolean done = fraction >= 1.0;
            Color start = done ? Theme.SUCCESS : Theme.PRIMARY;
            Color end = done ? Theme.blend(Theme.SUCCESS, Color.WHITE, 0.25f)
                             : Theme.blend(Theme.PRIMARY, Color.decode("#7C3AED"), 0.7f);
            g2.setPaint(new GradientPaint(0, 0, start, fill, 0, end));
            g2.fillRoundRect(0, 0, fill, h, h, h);
        }
        g2.dispose();
    }
}