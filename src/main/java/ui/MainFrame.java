package ui;

import dao.QuestionDAO;
import model.Question;
import model.Quiz;
import model.Result;
import model.User;
import util.DatabaseException;
import util.Session;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/** The one application window. CardLayout swaps screens inside it. */
public class MainFrame extends JFrame implements ScreenNavigator {

    private static final String LOGIN = "login";
    private static final String REGISTER = "register";
    private static final String HOME = "home";
    private static final String QUIZ = "quiz";

    private final CardLayout cards = new CardLayout();
    private final JPanel container = new JPanel(cards);
    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;
    private final QuestionDAO questionDao = new QuestionDAO();

    private BaseScreen homeScreen;               // dashboard, created after login
    private BaseScreen quizScreen;               // created when a quiz starts

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

    // ---------------- navigation ----------------

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
        showHome();
    }

    @Override
    public void logout() {
        Session.clear();
        removeQuiz();
        removeHome();
        showLogin();
    }

    @Override
    public void startQuiz(Quiz quiz) {
        try {
            List<Question> questions = questionDao.findByQuiz(quiz.getId(), true);   // shuffled
            if (questions.isEmpty()) {
                UIHelper.info(this, "This quiz has no questions yet.");
                return;
            }
            removeQuiz();
            quizScreen = new QuizScreen(quiz, questions, Session.getCurrentUser(), this);
            container.add(quizScreen, QUIZ);
            cards.show(container, QUIZ);
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }

    @Override
    public void leaveQuiz() {
        showHome();
    }

    /** TEMPORARY: a simple summary box. Step 7 replaces this with the result screen. */
    @Override
    public void onQuizFinished(Quiz quiz, List<Question> questions, int[] answers, Result result) {
        StringBuffer sb = new StringBuffer();
        sb.append("Quiz: ").append(quiz.getTitle()).append("\n");
        sb.append("Score: ").append(result.getScoreText())
          .append(String.format(" (%.1f%%)", result.getPercentage())).append("\n");
        sb.append("Result: ").append(result.getStatus()).append("\n");
        sb.append("Time taken: ").append(result.getTimeTakenText());
        UIHelper.info(this, sb.toString());
        showHome();
    }

    // ---------------- helpers ----------------

    /** Builds a fresh dashboard for the signed-in user (polymorphism: both are BaseScreen). */
    private void showHome() {
        removeQuiz();
        removeHome();
        User user = Session.getCurrentUser();
        homeScreen = user.isAdmin()
                ? new AdminDashboard(user, this)
                : new StudentDashboard(user, this);
        container.add(homeScreen, HOME);
        cards.show(container, HOME);
    }

    private void removeHome() {
        if (homeScreen != null) {
            container.remove(homeScreen);
            homeScreen = null;
        }
    }

    private void removeQuiz() {
        if (quizScreen != null) {
            container.remove(quizScreen);        // removeNotify() inside QuizScreen stops its timer
            quizScreen = null;
        }
    }

    private void confirmExit() {
        if (!Session.isLoggedIn()) {
            dispose();
            System.exit(0);
        }
        String message = quizScreen != null
                ? "A quiz is in progress and will be lost. Exit the application?"
                : "You are still signed in. Exit the application?";
        if (UIHelper.confirm(this, message)) {
            dispose();
            System.exit(0);
        }
    }
}