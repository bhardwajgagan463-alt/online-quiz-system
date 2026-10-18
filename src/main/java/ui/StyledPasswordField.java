package ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import java.awt.Cursor;
import java.awt.Dimension;

/** Password field with a lock icon and a show/hide (eye) toggle. */
public class StyledPasswordField extends JPasswordField {

    private static final char DOT = '\u2022';
    private boolean revealed = false;

    public StyledPasswordField(String placeholder) {
        UIHelper.styleField(this, placeholder, Icons.muted(Icons.Type.LOCK, 18));
        setColumns(18);
        setEchoChar(DOT);

        JButton eye = new JButton(Icons.muted(Icons.Type.EYE, 18));
        eye.setToolTipText("Show / hide password");
        eye.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        eye.setContentAreaFilled(false);
        eye.setFocusable(false);
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.addActionListener(e -> toggle(eye));            // event handling: ActionListener

        putClientProperty("JTextField.trailingComponent", eye);   // FlatLaf places it inside the field
    }

    private void toggle(JButton eye) {
        revealed = !revealed;
        setEchoChar(revealed ? (char) 0 : DOT);              // char 0 = show the real text
        eye.setIcon(Icons.muted(revealed ? Icons.Type.EYE_OFF : Icons.Type.EYE, 18));
    }

    /** Convenience: password as a String. */
    public String getPasswordText() {
        return new String(getPassword());
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, 42);
        return d;
    }
}