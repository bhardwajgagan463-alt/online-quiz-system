package ui;

import javax.swing.Icon;
import javax.swing.JTextField;
import java.awt.Dimension;

/** Rounded text field with placeholder text and an optional leading icon. */
public class StyledTextField extends JTextField {

    public StyledTextField(String placeholder) {
        this(placeholder, null);
    }

    public StyledTextField(String placeholder, Icon leadingIcon) {
        UIHelper.styleField(this, placeholder, leadingIcon);
        setColumns(18);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, 42);
        return d;
    }
}