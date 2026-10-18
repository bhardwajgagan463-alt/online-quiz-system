package model;

public class Result {

    private int id;
    private int userId;
    private int quizId;
    private int score;
    private int totalMarks;
    private double percentage;
    private boolean passed;
    private int timeTakenSec;
    private String attemptedAt;
    private String studentName;     // filled by joined queries
    private String quizTitle;       // filled by joined queries

    public Result() { }

    /** Used when a student finishes a quiz: percentage and pass/fail are calculated here. */
    public Result(int userId, int quizId, int score, int totalMarks, int passPercent, int timeTakenSec) {
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.totalMarks = totalMarks;
        this.timeTakenSec = timeTakenSec;
        this.percentage = totalMarks == 0 ? 0 : score * 100.0 / totalMarks;
        this.passed = percentage >= passPercent;
    }

    public String getStatus() { return passed ? "PASS" : "FAIL"; }
    public String getScoreText() { return score + "/" + totalMarks; }

    public String getTimeTakenText() {
        return String.format("%02d:%02d", timeTakenSec / 60, timeTakenSec % 60);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public int getTimeTakenSec() { return timeTakenSec; }
    public void setTimeTakenSec(int timeTakenSec) { this.timeTakenSec = timeTakenSec; }
    public String getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(String attemptedAt) { this.attemptedAt = attemptedAt; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }
}