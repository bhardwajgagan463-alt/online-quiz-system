package model;

/** Inheritance: Student IS-A User. */
public class Student extends User {

    public Student(int id, String fullName, String username, String email, String passwordHash) {
        super(id, fullName, username, email, passwordHash);
    }

    public Student(String fullName, String username, String email, String passwordHash) {
        super(fullName, username, email, passwordHash);
    }

    @Override
    public String getRole() {
        return ROLE_STUDENT;
    }

    @Override
    public String getWelcomeText() {
        return "Hello, " + getFullName() + "! Ready for a quiz?";
    }
}