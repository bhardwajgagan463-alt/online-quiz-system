package ui;

import model.User;

/** Interface (syllabus): how screens ask the main window to switch screens. */
public interface ScreenNavigator {

    String APP_NAME = "QuizMaster";      // interface fields are automatically public static final

    void showLogin();

    void showRegister();

    void onLoginSuccess(User user);

    void logout();
}