package ui;

import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/** Temporary component preview. Replaced by the real login flow in step 4. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();

            // ---- Left card: fields and buttons ----
            CardPanel form = new CardPanel(new GridLayout(0, 1, 0, Theme.PAD_SM));
            form.setPreferredSize(new Dimension(360, 0));
            form.add(UIHelper.heading("Components preview"));
            form.add(new StyledTextField("Username", Icons.muted(Icons.Type.USER, 18)));
            form.add(new StyledPasswordField("Password"));

            RoundedButton login = new RoundedButton("Login", Icons.of(Icons.Type.CHECK, 18));
            login.setToolTipText("Click to login");
            form.add(login);
            form.add(new RoundedButton("Add", Icons.of(Icons.Type.PLUS, 18), RoundedButton.Style.SUCCESS));
            form.add(new RoundedButton("Edit", Icons.of(Icons.Type.EDIT, 18), RoundedButton.Style.SECONDARY));
            form.add(new RoundedButton("Delete", Icons.of(Icons.Type.TRASH, 18), RoundedButton.Style.DANGER));

            // ---- Right card: styled table ----
            DefaultTableModel model = UIHelper.readOnlyModel("Student", "Quiz", "Score", "Result");
            model.addRow(new Object[] {"Asha",  "Java Basics", "8/10", "Pass"});
            model.addRow(new Object[] {"Rahul", "DBMS",        "3/10", "Fail"});
            model.addRow(new Object[] {"Meera", "OOP",         "9/10", "Pass"});
            model.addRow(new Object[] {"Karan", "Networks",    "4/10", "Fail"});
            model.addRow(new Object[] {"Sana",  "Java Basics", "7/10", "Pass"});

            JTable table = new JTable(model);
            UIHelper.styleTable(table);
            // Inheritance: small subclass of StripedRenderer to color the Result column
            table.getColumnModel().getColumn(3).setCellRenderer(new UIHelper.StripedRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    setForeground("Pass".equals(v) ? Theme.SUCCESS : Theme.DANGER);
                    setFont(Theme.BODY_BOLD);
                    return c;
                }
            });

            CardPanel tableCard = new CardPanel(new BorderLayout(), Theme.PAD_SM);
            tableCard.add(UIHelper.scroll(table), BorderLayout.CENTER);

            // ---- Top bar: theme toggle ----
            RoundedButton themeBtn = new RoundedButton("Toggle theme",
                    Icons.of(Icons.Type.MOON, 18), RoundedButton.Style.GHOST);
            themeBtn.setToolTipText("Switch light / dark mode");
            themeBtn.addActionListener(e -> {
                Theme.toggle();
                themeBtn.setIcon(Icons.of(Theme.isDark() ? Icons.Type.SUN : Icons.Type.MOON, 18));
            });
            JPanel top = UIHelper.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            top.add(themeBtn);

            // ---- Assemble ----
            JPanel root = new JPanel(new BorderLayout(Theme.PAD, Theme.PAD));
            root.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD));
            root.add(top, BorderLayout.NORTH);
            root.add(form, BorderLayout.WEST);
            root.add(tableCard, BorderLayout.CENTER);

            JFrame frame = new JFrame("Components Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(root);
            frame.setSize(960, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}