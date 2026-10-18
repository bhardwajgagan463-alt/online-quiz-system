package ui;

import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Rounded button with hover and press effects. Inheritance: extends JButton. */
public class RoundedButton extends JButton {

    public enum Style { PRIMARY, SECONDARY, DANGER, SUCCESS, GHOST }

    private final Style style;
    private boolean hover = false;
    private boolean pressed = false;

    // ---- Method overloading: several constructors ----
    public RoundedButton(String text) {
        this(text, null, Style.PRIMARY);
    }

    public RoundedButton(String text, Icon icon) {
        this(text, icon, Style.PRIMARY);
    }

    public RoundedButton(String text, Style style) {
        this(text, null, style);
    }

    public RoundedButton(String text, Icon icon, Style style) {
        super(text, icon);
        this.style = style;

        setFont(Theme.BODY_BOLD);
        setIconTextGap(8);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);     // we paint the background ourselves
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Event handling: MouseListener (via MouseAdapter) for hover/press effects
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { hover = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)   { hover = false; pressed = false; repaint(); }
            @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
        });
    }

    // {normal, hover, pressed} colors for each style (read at paint time -> theme toggle works)
    private Color[] palette() {
        return switch (style) {
            case PRIMARY   -> new Color[] {Theme.PRIMARY, Theme.PRIMARY_HOVER, Theme.PRIMARY_PRESSED};
            case SUCCESS   -> new Color[] {Theme.SUCCESS,
                    Theme.blend(Theme.SUCCESS, Color.BLACK, 0.12f),
                    Theme.blend(Theme.SUCCESS, Color.BLACK, 0.25f)};
            case DANGER    -> new Color[] {Theme.DANGER,
                    Theme.blend(Theme.DANGER, Color.BLACK, 0.12f),
                    Theme.blend(Theme.DANGER, Color.BLACK, 0.25f)};
            case SECONDARY -> new Color[] {Theme.SURFACE, Theme.PRIMARY_SOFT,
                    Theme.blend(Theme.PRIMARY_SOFT, Theme.BORDER, 0.5f)};
            case GHOST     -> new Color[] {null, Theme.PRIMARY_SOFT,
                    Theme.blend(Theme.PRIMARY_SOFT, Theme.BORDER, 0.5f)};
        };
    }

    private Color background() {
        Color[] c = palette();
        if (!isEnabled()) {
            return c[0] == null ? null : Theme.blend(c[0], Theme.BACKGROUND, 0.55f);
        }
        if (pressed) return c[2];
        if (hover)   return c[1];
        return c[0];
    }

    // Text and icon color follow the style (icons use the button's foreground)
    @Override
    public Color getForeground() {
        if (style == null) {                 // called by the JButton constructor before 'style' is set
            return super.getForeground();
        }
        Color fg = switch (style) {
            case PRIMARY, DANGER, SUCCESS -> Color.WHITE;
            case SECONDARY, GHOST         -> Theme.TEXT;
        };
        return isEnabled() ? fg : Theme.blend(fg, Theme.BACKGROUND, 0.5f);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = Theme.RADIUS_BTN * 2;
        Color bg = background();
        if (bg != null) {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        }
        if (style == Style.SECONDARY) {
            g2.setColor(hover && isEnabled() ? Theme.PRIMARY : Theme.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g2.dispose();

        super.paintComponent(g);             // draws the text and the icon
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, 42);
        return d;
    }
}