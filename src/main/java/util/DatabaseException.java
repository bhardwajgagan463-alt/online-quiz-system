package util;

/** Thrown when a JDBC / SQL operation fails. */
public class DatabaseException extends QuizException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}