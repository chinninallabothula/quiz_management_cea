# Quiz Management System

A Java Swing desktop application that meets the project proposal: it lets students take multiple-choice quizzes, evaluates answers automatically, shows immediate feedback, and stores questions and results in SQLite.

## Requirements

- Java 17+
- Maven 3.9+ (downloads the SQLite JDBC driver on the first run)

## Run

```bash
mvn compile exec:java
```

The application creates `quiz_management.db` in the project folder and seeds a few sample questions the first time it runs.

## Features

- Student quiz workflow with name capture, one question at a time, and answer selection
- Automatic scoring and instant per-question feedback on submission
- SQLite-backed question management: add, edit, delete, and list questions
- Persistent results table with student name, score, percentage, and completion time
- Separate Quiz Maker portal protected by local credentials

## Quiz Maker login

The default Quiz Maker credentials are:

```text
Username: quizmaker
Password: Quiz@123
```

Before sharing the program, change `QUIZ_MAKER_USERNAME` and
`QUIZ_MAKER_PASSWORD` near the top of `QuizManagementApp.java`.
