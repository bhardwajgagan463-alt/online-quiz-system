package ui;

import dao.ResultDAO;
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
import java.awt.GridLayout;
import java.util.List;

/** Student: all past attempts in a table. */
public class MyAttemptsPage extends BaseScreen implements Refreshable {

    private final User user;
    private final ResultDAO resultDao = new ResultDAO();

    private final DefaultTableModel model = UIHelper.readOnlyModel(
            "Quiz", "Score", "Percentage", "Result", "Time taken", "Date");
    private final JTable table = new JTable(model);
    private final JLabel summaryLabel = UIHelper.muted(" ");

    public MyAttemptsPage(User user) {
        this.user = user;
        buildUI();
    }

    @Override
    protected void buildUI() {
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());

        JPanel header = UIHelper.transparentPanel(new GridLayout(0, 1));
        header.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_SM, Theme.PAD_SM, 0, Theme.PAD_SM));
        header.add(UIHelper.heading("Your past attempts"));
        header.add(summaryLabel);

        CardPanel card = new CardPanel(new BorderLayout(0, Theme.PAD_SM), Theme.PAD_SM);
        card.add(header, BorderLayout.NORTH);
        card.add(UIHelper.scroll(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            List<Result> results = resultDao.findByUser(user.getId());

            model.setRowCount(0);
            double total = 0;
            double best = 0;
            for (Result r : results) {
                total += r.getPercentage();
                best = Math.max(best, r.getPercentage());
                model.addRow(new Object[] {
                        r.getQuizTitle(), r.getScoreText(),
                        String.format("%.1f%%", r.getPercentage()), r.getStatus(),
                        r.getTimeTakenText(), r.getAttemptedAt()
                });
            }
            summaryLabel.setText(results.isEmpty()
                    ? "No attempts yet"
                    : results.size() + " attempts  |  Average " + String.format("%.1f%%", total / results.size())
                      + "  |  Best " + String.format("%.1f%%", best));
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }
}