package ui;

import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.function.Supplier;

/** Helper class with reusable styled components and factory methods. */
public final class UIHelper {

    private UIHelper() { }

    // ================= Labels =================

    /** JLabel whose color is looked up on every paint, so it follows light/dark mode. */
    public static class ThemedLabel extends JLabel {
        private final Supplier<Color> color;

        public ThemedLabel(String text, Font font, Supplier<Color> color) {
            super(text);
            this.color = color;
            setFont(font);
        }

        @Override
        public Color getForeground() {
            return color == null ? super.getForeground() : color.get();
        }
    }

    // Method overloading: label(...)
    public static JLabel label(String text, Font font) {
        return new ThemedLabel(text, font, () -> Theme.TEXT);
    }

    public static JLabel label(String text, Font font, Supplier<Color> color) {
        return new ThemedLabel(text, font, color);
    }

    public static JLabel title(String text)   { return label(text, Theme.TITLE); }
    public static JLabel heading(String text) { return label(text, Theme.HEADING); }
    public static JLabel body(String text)    { return label(text, Theme.BODY); }
    public static JLabel muted(String text)   { return label(text, Theme.BODY, () -> Theme.TEXT_MUTED); }

    // ================= Panels =================

    public static JPanel transparentPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    public static JPanel transparentPanel() {
        return transparentPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    }

    public static JScrollPane scroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false); 
        sp.getViewport().setOpaque(false);
        return sp;
    }

    // ================= Text fields =================

    public static void styleField(JTextComponent field, String placeholder, Icon leadingIcon) {
        field.setFont(Theme.BODY);
        field.setMargin(new Insets(8, 12, 8, 12));
        field.putClientProperty("JTextField.placeholderText", placeholder);   // FlatLaf placeholder
        if (leadingIcon != null) {
            field.putClientProperty("JTextField.leadingIcon", leadingIcon);
        }
    }

    // ================= Tables =================

    /** Table model whose cells cannot be edited by double-click. */
    public static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Alternating row colors, padding, selection color. Extend it to color single columns. */
    public static class StripedRenderer extends DefaultTableCellRenderer {
        private static final Border PADDING = BorderFactory.createEmptyBorder(0, 14, 0, 14);

        public StripedRenderer() {
            this(SwingConstants.LEFT);
        }

        public StripedRenderer(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setBorder(PADDING);
            setFont(Theme.BODY);
            setForeground(Theme.TEXT);
            if (isSelected) {
                setBackground(Theme.PRIMARY_SOFT);
            } else {
                setBackground(row % 2 == 0 ? Theme.SURFACE : Theme.TABLE_STRIPE);
            }
            return this;
        }
    }

    /** Styled table header. */
    public static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setFont(Theme.BODY_BOLD);
            setForeground(Theme.TEXT);
            setBackground(Theme.TABLE_HEADER_BG);
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)));
            return this;
        }
    }
        /** Small bold grey label used above form fields. */
    public static JLabel caption(String text) {
        return label(text, Theme.font(Font.BOLD, Theme.SIZE_SMALL), () -> Theme.TEXT_MUTED);
    }

    

    public static void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setFont(Theme.BODY);
        table.setDefaultRenderer(Object.class, new StripedRenderer());
        table.setDefaultRenderer(Number.class, new StripedRenderer());

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
    }

    // ================= Dialogs (method overloading style helpers) =================

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}