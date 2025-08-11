package com.mycompany.courseregistrationsystem.view.swing;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;


public class StudentSwingView extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;

    private JTextField txtMatricola;
    private JTextField txtFullName;
    private JTextField txtEmail;

    private JComboBox<String> cmbCourse; 
    private JTable tblStudents;
    private DefaultTableModel tableModel;


    private long nextId = 1;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                StudentSwingView frame = new StudentSwingView();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public StudentSwingView() {
        setTitle("Students");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(140, 140, 860, 520);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(contentPane);

        JLabel lblTitle = new JLabel("Student Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel lblMatricola = new JLabel("Matricola:");
        JLabel lblFullName  = new JLabel("Full Name:");
        JLabel lblEmail     = new JLabel("Email:");
        JLabel lblCourse    = new JLabel("Assign Course:");

        txtMatricola = new JTextField();
        txtMatricola.setColumns(10);

        txtFullName = new JTextField();
        txtFullName.setColumns(10);

        txtEmail = new JTextField();
        txtEmail.setColumns(10);

        cmbCourse = new JComboBox<>();

        cmbCourse.addItem("-- Select Course --");
        cmbCourse.addItem("SE101 - Software Engineering");
        cmbCourse.addItem("DB201 - Databases");
        cmbCourse.addItem("AI301 - Intro to AI");

        JScrollPane scrollPane = new JScrollPane();
        tblStudents = new JTable();
        tableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {"ID", "Matricola", "Full Name", "Email", "Course"}
        ) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblStudents.setModel(tableModel);
        tblStudents.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tblStudents.getSelectedRow();
                if (row >= 0) {
                    txtMatricola.setText(val(row, 1));
                    txtFullName.setText(val(row, 2));
                    txtEmail.setText(val(row, 3));
                    cmbCourse.setSelectedItem(val(row, 4));
                }
            }
        });
        scrollPane.setViewportView(tblStudents);

        JButton btnAdd    = new JButton("Add");
        btnAdd.addActionListener(this::onAdd);

        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(this::onUpdate);

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(this::onDelete);

        JButton btnClear  = new JButton("Clear");
        btnClear.addActionListener(e -> clearForm());


        GroupLayout gl = new GroupLayout(contentPane);
        gl.setHorizontalGroup(
            gl.createParallelGroup(Alignment.LEADING)
              .addGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup(Alignment.LEADING)
                  .addComponent(lblTitle)
                  .addGroup(gl.createSequentialGroup()
                    .addGroup(gl.createParallelGroup(Alignment.LEADING, false)
                      .addGroup(gl.createSequentialGroup()
                        .addComponent(lblMatricola)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(txtMatricola, GroupLayout.PREFERRED_SIZE, 240, GroupLayout.PREFERRED_SIZE))
                      .addGroup(gl.createSequentialGroup()
                        .addComponent(lblFullName)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(txtFullName))
                      .addGroup(gl.createSequentialGroup()
                        .addComponent(lblEmail)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(txtEmail))
                      .addGroup(gl.createSequentialGroup()
                        .addComponent(lblCourse)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(cmbCourse, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGap(20)
                    .addGroup(gl.createParallelGroup(Alignment.LEADING, false)
                      .addComponent(btnAdd, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                      .addComponent(btnUpdate, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                      .addComponent(btnDelete, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                      .addComponent(btnClear, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)))
                  .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 820, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gl.setVerticalGroup(
            gl.createParallelGroup(Alignment.LEADING)
              .addGroup(gl.createSequentialGroup()
                .addComponent(lblTitle)
                .addGap(16)
                .addGroup(gl.createParallelGroup(Alignment.BASELINE)
                  .addComponent(lblMatricola)
                  .addComponent(txtMatricola, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addComponent(btnAdd))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(gl.createParallelGroup(Alignment.BASELINE)
                  .addComponent(lblFullName)
                  .addComponent(txtFullName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addComponent(btnUpdate))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(gl.createParallelGroup(Alignment.BASELINE)
                  .addComponent(lblEmail)
                  .addComponent(txtEmail, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addComponent(btnDelete))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(gl.createParallelGroup(Alignment.BASELINE)
                  .addComponent(lblCourse)
                  .addComponent(cmbCourse, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addComponent(btnClear))
                .addGap(18)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
        );
        contentPane.setLayout(gl);
    }


    private void onAdd(ActionEvent e) {
        String m = txtMatricola.getText().trim();
        String n = txtFullName.getText().trim();
        String em = txtEmail.getText().trim();
        Object course = cmbCourse.getSelectedItem();

        if (m.isEmpty() || n.isEmpty() || em.isEmpty()) return;
        tableModel.addRow(new Object[] { nextId++, m, n, em, course });
        clearForm();
    }

    private void onUpdate(ActionEvent e) {
        int row = tblStudents.getSelectedRow();
        if (row < 0) return;
        tableModel.setValueAt(txtMatricola.getText().trim(), row, 1);
        tableModel.setValueAt(txtFullName.getText().trim(), row, 2);
        tableModel.setValueAt(txtEmail.getText().trim(), row, 3);
        tableModel.setValueAt(cmbCourse.getSelectedItem(), row, 4);
        clearForm();
    }

    private void onDelete(ActionEvent e) {
        int row = tblStudents.getSelectedRow();
        if (row < 0) return;
        tableModel.removeRow(row);
        clearForm();
    }

    private void clearForm() {
        txtMatricola.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        cmbCourse.setSelectedIndex(0);
        tblStudents.clearSelection();
    }

    private String val(int row, int col) {
        Object v = tblStudents.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }



    public JTextField getTxtMatricola() { return txtMatricola; }
    public JTextField getTxtFullName()  { return txtFullName; }
    public JTextField getTxtEmail()     { return txtEmail; }
    public JComboBox<String> getCmbCourse() { return cmbCourse; }
    public JTable getTblStudents() { return tblStudents; }
    public DefaultTableModel getTableModel() { return tableModel; }
}
