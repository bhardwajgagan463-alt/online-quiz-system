package ui;

import model.Question;
import model.Quiz;
import model.Result;
import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.Supplier;

/** Result screen: score ring, pass/fail status, statistics and a review of every answer. */
public class ResultScreen extends BaseScreen {

    private static final int REVIEW_WRAP = 440;

    // Colors looked up at paint time, so they follow light/dark mode
    private static final Supplier<Color> GOOD_TEXT = () ->
            Theme.isDark() ? Theme.blend(Theme.SUCCESS, Color.WHITE, 0.6f) : Color.decode("#166534");
    private static final Supplier<Color> BAD_TEXT = () ->
            Theme.isDark() ? Theme.blend(Theme.DANGER, Color.WHITE, 0.55f) : Color.decode("#991B1B");
    private static final Supplier<Color> MUTED_TEXT = () -> Theme.TEXT_MUTED;
    private static final Supplier<Color> GOOD_ICON = () -> Theme.SUCCESS;
    private static final Supplier<Color> BAD_ICON = () -> Theme.DANGER;

    private final Quiz quiz;
    private final List<Question> questions;
    private final int[] answers;
    private final Result result;
    private final ScreenNavigator nav;

    private int correctCount = 0;
    private int wrongCount = 0;
    private int skippedCount = 0;

    public ResultScreen(Quiz quiz, List<Question> questions, int[] answers,
                        Result result, ScreenNavigator nav) {
        this.quiz = quiz;
        this.questions = questions;
        this.answers = answers;
        this.result = result;
        this.nav = nav;
        countAnswers();
        buildUI();
    }

    private void countAnswers() {
        for (int i = 0; i < questions.size(); i++) {
            if (answers[i] == 0) {
                skippedCount++;
            } else if (questions.get(i).isCorrect(answers[i])) {
                correctCount++;
            } else {
                wrongCount++;
            }
        }
    }

    // ================= layout =================

    @Override
    protected void buildUI() {
        setBorder(BorderFactory.createEmptyBorder(0, Theme.PAD, Theme.PAD, Theme.PAD));

        JPanel body = UIHelper.transparentPanel(new BorderLayout(Theme.PAD_SM, 0));
        body.add(buildSummaryCard(), BorderLayout.WEST);
        body.add(buildReviewCard(), BorderLayout.CENTER);

        add(buildTopBar(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    private JComponent buildTopBar() {
        String subject = quiz.getSubject() == null || quiz.getSubject().isEmpty()
                ? "" : "  |  " + quiz.getSubject();

        JPanel titles = UIHelper.transparentPanel(new GridLayout(0, 1));
        titles.add(UIHelper.label("Quiz result", Theme.font(Font.BOLD, 24)));
        titles.add(UIHelper.muted(quiz.getTitle() + subject));

        JPanel bar = UIHelper.transparentPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD_SM, Theme.PAD_SM, Theme.PAD_SM));
        bar.add(titles, BorderLayout.WEST);
        bar.add(new ThemeToggleButton(), BorderLayout.EAST);
        return bar;
    }

    private JComponent buildSummaryCard() {
        boolean passed = result.isPassed();

        ScoreRing ring = new ScoreRing(210);
        ring.setValues(result.getScoreText(), String.format("%.1f%%", result.getPercentage()),
                result.getPercentage(), passed);
        ring.setToolTipText("Your score");

        JLabel message = UIHelper.muted(passed
                ? "Congratulations, you passed!"
                : "Keep practising. You needed " + quiz.getPassPercent() + "% to pass.");
        message.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel stats = UIHelper.transparentPanel(new GridLayout(2, 3, 8, 8));
        stats.add(new MiniStat(String.valueOf(correctCount), "Correct", () -> Theme.SUCCESS));
        stats.add(new MiniStat(String.valueOf(wrongCount), "Wrong", () -> Theme.DANGER));
        stats.add(new MiniStat(String.valueOf(skippedCount), "Skipped", () -> Theme.TEXT_MUTED));
        stats.add(new MiniStat(result.getTimeTakenText(), "Time taken", () -> Theme.TEXT));
        stats.add(new MiniStat(quiz.getPassPercent() + "%", "Pass mark", () -> Theme.TEXT));
        stats.add(new MiniStat(String.valueOf(questions.size()), "Questions", () -> Theme.TEXT));

        FormPanel form = new FormPanel();
        form.addCentered(ring, 0);
        form.addCentered(new StatusPill(passed), 14);
        form.addRow(message, 10);
        form.addRow(stats, 18);

        // ---- buttons ----
        RoundedButton backBtn = new RoundedButton("Back to dashboard", Icons.of(Icons.Type.HOME, 18));
        backBtn.setToolTipText("Return to your dashboard");
        backBtn.addActionListener(e -> nav.leaveQuiz());

        RoundedButton retakeBtn = new RoundedButton("Retake quiz", Icons.of(Icons.Type.ARROW_RIGHT, 18),
                RoundedButton.Style.SECONDARY);
        retakeBtn.setToolTipText("Try this quiz again");
        retakeBtn.addActionListener(e -> {
            String question = "Retake \"" + quiz.getTitle() + "\"?\nThe timer starts immediately.";
            if (UIHelper.confirm(this, question)) {
                nav.startQuiz(quiz);
            }
        });

        JPanel buttons = UIHelper.transparentPanel(new GridLayout(0, 1, 0, Theme.PAD_SM));
        buttons.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, 0, 0, 0));
        buttons.add(backBtn);
        buttons.add(retakeBtn);

        JPanel holder = UIHelper.transparentPanel(new BorderLayout());
        holder.add(form, BorderLayout.NORTH);

        SideCard card = new SideCard(380);
        card.add(UIHelper.scroll(holder), BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildReviewCard() {
        JPanel head = UIHelper.transparentPanel(new GridLayout(0, 1));
        head.setBorder(BorderFactory.createEmptyBorder(Theme.PAD_SM, Theme.PAD_SM, 0, Theme.PAD_SM));
        head.add(UIHelper.heading("Answer review"));
        head.add(UIHelper.muted("Green is correct, red is wrong, grey was not answered"));

        FormPanel list = new FormPanel();          // GridBagLayout keeps each item at its own height
        for (int i = 0; i < questions.size(); i++) {
            list.addRow(new ReviewItem(i), i == 0 ? 0 : 12);
        }
        JPanel holder = UIHelper.transparentPanel(new BorderLayout());
        holder.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        holder.add(list, BorderLayout.NORTH);

        CardPanel card = new CardPanel(new BorderLayout(0, Theme.PAD_SM), Theme.PAD_SM);
        card.add(head, BorderLayout.NORTH);
        card.add(UIHelper.scroll(holder), BorderLayout.CENTER);
        return card;
    }

    // ================= helpers =================

    private static String optionText(Question q, int number) {
        return (char) ('A' + number - 1) + ". " + q.getOption(number);
    }

    /** One line with an icon and wrapped colored text. */
    private static JComponent answerLine(String text, Icons.Type icon,
                                         Supplier<Color> iconColor, Supplier<Color> textColor) {
        JLabel iconLabel = new JLabel(Icons.of(icon, 18, iconColor));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

        ColoredText label = new ColoredText(Theme.BODY, REVIEW_WRAP - 28, textColor);
        label.setWrappedText(text);

        JPanel row = UIHelper.transparentPanel(new BorderLayout(8, 0));
        row.add(iconLabel, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    // ================= inner classes =================

    /** One question in the review list. Custom painted rounded card (green / red / grey). */
    private class ReviewItem extends JPanel {

        private final boolean answered;
        private final boolean correct;

        ReviewItem(int index) {
            super(new BorderLayout(0, 8));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

            Question q = questions.get(index);
            int chosen = answers[index];
            this.answered = chosen > 0;
            this.correct = q.isCorrect(chosen);

            // header: "Question 3" ........ "1 / 1 mark"
            JLabel number = UIHelper.label("Question " + (index + 1),
                    Theme.font(Font.BOLD, 13), () -> Theme.TEXT_MUTED);
            String marksText = (correct ? q.getMarks() : 0) + " / " + q.getMarks()
                    + (q.getMarks() == 1 ? " mark" : " marks");
            JLabel marks = UIHelper.label(marksText, Theme.font(Font.BOLD, 13), this::markColor);

            JPanel header = UIHelper.transparentPanel(new BorderLayout());
            header.add(number, BorderLayout.WEST);
            header.add(marks, BorderLayout.EAST);

            // body: question text and answers
            WrappedLabel text = new WrappedLabel(Theme.font(Font.BOLD, 15), REVIEW_WRAP, 0);
            text.setWrappedText(q.getText());

            FormPanel body = new FormPanel();
            body.addRow(text, 0);
            if (!answered) {
                body.addRow(answerLine("Not answered", Icons.Type.CROSS, MUTED_TEXT, MUTED_TEXT), 10);
                body.addRow(answerLine("Correct answer: " + optionText(q, q.getCorrectOption()),
                        Icons.Type.CHECK, GOOD_ICON, GOOD_TEXT), 6);
            } else if (correct) {
                body.addRow(answerLine("Your answer: " + optionText(q, chosen),
                        Icons.Type.CHECK, GOOD_ICON, GOOD_TEXT), 10);
            } else {
                body.addRow(answerLine("Your answer: " + optionText(q, chosen),
                        Icons.Type.CROSS, BAD_ICON, BAD_TEXT), 10);
                body.addRow(answerLine("Correct answer: " + optionText(q, q.getCorrectOption()),
                        Icons.Type.CHECK, GOOD_ICON, GOOD_TEXT), 6);
            }

            add(header, BorderLayout.NORTH);
            add(body, BorderLayout.CENTER);
        }

        private Color markColor() {
            if (!answered) {
                return Theme.TEXT_MUTED;
            }
            return correct ? GOOD_TEXT.get() : BAD_TEXT.get();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill;
            Color border;
            if (!answered) {
                fill = Theme.TABLE_STRIPE;
                border = Theme.BORDER;
            } else if (correct) {
                fill = Theme.SUCCESS_SOFT;
                border = Theme.SUCCESS;
            } else {
                fill = Theme.DANGER_SOFT;
                border = Theme.DANGER;
            }
            int arc = Theme.RADIUS_BTN * 2;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
        }
    }

    /** Wrapped text with its own color that follows light/dark mode. Inheritance: extends WrappedLabel. */
    private static class ColoredText extends WrappedLabel {

        private final Supplier<Color> color;

        ColoredText(Font font, int wrapWidth, Supplier<Color> color) {
            super(font, wrapWidth, 0);
            this.color = color;
            setForeground(color.get());
        }

        @Override
        public void themeChanged() {
            setForeground(color.get());
        }
    }

    /** "PASSED" / "FAILED" pill with an icon. */
    private static class StatusPill extends JComponent {

        private final boolean passed;

        StatusPill(boolean passed) {
            this.passed = passed;
        }

        private String label() {
            return passed ? "PASSED" : "FAILED";
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.BODY_BOLD);
            return new Dimension(fm.stringWidth(label()) + 18 + 8 + 44, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(passed ? Theme.SUCCESS_SOFT : Theme.DANGER_SOFT);
            g2.fillRoundRect(0, 0, w, h, h, h);

            Color fg = (passed ? GOOD_TEXT : BAD_TEXT).get();
            g2.setFont(Theme.BODY_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            int contentWidth = 18 + 8 + fm.stringWidth(label());
            int x = (w - contentWidth) / 2;

            Icons.of(passed ? Icons.Type.CHECK : Icons.Type.CROSS, 18, fg)
                    .paintIcon(this, g2, x, (h - 18) / 2);
            g2.setColor(fg);
            g2.drawString(label(), x + 18 + 8, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Small rounded box: a value on top and a caption below. */
    private static class MiniStat extends JPanel {

        MiniStat(String value, String caption, Supplier<Color> valueColor) {
            super(new GridLayout(2, 1));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));

            JLabel v = UIHelper.label(value, Theme.font(Font.BOLD, 20), valueColor);
            v.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel c = UIHelper.label(caption, Theme.SMALL, () -> Theme.TEXT_MUTED);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            add(v);
            add(c);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BACKGROUND);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose();
        }
    }
}