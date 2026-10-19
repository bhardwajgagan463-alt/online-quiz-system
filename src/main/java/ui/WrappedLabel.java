package ui;

import util.Theme;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;

/** Label that wraps long text (HTML) and follows light/dark mode. */
public class WrappedLabel extends JLabel implements Theme.ThemeListener {

    private final int wrapWidth;
    private final int minHeight;

    public WrappedLabel(Font font, int wrapWidth, int minHeight) {
        this.wrapWidth = wrapWidth;
        this.minHeight = minHeight;
        setFont(font);
        setForeground(Theme.TEXT);
        setVerticalAlignment(SwingConstants.TOP);
    }

    public void setWrappedText(String text) {
        setText("<html><body style='width:" + wrapWidth + "px'>" + escape(text) + "</body></html>");
    }

    /** Makes text safe inside HTML (questions may contain < > & characters). */
    public static String escape(String s) {
        StringBuffer sb = new StringBuffer();               // StringBuffer
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '\n' -> sb.append("<br>");
                default -> sb.append(ch);
            }
        }
        return sb.toString();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, minHeight);
        return d;
    }

    @Override
    public void themeChanged() {
        setForeground(Theme.TEXT);           // a color change rebuilds the HTML text color
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
}