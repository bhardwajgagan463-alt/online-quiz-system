package ui;

import model.User;
import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract dashboard: sidebar on the left, pages on the right.
 * Subclasses (AdminDashboard, StudentDashboard) only add their pages.
 * Syllabus: abstract class, inheritance, layout managers.
 */
public abstract class BaseDashboard extends BaseScreen {

    protected final User user;
    protected final ScreenNavigator nav;

    private final FormPanel navList = new FormPanel();
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(pageLayout);
    private final JLabel pageTitle = UIHelper.label(" ", Theme.font(Font.BOLD, 24));

    private final Map<String, SidebarButton> navButtons = new LinkedHashMap<>();
    private final Map<String, JComponent> pages = new LinkedHashMap<>();
    private final Map<String, String> titles = new LinkedHashMap<>();

    protected BaseDashboard(User user, ScreenNavigator nav) {
        this.user = user;
        this.nav = nav;
        buildShell();
    }

    /** Adds a sidebar item and its page. */
    protected void addPage(String key, String title, Icons.Type icon, JComponent page) {
        SidebarButton button = new SidebarButton(title, icon);
        button.setToolTipText("Open " + title);
        button.addActionListener(e -> showPage(key));       // event handling: ActionListener
        navList.addRow(button, 4);

        navButtons.put(key, button);
        pages.put(key, page);
        titles.put(key, title);
        pageHost.add(page, key);
    }

    protected void showPage(String key) {
        pageLayout.show(pageHost, key);
        for (Map.Entry<String, SidebarButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(key));
        }
        pageTitle.setText(titles.get(key));

        JComponent page = pages.get(key);
        if (page instanceof Refreshable r) {
            r.refresh();                                     // reload data every time
        }
    }

    // ---------------- layout ----------------

    private void buildShell() {
        add(buildSidebar(), BorderLayout.WEST);

        JPanel topBar = UIHelper.transparentPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(
                Theme.PAD, Theme.PAD_LG, Theme.PAD_SM, Theme.PAD_LG));
        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(new ThemeToggleButton(), BorderLayout.EAST);

        pageHost.setOpaque(false);
        pageHost.setBorder(BorderFactory.createEmptyBorder(0, Theme.PAD, Theme.PAD, Theme.PAD));

        JPanel content = UIHelper.transparentPanel(new BorderLayout());
        content.add(topBar, BorderLayout.NORTH);
        content.add(pageHost, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JComponent buildSidebar() {
        SidebarPanel side = new SidebarPanel();
        side.setPreferredSize(new Dimension(240, 0));
        side.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_LG, Theme.PAD, Theme.PAD, Theme.PAD));

        // Brand
        JPanel brand = UIHelper.transparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.add(new LogoBadge(40));
        brand.add(UIHelper.label(ScreenNavigator.APP_NAME, Theme.font(Font.BOLD, 20)));

        JPanel top = UIHelper.transparentPanel(new BorderLayout(0, Theme.PAD_LG));
        top.add(brand, BorderLayout.NORTH);
        top.add(navList, BorderLayout.CENTER);

        // Signed-in user + logout
        JPanel who = UIHelper.transparentPanel(new GridLayout(2, 1));
        who.add(UIHelper.label(user.getFullName(), Theme.BODY_BOLD));
        who.add(UIHelper.label(user.isAdmin() ? "Administrator" : "Student",
                Theme.SMALL, () -> Theme.TEXT_MUTED));

        JPanel chip = UIHelper.transparentPanel(new BorderLayout(12, 0));
        chip.add(new Avatar(user.getFullName()), BorderLayout.WEST);
        chip.add(who, BorderLayout.CENTER);

        RoundedButton logout = new RoundedButton("Logout", Icons.of(Icons.Type.LOGOUT, 18),
                RoundedButton.Style.SECONDARY);
        logout.setToolTipText("Sign out of your account");
        logout.addActionListener(e -> nav.logout());

        FormPanel bottom = new FormPanel();
        bottom.addRow(chip, 0);
        bottom.addRow(logout, 12);

        side.add(top, BorderLayout.NORTH);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    // ---------------- small inner classes ----------------

    /** Sidebar background with a thin line on its right edge. */
    private static class SidebarPanel extends JPanel {
        SidebarPanel() {
            super(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Theme.SURFACE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Theme.BORDER);
            g.fillRect(getWidth() - 1, 0, 1, getHeight());
        }
    }

    /** Round badge with the first letter of the user's name. */
    private static class Avatar extends JComponent {
        private final String letter;

        Avatar(String name) {
            this.letter = (name == null || name.isEmpty()) ? "?" : name.substring(0, 1).toUpperCase();
            setPreferredSize(new Dimension(40, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight());
            g2.setColor(Theme.PRIMARY);
            g2.fillOval(0, 0, s, s);

            g2.setColor(Theme.WHITE);
            g2.setFont(Theme.BODY_BOLD.deriveFont(16f));
            FontMetrics fm = g2.getFontMetrics();
            int x = (s - fm.stringWidth(letter)) / 2;
            int y = (s - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(letter, x, y);
            g2.dispose();
        }
    }
}