package util;

/** Input validation. Every method throws our custom ValidationException (syllabus). */
public final class Validator {

    // static final regular expressions
    private static final String NAME_REGEX = "^[A-Za-z][A-Za-z .'-]{1,49}$";
    private static final String USERNAME_REGEX = "^[A-Za-z][A-Za-z0-9_]{3,19}$";
    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$";

    private Validator() { }

    // ---- Method overloading: required(...) ----
    public static void required(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
    }

    public static void required(String value, String fieldName, int minLength) throws ValidationException {
        required(value, fieldName);
        if (value.trim().length() < minLength) {
            throw new ValidationException(fieldName + " must be at least " + minLength + " characters");
        }
    }

    public static void validateName(String name) throws ValidationException {
        required(name, "Full name");
        if (!name.trim().matches(NAME_REGEX)) {
            throw new ValidationException("Full name must be 2-50 letters (spaces . ' - allowed)");
        }
    }

    public static void validateUsername(String username) throws ValidationException {
        required(username, "Username");
        if (!username.trim().matches(USERNAME_REGEX)) {
            throw new ValidationException("Username: 4-20 letters, digits or _ and must start with a letter");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        required(email, "Email");
        if (!isValidEmail(email)) {
            throw new ValidationException("Enter a valid email address");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.trim().matches(EMAIL_REGEX);
    }

    public static void validatePassword(String password) throws ValidationException {
        required(password, "Password", 6);
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char ch : password.toCharArray()) {
            if (Character.isLetter(ch)) {
                hasLetter = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            throw new ValidationException("Password needs at least one letter and one digit");
        }
    }

    public static void validateConfirm(String password, String confirm) throws ValidationException {
        if (!password.equals(confirm)) {
            throw new ValidationException("Passwords do not match");
        }
    }

    /** Converts text like "15" to a positive int (used later by the admin forms). */
    public static int parsePositiveInt(String text, String fieldName) throws ValidationException {
        required(text, fieldName);
        try {
            int value = Integer.parseInt(text.trim());
            if (value <= 0) {
                throw new ValidationException(fieldName + " must be greater than 0");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a whole number");
        }
    }

    /** "  aSHA   kumar " -> "Asha Kumar" (String + StringBuffer). */
    public static String capitalizeWords(String text) {
        if (text == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (String word : text.trim().split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(word.charAt(0)));
            sb.append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}