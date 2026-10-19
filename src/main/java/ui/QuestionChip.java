package ui;

import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Numbered chip in the question navigator. Color shows current / answered / unanswered. */
public class QuestionChip extends JButton {

    public enum State { CURRENT, ANSWERED, UNANSWERED }

    private State state = State.UNANSWERED;
    private boolean hover = false;

    public QuestionChip(int number, Runnable onClick) {
        super(String.valueOf(number));
        setFont(Theme.BODY_BOLD);
        setMargin(new Insets(0, 0, 0, 0));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Go to question " + number);
        addActionListener(e -> onClick.run());

        // Event handling: MouseListener (via MouseAdapter) for the hover effect
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
        });
    }

    public void setState(State newState) {
        if (newState != state) {
            state = newState;
            repaint();
        }
    }

    // ---- colors (public static so the legend in QuizScreen can reuse them) ----
    public static Color fillColor(State s) {
        return switch (s) {
            case CURRENT    -> Theme.PRIMARY;
            case ANSWERED   -> Theme.SUCCESS_SOFT;
            case UNANSWERED -> Theme.SURFACE;
        };
    }

    public static Color borderColor(State s) {
        return switch (s) {
            case CURRENT    -> Theme.PRIMARY;
            case ANSWERED   -> Theme.SUCCESS;
            case UNANSWERED -> Theme.BORDER;
        };
    }

    public static Color textColor(State s) {
        return switch (s) {
            case CURRENT    -> Theme.WHITE;
            case ANSWERED   -> Theme.isDark() ? Theme.blend(Theme.SUCCESS, Color.WHITE, 0.6f)
                                              : Color.decode("#166534");
            case UNANSWERED -> Theme.TEXT;
        };
    }

    @Override
    public Color getForeground() {
        if (state == null) {                    // called by the JButton constructor before 'state' is set
            return super.getForeground();
        }
        return textColor(state);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(40, 40);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = fillColor(state);
        if (hover && state != State.CURRENT) {
            fill = Theme.blend(fill, Theme.PRIMARY_SOFT, 0.6f);
        }
        int arc = 20;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.setColor(hover && state == State.UNANSWERED ? Theme.PRIMARY : borderColor(state));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
        g2.dispose();

        super.paintComponent(g);                // draws the number
    }
}