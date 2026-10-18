package ui;

import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/**
 * White (or dark) rounded card with a soft shadow, custom painted.
 * Children are placed inside the card; put transparent panels inside it
 * (see UIHelper.transparentPanel) so no grey rectangles show.
 */
public class CardPanel extends JPanel {

    // ---- Method overloading ----
    public CardPanel() {
        this(new BorderLayout(), Theme.PAD_LG);
    }

    public CardPanel(LayoutManager layout) {
        this(layout, Theme.PAD_LG);
    }

    public CardPanel(LayoutManager layout, int padding) {
        super(layout);
        setOpaque(false);
        int inset = Theme.SHADOW_SIZE + padding;       // room for the shadow + inner padding
        setBorder(BorderFactory.createEmptyBorder(inset, inset, inset, inset));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int s = Theme.SHADOW_SIZE;
        int w = getWidth() - 2 * s;
        int h = getHeight() - 2 * s;

        // Soft shadow: stacked translucent rounded rectangles, slightly lower than the card
        int layers = s - 2;
        Color shadow = Theme.withAlpha(Theme.SHADOW, Theme.SHADOW.getAlpha() / layers + 1);
        g2.setColor(shadow);
        for (int i = layers; i >= 1; i--) {
            int arc = (Theme.RADIUS + i) * 2;
            g2.fillRoundRect(s - i, s - i + 2, w + 2 * i, h + 2 * i, arc, arc);
        }

        // Card surface + thin border
        int arc = Theme.RADIUS * 2;
        g2.setColor(Theme.SURFACE);
        g2.fillRoundRect(s, s, w, h, arc, arc);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(s, s, w - 1, h - 1, arc, arc);

        g2.dispose();
    }
}