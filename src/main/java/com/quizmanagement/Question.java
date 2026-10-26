package com.quizmanagement;

public record Question(int id, String text, String a, String b, String c, String d, String correct) {
    public String option(String letter) { return switch (letter) { case "A" -> a; case "B" -> b; case "C" -> c; case "D" -> d; default -> ""; }; }
}
