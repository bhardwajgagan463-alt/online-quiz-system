package dao;

import model.Quiz;
import model.User;
import util.AuthenticationException;
import util.QuizException;

/** Temporary check that the database layer works. Delete it later. */
public class DbSmokeTest {

    public static void main(String[] args) {
        try {
            DBConnection.initializeDatabase();

            UserDAO users = new UserDAO();
            System.out.println("Users : " + users.findAll());

            for (Quiz q : new QuizDAO().findAll()) {
                System.out.println("Quiz  : " + q + " | questions = " + q.getQuestionCount()
                        + " | marks = " + q.getTotalMarks());
            }

            User admin = users.login("admin", "admin123", User.ROLE_ADMIN);
            System.out.println("Login : " + admin.getWelcomeText());

            try {
                users.login("admin", "wrong-password");
            } catch (AuthenticationException e) {
                System.out.println("Blocked: " + e.getMessage());
            }
        } catch (QuizException e) {          // catches DatabaseException / AuthenticationException
            e.printStackTrace();
        }
    }
}