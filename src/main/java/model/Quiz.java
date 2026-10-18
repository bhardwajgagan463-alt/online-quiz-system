package model;

public class Quiz {

    private int id;
    private String title;
    private String subject;
    private int durationMin;
    private int passPercent = 40;
    private int createdBy;
    private String createdAt;
    private int questionCount;      // filled by QuizDAO queries
    private int totalMarks;         // filled by QuizDAO queries

    public Quiz() { }

    // Method overloading: second constructor
    public Quiz(String title, String subject, int durationMin, int passPercent, int createdBy) {
        this.title = title;
        this.subject = subject;
        this.durationMin = durationMin;
        this.passPercent = passPercent;
        this.createdBy = createdBy;
    }

    public int getDurationSeconds() { return durationMin * 60; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public int getDurationMin() { return durationMin; }
    public void setDurationMin(int durationMin) { this.durationMin = durationMin; }
    public int getPassPercent() { return passPercent; }
    public void setPassPercent(int passPercent) { this.passPercent = passPercent; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    @Override
    public String toString() {
        return title;               // lets a JList / JComboBox show the title directly
    }
}