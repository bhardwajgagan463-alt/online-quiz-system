package model;

public class Question {

    private int id;
    private int quizId;
    private String text;
    private final String[] options = new String[4];    // option A..D
    private int correctOption;                         // 1 = A ... 4 = D
    private int marks = 1;

    public Question() { }

    public Question(int quizId, String text, String a, String b, String c, String d,
                    int correctOption, int marks) {
        this.quizId = quizId;
        this.text = text;
        options[0] = a;
        options[1] = b;
        options[2] = c;
        options[3] = d;
        this.correctOption = correctOption;
        this.marks = marks;
    }

    /** number is 1..4 */
    public String getOption(int number) { return options[number - 1]; }
    public void setOption(int number, String value) { options[number - 1] = value; }

    public boolean isCorrect(int selected) { return selected == correctOption; }
    public String getCorrectText() { return getOption(correctOption); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getCorrectOption() { return correctOption; }
    public void setCorrectOption(int correctOption) { this.correctOption = correctOption; }
    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    @Override
    public String toString() {
        return text;
    }
}