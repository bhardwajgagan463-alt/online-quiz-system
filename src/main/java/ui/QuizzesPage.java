package ui;

import dao.QuizDAO;
import model.Quiz;
import util.DatabaseException;
import util.Session;
import util.Theme;
import util.ValidationException;
import util.Validator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** Admin: create, edit and delete quizzes. */
public class QuizzesPage extends BaseScreen implements Refreshable {

    private final QuizDAO quizDao = new QuizDAO();
    private List<Quiz> quizzes = new ArrayList<>();
    private Quiz editing;                                  // null = creating a new quiz

    private final DefaultTableModel model =
            UIHelper.readOnlyModel("Title", "Subject", "Duration", "Pass %", "Questions", "Marks");
    private final JTable table = new JTable(model);

    private final JLabel formTitle = UIHelper.heading("New quiz");
    private boolean messageOk = false;
    private final JLabel messageLabel =
            UIHelper.label(" ", Theme.SMALL, () -> messageOk ? Theme.SUCCESS : Theme.DANGER);

    private final StyledTextField titleField = new StyledTextField("e.g. Java Basics");
    private final StyledTextField subjectField = new StyledTextField("e.g. Programming");
    private final StyledTextField durationField = new StyledTextField("Minutes, e.g. 10");
    private final StyledTextField passField = new StyledTextField("Percent, e.g. 40");

    private final RoundedButton addBtn = new RoundedButton("Add quiz", Icons.of(Icons.Type.PLUS, 18));
    private final RoundedButton updateBtn = new RoundedButton("Update", Icons.of(Icons.Type.SAVE, 18),
            RoundedButton.Style.SECONDARY);
    private final RoundedButton deleteBtn = new RoundedButton("Delete", Icons.of(Icons.Type.TRASH, 18),
            RoundedButton.Style.DANGER);
    private final RoundedButton clearBtn = new RoundedButton("Clear form", Icons.of(Icons.Type.CROSS, 18),
            RoundedButton.Style.GHOST);

    public QuizzesPage() {
        buildUI();
    }

    @Override
    protected void buildUI() {
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);

        // Event handling: selecting a row fills the form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        addBtn.setToolTipText("Create a new quiz");
        updateBtn.setToolTipText("Save changes to the selected quiz");
        deleteBtn.setToolTipText("Delete the selected quiz");
        clearBtn.setToolTipText("Clear the form and selection");
        addBtn.addActionListener(e -> saveQuiz(false));
        updateBtn.addActionListener(e -> saveQuiz(true));
        deleteBtn.addActionListener(e -> deleteQuiz());
        clearBtn.addActionListener(e -> clearForm());

        titleField.setToolTipText("Name shown to students");
        durationField.setToolTipText("Time limit in minutes (1-300)");
        passField.setToolTipText("Minimum percentage needed to pass (1-100)");

        JPanel pair = UIHelper.transparentPanel(new GridLayout(1, 2, Theme.PAD_SM, 0));
        pair.add(updateBtn);
        pair.add(deleteBtn);

        FormPanel form = new FormPanel();
        form.addRow(formTitle, 0);
        form.addRow(UIHelper.caption("Quiz title"), 16);
        form.addRow(titleField, 4);
        form.addRow(UIHelper.caption("Subject"), 12);
        form.addRow(subjectField, 4);
        form.addRow(UIHelper.caption("Duration (minutes)"), 12);
        form.addRow(durationField, 4);
        form.addRow(UIHelper.caption("Pass percentage"), 12);
        form.addRow(passField, 4);
        form.addRow(messageLabel, 8);
        form.addRow(addBtn, 8);
        form.addRow(pair, 8);
        form.addRow(clearBtn, 4);

        SideCard formCard = new SideCard(340);
        formCard.add(form, BorderLayout.NORTH);

        CardPanel tableCard = new CardPanel(new BorderLayout(), Theme.PAD_SM);
        tableCard.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
        add(formCard, BorderLayout.EAST);
        setEditing(null);
    }

    @Override
    public void refresh() {
        try {
            quizzes = quizDao.findAll();
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
            return;
        }
        model.setRowCount(0);
        for (Quiz q : quizzes) {
            model.addRow(new Object[] {
                    q.getTitle(), q.getSubject(), q.getDurationMin() + " min",
                    q.getPassPercent() + "%", q.getQuestionCount(), q.getTotalMarks()
            });
        }
    }

    // ---------------- actions ----------------

    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= quizzes.size()) {
            setEditing(null);
            return;
        }
        Quiz q = quizzes.get(row);
        titleField.setText(q.getTitle());
        subjectField.setText(q.getSubject());
        durationField.setText(String.valueOf(q.getDurationMin()));
        passField.setText(String.valueOf(q.getPassPercent()));
        setEditing(q);
        showMessage(" ", true);
    }

    private void saveQuiz(boolean isUpdate) {
        boolean updating = isUpdate && editing != null;
        try {
            String title = titleField.getText().trim();
            String subject = subjectField.getText().trim();
            Validator.required(title, "Quiz title", 3);
            Validator.required(subject, "Subject");
            int duration = Validator.parsePositiveInt(durationField.getText(), "Duration");
            int pass = Validator.parsePositiveInt(passField.getText(), "Pass percent");
            if (duration > 300) {
                throw new ValidationException("Duration cannot exceed 300 minutes");
            }
            if (pass > 100) {
                throw new ValidationException("Pass percent must be between 1 and 100");
            }

            if (updating) {
                editing.setTitle(title);
                editing.setSubject(subject);
                editing.setDurationMin(duration);
                editing.setPassPercent(pass);
                quizDao.update(editing);
            } else {
                quizDao.insert(new Quiz(title, subject, duration, pass,
                        Session.getCurrentUser().getId()));
            }

            clearForm();
            refresh();
            showMessage(updating ? "Quiz updated" : "Quiz created. Add questions next.", true);
        } catch (ValidationException e) {
            showMessage(e.getMessage(), false);
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }

    private void deleteQuiz() {
        if (editing == null) {
            return;
        }
        String question = "Delete \"" + editing.getTitle() + "\"?\n"
                + "Its questions and results will be deleted too.";
        if (!UIHelper.confirm(this, question)) {
            return;
        }
        try {
            quizDao.delete(editing.getId());
            clearForm();
            refresh();
            showMessage("Quiz deleted", true);
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }

    private void clearForm() {
        table.clearSelection();
        titleField.setText("");
        subjectField.setText("");
        durationField.setText("");
        passField.setText("");
        setEditing(null);
        showMessage(" ", true);
    }

    private void setEditing(Quiz q) {
        editing = q;
        boolean edit = q != null;
        formTitle.setText(edit ? "Edit quiz" : "New quiz");
        addBtn.setEnabled(!edit);
        updateBtn.setEnabled(edit);
        deleteBtn.setEnabled(edit);
    }

    private void showMessage(String text, boolean ok) {
        messageOk = ok;
        messageLabel.setText(text);
    }
}