package util;

/** Base class of all our custom exceptions (syllabus: custom exceptions + inheritance). */
public class QuizException extends Exception {

    public QuizException(String message) {
        super(message);
    }

    public QuizException(String message, Throwable cause) {
        super(message, cause);
    }
}