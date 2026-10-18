package dao;

import model.Admin;
import util.DatabaseException;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** JDBC connection + automatic table creation on first run. */
public final class DBConnection {

    // static final: one shared constant. foreign_keys=on makes SQLite enforce REFERENCES.
    private static final String URL = "jdbc:sqlite:quiz.db?foreign_keys=on";

    private static final String[] CREATE_STATEMENTS = {
        """
        CREATE TABLE IF NOT EXISTS users (
            id          INTEGER PRIMARY KEY AUTOINCREMENT,
            full_name   TEXT NOT NULL,
            username    TEXT NOT NULL UNIQUE,
            email       TEXT NOT NULL UNIQUE,
            password    TEXT NOT NULL,
            role        TEXT NOT NULL CHECK (role IN ('ADMIN', 'STUDENT')),
            created_at  TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS quizzes (
            id            INTEGER PRIMARY KEY AUTOINCREMENT,
            title         TEXT NOT NULL,
            subject       TEXT,
            duration_min  INTEGER NOT NULL CHECK (duration_min > 0),
            pass_percent  INTEGER NOT NULL DEFAULT 40 CHECK (pass_percent BETWEEN 0 AND 100),
            created_by    INTEGER NOT NULL REFERENCES users(id),
            created_at    TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS questions (
            id              INTEGER PRIMARY KEY AUTOINCREMENT,
            quiz_id         INTEGER NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
            question_text   TEXT NOT NULL,
            option_a        TEXT NOT NULL,
            option_b        TEXT NOT NULL,
            option_c        TEXT NOT NULL,
            option_d        TEXT NOT NULL,
            correct_option  INTEGER NOT NULL CHECK (correct_option BETWEEN 1 AND 4),
            marks           INTEGER NOT NULL DEFAULT 1
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS results (
            id              INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id         INTEGER NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
            quiz_id         INTEGER NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
            score           INTEGER NOT NULL,
            total_marks     INTEGER NOT NULL,
            percentage      REAL NOT NULL,
            passed          INTEGER NOT NULL CHECK (passed IN (0, 1)),
            time_taken_sec  INTEGER NOT NULL,
            attempted_at    TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
        )
        """,
        "CREATE INDEX IF NOT EXISTS idx_questions_quiz ON questions(quiz_id)",
        "CREATE INDEX IF NOT EXISTS idx_results_user ON results(user_id)"
    };

    private DBConnection() { }

    /** Opens a new connection. DAOs close it with try-with-resources. */
    public static Connection getConnection() throws DatabaseException {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new DatabaseException("Cannot open database: " + e.getMessage(), e);
        }
    }

    /** Call once at startup. Creates quiz.db and the tables if they do not exist. */
    public static void initializeDatabase() throws DatabaseException {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {   // JDBC Statement
            for (String sql : CREATE_STATEMENTS) {
                st.execute(sql);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not create tables: " + e.getMessage(), e);
        }

        // First run only: default admin (admin / admin123) + one sample quiz
        UserDAO users = new UserDAO();
        if (users.findByUsername("admin") == null) {
            users.insert(new Admin("Administrator", "admin", "admin@quiz.com",
                    PasswordUtil.hash("admin123")));
            SampleData.insert();
        }
    }
}