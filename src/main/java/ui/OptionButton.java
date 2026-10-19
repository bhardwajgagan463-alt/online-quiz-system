package ui;

import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Large answer button (A-D). Use a ButtonGroup so only one can be selected.
 * Inheritance: extends JToggleButton. Interface: Theme.ThemeListener.
 */
public class OptionButton extends JToggleButton implements Theme.ThemeListener {

    /** For reviewing answers later: color the option green or red. */
    public enum Mark { NONE, CORRECT, WRONG }

    private static final int BADGE = 30;
    private static final int WRAP_WIDTH = 440;

    private final int number;               // 1..4
    private boolean hover = false;
    private Mark mark = Mark.NONE;

    public OptionButton(int number) {
        this.number = number;

        Icon badge = new LetterIcon();
        setIcon(badge);
        setDisabledIcon(badge);             // same look when disabled
        setHorizontalAlignment(SwingConstants.LEFT);
        setIconTextGap(16);
        setFont(Theme.font(16));
        setForeground(Theme.TEXT);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Choose option " + letter());

        // Event handling: MouseListener (via MouseAdapter) for the hover effect
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
        });
    }

    private char letter() {
        return (char) ('A' + number - 1);
    }

    public void setOptionText(String text) {
        setText("<html><body style='width:" + WRAP_WIDTH + "px'>" + WrappedLabel.escape(text) + "</body></html>");
    }

    public void setMark(Mark mark) {
        this.mark = mark;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg;
        Color border;
        boolean strong = true;
        if (mark == Mark.CORRECT) {
            bg = Theme.SUCCESS_SOFT;
            border = Theme.SUCCESS;
        } else if (mark == Mark.WRONG) {
            bg = Theme.DANGER_SOFT;
            border = Theme.DANGER;
        } else if (isSelected()) {
            bg = Theme.PRIMARY_SOFT;
            border = Theme.PRIMARY;
        } else if (hover && isEnabled()) {
            bg = Theme.blend(Theme.SURFACE, Theme.PRIMARY_SOFT, 0.5f);
            border = Theme.blend(Theme.BORDER, Theme.PRIMARY, 0.5f);
            strong = false;
        } else {
            bg = Theme.SURFACE;
            border = Theme.BORDER;
            strong = false;
        }

        int arc = Theme.RADIUS_BTN * 2;
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(strong ? 2f : 1.5f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
        g2.dispose();

        super.paintComponent(g);            // draws the letter badge and the text
    }

    // ---- Theme support: rebuild the text color when light/dark changes ----
    @Override
    public void themeChanged() {
        setForeground(Theme.TEXT);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Theme.addListener(this);
    }

    @Override
    public void removeNotify() {
        Theme.removeListener(this);
        super.removeNotify();
    }

    /** Round badge with the option letter (A, B, C, D). Inner class implementing Icon. */
    private class LetterIcon implements Icon {

        @Override public int getIconWidth()  { return BADGE; }
        @Override public int getIconHeight() { return BADGE; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean filled = isSelected() || mark != Mark.NONE;
            if (filled) {
                Color fill = mark == Mark.CORRECT ? Theme.SUCCESS
                           : mark == Mark.WRONG ? Theme.DANGER : Theme.PRIMARY;
                g2.setColor(fill);
                g2.fillOval(x, y, BADGE, BADGE);
            } else {
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x + 1, y + 1, BADGE - 3, BADGE - 3);
            }

            if (mark == Mark.CORRECT) {
                Icons.of(Icons.Type.CHECK, 16, Color.WHITE).paintIcon(c, g2, x + 7, y + 7);
            } else if (mark == Mark.WRONG) {
                Icons.of(Icons.Type.CROSS, 16, Color.WHITE).paintIcon(c, g2, x + 7, y + 7);
            } else {
                String text = String.valueOf(letter());
                g2.setFont(Theme.BODY_BOLD);
                g2.setColor(filled ? Color.WHITE : Theme.TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, x + (BADGE - fm.stringWidth(text)) / 2,
                        y + (BADGE - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }
}