package ui;

import dao.QuizDAO;
import dao.ResultDAO;
import model.Quiz;
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

/** Student home: summary numbers and the latest attempts. */
public class StudentHomePage extends BaseScreen implements Refreshable {

    private final User user;
    private final Consumer<String> navigate;
    private final QuizDAO quizDao = new QuizDAO();
    private final ResultDAO resultDao = new ResultDAO();

    private final StatCard quizzesCard = new StatCard("Quizzes available", Icons.Type.DOCUMENT, Theme.PRIMARY);
    private final StatCard attemptsCard = new StatCard("Your attempts", Icons.Type.LIST, Theme.WARNING);
    private final StatCard averageCard = new StatCard("Average score", Icons.Type.CHART, Color.decode("#0EA5E9"));
    private final StatCard passedCard = new StatCard("Attempts passed", Icons.Type.CHECK, Theme.SUCCESS);

    private final JLabel summaryLabel = UIHelper.muted(" ");
    private final DefaultTableModel model =
            UIHelper.readOnlyModel("Quiz", "Score", "Percentage", "Result", "Date");
    private final JTable table = new JTable(model);

    public StudentHomePage(User user, Consumer<String> navigate) {
        this.user = user;
        this.navigate = navigate;
        buildUI();
    }

    @Override
    protected void buildUI() {
        JLabel welcome = UIHelper.label(user.getWelcomeText(), Theme.font(16), () -> Theme.TEXT_MUTED);
        welcome.setBorder(BorderFactory.createEmptyBorder(0, Theme.PAD_SM, Theme.PAD_SM, 0));

        JPanel stats = UIHelper.transparentPanel(new GridLayout(1, 4, 0, 0));
        stats.add(quizzesCard);
        stats.add(attemptsCard);
        stats.add(averageCard);
        stats.add(passedCard);

        JPanel north = UIHelper.transparentPanel(new BorderLayout());
        north.add(welcome, BorderLayout.NORTH);
        north.add(stats, BorderLayout.CENTER);

        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());

        JPanel titles = UIHelper.transparentPanel(new GridLayout(0, 1));
        titles.add(UIHelper.heading("Recent attempts"));
        titles.add(summaryLabel);

        RoundedButton start = new RoundedButton("Take a quiz", Icons.of(Icons.Type.ARROW_RIGHT, 18));
        start.setToolTipText("Choose a quiz to take");
        start.addActionListener(e -> navigate.accept("quizzes"));
        JPanel actions = UIHelper.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.add(start);

        JPanel header = UIHelper.transparentPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_SM, Theme.PAD_SM, 0, Theme.PAD_SM));
        header.add(titles, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        CardPanel tableCard = new CardPanel(new BorderLayout(0, Theme.PAD_SM), Theme.PAD_SM);
        tableCard.add(header, BorderLayout.NORTH);
        tableCard.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            int available = 0;
            for (Quiz q : quizDao.findAll()) {
                if (q.getQuestionCount() > 0) {
                    available++;
                }
            }
            quizzesCard.setValue(String.valueOf(available));

            List<Result> results = resultDao.findByUser(user.getId());
            attemptsCard.setValue(String.valueOf(results.size()));

            double total = 0;
            int passed = 0;
            for (Result r : results) {
                total += r.getPercentage();
                if (r.isPassed()) {
                    passed++;
                }
            }
            averageCard.setValue(results.isEmpty() ? "-" : String.format("%.0f%%", total / results.size()));
            passedCard.setValue(String.valueOf(passed));

            int shown = Math.min(5, results.size());
            summaryLabel.setText(results.isEmpty()
                    ? "You have not taken any quiz yet"
                    : "Your latest " + shown + (shown == 1 ? " attempt" : " attempts"));

            model.setRowCount(0);
            for (int i = 0; i < shown; i++) {
                Result r = results.get(i);
                model.addRow(new Object[] {
                        r.getQuizTitle(), r.getScoreText(),
                        String.format("%.1f%%", r.getPercentage()), r.getStatus(), r.getAttemptedAt()
                });
            }
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }
}