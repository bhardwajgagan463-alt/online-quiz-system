package ui;

import dao.ResultDAO;
import model.Result;
import util.DatabaseException;
import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Admin: view all attempts (JTable) and a per-quiz summary (JTabbedPane). */
public class ResultsPage extends BaseScreen implements Refreshable {

    private final ResultDAO resultDao = new ResultDAO();
    private List<Result> results = new ArrayList<>();

    // ---- tab 1: all attempts ----
    private final DefaultTableModel attemptsModel = UIHelper.readOnlyModel(
            "Student", "Quiz", "Score", "Percentage", "Result", "Time", "Date");
    private final JTable attemptsTable = new JTable(attemptsModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(attemptsModel);
    private final StyledTextField searchField =
            new StyledTextField("Search by student or quiz", Icons.muted(Icons.Type.SEARCH, 18));
    private final JLabel countLabel = UIHelper.muted(" ");
    private final RoundedButton deleteBtn = new RoundedButton("Delete", Icons.of(Icons.Type.TRASH, 18),
            RoundedButton.Style.DANGER);

    // ---- tab 2: summary per quiz ----
    private final DefaultTableModel summaryModel = UIHelper.readOnlyModel(
            "Quiz", "Attempts", "Average %", "Pass rate", "Highest %");
    private final JTable summaryTable = new JTable(summaryModel);

    public ResultsPage() {
        buildUI();
    }

    @Override
    protected void buildUI() {
        // ---------- tab 1 ----------
        UIHelper.styleTable(attemptsTable);
        attemptsTable.setRowSorter(sorter);                        // click a header to sort
        sorter.setSortable(2, false);
        sorter.setSortable(3, false);
        attemptsTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        attemptsTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        attemptsTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        attemptsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                deleteBtn.setEnabled(attemptsTable.getSelectedRow() >= 0);
            }
        });

        searchField.setColumns(22);
        searchField.setToolTipText("Type to filter the table");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        deleteBtn.setToolTipText("Delete the selected attempt");
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> deleteSelected());

        JPanel left = UIHelper.transparentPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.add(searchField);
        JPanel right = UIHelper.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.add(countLabel);
        right.add(deleteBtn);

        JPanel toolbar = UIHelper.transparentPanel(new BorderLayout());
        toolbar.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_SM, Theme.PAD_SM, 0, Theme.PAD_SM));
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        CardPanel attemptsCard = new CardPanel(new BorderLayout(0, Theme.PAD_SM), Theme.PAD_SM);
        attemptsCard.add(toolbar, BorderLayout.NORTH);
        attemptsCard.add(UIHelper.scroll(attemptsTable), BorderLayout.CENTER);

        // ---------- tab 2 ----------
        UIHelper.styleTable(summaryTable);
        summaryTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        CardPanel summaryCard = new CardPanel(new BorderLayout(), Theme.PAD_SM);
        summaryCard.add(UIHelper.scroll(summaryTable), BorderLayout.CENTER);

        // ---------- tabs ----------
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("All attempts", Icons.muted(Icons.Type.LIST, 16), attemptsCard);
        tabs.addTab("Quiz summary", Icons.muted(Icons.Type.CHART, 16), summaryCard);
        add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            results = resultDao.findAll();
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
            return;
        }

        attemptsModel.setRowCount(0);
        for (Result r : results) {
            attemptsModel.addRow(new Object[] {
                    r.getStudentName(), r.getQuizTitle(), r.getScoreText(),
                    String.format("%.1f%%", r.getPercentage()), r.getStatus(),
                    r.getTimeTakenText(), r.getAttemptedAt()
            });
        }
        buildSummary();
        updateCount();
    }

    /** Groups the results by quiz and calculates average, pass rate and best score. */
    private void buildSummary() {
        Map<Integer, List<Result>> byQuiz = new LinkedHashMap<>();
        for (Result r : results) {
            byQuiz.computeIfAbsent(r.getQuizId(), k -> new ArrayList<>()).add(r);
        }

        summaryModel.setRowCount(0);
        for (List<Result> list : byQuiz.values()) {
            double total = 0;
            double best = 0;
            int passed = 0;
            for (Result r : list) {
                total += r.getPercentage();
                best = Math.max(best, r.getPercentage());
                if (r.isPassed()) {
                    passed++;
                }
            }
            summaryModel.addRow(new Object[] {
                    list.get(0).getQuizTitle(),
                    list.size(),
                    String.format("%.1f%%", total / list.size()),
                    Math.round(passed * 100.0 / list.size()) + "%",
                    String.format("%.1f%%", best)
            });
        }
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0, 1));
        }
        updateCount();
    }

    private void updateCount() {
        countLabel.setText(attemptsTable.getRowCount() + " attempts");
    }

    private void deleteSelected() {
        int viewRow = attemptsTable.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        // The sorter changes row order, so convert the visible row to the model row
        Result r = results.get(attemptsTable.convertRowIndexToModel(viewRow));
        String question = "Delete the attempt of " + r.getStudentName() + " on \"" + r.getQuizTitle() + "\"?";
        if (!UIHelper.confirm(this, question)) {
            return;
        }
        try {
            resultDao.delete(r.getId());
            refresh();
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }
}