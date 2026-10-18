package ui;

import model.User;
import util.Session;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** The one application window. CardLayout swaps screens inside it. */
public class MainFrame extends JFrame implements ScreenNavigator {

    private static final String LOGIN = "login";
    private static final String REGISTER = "register";
    private static final String HOME = "home";

    private final CardLayout cards = new CardLayout();
    private final JPanel container = new JPanel(cards);
    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;
    private BaseScreen homeScreen;               // created after login

    public MainFrame() {
        super(ScreenNavigator.APP_NAME + " - Online Quiz System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Event handling: WindowAdapter -> ask before closing if someone is signed in
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        container.add(loginPanel, LOGIN);
        container.add(registerPanel, REGISTER);

        setContentPane(container);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        showLogin();
    }

    @Override
    public void showLogin() {
        loginPanel.reset();
        cards.show(container, LOGIN);
    }

    @Override
    public void showRegister() {
        registerPanel.reset();
        cards.show(container, REGISTER);
    }

    @Override
    public void onLoginSuccess(User user) {
        Session.setCurrentUser(user);
        removeHome();
        homeScreen = user.isAdmin()
                ? new AdminDashboard(user, this)
                : new HomePlaceholder(user, this);    
        container.add(homeScreen, HOME);
        cards.show(container, HOME);
    }

    @Override
    public void logout() {
        Session.clear();
        removeHome();
        showLogin();
    }

    private void removeHome() {
        if (homeScreen != null) {
            container.remove(homeScreen);
            homeScreen = null;
        }
    }

    private void confirmExit() {
        if (!Session.isLoggedIn() || UIHelper.confirm(this, "You are still signed in. Exit the application?")) {
            dispose();
            System.exit(0);
        }
    }
}