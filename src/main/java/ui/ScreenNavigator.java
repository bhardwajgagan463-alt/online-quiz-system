package ui;

import model.Question;
import model.Quiz;
import model.Result;
import model.User;

import java.util.List;

/** Interface (syllabus): how screens ask the main window to switch screens. */
public interface ScreenNavigator {

    String APP_NAME = "QuizMaster";      // interface fields are automatically public static final

    void showLogin();

    void showRegister();

    void onLoginSuccess(User user);

    void logout();

    /** Loads the questions and opens the timed quiz screen. */
    void startQuiz(Quiz quiz);

    /** Leaves the quiz without submitting and returns to the dashboard. */
    void leaveQuiz();

    /** Called by QuizScreen after the result has been saved (manual or auto submit). */
    void onQuizFinished(Quiz quiz, List<Question> questions, int[] answers, Result result);
}