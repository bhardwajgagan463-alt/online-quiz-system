package ui;

import dao.UserDAO;
import model.User;
import util.AuthenticationException;
import util.DatabaseException;
import util.Theme;
import util.ValidationException;
import util.Validator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

/** Login screen. Inheritance: extends BaseScreen (abstract class). */
public class LoginPanel extends BaseScreen {

    private final ScreenNavigator nav;
    private final UserDAO userDao = new UserDAO();

    private final RoleSelector roleSelector = new RoleSelector("Sign in as");
    private final StyledTextField usernameField =
            new StyledTextField("Username", Icons.muted(Icons.Type.USER, 18));
    private final StyledPasswordField passwordField = new StyledPasswordField("Password");
    private final JLabel errorLabel = UIHelper.label(" ", Theme.SMALL, () -> Theme.DANGER);

    public LoginPanel(ScreenNavigator nav) {
        this.nav = nav;
        buildUI();
    }

    @Override
    protected void buildUI() {
        // Top bar with theme toggle
        JPanel top = UIHelper.transparentPanel(new FlowLayout(FlowLayout.RIGHT, Theme.PAD, Theme.PAD));
        top.add(new ThemeToggleButton());
        add(top, BorderLayout.NORTH);

        JLabel title = UIHelper.title("Welcome back");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subtitle = UIHelper.muted("Sign in to continue to " + ScreenNavigator.APP_NAME);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        usernameField.setToolTipText("Enter your username");
        passwordField.setToolTipText("Enter your password");

        RoundedButton loginBtn = new RoundedButton("Sign in", Icons.of(Icons.Type.ARROW_RIGHT, 18));
        loginBtn.setToolTipText("Sign in to your account");

        // Event handling: one ActionListener shared by the button and the Enter key
        ActionListener loginAction = e -> doLogin();
        loginBtn.addActionListener(loginAction);
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        JPanel links = UIHelper.transparentPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        links.add(UIHelper.muted("New here?"));
        links.add(new LinkLabel("Create an account", nav::showRegister));

        FormPanel form = new FormPanel();
        form.addCentered(new LogoBadge(64), 0);
        form.addRow(title, 14);
        form.addRow(subtitle, 4);
        form.addRow(roleSelector, 18);
        form.addRow(usernameField, 16);
        form.addRow(passwordField);
        form.addRow(errorLabel, 4);
        form.addRow(loginBtn, 8);
        form.addRow(links, 16);

        // Card with a fixed width (anonymous subclass of CardPanel)
        CardPanel card = new CardPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 440;
                return d;
            }
        };
        card.add(form, BorderLayout.CENTER);

        JPanel center = UIHelper.transparentPanel(new GridBagLayout());   // centers the card
        center.add(card);
        add(center, BorderLayout.CENTER);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getPasswordText();
        try {
            Validator.required(username, "Username");
            Validator.required(password, "Password");
            User user = userDao.login(username, password, roleSelector.getRole());
            reset();
            nav.onLoginSuccess(user);
        } catch (ValidationException | AuthenticationException e) {
            showError(e.getMessage());            // inline red message
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    public void reset() {
        usernameField.setText("");
        passwordField.setText("");
        roleSelector.reset();
        showError(" ");
    }
}