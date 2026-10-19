package ui;

import util.Theme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Countdown pill (clock icon + mm:ss). Turns red in the last 30 seconds. */
public class TimerBadge extends JComponent {

    public static final int WARNING_SECONDS = 30;       // static final

    private final String widestText;
    private int seconds;

    public TimerBadge(int seconds) {
        this.seconds = seconds;
        this.widestText = format(seconds);
        setToolTipText("Time remaining");
    }

    public void setSeconds(int seconds) {
        this.seconds = Math.max(0, seconds);
        repaint();
    }

    public boolean isLow() {
        return seconds <= WARNING_SECONDS;
    }

    private static String format(int s) {
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.TIMER);
        return new Dimension(fm.stringWidth(widestText) + 62, 44);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        boolean low = isLow();
        Color bg = low ? Theme.DANGER_SOFT : Theme.PRIMARY_SOFT;
        Color fg;
        if (low) {
            fg = Theme.isDark() ? Theme.blend(Theme.DANGER, Color.WHITE, 0.45f) : Theme.DANGER;
        } else {
            fg = Theme.isDark() ? Theme.blend(Theme.PRIMARY, Color.WHITE, 0.55f) : Theme.PRIMARY;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, h, h);
        if (low) {
            g2.setColor(Theme.DANGER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, h, h);
        }

        Icons.of(Icons.Type.CLOCK, 22, fg).paintIcon(this, g2, 16, (h - 22) / 2);

        g2.setFont(Theme.TIMER);
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(format(seconds), 16 + 22 + 10, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}