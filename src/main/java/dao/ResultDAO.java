package dao;

import model.Result;
import util.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO implements CrudDAO<Result> {

    // Joins give us the student name and quiz title for tables
    private static final String SELECT_JOINED = """
            SELECT r.*, u.full_name AS student_name, q.title AS quiz_title
            FROM results r
            JOIN users u ON u.id = r.user_id
            JOIN quizzes q ON q.id = r.quiz_id
            """;

    @Override
    public void insert(Result r) throws DatabaseException {
        String sql = "INSERT INTO results (user_id, quiz_id, score, total_marks, percentage, passed, time_taken_sec) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getQuizId());
            ps.setInt(3, r.getScore());
            ps.setInt(4, r.getTotalMarks());
            ps.setDouble(5, r.getPercentage());
            ps.setInt(6, r.isPassed() ? 1 : 0);
            ps.setInt(7, r.getTimeTakenSec());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save result: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM results WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete result: " + e.getMessage(), e);
        }
    }

    @Override
    public Result findById(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_JOINED + " WHERE r.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read result: " + e.getMessage(), e);
        }
    }

    /** All results (admin view), newest first. */
    @Override
    public List<Result> findAll() throws DatabaseException {
        List<Result> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_JOINED + " ORDER BY r.id DESC")) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read results: " + e.getMessage(), e);
        }
        return list;
    }

    /** Past attempts of one student, newest first. */
    public List<Result> findByUser(int userId) throws DatabaseException {
        List<Result> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_JOINED + " WHERE r.user_id = ? ORDER BY r.id DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read results: " + e.getMessage(), e);
        }
        return list;
    }

    private Result mapRow(ResultSet rs) throws SQLException {
        Result r = new Result();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setQuizId(rs.getInt("quiz_id"));
        r.setScore(rs.getInt("score"));
        r.setTotalMarks(rs.getInt("total_marks"));
        r.setPercentage(rs.getDouble("percentage"));
        r.setPassed(rs.getInt("passed") == 1);
        r.setTimeTakenSec(rs.getInt("time_taken_sec"));
        r.setAttemptedAt(rs.getString("attempted_at"));
        r.setStudentName(rs.getString("student_name"));
        r.setQuizTitle(rs.getString("quiz_title"));
        return r;
    }
}