package model;

/** Abstract class (syllabus): a general user. Only Admin and Student objects are created. */
public abstract class User {

    public static final String ROLE_ADMIN = "ADMIN";        // static + final constants
    public static final String ROLE_STUDENT = "STUDENT";

    private int id;
    private String fullName;
    private String username;
    private String email;
    private final String passwordHash;

    protected User(int id, String fullName, String username, String email, String passwordHash) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Method overloading: constructor without id (database gives the id)
    protected User(String fullName, String username, String email, String passwordHash) {
        this(0, fullName, username, email, passwordHash);
    }

    public abstract String getRole();           // abstract methods: subclasses must implement
    public abstract String getWelcomeText();

    public boolean isAdmin() { return ROLE_ADMIN.equals(getRole()); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();       // StringBuffer
        sb.append(fullName).append(" (").append(username).append(", ").append(getRole()).append(")");
        return sb.toString();
    }
}