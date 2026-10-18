package ui;

import util.Theme;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

/** Small card with a colored icon, a big number and a caption. Inheritance: extends CardPanel. */
public class StatCard extends CardPanel {

    private final JLabel valueLabel = UIHelper.label("0", Theme.font(Font.BOLD, 28));

    public StatCard(String caption, Icons.Type icon, Color accent) {
        super(new BorderLayout(14, 0), Theme.PAD);

        JPanel text = UIHelper.transparentPanel(new GridLayout(2, 1));
        text.add(valueLabel);
        text.add(UIHelper.muted(caption));

        add(new IconCircle(icon, accent), BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    /** Round tinted background with the icon in the middle. */
    private static class IconCircle extends JComponent {
        private final Icons.Type icon;
        private final Color accent;

        IconCircle(Icons.Type icon, Color accent) {
            this.icon = icon;
            this.accent = accent;
            setPreferredSize(new Dimension(48, 48));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight());
            int x = (getWidth() - s) / 2;
            int y = (getHeight() - s) / 2;
            g2.setColor(Theme.withAlpha(accent, 40));
            g2.fillOval(x, y, s, s);
            Icons.of(icon, 24, accent).paintIcon(this, g2, x + (s - 24) / 2, y + (s - 24) / 2);
            g2.dispose();
        }
    }
}