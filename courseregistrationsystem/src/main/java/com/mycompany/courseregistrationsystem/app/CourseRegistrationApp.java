package com.mycompany.courseregistrationsystem.app;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

public class CourseRegistrationApp extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                CourseRegistrationApp frame = new CourseRegistrationApp();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CourseRegistrationApp() {
        setTitle("Course Registration System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 520, 300);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(contentPane);

        JLabel lblTitle = new JLabel("Course Registration System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton btnStudent = new JButton("Student Screen");
        btnStudent.addActionListener(this::openStudentScreen);

        JButton btnCourse = new JButton("Course Screen");
        btnCourse.addActionListener(this::openCourseScreen);

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
            gl_contentPane.createParallelGroup(Alignment.CENTER)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addGap(10)
                    .addComponent(lblTitle)
                    .addContainerGap(10, Short.MAX_VALUE))
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addGap(40)
                    .addComponent(btnStudent, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                    .addComponent(btnCourse, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                    .addGap(40))
        );
        gl_contentPane.setVerticalGroup(
            gl_contentPane.createParallelGroup(Alignment.CENTER)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addGap(20)
                    .addComponent(lblTitle)
                    .addGap(40)
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnStudent, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnCourse, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(120, Short.MAX_VALUE))
        );
        contentPane.setLayout(gl_contentPane);
    }

    private void openStudentScreen(ActionEvent e) {
        
        new StudentFrame().setVisible(true);
    }

    private void openCourseScreen(ActionEvent e) {
      
        new CourseFrame().setVisible(true);
    }

    private static class StudentFrame extends JFrame {
        private static final long serialVersionUID = 1L;
        StudentFrame() {
            setTitle("Students");
            setBounds(150, 150, 600, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
    }

    private static class CourseFrame extends JFrame {
        private static final long serialVersionUID = 1L;
        CourseFrame() {
            setTitle("Courses");
            setBounds(170, 170, 600, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
    }
}