package ui;

import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Navigation item for the sidebar (icon + text, hover and selected states). */
public class SidebarButton extends JButton {

    private boolean hover = false;
    private boolean active = false;

    public SidebarButton(String text, Icons.Type icon) {
        super(text, Icons.of(icon, 20));
        setHorizontalAlignment(SwingConstants.LEFT);
        setIconTextGap(14);
        setFont(Theme.BODY_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Event handling: MouseListener (via MouseAdapter)
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    // Text and icon color (the icon reads the button's foreground)
    @Override
    public Color getForeground() {
        if (active) {
            return Theme.isDark() ? Theme.blend(Theme.PRIMARY, Color.WHITE, 0.55f) : Theme.PRIMARY;
        }
        return hover ? Theme.TEXT : Theme.TEXT_MUTED;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg = null;
        if (active) {
            bg = Theme.PRIMARY_SOFT;
        } else if (hover) {
            bg = Theme.blend(Theme.SURFACE, Theme.PRIMARY_SOFT, 0.6f);
        }
        if (bg != null) {
            g2.setColor(bg);
            int arc = Theme.RADIUS_BTN * 2;
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
