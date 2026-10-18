package dao;

import model.Quiz;
import util.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO implements CrudDAO<Quiz> {

    // Also counts the questions and adds up their marks for each quiz
    private static final String SELECT_WITH_COUNTS = """
            SELECT q.*,
                   (SELECT COUNT(*) FROM questions WHERE quiz_id = q.id) AS question_count,
                   (SELECT COALESCE(SUM(marks), 0) FROM questions WHERE quiz_id = q.id) AS total_marks
            FROM quizzes q
            """;

    @Override
    public void insert(Quiz q) throws DatabaseException {
        String sql = "INSERT INTO quizzes (title, subject, duration_min, pass_percent, created_by) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getTitle());
            ps.setString(2, q.getSubject());
            ps.setInt(3, q.getDurationMin());
            ps.setInt(4, q.getPassPercent());
            ps.setInt(5, q.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    q.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save quiz: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Quiz q) throws DatabaseException {
        String sql = "UPDATE quizzes SET title = ?, subject = ?, duration_min = ?, pass_percent = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, q.getTitle());
            ps.setString(2, q.getSubject());
            ps.setInt(3, q.getDurationMin());
            ps.setInt(4, q.getPassPercent());
            ps.setInt(5, q.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update quiz: " + e.getMessage(), e);
        }
    }

    /** Deletes the quiz; its questions and results go too (ON DELETE CASCADE). */
    @Override
    public void delete(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM quizzes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete quiz: " + e.getMessage(), e);
        }
    }

    @Override
    public Quiz findById(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_WITH_COUNTS + " WHERE q.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read quiz: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Quiz> findAll() throws DatabaseException {
        List<Quiz> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();                                  // JDBC Statement
             ResultSet rs = st.executeQuery(SELECT_WITH_COUNTS + " ORDER BY q.id DESC")) {   // ResultSet
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read quizzes: " + e.getMessage(), e);
        }
        return list;
    }

    private Quiz mapRow(ResultSet rs) throws SQLException {
        Quiz q = new Quiz();
        q.setId(rs.getInt("id"));
        q.setTitle(rs.getString("title"));
        q.setSubject(rs.getString("subject"));
        q.setDurationMin(rs.getInt("duration_min"));
        q.setPassPercent(rs.getInt("pass_percent"));
        q.setCreatedBy(rs.getInt("created_by"));
        q.setCreatedAt(rs.getString("created_at"));
        q.setQuestionCount(rs.getInt("question_count"));
        q.setTotalMarks(rs.getInt("total_marks"));
        return q;
    }
}