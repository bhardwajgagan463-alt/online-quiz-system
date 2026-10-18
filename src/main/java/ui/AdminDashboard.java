package ui;

import model.User;

/** Inheritance: AdminDashboard IS-A BaseDashboard. It only decides which pages exist. */
public class AdminDashboard extends BaseDashboard {

    public AdminDashboard(User user, ScreenNavigator nav) {
        super(user, nav);
        buildUI();
    }

    @Override
    protected void buildUI() {
        addPage("overview",  "Dashboard",  Icons.Type.DASHBOARD, new AdminOverviewPage(this::showPage));
        addPage("quizzes",   "Quizzes",    Icons.Type.DOCUMENT,  new QuizzesPage());
        addPage("questions", "Questions",  Icons.Type.LIST,      new QuestionsPage());
        addPage("results",   "Results",    Icons.Type.CHART,     new ResultsPage());
        showPage("overview");
    }
}