package ui;

import dao.QuizDAO;
import model.Quiz;
import util.DatabaseException;
import util.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

/** Student: all quizzes as cards with a Start button. */
public class AvailableQuizzesPage extends BaseScreen implements Refreshable {

    private final Consumer<Quiz> onStart;
    private final QuizDAO quizDao = new QuizDAO();
    private final JPanel grid = UIHelper.transparentPanel(new GridLayout(0, 2, Theme.PAD_SM, Theme.PAD_SM));

    public AvailableQuizzesPage(Consumer<Quiz> onStart) {
        this.onStart = onStart;
        buildUI();
    }

    @Override
    protected void buildUI() {
        JPanel holder = UIHelper.transparentPanel(new BorderLayout());
        holder.add(grid, BorderLayout.NORTH);
        add(UIHelper.scroll(holder), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        grid.removeAll();
        try {
            List<Quiz> quizzes = quizDao.findAll();
            if (quizzes.isEmpty()) {
                CardPanel empty = new CardPanel(new BorderLayout());
                empty.add(UIHelper.muted("No quizzes yet. Ask your administrator to create one."),
                        BorderLayout.CENTER);
                grid.add(empty);
            }
            for (Quiz q : quizzes) {
                grid.add(new QuizCard(q));
            }
        } catch (DatabaseException e) {
            UIHelper.error(this, e.getMessage());
        }
        grid.revalidate();
        grid.repaint();
    }

    private JLabel fact(Icons.Type icon, String text) {
        JLabel label = UIHelper.label(text, Theme.SMALL, () -> Theme.TEXT_MUTED);
        label.setIcon(Icons.muted(icon, 16));
        label.setIconTextGap(6);
        return label;
    }

    /** One quiz card. Inheritance: extends CardPanel. */
    private class QuizCard extends CardPanel {

        QuizCard(Quiz q) {
            super(new BorderLayout(0, Theme.PAD), Theme.PAD_SM);

            JPanel head = UIHelper.transparentPanel(new GridLayout(0, 1, 0, 2));
            head.add(UIHelper.heading(q.getTitle()));
            head.add(UIHelper.muted(q.getSubject() == null ? "General" : q.getSubject()));

            JPanel facts = UIHelper.transparentPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            facts.add(fact(Icons.Type.LIST, q.getQuestionCount() + " questions   "));
            facts.add(fact(Icons.Type.CLOCK, q.getDurationMin() + " min   "));
            facts.add(fact(Icons.Type.CHECK, "Pass " + q.getPassPercent() + "%"));

            RoundedButton start = new RoundedButton("Start quiz", Icons.of(Icons.Type.ARROW_RIGHT, 18));
            start.setToolTipText("Start this quiz");
            if (q.getQuestionCount() == 0) {
                start.setText("No questions yet");
                start.setEnabled(false);
            }
            start.addActionListener(e -> {
                String message = "Start \"" + q.getTitle() + "\"?\nYou have " + q.getDurationMin()
                        + " minutes. The timer starts immediately.";
                if (UIHelper.confirm(AvailableQuizzesPage.this, message)) {
                    onStart.accept(q);
                }
            });

            add(head, BorderLayout.NORTH);
            add(facts, BorderLayout.CENTER);
            add(start, BorderLayout.SOUTH);
        }
    }
}