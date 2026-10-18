package dao;

import model.Question;
import model.Quiz;
import model.User;
import util.DatabaseException;

/** Inserts one demo quiz on the very first run so you can test the screens. */
final class SampleData {

    private SampleData() { }

    static void insert() throws DatabaseException {
        User admin = new UserDAO().findByUsername("admin");
        if (admin == null) {
            return;
        }

        Quiz quiz = new Quiz("Java Basics", "Programming", 5, 40, admin.getId());
        new QuizDAO().insert(quiz);          // insert() fills in quiz.getId()

        QuestionDAO qd = new QuestionDAO();
        int id = quiz.getId();
        qd.insert(new Question(id, "Which keyword is used to inherit a class in Java?",
                "extends", "implements", "inherits", "super", 1, 1));
        qd.insert(new Question(id, "Which of these is NOT a primitive data type?",
                "int", "boolean", "String", "char", 3, 1));
        qd.insert(new Question(id, "Which collection does not allow duplicate elements?",
                "List", "Set", "ArrayList", "Vector", 2, 1));
        qd.insert(new Question(id, "What is the default value of an int instance variable?",
                "0", "null", "-1", "undefined", 1, 1));
        qd.insert(new Question(id, "Which keyword prevents a method from being overridden?",
                "static", "final", "abstract", "private", 2, 1));
        qd.insert(new Question(id, "Which interface handles button clicks in Swing?",
                "MouseListener", "WindowAdapter", "ActionListener", "KeyListener", 3, 1));
    }
}