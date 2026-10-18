-- Reference schema. Step 3 runs these same statements from DBConnection.
-- Foreign keys are enabled per connection (jdbc:sqlite:quiz.db?foreign_keys=on).

CREATE TABLE IF NOT EXISTS users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name   TEXT    NOT NULL,
    username    TEXT    NOT NULL UNIQUE,
    email       TEXT    NOT NULL UNIQUE,
    password    TEXT    NOT NULL,                 -- SHA-256 hash, never plain text
    role        TEXT    NOT NULL CHECK (role IN ('ADMIN', 'STUDENT')),
    created_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS quizzes (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    title         TEXT    NOT NULL,
    subject       TEXT,
    duration_min  INTEGER NOT NULL CHECK (duration_min > 0),
    pass_percent  INTEGER NOT NULL DEFAULT 40 CHECK (pass_percent BETWEEN 0 AND 100),
    created_by    INTEGER NOT NULL REFERENCES users(id),
    created_at    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS questions (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id         INTEGER NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question_text   TEXT    NOT NULL,
    option_a        TEXT    NOT NULL,
    option_b        TEXT    NOT NULL,
    option_c        TEXT    NOT NULL,
    option_d        TEXT    NOT NULL,
    correct_option  INTEGER NOT NULL CHECK (correct_option BETWEEN 1 AND 4),  -- 1=A ... 4=D
    marks           INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS results (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    quiz_id         INTEGER NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    score           INTEGER NOT NULL,
    total_marks     INTEGER NOT NULL,
    percentage      REAL    NOT NULL,
    passed          INTEGER NOT NULL CHECK (passed IN (0, 1)),
    time_taken_sec  INTEGER NOT NULL,
    attempted_at    TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE INDEX IF NOT EXISTS idx_questions_quiz ON questions(quiz_id);
CREATE INDEX IF NOT EXISTS idx_results_user   ON results(user_id);