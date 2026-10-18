package ui;

import util.Theme;

import javax.swing.JTable;
import java.awt.Component;

/** Table cell renderer: PASS in green, FAIL in red. Inheritance: extends StripedRenderer. */
public class StatusRenderer extends UIHelper.StripedRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        boolean pass = "PASS".equalsIgnoreCase(String.valueOf(value));
        setForeground(pass ? Theme.SUCCESS : Theme.DANGER);
        setFont(Theme.BODY_BOLD);
        return c;
    }
}