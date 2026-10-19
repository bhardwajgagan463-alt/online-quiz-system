package ui;

import model.User;

/** Inheritance: StudentDashboard IS-A BaseDashboard. */
public class StudentDashboard extends BaseDashboard {

    public StudentDashboard(User user, ScreenNavigator nav) {
        super(user, nav);
        buildUI();
    }

    @Override
    protected void buildUI() {
        addPage("home",     "Dashboard",  Icons.Type.DASHBOARD, new StudentHomePage(user, this::showPage));
        addPage("quizzes",  "Take a Quiz", Icons.Type.DOCUMENT, new AvailableQuizzesPage(nav::startQuiz));
        addPage("attempts", "My Attempts", Icons.Type.CHART,    new MyAttemptsPage(user));
        showPage("home");
    }
}