package ui;

import util.Theme;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Vertical form: every row is full width. Uses GridBagLayout (layout manager). */
public class FormPanel extends JPanel {

    private int row = 0;

    public FormPanel() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    // Method overloading
    public void addRow(JComponent c) {
        addRow(c, Theme.PAD_SM);
    }

    public void addRow(JComponent c, int topGap) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = row++;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(topGap, 0, 0, 0);
        add(c, gc);
    }

    /** Adds a component at its own size, centered in the row. */
    public void addCentered(JComponent c, int topGap) {
        JPanel wrap = UIHelper.transparentPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.add(c);
        addRow(wrap, topGap);
    }
}