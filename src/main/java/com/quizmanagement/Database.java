package com.quizmanagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** SQLite persistence for questions and quiz attempts. */
public final class Database {
    private static final String URL = "jdbc:sqlite:quiz_management.db";

    private Database() { }

    public static Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        // SQLite permits only one writer. Waiting avoids a startup failure when
        // a previous quiz action has only just released the database file.
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout = 10000");
        }
        return connection;
    }

    public static void initialize() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS questions (id INTEGER PRIMARY KEY AUTOINCREMENT, question_text TEXT NOT NULL, option_a TEXT NOT NULL, option_b TEXT NOT NULL, option_c TEXT NOT NULL, option_d TEXT NOT NULL, correct_option TEXT NOT NULL CHECK(correct_option IN ('A','B','C','D')))" );
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS results (id INTEGER PRIMARY KEY AUTOINCREMENT, student_name TEXT NOT NULL, score INTEGER NOT NULL, total_questions INTEGER NOT NULL, completed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
        seedQuestionsIfEmpty();
    }

    private static void seedQuestionsIfEmpty() throws SQLException {
        boolean isEmpty;
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM questions")) {
            isEmpty = rs.next() && rs.getInt(1) == 0;
        }
        // The read connection above must be closed before opening a writing
        // connection; otherwise SQLite can report its own database as busy.
        if (isEmpty) {
            addQuestion(new Question(0, "Which keyword is used to inherit a class in Java?", "implements", "extends", "inherit", "super", "B"));
            addQuestion(new Question(0, "Which collection does not allow duplicate elements?", "List", "ArrayList", "Set", "Vector", "C"));
            addQuestion(new Question(0, "What does JDBC stand for?", "Java Database Connectivity", "Java Data Binary Code", "Joint Database Control", "Java Desktop Component", "A"));
        }
    }

    public static List<Question> getQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("SELECT * FROM questions ORDER BY id"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) questions.add(questionFrom(rs));
        }
        return questions;
    }

    public static void addQuestion(Question q) throws SQLException {
        executeQuestion("INSERT INTO questions(question_text,option_a,option_b,option_c,option_d,correct_option) VALUES(?,?,?,?,?,?)", q, false);
    }

    public static void updateQuestion(Question q) throws SQLException {
        executeQuestion("UPDATE questions SET question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_option=? WHERE id=?", q, true);
    }

    private static void executeQuestion(String sql, Question q, boolean hasId) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, q.text()); ps.setString(2, q.a()); ps.setString(3, q.b()); ps.setString(4, q.c()); ps.setString(5, q.d()); ps.setString(6, q.correct());
            if (hasId) ps.setInt(7, q.id());
            ps.executeUpdate();
        }
    }

    public static void deleteQuestion(int id) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM questions WHERE id=?")) { ps.setInt(1, id); ps.executeUpdate(); }
    }

    public static void saveResult(String student, int score, int total) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("INSERT INTO results(student_name,score,total_questions) VALUES(?,?,?)")) {
            ps.setString(1, student); ps.setInt(2, score); ps.setInt(3, total); ps.executeUpdate();
        }
    }

    public static List<Result> getResults() throws SQLException {
        List<Result> results = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("SELECT * FROM results ORDER BY id DESC"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(new Result(rs.getInt("id"), rs.getString("student_name"), rs.getInt("score"), rs.getInt("total_questions"), rs.getString("completed_at")));
        }
        return results;
    }

    private static Question questionFrom(ResultSet rs) throws SQLException { return new Question(rs.getInt("id"), rs.getString("question_text"), rs.getString("option_a"), rs.getString("option_b"), rs.getString("option_c"), rs.getString("option_d"), rs.getString("correct_option")); }
}
