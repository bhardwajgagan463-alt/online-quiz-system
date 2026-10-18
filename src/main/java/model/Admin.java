package model;

/** Inheritance: Admin IS-A User. */
public class Admin extends User {

    public Admin(int id, String fullName, String username, String email, String passwordHash) {
        super(id, fullName, username, email, passwordHash);
    }

    public Admin(String fullName, String username, String email, String passwordHash) {
        super(fullName, username, email, passwordHash);
    }

    @Override
    public String getRole() {
        return ROLE_ADMIN;
    }

    @Override
    public String getWelcomeText() {
        return "Welcome back, " + getFullName() + " (Administrator)";
    }
}