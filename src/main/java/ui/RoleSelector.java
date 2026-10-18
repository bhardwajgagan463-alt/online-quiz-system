package ui;

import model.User;
import util.Theme;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

/** "Student / Admin" choice using JRadioButton + ButtonGroup. */
public class RoleSelector extends JPanel {

    private final JRadioButton studentRadio = new JRadioButton("Student", true);
    private final JRadioButton adminRadio = new JRadioButton("Admin");

    public RoleSelector(String label) {
        super(new FlowLayout(FlowLayout.CENTER, 14, 0));
        setOpaque(false);
        add(UIHelper.muted(label));

        ButtonGroup group = new ButtonGroup();
        for (JRadioButton rb : new JRadioButton[] {studentRadio, adminRadio}) {
            rb.setFont(Theme.BODY);
            rb.setOpaque(false);
            rb.setFocusPainted(false);
            rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            group.add(rb);
            add(rb);
        }
        studentRadio.setToolTipText("Take quizzes and see your results");
        adminRadio.setToolTipText("Manage questions, quizzes and results");
    }

    public String getRole() {
        return adminRadio.isSelected() ? User.ROLE_ADMIN : User.ROLE_STUDENT;
    }

    public boolean isAdminSelected() {
        return adminRadio.isSelected();
    }

    public void onChange(Runnable action) {
        ActionListener al = e -> action.run();
        studentRadio.addActionListener(al);
        adminRadio.addActionListener(al);
    }

    public void reset() {
        studentRadio.setSelected(true);
    }
}