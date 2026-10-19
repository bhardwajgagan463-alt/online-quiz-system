package ui;

import util.Theme;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

/** Animated progress-ring chart with the big score in the middle. Custom painted with Java2D. */
public class ScoreRing extends JComponent {

    private static final int STROKE = 16;
    private static final int ANIMATION_MS = 900;

    private final int size;
    private String bigText = "";
    private String smallText = "";
    private double target = 0;          // final percentage (0..100)
    private double shown = 0;           // percentage drawn right now (grows during the animation)
    private boolean passed = false;

    private Timer animation;            // javax.swing.Timer: ticks on the UI thread
    private long startTime;

    public ScoreRing(int size) {
        this.size = size;
        setPreferredSize(new Dimension(size, size));
    }

    public void setValues(String bigText, String smallText, double percentage, boolean passed) {
        this.bigText = bigText;
        this.smallText = smallText;
        this.target = Math.max(0, Math.min(100, percentage));
        this.passed = passed;
        repaint();
    }

    // ---------------- animation ----------------

    private void startAnimation() {
        stopAnimation();
        shown = 0;
        startTime = System.currentTimeMillis();
        animation = new Timer(16, e -> {
            double t = Math.min(1.0, (System.currentTimeMillis() - startTime) / (double) ANIMATION_MS);
            double eased = 1 - Math.pow(1 - t, 3);           // starts fast, slows down at the end
            shown = target * eased;
            repaint();
            if (t >= 1.0) {
                stopAnimation();
            }
        });
        animation.start();
    }

    private void stopAnimation() {
        if (animation != null) {
            animation.stop();
            animation = null;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        startAnimation();               // plays when the screen appears
    }

    @Override
    public void removeNotify() {
        stopAnimation();
        super.removeNotify();
    }

    // ---------------- painting ----------------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int d = Math.min(getWidth(), getHeight()) - STROKE - 2;
        double x = (getWidth() - d) / 2.0;
        double y = (getHeight() - d) / 2.0;

        // grey track
        g2.setStroke(new BasicStroke(STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Theme.BORDER);
        g2.draw(new Ellipse2D.Double(x, y, d, d));

        // colored arc: starts at the top (90 degrees) and goes clockwise (negative extent)
        if (shown > 0.2) {
            g2.setColor(passed ? Theme.SUCCESS : Theme.DANGER);
            g2.draw(new Arc2D.Double(x, y, d, d, 90, -360.0 * shown / 100.0, Arc2D.OPEN));
        }

        // big score text, shrunk if it does not fit inside the ring
        int maxWidth = (int) (d * 0.70);
        int bigSize = Theme.SIZE_SCORE;
        Font bigFont = Theme.font(Font.BOLD, bigSize);
        FontMetrics bigFm = g2.getFontMetrics(bigFont);
        while (bigFm.stringWidth(bigText) > maxWidth && bigSize > 16) {
            bigSize -= 2;
            bigFont = Theme.font(Font.BOLD, bigSize);
            bigFm = g2.getFontMetrics(bigFont);
        }
        Font smallFont = Theme.font(Font.BOLD, 18);
        FontMetrics smallFm = g2.getFontMetrics(smallFont);

        int totalHeight = bigFm.getHeight() + smallFm.getHeight() - 6;
        int top = (getHeight() - totalHeight) / 2;

        g2.setFont(bigFont);
        g2.setColor(Theme.TEXT);
        g2.drawString(bigText, (getWidth() - bigFm.stringWidth(bigText)) / 2, top + bigFm.getAscent());

        g2.setFont(smallFont);
        g2.setColor(passed ? Theme.SUCCESS : Theme.DANGER);
        g2.drawString(smallText, (getWidth() - smallFm.stringWidth(smallText)) / 2,
                top + bigFm.getHeight() - 6 + smallFm.getAscent());

        g2.dispose();
    }
}