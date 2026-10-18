package util;

/** Thrown when login fails. */
public class AuthenticationException extends QuizException {

    public AuthenticationException(String message) {
        super(message);
    }
}