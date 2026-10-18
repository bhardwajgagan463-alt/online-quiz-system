package ui;

import dao.QuestionDAO;
import dao.QuizDAO;
import model.Question;
import model.Quiz;
import util.DatabaseException;
import util.Theme;
import util.ValidationException;
import util.Validator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin: add / edit / delete questions.
 * JList (pick a quiz) + JTabbedPane (question table | question form) + JTable.
 */
public class QuestionsPage extends BaseScreen implements Refreshable {

    private final QuizDAO quizDao = new QuizDAO();
    private final QuestionDAO questionDao = new QuestionDAO();

    // ---- quiz list (JList) ----
    private final DefaultListModel<Quiz> listModel = new DefaultListModel<>();
    private final JList<Quiz> quizList = new JList<>(listModel);
    private boolean updatingList = false;

    // ---- tabs (JTabbedPane) ----
    private final JTabbedPane tabs = new JTabbedPane();

    // ---- tab 1: question table ----
    private final JLabel listTitle = UIHelper.heading("Questions");
    private final JLabel listSubtitle = UIHelper.muted("Create a quiz first");
    private final DefaultTableModel tableModel =
            UIHelper.readOnlyModel("#", "Question", "Correct answer", "Marks");
    private final JTable table = new JTable(tableModel);
    private List<Question> questions = new ArrayList<>();

    private final RoundedButton newBtn = new RoundedButton("New", Icons.of(Icons.Type.PLUS, 18));
    private final RoundedButton editBtn = new RoundedButton("Edit", Icons.of(Icons.Type.EDIT, 18),
            RoundedButton.Style.SECONDARY);
    private final RoundedButton deleteBtn = new RoundedButton("Delete", Icons.of(Icons.Type.TRASH, 18),
            RoundedButton.Style.DANGER);

    // ---- tab 2: question form ----
    private Question editing;                              // null = new question
    private final JLabel formTitle = UIHelper.heading("New question");
    private final JTextArea questionArea = new JTextArea(4, 20);
    private final StyledTextField[] optionFields = new StyledTextField[4];
    private final JRadioButton[] correctButtons = new JRadioButton[4];
    private final StyledTextField marksField = new StyledTextField("Marks, e.g. 1");
    private boolean messageOk = false;
    private final JLabel messageLabel =
            UIHelper.label(" ", Theme.SMALL, () -> messageOk ? Theme.SUCCESS : Theme.DANGER);

    private final RoundedButton saveBtn = new RoundedButton("Save", Icons.of(Icons.Type.SAVE, 18));
    private final RoundedButton saveNextBtn = new RoundedButton("Save & add next",
            Icons.of(Icons.Type.PLUS, 18), RoundedButton.Style.SECONDARY);
    private final RoundedButton cancelBtn = new RoundedButton("Cancel", Icons.of(Icons.Type.CROSS, 18),
            RoundedButton.Style.GHOST);

    public QuestionsPage() {
        buildUI();
    }

    @Override
    protected void buildUI() {
        // ================= left: JList of quizzes =================
        quizList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        quizList.setCellRenderer(new QuizCellRenderer());
        quizList.setOpaque(false);
        quizList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !updatingList) {
                onQuizSelected();
            }
        });

        JPanel listHead = UIHelper.transparentPanel(new GridLayout(0, 1));
        listHead.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.PAD_SM, 0));
        listHead.add(UIHelper.heading("Quizzes"));
        listHead.add(UIHelper.muted("Select one to manage"));

        SideCard listCard = new SideCard(250);
        listCard.add(listHead, BorderLayout.NORTH);
        listCard.add(UIHelper.scroll(quizList), BorderLayout.CENTER);

        // ================= tab 1: questions table =================
        UIHelper.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(360);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtons();
            }
        });
        // Event handling: MouseListener (double-click a row to edit)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelected();
                }
            }
        });

        newBtn.setToolTipText("Add a question to this quiz");
        editBtn.setToolTipText("Edit the selected question (or double-click a row)");
        deleteBtn.setToolTipText("Delete the selected question");
        newBtn.addActionListener(e -> newQuestion());
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());

        JPanel titles = UIHelper.transparentPanel(new GridLayout(0, 1));
        titles.add(listTitle);
        titles.add(listSubtitle);

        JPanel buttons = UIHelper.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(newBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);

        JPanel toolbar = UIHelper.transparentPanel(new BorderLayout());
        toolbar.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_SM, Theme.PAD_SM, 0, Theme.PAD_SM));
        toolbar.add(titles, BorderLayout.WEST);
        toolbar.add(buttons, BorderLayout.EAST);

        CardPanel tableCard = new CardPanel(new BorderLayout(0, Theme.PAD_SM), Theme.PAD_SM);
        tableCard.add(toolbar, BorderLayout.NORTH);
        tableCard.add(UIHelper.scroll(table), BorderLayout.CENTER);

        // ================= tab 2: form =================
        questionArea.setFont(Theme.BODY);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setMargin(new Insets(8, 12, 8, 12));
        JScrollPane areaScroll = new JScrollPane(questionArea);
        areaScroll.setPreferredSize(new Dimension(0, 104));

        FormPanel form = new FormPanel();
        form.addRow(formTitle, 0);
        form.addRow(UIHelper.caption("Question"), 14);
        form.addRow(areaScroll, 4);
        form.addRow(UIHelper.caption("Answer options (select the correct one)"), 14);

        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            String letter = String.valueOf((char) ('A' + i));
            optionFields[i] = new StyledTextField("Option " + letter);
            correctButtons[i] = new JRadioButton(letter);
            correctButtons[i].setFont(Theme.BODY_BOLD);
            correctButtons[i].setOpaque(false);
            correctButtons[i].setToolTipText("Mark option " + letter + " as the correct answer");
            group.add(correctButtons[i]);

            JPanel row = UIHelper.transparentPanel(new BorderLayout(8, 0));
            row.add(correctButtons[i], BorderLayout.WEST);
            row.add(optionFields[i], BorderLayout.CENTER);
            form.addRow(row, 6);
        }
        correctButtons[0].setSelected(true);

        form.addRow(UIHelper.caption("Marks"), 14);
        form.addRow(marksField, 4);
        form.addRow(messageLabel, 8);

        saveBtn.setToolTipText("Save and go back to the list");
        saveNextBtn.setToolTipText("Save and start the next question");
        saveBtn.addActionListener(e -> saveQuestion(false));
        saveNextBtn.addActionListener(e -> saveQuestion(true));
        cancelBtn.addActionListener(e -> cancelForm());

        JPanel saveRow = UIHelper.transparentPanel(new GridLayout(1, 2, Theme.PAD_SM, 0));
        saveRow.add(saveBtn);
        saveRow.add(saveNextBtn);
        form.addRow(saveRow, 8);
        form.addRow(cancelBtn, 4);

        CardPanel formCard = new CardPanel(new BorderLayout());
        formCard.add(form, BorderLayout.NORTH);
        JPanel formHolder = UIHelper.transparentPanel(new BorderLayout());
        formHolder.add(formCard, BorderLayout.NORTH);

        // ================= tabs =================
        tabs.addTab("Questions", Icons.muted(Icons.Type.LIST, 16), tableCard);
        tabs.addTab("Add / Edit", Icons.muted(Icons.Type.EDIT, 16), UIHelper.scroll(formHolder));

        JPanel centerWrap = UIHelper.transparentPanel(new BorderLayout());
        centerWrap.setBorder(BorderFactory.createEmptyBorder(0, Theme.PAD_SM, 0, 0));
        centerWrap.add(tabs, BorderLayout.CENTER);

        add(listCard, BorderLayout.WEST);
        add(centerWrap, BorderLayout.CENTER);

        clearForm();
        updateButtons();
    }

    // ---------------- loading ----------------

    @Override
    public void refresh() {
        Quiz selected = quizList.getSelectedValue();
        int keepId = selected == null ? -1 : selected.getId();
        try {
            List<Quiz> quizzes = quizDao.findAll();
            updatingList = true;
            try {
                listModel.clear();
                for (Quiz q : quizzes) {
                    listModel.addElement(q);
                }
            } finally {
                updatingList = false;
            }

            int index = 0;
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.get(i).getId() == keepId) {
                    index = i;
                }
            }
            if (listModel.isEmpty()) {
                onQuizSelected();                         // clears the table
            } else {
                quizList.setSelectedIndex(index);         // listener calls onQuizSelected()
            }
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }

    private void onQuizSelected() {
        clearForm();
        tabs.setSelectedIndex(0);
        loadQuestions(quizList.getSelectedValue());
    }

    private void loadQuestions(Quiz quiz) {
        tableModel.setRowCount(0);
        questions = new ArrayList<>();
        if (quiz == null) {
            listTitle.setText("Questions");
            listSubtitle.setText("Create a quiz first (Quizzes page)");
        } else {
            try {
                questions = questionDao.findByQuiz(quiz.getId());
            } catch (DatabaseException e) {
                UIHelper.error(this, e.getMessage());
            }
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String correct = (char) ('A' + q.getCorrectOption() - 1) + ". " + q.getCorrectText();
                tableModel.addRow(new Object[] {i + 1, q.getText(), correct, q.getMarks()});
            }
            quiz.setQuestionCount(questions.size());
            quizList.repaint();
            listTitle.setText(quiz.getTitle());
            listSubtitle.setText(questions.size() + " questions");
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean hasQuiz = quizList.getSelectedValue() != null;
        boolean hasRow = table.getSelectedRow() >= 0;
        newBtn.setEnabled(hasQuiz);
        editBtn.setEnabled(hasRow);
        deleteBtn.setEnabled(hasRow);
    }

    // ---------------- actions ----------------

    private void newQuestion() {
        clearForm();
        tabs.setSelectedIndex(1);
        questionArea.requestFocusInWindow();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= questions.size()) {
            UIHelper.info(this, "Select a question first.");
            return;
        }
        fillForm(questions.get(row));
        tabs.setSelectedIndex(1);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= questions.size()) {
            return;
        }
        if (!UIHelper.confirm(this, "Delete this question?")) {
            return;
        }
        try {
            questionDao.delete(questions.get(row).getId());
            loadQuestions(quizList.getSelectedValue());
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }

    private void saveQuestion(boolean addAnother) {
        Quiz quiz = quizList.getSelectedValue();
        if (quiz == null) {
            showMessage("Select a quiz first", false);
            return;
        }
        try {
            String text = questionArea.getText().trim();
            Validator.required(text, "Question", 5);

            String[] opts = new String[4];
            for (int i = 0; i < 4; i++) {
                opts[i] = optionFields[i].getText().trim();
                Validator.required(opts[i], "Option " + (char) ('A' + i));
            }
            int marks = Validator.parsePositiveInt(marksField.getText(), "Marks");
            if (marks > 100) {
                throw new ValidationException("Marks cannot exceed 100");
            }
            int correct = 1;
            for (int i = 0; i < 4; i++) {
                if (correctButtons[i].isSelected()) {
                    correct = i + 1;
                }
            }

            if (editing == null) {
                questionDao.insert(new Question(quiz.getId(), text,
                        opts[0], opts[1], opts[2], opts[3], correct, marks));
            } else {
                editing.setText(text);
                for (int i = 0; i < 4; i++) {
                    editing.setOption(i + 1, opts[i]);
                }
                editing.setCorrectOption(correct);
                editing.setMarks(marks);
                questionDao.update(editing);
            }

            loadQuestions(quiz);
            clearForm();
            if (addAnother) {
                showMessage("Saved. Add the next question.", true);
                questionArea.requestFocusInWindow();
            } else {
                tabs.setSelectedIndex(0);
            }
        } catch (ValidationException e) {
            showMessage(e.getMessage(), false);
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
    }

    private void cancelForm() {
        clearForm();
        tabs.setSelectedIndex(0);
    }

    // ---------------- form helpers ----------------

    private void fillForm(Question q) {
        editing = q;
        formTitle.setText("Edit question");
        questionArea.setText(q.getText());
        for (int i = 0; i < 4; i++) {
            optionFields[i].setText(q.getOption(i + 1));
        }
        correctButtons[q.getCorrectOption() - 1].setSelected(true);
        marksField.setText(String.valueOf(q.getMarks()));
        saveNextBtn.setEnabled(false);
        showMessage(" ", true);
    }

    private void clearForm() {
        editing = null;
        formTitle.setText("New question");
        questionArea.setText("");
        for (int i = 0; i < 4; i++) {
            optionFields[i].setText("");
        }
        correctButtons[0].setSelected(true);
        marksField.setText("1");
        saveNextBtn.setEnabled(true);
        showMessage(" ", true);
    }

    private void showMessage(String text, boolean ok) {
        messageOk = ok;
        messageLabel.setText(text);
    }

    // ---------------- list cell renderer (interface: ListCellRenderer) ----------------

    private static class QuizCellRenderer extends JPanel implements ListCellRenderer<Quiz> {

        private final JLabel title = UIHelper.label(" ", Theme.BODY_BOLD);
        private final JLabel info = UIHelper.muted(" ");

        QuizCellRenderer() {
            super(new GridLayout(2, 1));
            setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            add(title);
            add(info);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Quiz> list, Quiz quiz,
                int index, boolean isSelected, boolean cellHasFocus) {
            title.setText(quiz.getTitle());
            info.setText(quiz.getQuestionCount() + " questions, " + quiz.getDurationMin() + " min");
            setBackground(isSelected ? Theme.PRIMARY_SOFT : Theme.SURFACE);
            return this;
        }
    }
}