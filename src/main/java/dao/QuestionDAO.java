package dao;

import model.Question;
import util.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionDAO implements CrudDAO<Question> {

    @Override
    public void insert(Question q) throws DatabaseException {
        String sql = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, "
                + "correct_option, marks) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, q);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    q.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save question: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Question q) throws DatabaseException {
        String sql = "UPDATE questions SET quiz_id = ?, question_text = ?, option_a = ?, option_b = ?, "
                + "option_c = ?, option_d = ?, correct_option = ?, marks = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, q);
            ps.setInt(9, q.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update question: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM questions WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete question: " + e.getMessage(), e);
        }
    }

    @Override
    public Question findById(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM questions WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read question: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Question> findAll() throws DatabaseException {
        List<Question> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM questions ORDER BY id")) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read questions: " + e.getMessage(), e);
        }
        return list;
    }

    // Method overloading: findByQuiz(id) and findByQuiz(id, shuffle)
    public List<Question> findByQuiz(int quizId) throws DatabaseException {
        List<Question> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM questions WHERE quiz_id = ? ORDER BY id")) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read questions: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Question> findByQuiz(int quizId, boolean shuffle) throws DatabaseException {
        List<Question> list = findByQuiz(quizId);
        if (shuffle) {
            Collections.shuffle(list);
        }
        return list;
    }

    private void bind(PreparedStatement ps, Question q) throws SQLException {
        ps.setInt(1, q.getQuizId());
        ps.setString(2, q.getText());
        ps.setString(3, q.getOption(1));
        ps.setString(4, q.getOption(2));
        ps.setString(5, q.getOption(3));
        ps.setString(6, q.getOption(4));
        ps.setInt(7, q.getCorrectOption());
        ps.setInt(8, q.getMarks());
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setQuizId(rs.getInt("quiz_id"));
        q.setText(rs.getString("question_text"));
        q.setOption(1, rs.getString("option_a"));
        q.setOption(2, rs.getString("option_b"));
        q.setOption(3, rs.getString("option_c"));
        q.setOption(4, rs.getString("option_d"));
        q.setCorrectOption(rs.getInt("correct_option"));
        q.setMarks(rs.getInt("marks"));
        return q;
    }
}