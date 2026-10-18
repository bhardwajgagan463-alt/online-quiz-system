package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

/** CardPanel with a fixed width (used for forms and lists on the side). */
public class SideCard extends CardPanel {

    private final int width;

    public SideCard(int width) {
        super(new BorderLayout());
        this.width = width;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = width;
        return d;
    }
}