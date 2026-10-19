package ui;

import dao.ResultDAO;
import model.Question;
import model.Quiz;
import model.Result;
import model.User;
import util.DatabaseException;
import util.Theme;
import util.TimerThread;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.List;

/**
 * The timed quiz screen: question card, option buttons, navigator, progress bar and countdown.
 * Inheritance: extends BaseScreen. Interface: TimerThread.TimerListener.
 */
public class QuizScreen extends BaseScreen implements TimerThread.TimerListener {

    private final Quiz quiz;
    private final List<Question> questions;
    private final User user;
    private final ScreenNavigator nav;
    private final ResultDAO resultDao = new ResultDAO();

    private final int[] answers;                 // 0 = unanswered, 1..4 = chosen option
    private int current = 0;
    private boolean finished = false;            // stops double submit
    private final long startMillis = System.currentTimeMillis();
    private TimerThread timer;                   // the countdown thread

    // ---- header ----
    private final TimerBadge timerBadge;
    private final ColorProgressBar progressBar = new ColorProgressBar();
    private final JLabel progressLabel = UIHelper.muted(" ");

    // ---- question card ----
    private final JLabel numberLabel = UIHelper.label(" ", Theme.font(Font.BOLD, 13),
            () -> Theme.isDark() ? Theme.blend(Theme.PRIMARY, Color.WHITE, 0.5f) : Theme.PRIMARY);
    private final JLabel marksLabel = UIHelper.muted(" ");
    private final WrappedLabel questionText = new WrappedLabel(Theme.font(Font.BOLD, 20), 520, 84);
    private final OptionButton[] options = new OptionButton[4];
    private final ButtonGroup group = new ButtonGroup();

    private final RoundedButton prevBtn = new RoundedButton("Previous",
            Icons.of(Icons.Type.ARROW_LEFT, 18), RoundedButton.Style.SECONDARY);
    private final RoundedButton clearBtn = new RoundedButton("Clear answer",
            Icons.of(Icons.Type.CROSS, 18), RoundedButton.Style.GHOST);
    private final RoundedButton nextBtn = new RoundedButton("Next",
            Icons.of(Icons.Type.ARROW_RIGHT, 18));

    // ---- navigator ----
    private QuestionChip[] chips;

    public QuizScreen(Quiz quiz, List<Question> questions, User user, ScreenNavigator nav) {
        this.quiz = quiz;
        this.questions = questions;
        this.user = user;
        this.nav = nav;
        this.answers = new int[questions.size()];
        this.timerBadge = new TimerBadge(quiz.getDurationSeconds());

        buildUI();
        showQuestion(0);
        updateProgress();

        timer = new TimerThread(quiz.getDurationSeconds(), this);   // start the countdown thread
        timer.start();
    }

    // ================= building the screen =================

    @Override
    protected void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = UIHelper.transparentPanel(new BorderLayout(Theme.PAD_SM, 0));
        center.setBorder(BorderFactory.createEmptyBorder(0, Theme.PAD, Theme.PAD, Theme.PAD));
        center.add(buildQuestionCard(), BorderLayout.CENTER);
        center.add(buildNavigatorCard(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        RoundedButton exitBtn = new RoundedButton("Exit", Icons.of(Icons.Type.ARROW_LEFT, 18),
                RoundedButton.Style.GHOST);
        exitBtn.setToolTipText("Leave the quiz without submitting");
        exitBtn.addActionListener(e -> onExit());

        String subject = quiz.getSubject() == null ? "" : quiz.getSubject() + "  |  ";
        JPanel titles = UIHelper.transparentPanel(new GridLayout(0, 1));
        titles.add(UIHelper.heading(quiz.getTitle()));
        titles.add(UIHelper.muted(subject + questions.size() + " questions  |  Pass mark "
                + quiz.getPassPercent() + "%"));

        JPanel left = UIHelper.transparentPanel(new BorderLayout(12, 0));
        left.add(exitBtn, BorderLayout.WEST);
        left.add(titles, BorderLayout.CENTER);

        JPanel right = UIHelper.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.add(timerBadge);
        right.add(new ThemeToggleButton());

        JPanel topRow = UIHelper.transparentPanel(new BorderLayout());
        topRow.add(left, BorderLayout.WEST);
        topRow.add(right, BorderLayout.EAST);

        JPanel progressBlock = UIHelper.transparentPanel(new BorderLayout(0, 6));
        progressBlock.add(progressLabel, BorderLayout.NORTH);
        progressBlock.add(progressBar, BorderLayout.CENTER);

        JPanel header = UIHelper.transparentPanel(new BorderLayout(0, Theme.PAD_SM));
        header.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD_LG, Theme.PAD, Theme.PAD_LG));
        header.add(topRow, BorderLayout.NORTH);
        header.add(progressBlock, BorderLayout.CENTER);
        return header;
    }

    private JComponent buildQuestionCard() {
        JPanel cardHead = UIHelper.transparentPanel(new BorderLayout());
        cardHead.add(numberLabel, BorderLayout.WEST);
        cardHead.add(marksLabel, BorderLayout.EAST);

        FormPanel form = new FormPanel();
        form.addRow(cardHead, 0);
        form.addRow(questionText, 14);

        for (int i = 0; i < 4; i++) {
            final int number = i + 1;
            options[i] = new OptionButton(number);
            group.add(options[i]);
            options[i].addActionListener(e -> selectAnswer(number));      // event handling
            form.addRow(options[i], i == 0 ? 20 : 10);
        }

        prevBtn.setToolTipText("Go to the previous question");
        clearBtn.setToolTipText("Remove your answer for this question");
        nextBtn.setToolTipText("Go to the next question");
        nextBtn.setHorizontalTextPosition(SwingConstants.LEFT);           // arrow after the text
        prevBtn.addActionListener(e -> showQuestion(current - 1));
        nextBtn.addActionListener(e -> showQuestion(current + 1));
        clearBtn.addActionListener(e -> clearAnswer());

        JPanel pair = UIHelper.transparentPanel(new GridLayout(1, 2, 8, 0));
        pair.add(prevBtn);
        pair.add(clearBtn);

        JPanel bar = UIHelper.transparentPanel(new BorderLayout());
        bar.add(pair, BorderLayout.WEST);
        bar.add(nextBtn, BorderLayout.EAST);

        JPanel holder = UIHelper.transparentPanel(new BorderLayout());
        holder.add(form, BorderLayout.NORTH);

        CardPanel card = new CardPanel(new BorderLayout(0, Theme.PAD));
        card.add(UIHelper.scroll(holder), BorderLayout.CENTER);
        card.add(bar, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildNavigatorCard() {
        JPanel grid = UIHelper.transparentPanel(new GridLayout(0, 5, 8, 8));
        chips = new QuestionChip[questions.size()];
        for (int i = 0; i < chips.length; i++) {
            final int index = i;
            chips[i] = new QuestionChip(i + 1, () -> showQuestion(index));
            grid.add(chips[i]);
        }
        JPanel chipHolder = UIHelper.transparentPanel(new BorderLayout());
        chipHolder.add(grid, BorderLayout.NORTH);

        JPanel legend = UIHelper.transparentPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        legend.add(legendItem(QuestionChip.State.CURRENT, "Current"));
        legend.add(legendItem(QuestionChip.State.ANSWERED, "Answered"));
        legend.add(legendItem(QuestionChip.State.UNANSWERED, "Unanswered"));

        JPanel head = UIHelper.transparentPanel(new GridLayout(0, 1, 0, 8));
        head.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.PAD, 0));
        head.add(UIHelper.heading("Question navigator"));
        head.add(legend);

        RoundedButton submitBtn = new RoundedButton("Submit quiz", Icons.of(Icons.Type.CHECK, 18),
                RoundedButton.Style.SUCCESS);
        submitBtn.setToolTipText("Finish the quiz and see your score");
        submitBtn.addActionListener(e -> onSubmitClicked());

        JPanel south = UIHelper.transparentPanel(new BorderLayout());
        south.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, 0, 0, 0));
        south.add(submitBtn, BorderLayout.CENTER);

        SideCard card = new SideCard(340);
        card.add(head, BorderLayout.NORTH);
        card.add(UIHelper.scroll(chipHolder), BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private JComponent legendItem(QuestionChip.State state, String text) {
        JPanel item = UIHelper.transparentPanel(new BorderLayout(6, 0));
        item.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 14));
        item.add(new Dot(state), BorderLayout.WEST);
        item.add(UIHelper.label(text, Theme.SMALL, () -> Theme.TEXT_MUTED), BorderLayout.CENTER);
        return item;
    }

    // ================= showing questions and answers =================

    private void showQuestion(int index) {
        current = index;
        Question q = questions.get(index);

        numberLabel.setText("QUESTION " + (index + 1) + " OF " + questions.size());
        marksLabel.setText(q.getMarks() + (q.getMarks() == 1 ? " mark" : " marks"));
        questionText.setWrappedText(q.getText());
        for (int i = 0; i < 4; i++) {
            options[i].setOptionText(q.getOption(i + 1));
        }

        if (answers[index] > 0) {
            options[answers[index] - 1].setSelected(true);
        } else {
            group.clearSelection();
        }

        prevBtn.setEnabled(index > 0);
        nextBtn.setEnabled(index < questions.size() - 1);
        clearBtn.setEnabled(answers[index] > 0);
        updateChips();
    }

    private void selectAnswer(int number) {
        answers[current] = number;
        clearBtn.setEnabled(true);
        updateChips();
        updateProgress();
    }

    private void clearAnswer() {
        answers[current] = 0;
        group.clearSelection();
        clearBtn.setEnabled(false);
        updateChips();
        updateProgress();
    }

    private void updateChips() {
        for (int i = 0; i < chips.length; i++) {
            if (i == current) {
                chips[i].setState(QuestionChip.State.CURRENT);
            } else if (answers[i] > 0) {
                chips[i].setState(QuestionChip.State.ANSWERED);
            } else {
                chips[i].setState(QuestionChip.State.UNANSWERED);
            }
        }
    }

    private void updateProgress() {
        int answered = answers.length - countUnanswered();
        progressLabel.setText("Answered " + answered + " of " + answers.length);
        progressBar.setProgress(answered, answers.length);
    }

    private int countUnanswered() {
        int count = 0;
        for (int a : answers) {
            if (a == 0) {
                count++;
            }
        }
        return count;
    }

    // ================= timer events (run on the UI thread) =================

    @Override
    public void onTick(int secondsLeft) {
        if (finished) {
            return;
        }
        timerBadge.setSeconds(secondsLeft);          // badge turns red in the last 30 seconds
    }

    @Override
    public void onTimeUp() {
        if (finished) {
            return;
        }
        closeOpenDialogs();                          // e.g. a "Submit?" question still open
        UIHelper.info(this, "Time is up! Your answers will be submitted now.");
        submit();
    }

    // ================= leaving and submitting =================

    private void onExit() {
        if (finished) {
            return;
        }
        if (UIHelper.confirm(this, "Leave this quiz?\nYour answers will not be saved.")) {
            if (finished) {
                return;
            }
            finished = true;
            stopTimer();
            nav.leaveQuiz();
        }
    }

    private void onSubmitClicked() {
        if (finished) {
            return;
        }
        int unanswered = countUnanswered();
        String message = unanswered == 0
                ? "Submit your quiz now?"
                : "You have " + unanswered + " unanswered question" + (unanswered == 1 ? "" : "s")
                  + ".\nSubmit anyway?";
        if (UIHelper.confirm(this, message)) {
            submit();
        }
    }

    /** Grades the answers, saves the result and hands over to the navigator. */
    private void submit() {
        if (finished) {
            return;
        }
        finished = true;
        stopTimer();

        int score = 0;
        int total = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            total += q.getMarks();
            if (q.isCorrect(answers[i])) {
                score += q.getMarks();
            }
        }
        int taken = (int) Math.min(quiz.getDurationSeconds(),
                (System.currentTimeMillis() - startMillis) / 1000);

        Result result = new Result(user.getId(), quiz.getId(), score, total,
                quiz.getPassPercent(), taken);
        try {
            resultDao.insert(result);
        } catch (DatabaseException e) {
            UIHelper.error(this, "Could not save your result:\n" + e.getMessage());
        }
        nav.onQuizFinished(quiz, questions, answers, result);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stopTimer();
        }
    }

    /** Closes message boxes so an auto-submit is not blocked by them. */
    private void closeOpenDialogs() {
        for (Window w : Window.getWindows()) {
            if (w instanceof JDialog && w.isShowing()) {
                w.dispose();
            }
        }
    }

    @Override
    public void removeNotify() {
        stopTimer();                                 // never leave the thread running
        super.removeNotify();
    }

    // ================= legend dot =================

    private static class Dot extends JComponent {
        private final QuestionChip.State state;

        Dot(QuestionChip.State state) {
            this.state = state;
            setPreferredSize(new Dimension(16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int y = (getHeight() - 14) / 2;
            g2.setColor(QuestionChip.fillColor(state));
            g2.fillRoundRect(1, y, 14, 14, 6, 6);
            g2.setColor(QuestionChip.borderColor(state));
            g2.drawRoundRect(1, y, 13, 13, 6, 6);
            g2.dispose();
        }
    }
}