package util;

/** Thrown when user input is invalid (empty field, bad email, duplicate username...). */
public class ValidationException extends QuizException {

    public ValidationException(String message) {
        super(message);
    }
}