package com.mycompany.courseregistrationsystem.app;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import com.mycompany.courseregistrationsystem.controller.CourseController;
import com.mycompany.courseregistrationsystem.view.swing.StudentSwingView;
import com.mycompany.courseregistrationsystem.view.swing.CourseSwingView;

public class CourseRegistrationApp extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;


    private CourseController injectedCourseController;

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
        setBounds(100, 100, 560, 320);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(contentPane);

        JLabel lblTitle = new JLabel("Course Registration System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton btnStudentPortal = new JButton("Student's Portal");
        btnStudentPortal.setName("btnStudentPortal");
        btnStudentPortal.addActionListener(this::openStudentPortal);

        JButton btnProfessorPortal = new JButton("Professor's Portal");
        btnProfessorPortal.setName("btnProfessorPortal");
        btnProfessorPortal.addActionListener(this::openProfessorPortal);

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
            gl_contentPane.createParallelGroup(Alignment.CENTER)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addGap(10)
                    .addComponent(lblTitle)
                    .addContainerGap(10, Short.MAX_VALUE))
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addGap(40)
                    .addComponent(btnStudentPortal, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                    .addComponent(btnProfessorPortal, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                    .addGap(40))
        );
        gl_contentPane.setVerticalGroup(
            gl_contentPane.createParallelGroup(Alignment.CENTER)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addGap(20)
                    .addComponent(lblTitle)
                    .addGap(40)
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnStudentPortal, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnProfessorPortal, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(120, Short.MAX_VALUE))
        );
        contentPane.setLayout(gl_contentPane);
    }

    public void setInjectedCourseController(CourseController controller) {
        this.injectedCourseController = controller;
    }

    private void openStudentPortal(ActionEvent e) {
        setEnabled(false);

        StudentSwingView studentView = new StudentSwingView();
        studentView.setTitle("Student's Portal");

        studentView.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent we)  { reenableMain(); }
            @Override public void windowClosing(WindowEvent we) { reenableMain(); }
        });

        studentView.setVisible(true);
    }

    private void openProfessorPortal(ActionEvent e) {
        setEnabled(false);

        CourseSwingView courseView = new CourseSwingView();
        courseView.setTitle("Professor's Portal");

        if (injectedCourseController != null) {
            courseView.setController(injectedCourseController);
        }

        courseView.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent we)  { reenableMain(); }
            @Override public void windowClosing(WindowEvent we) { reenableMain(); }
        });

        courseView.setVisible(true);
    }

    private void reenableMain() {
        setEnabled(true);
        toFront();
        requestFocus();
    }
}
