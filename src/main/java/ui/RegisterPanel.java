package ui;

import dao.UserDAO;
import model.Admin;
import model.Student;
import model.User;
import util.DatabaseException;
import util.PasswordUtil;
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
import java.awt.GridLayout;

/** Registration screen for Student and Admin accounts. */
public class RegisterPanel extends BaseScreen {

    /** Admins must type this code to register (change it as you like). */
    private static final String ADMIN_CODE = "QUIZ-ADMIN";

    private final ScreenNavigator nav;
    private final UserDAO userDao = new UserDAO();

    private final RoleSelector roleSelector = new RoleSelector("Register as");
    private final StyledTextField nameField =
            new StyledTextField("Full name", Icons.muted(Icons.Type.USER, 18));
    private final StyledTextField usernameField =
            new StyledTextField("Username", Icons.muted(Icons.Type.USER, 18));
    private final StyledTextField emailField =
            new StyledTextField("Email address", Icons.muted(Icons.Type.MAIL, 18));
    private final StyledPasswordField passwordField = new StyledPasswordField("Password");
    private final StyledPasswordField confirmField = new StyledPasswordField("Confirm password");
    private final StyledPasswordField codeField = new StyledPasswordField("Admin access code");
    private final JLabel errorLabel = UIHelper.label(" ", Theme.SMALL, () -> Theme.DANGER);

    public RegisterPanel(ScreenNavigator nav) {
        this.nav = nav;
        buildUI();
    }

    @Override
    protected void buildUI() {
        JPanel top = UIHelper.transparentPanel(new FlowLayout(FlowLayout.RIGHT, Theme.PAD, Theme.PAD));
        top.add(new ThemeToggleButton());
        add(top, BorderLayout.NORTH);

        JLabel title = UIHelper.title("Create account");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subtitle = UIHelper.muted("Join " + ScreenNavigator.APP_NAME + " in a few seconds");
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        nameField.setToolTipText("Your full name");
        usernameField.setToolTipText("4-20 letters, digits or _ (starts with a letter)");
        emailField.setToolTipText("Example: name@college.edu");
        passwordField.setToolTipText("At least 6 characters with a letter and a digit");
        confirmField.setToolTipText("Type the same password again");
        codeField.setToolTipText("Ask your administrator for the access code");
        codeField.setVisible(false);

        // Show the admin code field only when "Admin" is selected
        roleSelector.onChange(() -> {
            codeField.setVisible(roleSelector.isAdminSelected());
            revalidate();
            repaint();
        });

        RoundedButton registerBtn = new RoundedButton("Create account", Icons.of(Icons.Type.PLUS, 18));
        registerBtn.setToolTipText("Create your account");
        registerBtn.addActionListener(e -> doRegister());
        confirmField.addActionListener(e -> doRegister());

        JPanel passRow = UIHelper.transparentPanel(new GridLayout(1, 2, Theme.PAD_SM, 0));
        passRow.add(passwordField);
        passRow.add(confirmField);

        JPanel links = UIHelper.transparentPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        links.add(UIHelper.muted("Already have an account?"));
        links.add(new LinkLabel("Sign in", nav::showLogin));

        FormPanel form = new FormPanel();
        form.addRow(title, 0);
        form.addRow(subtitle, 4);
        form.addRow(roleSelector, 16);
        form.addRow(nameField, 16);
        form.addRow(usernameField);
        form.addRow(emailField);
        form.addRow(passRow);
        form.addRow(codeField);
        form.addRow(errorLabel, 4);
        form.addRow(registerBtn, 8);
        form.addRow(links, 16);

        CardPanel card = new CardPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 500;
                return d;
            }
        };
        card.add(form, BorderLayout.CENTER);

        JPanel center = UIHelper.transparentPanel(new GridBagLayout());
        center.add(card);
        add(center, BorderLayout.CENTER);
    }

    private void doRegister() {
        try {
            String name = Validator.capitalizeWords(nameField.getText());
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim().toLowerCase();
            String password = passwordField.getPasswordText();
            String confirm = confirmField.getPasswordText();

            Validator.validateName(name);
            Validator.validateUsername(username);
            Validator.validateEmail(email);
            Validator.validatePassword(password);
            Validator.validateConfirm(password, confirm);

            boolean wantsAdmin = roleSelector.isAdminSelected();
            if (wantsAdmin && !ADMIN_CODE.equals(codeField.getPasswordText().trim())) {
                throw new ValidationException("Invalid admin access code");
            }

            String hash = PasswordUtil.hash(password);
            // Polymorphism: a User variable can hold an Admin or a Student object
            User user = wantsAdmin
                    ? new Admin(name, username, email, hash)
                    : new Student(name, username, email, hash);

            userDao.register(user);
            UIHelper.info(this, "Account created! You can sign in now.");
            reset();
            nav.showLogin();
        } catch (ValidationException e) {
            errorLabel.setText(e.getMessage());
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }

    public void reset() {
        nameField.setText("");
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        codeField.setText("");
        codeField.setVisible(false);
        roleSelector.reset();
        errorLabel.setText(" ");
    }
}