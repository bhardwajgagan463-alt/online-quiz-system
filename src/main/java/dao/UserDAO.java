package dao;

import model.Admin;
import model.Student;
import model.User;
import util.AuthenticationException;
import util.DatabaseException;
import util.PasswordUtil;
import util.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC for users. Anything typed by the user goes through PreparedStatement
 * (a safer kind of Statement that prevents SQL injection).
 */
public class UserDAO implements CrudDAO<User> {

    @Override
    public void insert(User u) throws DatabaseException {
        String sql = "INSERT INTO users (full_name, username, email, password, role) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save user: " + e.getMessage(), e);
        }
    }

    /** Used by the Register screen: checks duplicates first, then inserts. */
    public void register(User u) throws ValidationException, DatabaseException {
        if (existsByUsername(u.getUsername())) {
            throw new ValidationException("Username is already taken");
        }
        if (existsByEmail(u.getEmail())) {
            throw new ValidationException("Email is already registered");
        }
        insert(u);
    }

    @Override
    public void update(User u) throws DatabaseException {
        String sql = "UPDATE users SET full_name = ?, username = ?, email = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getEmail());
            ps.setInt(4, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public User findById(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read user: " + e.getMessage(), e);
        }
    }

    /** Returns null when the username does not exist. */
    public User findByUsername(String username) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE username = ? COLLATE NOCASE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY id")) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read users: " + e.getMessage(), e);
        }
        return list;
    }

    // Method overloading: findAll() and findAll(role)
    public List<User> findAll(String role) throws DatabaseException {
        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE role = ? ORDER BY id")) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read users: " + e.getMessage(), e);
        }
        return list;
    }

    // Method overloading: login(user, pass) and login(user, pass, role)
    public User login(String username, String password)
            throws AuthenticationException, DatabaseException {
        User u = findByUsername(username);
        if (u == null || !PasswordUtil.matches(password, u.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password");
        }
        return u;
    }

    public User login(String username, String password, String role)
            throws AuthenticationException, DatabaseException {
        User u = login(username, password);
        if (!u.getRole().equals(role)) {
            throw new AuthenticationException("This is not a " + role.toLowerCase() + " account");
        }
        return u;
    }

    public boolean existsByUsername(String username) throws DatabaseException {
        return exists("username", username);
    }

    public boolean existsByEmail(String email) throws DatabaseException {
        return exists("email", email);
    }

    // 'column' only ever receives our own fixed names above, never user input
    private boolean exists(String column, String value) throws DatabaseException {
        String sql = "SELECT 1 FROM users WHERE " + column + " = ? COLLATE NOCASE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check user: " + e.getMessage(), e);
        }
    }

    // Polymorphism: returns an Admin or a Student object depending on the role column
    private User mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("full_name");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        if (User.ROLE_ADMIN.equals(rs.getString("role"))) {
            return new Admin(id, name, username, email, password);
        }
        return new Student(id, name, username, email, password);
    }
}