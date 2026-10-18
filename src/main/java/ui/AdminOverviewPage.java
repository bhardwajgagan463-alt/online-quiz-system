package ui;

import dao.QuestionDAO;
import dao.QuizDAO;
import dao.ResultDAO;
import dao.UserDAO;
import model.Result;
import model.User;
import util.DatabaseException;
import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

/** Admin home: summary numbers and the latest attempts. */
public class AdminOverviewPage extends BaseScreen implements Refreshable {

    private final Consumer<String> navigate;          // asks the dashboard to open another page

    private final QuizDAO quizDao = new QuizDAO();
    private final QuestionDAO questionDao = new QuestionDAO();
    private final UserDAO userDao = new UserDAO();
    private final ResultDAO resultDao = new ResultDAO();

    private final StatCard quizzesCard = new StatCard("Quizzes", Icons.Type.DOCUMENT, Theme.PRIMARY);
    private final StatCard questionsCard = new StatCard("Questions", Icons.Type.LIST, Theme.WARNING);
    private final StatCard studentsCard = new StatCard("Students", Icons.Type.USER, Theme.SUCCESS);
    private final StatCard attemptsCard = new StatCard("Attempts", Icons.Type.CHART, Color.decode("#0EA5E9"));

    private final JLabel summaryLabel = UIHelper.muted(" ");
    private final DefaultTableModel model =
            UIHelper.readOnlyModel("Student", "Quiz", "Score", "Percentage", "Result", "Date");
    private final JTable table = new JTable(model);

    public AdminOverviewPage(Consumer<String> navigate) {
        this.navigate = navigate;
        buildUI();
    }

    @Override
    protected void buildUI() {
        JPanel stats = UIHelper.transparentPanel(new GridLayout(1, 4, 0, 0));
        stats.add(quizzesCard);
        stats.add(questionsCard);
        stats.add(studentsCard);
        stats.add(attemptsCard);

        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        JPanel titles = UIHelper.transparentPanel(new GridLayout(0, 1));
        titles.add(UIHelper.heading("Recent attempts"));
        titles.add(summaryLabel);

        RoundedButton newQuiz = new RoundedButton("New quiz", Icons.of(Icons.Type.PLUS, 18));
        newQuiz.setToolTipText("Go to Quizzes");
        newQuiz.addActionListener(e -> navigate.accept("quizzes"));

        RoundedButton addQuestion = new RoundedButton("Add questions", Icons.of(Icons.Type.LIST, 18),
                RoundedButton.Style.SECONDARY);
        addQuestion.setToolTipText("Go to Questions");
        addQuestion.addActionListener(e -> navigate.accept("questions"));

        JPanel actions = UIHelper.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(newQuiz);
        actions.add(addQuestion);

        JPanel header = UIHelper.transparentPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_SM, Theme.PAD_SM, 0, Theme.PAD_SM));
        header.add(titles, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        CardPanel tableCard = new CardPanel(new BorderLayout(0, Theme.PAD_SM), Theme.PAD_SM);
        tableCard.add(header, BorderLayout.NORTH);
        tableCard.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(stats, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            quizzesCard.setValue(String.valueOf(quizDao.findAll().size()));
            questionsCard.setValue(String.valueOf(questionDao.findAll().size()));
            studentsCard.setValue(String.valueOf(userDao.findAll(User.ROLE_STUDENT).size()));

            List<Result> results = resultDao.findAll();
            attemptsCard.setValue(String.valueOf(results.size()));

            int passed = 0;
            for (Result r : results) {
                if (r.isPassed()) {
                    passed++;
                }
            }
            summaryLabel.setText(results.isEmpty()
                    ? "No attempts yet"
                    : Math.round(passed * 100.0 / results.size()) + "% overall pass rate");

            model.setRowCount(0);
            int limit = Math.min(8, results.size());
            for (int i = 0; i < limit; i++) {
                Result r = results.get(i);
                model.addRow(new Object[] {
                        r.getStudentName(), r.getQuizTitle(), r.getScoreText(),
                        String.format("%.1f%%", r.getPercentage()), r.getStatus(), r.getAttemptedAt()
                });
            }
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }
}