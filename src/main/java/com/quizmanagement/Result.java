package com.quizmanagement;

public record Result(int id, String student, int score, int total, String completedAt) {
    public double percentage() { return total == 0 ? 0 : score * 100.0 / total; }
}
