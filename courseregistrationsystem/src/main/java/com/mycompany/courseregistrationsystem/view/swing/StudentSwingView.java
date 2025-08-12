package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.model.Student;
import com.mycompany.courseregistrationsystem.repository.StudentRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

public class StudentSwingView extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTable tableStudents;
    private JTextField txtId;
    private JTextField txtMatricola;
    private JTextField txtFullName;
    private JTextField txtEmail;

    private StudentRepository studentRepo = new StudentRepository();

    public StudentSwingView() {
        setTitle("Student Portal");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 750, 500);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(10, 10));

        // ===== Form Panel =====
        JPanel panelForm = new JPanel(new GridLayout(5, 2, 10, 10));

        panelForm.add(new JLabel("ID:"));
        txtId = new JTextField();
        txtId.setEditable(false);
        panelForm.add(txtId);

        panelForm.add(new JLabel("Matricola:"));
        txtMatricola = new JTextField();
        panelForm.add(txtMatricola);

        panelForm.add(new JLabel("Full Name:"));
        txtFullName = new JTextField();
        panelForm.add(txtFullName);

        panelForm.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        panelForm.add(txtEmail);

        JPanel panelButtons = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        panelButtons.add(btnAdd);
        panelButtons.add(btnUpdate);
        panelButtons.add(btnDelete);
        panelButtons.add(btnClear);

        panelForm.add(new JLabel());
        panelForm.add(panelButtons);

        contentPane.add(panelForm, BorderLayout.NORTH);

        // ===== Table =====
        tableStudents = new JTable();
        tableStudents.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Matricola", "Full Name", "Email"}
        ));
        JScrollPane scrollPane = new JScrollPane(tableStudents);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Load data on startup
        loadStudents();

        // ===== Listeners =====
        btnAdd.addActionListener(this::addStudent);
        btnUpdate.addActionListener(this::updateStudent);
        btnDelete.addActionListener(this::deleteStudent);
        btnClear.addActionListener(e -> clearForm());

        tableStudents.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableStudents.getSelectedRow() != -1) {
                txtId.setText(tableStudents.getValueAt(tableStudents.getSelectedRow(), 0).toString());
                txtMatricola.setText(tableStudents.getValueAt(tableStudents.getSelectedRow(), 1).toString());
                txtFullName.setText(tableStudents.getValueAt(tableStudents.getSelectedRow(), 2).toString());
                txtEmail.setText(tableStudents.getValueAt(tableStudents.getSelectedRow(), 3).toString());
            }
        });
    }

    private void loadStudents() {
        DefaultTableModel model = (DefaultTableModel) tableStudents.getModel();
        model.setRowCount(0);
        List<Student> list = studentRepo.findAll();
        for (Student s : list) {
            model.addRow(new Object[]{
                    s.getId(), s.getMatricola(), s.getFullName(), s.getEmail()
            });
        }
    }

    private void addStudent(ActionEvent e) {
        Student s = new Student();
        s.setMatricola(txtMatricola.getText());
        s.setFullName(txtFullName.getText());
        s.setEmail(txtEmail.getText());
        studentRepo.save(s);
        loadStudents();
        clearForm();
    }

    private void updateStudent(ActionEvent e) {
        if (txtId.getText().isEmpty()) return;
        Optional<Student> opt = studentRepo.findById(Long.parseLong(txtId.getText()));
        if (opt.isPresent()) {
            Student s = opt.get();
            s.setMatricola(txtMatricola.getText());
            s.setFullName(txtFullName.getText());
            s.setEmail(txtEmail.getText());
            studentRepo.save(s);
            loadStudents();
            clearForm();
        }
    }

    private void deleteStudent(ActionEvent e) {
        if (txtId.getText().isEmpty()) return;
        studentRepo.deleteById(Long.parseLong(txtId.getText()));
        loadStudents();
        clearForm();
    }

    private void clearForm() {
        txtId.setText("");
        txtMatricola.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        tableStudents.clearSelection();
    }
}
