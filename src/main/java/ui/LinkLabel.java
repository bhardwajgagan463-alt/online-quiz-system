package ui;

import util.Theme;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

/** Clickable text link. Event handling: MouseListener (via MouseAdapter). */
public class LinkLabel extends JLabel {

    private static final Font NORMAL = Theme.BODY_BOLD;
    private static final Font UNDERLINED =
            Theme.BODY_BOLD.deriveFont(Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON));

    private boolean hover = false;

    public LinkLabel(String text, Runnable onClick) {
        super(text);
        setFont(NORMAL);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }

            @Override public void mouseEntered(MouseEvent e) {
                hover = true;
                setFont(UNDERLINED);
                repaint();
            }

            @Override public void mouseExited(MouseEvent e) {
                hover = false;
                setFont(NORMAL);
                repaint();
            }
        });
    }

    @Override
    public Color getForeground() {
        if (!hover) {
            return Theme.PRIMARY;
        }
        // lighter on dark mode, darker on light mode
        return Theme.isDark() ? Theme.blend(Theme.PRIMARY, Color.WHITE, 0.3f) : Theme.PRIMARY_HOVER;
    }
}