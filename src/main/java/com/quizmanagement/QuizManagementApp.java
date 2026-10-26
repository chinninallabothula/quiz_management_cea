package com.quizmanagement;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Main Swing interface for student quizzes, question administration and records. */
public class QuizManagementApp extends JFrame {
    private static final Color NAVY = new Color(20, 35, 68);
    private static final Color BLUE = new Color(45, 105, 210);
    private static final Color MINT = new Color(47, 179, 150);
    // Change these before distributing the application to students.
    private static final String QUIZ_MAKER_USERNAME = "quizmaker";
    private static final String QUIZ_MAKER_PASSWORD = "Quiz@123";
    private final CardLayout portalCards = new CardLayout();
    private final JPanel portals = new JPanel(portalCards);
    private final QuizPanel quizPanel = new QuizPanel();
    private final QuestionPanel questionPanel = new QuestionPanel();
    private final ResultsPanel resultsPanel = new ResultsPanel();

    public QuizManagementApp() {
        super("Quiz Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        portals.add(welcomeScreen(), "welcome");
        portals.add(portalScreen("Student Portal", "Take your quiz and receive instant results.", quizPanel, "← Back to portal selection", () -> portalCards.show(portals, "welcome")), "student");
        portals.add(makerLoginScreen(), "makerLogin");
        JTabbedPane makerTabs = new JTabbedPane();
        makerTabs.addTab("Question Bank", questionPanel);
        makerTabs.addTab("Quiz Results", resultsPanel);
        makerTabs.addChangeListener(e -> { if (makerTabs.getSelectedComponent() == questionPanel) questionPanel.refresh(); else resultsPanel.refresh(); });
        portals.add(portalScreen("Quiz Maker Portal", "Create questions and review student performance.", makerTabs, "← Back to portal selection", () -> portalCards.show(portals, "welcome")), "maker");
        add(portals); pack(); setLocationRelativeTo(null);
    }

    private static JPanel padded() { JPanel p = new JPanel(new BorderLayout(12, 12)); p.setBorder(new EmptyBorder(18, 22, 18, 22)); return p; }
    private static void error(Component parent, Exception e) { JOptionPane.showMessageDialog(parent, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    private static String centeredHtml(String text) { return centeredHtml(text, 600); }
    /** Constrain HTML labels so their preferred size remains within the panel that owns them. */
    private static String centeredHtml(String text, int width) {
        return "<html><div style='text-align:center; width:" + width + "px;'>"
                + text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div></html>";
    }
    private static void centerTable(JTable table) { DefaultTableCellRenderer renderer = new DefaultTableCellRenderer(); renderer.setHorizontalAlignment(SwingConstants.CENTER); table.setDefaultRenderer(Object.class, renderer); }
    private static JButton primaryButton(String text, Color color) {
        JButton button = new AccentButton(text); button.setBackground(color); button.setForeground(Color.WHITE); button.setFont(new Font("SansSerif", Font.BOLD, 14)); button.setBorder(new EmptyBorder(10, 18, 10, 18)); return button;
    }
    private JPanel welcomeScreen() {
        JPanel screen = new GradientPanel();
        screen.setLayout(new GridBagLayout());
        JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("QUIZFLOW"); brand.setForeground(new Color(185, 215, 255)); brand.setFont(new Font("SansSerif", Font.BOLD, 16)); brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel title = new JLabel("Welcome to your quiz space"); title.setForeground(Color.WHITE); title.setFont(new Font("SansSerif", Font.BOLD, 33)); title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Choose a portal to begin"); subtitle.setForeground(new Color(221, 231, 250)); subtitle.setFont(new Font("SansSerif", Font.PLAIN, 17)); subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel choices = new JPanel(new GridLayout(1, 2, 18, 0)); choices.setOpaque(false);
        choices.setPreferredSize(new Dimension(670, 244)); choices.setMaximumSize(new Dimension(670, 244));
        choices.add(portalCard("STUDENT", "Take a quiz", "Participate & see your results", MINT, () -> portalCards.show(portals, "student")));
        choices.add(portalCard("QUIZ MAKER", "Manage your quizzes", "Build the questions & view all results.", BLUE, () -> portalCards.show(portals, "makerLogin")));
        content.add(brand); content.add(Box.createVerticalStrut(14)); content.add(title); content.add(Box.createVerticalStrut(8)); content.add(subtitle); content.add(Box.createVerticalStrut(38)); content.add(choices);
        screen.add(content); return screen;
    }
    private JPanel portalCard(String label, String title, String description, Color accent, Runnable action) {
        JPanel card = new RoundedPanel(Color.WHITE, 24); card.setBorder(new EmptyBorder(22, 22, 22, 22)); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel type = new JLabel(label); type.setForeground(accent); type.setFont(new Font("SansSerif", Font.BOLD, 12)); type.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel heading = new JLabel(title); heading.setForeground(NAVY); heading.setFont(new Font("SansSerif", Font.BOLD, 22)); heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel details = new JLabel(centeredHtml(description, 260)); details.setForeground(new Color(86, 99, 125)); details.setFont(new Font("SansSerif", Font.PLAIN, 14)); details.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton enter = primaryButton("Enter portal  →", accent); enter.setAlignmentX(Component.CENTER_ALIGNMENT); enter.addActionListener(e -> action.run());
        card.add(type); card.add(Box.createVerticalStrut(13)); card.add(heading); card.add(Box.createVerticalStrut(8)); card.add(details); card.add(Box.createVerticalGlue()); card.add(enter); return card;
    }
    private JPanel portalScreen(String title, String subtitle, Component body, String backText, Runnable backAction) {
        JPanel screen = new JPanel(new BorderLayout()); screen.setBackground(new Color(245, 248, 253));
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(NAVY); header.setBorder(new EmptyBorder(15, 22, 15, 22));
        JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS)); JLabel heading = new JLabel(title); heading.setForeground(Color.WHITE); heading.setFont(new Font("SansSerif", Font.BOLD, 22)); heading.setAlignmentX(Component.CENTER_ALIGNMENT); JLabel sub = new JLabel(subtitle); sub.setForeground(new Color(202, 216, 243)); sub.setAlignmentX(Component.CENTER_ALIGNMENT); copy.add(heading); copy.add(sub); header.add(copy, BorderLayout.CENTER);
        JButton back = new JButton(backText); back.setForeground(Color.WHITE); back.setBackground(NAVY); back.setFocusPainted(false); back.setBorder(new EmptyBorder(8, 10, 8, 10)); back.addActionListener(e -> backAction.run()); header.add(back, BorderLayout.WEST);
        screen.add(header, BorderLayout.NORTH); screen.add(body, BorderLayout.CENTER); return screen;
    }
    private JPanel makerLoginScreen() {
        JPanel screen = new GradientPanel();
        screen.setLayout(new GridBagLayout());
        JPanel card = new RoundedPanel(Color.WHITE, 26); card.setBorder(new EmptyBorder(28, 34, 28, 34)); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setPreferredSize(new Dimension(390, 355));
        JLabel label = new JLabel("QUIZ MAKER ACCESS"); label.setAlignmentX(Component.CENTER_ALIGNMENT); label.setForeground(BLUE); label.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel title = new JLabel("Sign in to continue"); title.setAlignmentX(Component.CENTER_ALIGNMENT); title.setForeground(NAVY); title.setFont(new Font("SansSerif", Font.BOLD, 25));
        JLabel description = new JLabel("Only quiz makers can manage questions and results."); description.setAlignmentX(Component.CENTER_ALIGNMENT); description.setForeground(new Color(86, 99, 125));
        JTextField username = new JTextField(); JPasswordField password = new JPasswordField(); username.setMaximumSize(new Dimension(310, 31)); password.setMaximumSize(new Dimension(310, 31));
        JButton signIn = primaryButton("Sign in to Quiz Maker Portal", BLUE); signIn.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton back = new JButton("← Back"); back.setAlignmentX(Component.CENTER_ALIGNMENT); back.setBorderPainted(false); back.setContentAreaFilled(false); back.setForeground(BLUE);
        Runnable login = () -> { if (QUIZ_MAKER_USERNAME.equals(username.getText().trim()) && QUIZ_MAKER_PASSWORD.equals(new String(password.getPassword()))) { username.setText(""); password.setText(""); questionPanel.refresh(); resultsPanel.refresh(); portalCards.show(portals, "maker"); } else { password.setText(""); JOptionPane.showMessageDialog(this, "Incorrect username or password.", "Access denied", JOptionPane.WARNING_MESSAGE); } };
        signIn.addActionListener(e -> login.run()); password.addActionListener(e -> login.run()); back.addActionListener(e -> { username.setText(""); password.setText(""); portalCards.show(portals, "welcome"); });
        card.add(label); card.add(Box.createVerticalStrut(10)); card.add(title); card.add(Box.createVerticalStrut(7)); card.add(description); card.add(Box.createVerticalStrut(24)); card.add(new JLabel("Username")); card.add(Box.createVerticalStrut(5)); card.add(username); card.add(Box.createVerticalStrut(12)); card.add(new JLabel("Password")); card.add(Box.createVerticalStrut(5)); card.add(password); card.add(Box.createVerticalStrut(20)); card.add(signIn); card.add(Box.createVerticalStrut(10)); card.add(back);
        screen.add(card); return screen;
    }
    private static final class GradientPanel extends JPanel {
        protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setPaint(new GradientPaint(0, 0, new Color(10, 25, 55), getWidth(), getHeight(), new Color(48, 108, 215))); g2.fillRect(0, 0, getWidth(), getHeight()); g2.setColor(new Color(255, 255, 255, 18)); g2.fillOval(-110, -80, 320, 320); g2.fillOval(getWidth() - 210, getHeight() - 180, 360, 360); g2.setColor(new Color(91, 221, 193, 45)); g2.fillOval(getWidth() - 130, 55, 190, 190); g2.dispose(); }
    }
    private static final class RoundedPanel extends JPanel {
        private final Color fill; private final int radius;
        RoundedPanel(Color fill, int radius) { this.fill = fill; this.radius = radius; setOpaque(false); }
        protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(14, 31, 66, 20)); g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 5, radius, radius); g2.setColor(fill); g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, radius, radius); g2.dispose(); super.paintComponent(g); }
    }
    private static final class AccentButton extends JButton {
        AccentButton(String text) { super(text); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); Color color = getBackground(); if (getModel().isRollover()) color = color.brighter(); if (getModel().isPressed()) color = color.darker(); g2.setColor(color); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18); g2.dispose(); super.paintComponent(g); }
    }

    private final class QuizPanel extends JPanel {
        private final CardLayout cards = new CardLayout();
        private final JPanel content = new JPanel(cards);
        private final JTextField studentName = new JTextField(22);
        private final JLabel progress = new JLabel();
        private final JLabel questionText = new JLabel();
        private final ButtonGroup choices = new ButtonGroup();
        private final JRadioButton[] options = {new JRadioButton(), new JRadioButton(), new JRadioButton(), new JRadioButton()};
        private List<Question> questions = List.of();
        private int index;
        private int score;

        QuizPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(245, 248, 253));
            content.add(startScreen(), "start"); content.add(questionScreen(), "quiz"); content.add(new JPanel(), "done");
            add(content); cards.show(content, "start");
        }
        private JPanel startScreen() {
            JPanel p = padded(); p.setBackground(new Color(245, 248, 253)); JPanel center = new RoundedPanel(Color.WHITE, 26); center.setBorder(new EmptyBorder(38, 54, 38, 54)); center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            JLabel badge = new JLabel("STUDENT QUIZ"); badge.setForeground(MINT); badge.setFont(new Font("SansSerif", Font.BOLD, 12)); badge.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel heading = new JLabel("Ready to show what you know?"); heading.setForeground(NAVY); heading.setFont(new Font("SansSerif", Font.BOLD, 25)); heading.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel note = new JLabel("Enter your name and answer each multiple-choice question."); note.setForeground(new Color(86, 99, 125)); note.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel form = new JPanel(new FlowLayout()); JLabel nameLabel = new JLabel("Student name:"); form.add(nameLabel); form.add(studentName);
            form.setOpaque(false); nameLabel.setForeground(NAVY); studentName.setFont(new Font("SansSerif", Font.PLAIN, 15)); studentName.setHorizontalAlignment(JTextField.CENTER);
            JButton begin = primaryButton("Start Quiz  →", MINT); begin.setAlignmentX(Component.CENTER_ALIGNMENT); begin.addActionListener(e -> startQuiz());
            center.add(badge); center.add(Box.createVerticalStrut(12)); center.add(heading); center.add(Box.createVerticalStrut(10)); center.add(note); center.add(Box.createVerticalStrut(26)); center.add(form); center.add(Box.createVerticalStrut(18)); center.add(begin); p.add(center, BorderLayout.CENTER); return p;
        }
        private JPanel questionScreen() {
            JPanel p = padded(); p.setBackground(new Color(245, 248, 253)); progress.setFont(new Font("SansSerif", Font.BOLD, 14)); progress.setForeground(BLUE); p.add(progress, BorderLayout.NORTH);
            questionText.setHorizontalAlignment(SwingConstants.CENTER); questionText.setFont(new Font("SansSerif", Font.BOLD, 18)); questionText.setForeground(NAVY); questionText.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel middle = new RoundedPanel(Color.WHITE, 26); middle.setBorder(new EmptyBorder(28, 34, 28, 34)); middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS)); middle.add(questionText); middle.add(Box.createVerticalStrut(24));
            for (JRadioButton option : options) { choices.add(option); option.setAlignmentX(Component.LEFT_ALIGNMENT); option.setBackground(Color.WHITE); option.setForeground(NAVY); option.setFont(new Font("SansSerif", Font.PLAIN, 15)); middle.add(option); middle.add(Box.createVerticalStrut(12)); }
            p.add(middle, BorderLayout.CENTER); JButton next = primaryButton("Save answer and continue  →", BLUE); next.addActionListener(e -> nextQuestion()); p.add(next, BorderLayout.SOUTH); return p;
        }
        private void startQuiz() {
            if (studentName.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter your name."); return; }
            try { questions = Database.getQuestions(); } catch (SQLException e) { error(this, e); return; }
            if (questions.isEmpty()) { JOptionPane.showMessageDialog(this, "There are no questions yet. Add one in Manage Questions."); return; }
            index = score = 0; cards.show(content, "quiz"); showQuestion();
        }
        private void showQuestion() {
            Question q = questions.get(index); progress.setText("Question " + (index + 1) + " of " + questions.size()); progress.setHorizontalAlignment(SwingConstants.CENTER); questionText.setText(centeredHtml(q.text()));
            String[] letters = {"A", "B", "C", "D"}; choices.clearSelection(); for (int i = 0; i < 4; i++) options[i].setText(letters[i] + ". " + q.option(letters[i]));
        }
        private void nextQuestion() {
            String selected = null; for (int i = 0; i < 4; i++) if (options[i].isSelected()) selected = String.valueOf((char)('A' + i));
            if (selected == null) { JOptionPane.showMessageDialog(this, "Please select an answer."); return; }
            if (selected.equals(questions.get(index).correct())) score++;
            index++; if (index < questions.size()) showQuestion(); else finishQuiz();
        }
        private void finishQuiz() {
            try { Database.saveResult(studentName.getText().trim(), score, questions.size()); } catch (SQLException e) { error(this, e); return; }
            StringBuilder feedback = new StringBuilder("Score: ").append(score).append(" / ").append(questions.size()).append("\n\nAnswer review:\n");
            for (Question q : questions) feedback.append("• ").append(q.text()).append("\n  Correct answer: ").append(q.correct()).append(". ").append(q.option(q.correct())).append("\n");
            JTextArea details = new JTextArea(feedback.toString(), 15, 50); details.setEditable(false); details.setLineWrap(true); details.setWrapStyleWord(true);
            JOptionPane.showMessageDialog(this, new JScrollPane(details), "Quiz Submitted — Immediate Feedback", JOptionPane.INFORMATION_MESSAGE);
            studentName.setText(""); cards.show(content, "start"); resultsPanel.refresh();
        }
    }

    private final class QuestionPanel extends JPanel {
        private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Question", "A", "B", "C", "D", "Correct"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        private final JTable table = new JTable(model);
        QuestionPanel() { setLayout(new BorderLayout(8,8)); setBackground(new Color(245,248,253)); setBorder(new EmptyBorder(12,12,12,12)); table.setRowHeight(28); centerTable(table); table.setSelectionBackground(new Color(211, 226, 255)); table.setSelectionForeground(NAVY); table.getTableHeader().setBackground(NAVY); table.getTableHeader().setForeground(Color.WHITE); table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13)); add(new JScrollPane(table), BorderLayout.CENTER); JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER)); buttons.setOpaque(false); JButton add = primaryButton("+ Add Question", MINT), edit = primaryButton("Edit Selected", BLUE), delete = primaryButton("Delete Selected", new Color(192, 63, 71)), refresh = new JButton("Refresh"); add.addActionListener(e -> editQuestion(null)); edit.addActionListener(e -> selectedQuestion()); delete.addActionListener(e -> deleteSelected()); refresh.addActionListener(e -> refresh()); buttons.add(add); buttons.add(edit); buttons.add(delete); buttons.add(refresh); add(buttons, BorderLayout.SOUTH); }
        void refresh() { try { model.setRowCount(0); for (Question q : Database.getQuestions()) model.addRow(new Object[]{q.id(),q.text(),q.a(),q.b(),q.c(),q.d(),q.correct()}); } catch (SQLException e) { error(this,e); } }
        private void selectedQuestion() { int row = table.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this,"Select a question first."); return; } editQuestion(new Question((int)model.getValueAt(row,0),(String)model.getValueAt(row,1),(String)model.getValueAt(row,2),(String)model.getValueAt(row,3),(String)model.getValueAt(row,4),(String)model.getValueAt(row,5),(String)model.getValueAt(row,6))); }
        private void deleteSelected() { int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a question first.");return;} if(JOptionPane.showConfirmDialog(this,"Delete this question?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) try { Database.deleteQuestion((int)model.getValueAt(row,0)); refresh(); } catch(SQLException e){error(this,e);} }
        private void editQuestion(Question existing) { JTextArea text=new JTextArea(existing==null?"":existing.text(),3,32); JTextField a=new JTextField(existing==null?"":existing.a()), b=new JTextField(existing==null?"":existing.b()), c=new JTextField(existing==null?"":existing.c()), d=new JTextField(existing==null?"":existing.d()); JComboBox<String> correct=new JComboBox<>(new String[]{"A","B","C","D"}); if(existing!=null)correct.setSelectedItem(existing.correct()); JPanel form=new JPanel(new GridLayout(0,1,4,4)); form.add(new JLabel("Question:"));form.add(new JScrollPane(text));form.add(new JLabel("Option A:"));form.add(a);form.add(new JLabel("Option B:"));form.add(b);form.add(new JLabel("Option C:"));form.add(c);form.add(new JLabel("Option D:"));form.add(d);form.add(new JLabel("Correct option:"));form.add(correct); if(JOptionPane.showConfirmDialog(this,form,existing==null?"Add Question":"Edit Question",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return; if(text.getText().trim().isEmpty()||a.getText().trim().isEmpty()||b.getText().trim().isEmpty()||c.getText().trim().isEmpty()||d.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Every field is required.");return;} Question q=new Question(existing==null?0:existing.id(),text.getText().trim(),a.getText().trim(),b.getText().trim(),c.getText().trim(),d.getText().trim(),(String)correct.getSelectedItem()); try {if(existing==null)Database.addQuestion(q);else Database.updateQuestion(q);refresh();}catch(SQLException e){error(this,e);} }
    }

    private final class ResultsPanel extends JPanel {
        private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Student", "Score", "Total", "Percentage", "Completed"}, 0) { public boolean isCellEditable(int r,int c){return false;} };
        private final JLabel summary = new JLabel("No quiz attempts recorded yet.");
        ResultsPanel() { setLayout(new BorderLayout(8,8)); setBackground(new Color(245,248,253)); setBorder(new EmptyBorder(12,12,12,12)); JPanel report = new JPanel(new BorderLayout()); report.setBackground(Color.WHITE); report.setBorder(new EmptyBorder(15,18,15,18)); JLabel title = new JLabel("Performance Report", SwingConstants.CENTER); title.setFont(new Font("SansSerif",Font.BOLD,18)); title.setForeground(NAVY); summary.setForeground(new Color(86,99,125)); summary.setHorizontalAlignment(SwingConstants.CENTER); report.add(title,BorderLayout.NORTH); report.add(summary,BorderLayout.SOUTH); add(report,BorderLayout.NORTH); JTable resultTable = new JTable(model); resultTable.setRowHeight(28); centerTable(resultTable); resultTable.setSelectionBackground(new Color(211,226,255)); resultTable.getTableHeader().setBackground(NAVY); resultTable.getTableHeader().setForeground(Color.WHITE); resultTable.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,13)); add(new JScrollPane(resultTable),BorderLayout.CENTER); JButton refresh=primaryButton("Refresh Report",BLUE);refresh.addActionListener(e->refresh()); JPanel bar=new JPanel(new FlowLayout(FlowLayout.CENTER));bar.setOpaque(false);bar.add(refresh);add(bar,BorderLayout.SOUTH); }
        void refresh(){try{List<Result> results=Database.getResults(); model.setRowCount(0); double totalPercentage=0, highest=0; for(Result r:results){double percentage=r.percentage();totalPercentage+=percentage;highest=Math.max(highest,percentage);model.addRow(new Object[]{r.id(),r.student(),r.score(),r.total(),String.format("%.1f%%",percentage),r.completedAt()});} summary.setText(results.isEmpty()?"No quiz attempts recorded yet.":String.format("%d attempt(s)  •  Average score: %.1f%%  •  Highest score: %.1f%%",results.size(),totalPercentage/results.size(),highest));}catch(SQLException e){error(this,e);}}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { try { Database.initialize(); new QuizManagementApp().setVisible(true); } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Unable to initialize SQLite database.\n" + e.getMessage() + "\n\nClose any other copy of this app or database viewer, then try again.", "Startup error", JOptionPane.ERROR_MESSAGE); } });
    }
}
