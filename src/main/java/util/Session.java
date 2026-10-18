package util;

import model.User;

/** Remembers who is logged in. static = one shared copy for the whole app. */
public final class Session {

    private static User currentUser;

    private Session() { }

    public static void setCurrentUser(User user) { currentUser = user; }

    public static User getCurrentUser() { return currentUser; }

    public static boolean isLoggedIn() { return currentUser != null; }

    public static void clear() { currentUser = null; }
}