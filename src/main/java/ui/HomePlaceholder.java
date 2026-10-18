package ui;

import model.User;
import util.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

/** Temporary screen shown after login. Replaced by the dashboards in steps 5 and 6. */
public class HomePlaceholder extends BaseScreen {

    private final User user;
    private final ScreenNavigator nav;

    public HomePlaceholder(User user, ScreenNavigator nav) {
        this.user = user;
        this.nav = nav;
        buildUI();
    }

    @Override
    protected void buildUI() {
        JPanel top = UIHelper.transparentPanel(new FlowLayout(FlowLayout.RIGHT, Theme.PAD, Theme.PAD));
        top.add(new ThemeToggleButton());
        add(top, BorderLayout.NORTH);

        JLabel welcome = UIHelper.heading(user.getWelcomeText());   // polymorphism: Admin/Student text
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel info = UIHelper.muted("Signed in as " + user.getRole() + ". Dashboards arrive in the next steps.");
        info.setHorizontalAlignment(SwingConstants.CENTER);

        RoundedButton logout = new RoundedButton("Logout", Icons.of(Icons.Type.LOGOUT, 18), RoundedButton.Style.DANGER);
        logout.setToolTipText("Sign out");
        logout.addActionListener(e -> nav.logout());

        FormPanel form = new FormPanel();
        form.addCentered(new LogoBadge(64), 0);
        form.addRow(welcome, 16);
        form.addRow(info, 4);
        form.addRow(logout, 20);

        CardPanel card = new CardPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 480;
                return d;
            }
        };
        card.add(form, BorderLayout.CENTER);

        JPanel center = UIHelper.transparentPanel(new GridBagLayout());
        center.add(card);
        add(center, BorderLayout.CENTER);
    }
}