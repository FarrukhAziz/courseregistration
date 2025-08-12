package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.controller.CourseController;
import com.mycompany.courseregistrationsystem.model.Course;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;


public class CourseSwingView extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;

    private JTextField txtCode;
    private JTextField txtTitle;
    private JSpinner spnCfu;
    private JSpinner spnMaxSeats;

    private JTable tblCourses;
    private DefaultTableModel tableModel;

    private final CourseController controller = new CourseController();

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                CourseSwingView frame = new CourseSwingView();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CourseSwingView() {
        setTitle("Courses");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(160, 160, 960, 520);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(contentPane);

        JLabel lblTitle = new JLabel("Course Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel lblCode = new JLabel("Code:");
        JLabel lblName = new JLabel("Title:");
        JLabel lblCfu  = new JLabel("CFU:");
        JLabel lblMax  = new JLabel("Max Seats:");

        txtCode = new JTextField();
        txtCode.setColumns(10);

        txtTitle = new JTextField();
        txtTitle.setColumns(10);

        spnCfu = new JSpinner(new SpinnerNumberModel(6, 1, 60, 1));
        spnMaxSeats = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));

        JScrollPane scrollPane = new JScrollPane();
        tblCourses = new JTable();
        tableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] { "ID", "Code", "Title", "CFU", "Max Seats", "Students Enrolled" }
        ) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblCourses.setModel(tableModel);
        tblCourses.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tblCourses.getSelectedRow();
                if (row >= 0) {
                    txtCode.setText(val(row, 1));
                    txtTitle.setText(val(row, 2));
                    spnCfu.setValue(parseInt(val(row, 3), 6));
                    spnMaxSeats.setValue(parseInt(val(row, 4), 50));
                }
            }
        });
        scrollPane.setViewportView(tblCourses);

        JButton btnAdd    = new JButton("Add");
        btnAdd.addActionListener(this::onAdd);

        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(this::onUpdate);

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(this::onDelete);

        JButton btnClear  = new JButton("Clear");
        btnClear.addActionListener(e -> clearForm());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTable());

        GroupLayout gl = new GroupLayout(contentPane);
        gl.setHorizontalGroup(
            gl.createParallelGroup(Alignment.LEADING)
              .addGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup(Alignment.LEADING)
                  .addComponent(lblTitle)
                  .addGroup(gl.createSequentialGroup()
                    .addGroup(gl.createParallelGroup(Alignment.LEADING, false)
                      .addGroup(gl.createSequentialGroup()
                        .addComponent(lblCode)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(txtCode, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE))
                      .addGroup(gl.createSequentialGroup()
                        .addComponent(lblName)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(txtTitle))
                      .addGroup(gl.createSequentialGroup()
                        .addComponent(lblCfu)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(spnCfu, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addComponent(lblMax)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(spnMaxSeats, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)))
                    .addGap(20)
                    .addGroup(gl.createParallelGroup(Alignment.LEADING, false)
                      .addComponent(btnAdd, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                      .addComponent(btnUpdate, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                      .addComponent(btnDelete, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                      .addComponent(btnClear, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                      .addComponent(btnRefresh, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)))
                  .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 920, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gl.setVerticalGroup(
            gl.createParallelGroup(Alignment.LEADING)
              .addGroup(gl.createSequentialGroup()
                .addComponent(lblTitle)
                .addGap(16)
                .addGroup(gl.createParallelGroup(Alignment.BASELINE)
                  .addComponent(lblCode)
                  .addComponent(txtCode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addComponent(btnAdd))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(gl.createParallelGroup(Alignment.BASELINE)
                  .addComponent(lblName)
                  .addComponent(txtTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addComponent(btnUpdate))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(gl.createParallelGroup(Alignment.BASELINE)
                  .addComponent(lblCfu)
                  .addComponent(spnCfu, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMax)
                  .addComponent(spnMaxSeats, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addComponent(btnDelete))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnClear)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnRefresh)
                .addGap(12)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
        );
        contentPane.setLayout(gl);

        // Load from DB on window open
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                refreshTable();
            }
        });
    }


    private void onAdd(ActionEvent e) {
        String code  = txtCode.getText().trim();
        String title = txtTitle.getText().trim();
        int cfu      = (Integer) spnCfu.getValue();
        int max      = (Integer) spnMaxSeats.getValue();

        try {
            controller.add(code, title, cfu, max);
            clearForm();
            refreshTable();
            info("Course added.");
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        } catch (Exception ex) {
            error("Failed to add course: " + ex.getMessage());
        }
    }

    private void onUpdate(ActionEvent e) {
        int row = tblCourses.getSelectedRow();
        if (row < 0) { warn("Select a row to update."); return; }

        Long id   = parseLong(val(row, 0), null);
        String code  = txtCode.getText().trim();
        String title = txtTitle.getText().trim();
        int cfu      = (Integer) spnCfu.getValue();
        int max      = (Integer) spnMaxSeats.getValue();

        if (id == null) { warn("Invalid row selected."); return; }

        try {
            controller.update(id, code, title, cfu, max);
            clearForm();
            refreshTable();
            info("Course updated.");
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        } catch (Exception ex) {
            error("Failed to update course: " + ex.getMessage());
        }
    }

    private void onDelete(ActionEvent e) {
        int row = tblCourses.getSelectedRow();
        if (row < 0) { warn("Select a row to delete."); return; }

        Long id = parseLong(val(row, 0), null);
        if (id == null) { warn("Invalid row selected."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete selected course?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            controller.delete(id);
            clearForm();
            refreshTable();
            info("Course deleted.");
        } catch (Exception ex) {
            error("Failed to delete course: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Course c : controller.loadAll()) {
            int enrolled = 0;
            try {
                enrolled = controller.enrolledCount(c.getId());
            } catch (Exception ignore) { }
            tableModel.addRow(new Object[] {
                c.getId(),
                c.getCode(),
                c.getTitle(),
                c.getCfu(),
                c.getMaxSeats(),
                enrolled
            });
        }
        tblCourses.clearSelection();
    }

    private void clearForm() {
        txtCode.setText("");
        txtTitle.setText("");
        spnCfu.setValue(6);
        spnMaxSeats.setValue(50);
        tblCourses.clearSelection();
    }

    private String val(int row, int col) {
        Object v = tblCourses.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private Long parseLong(String s, Long def) {
        try { return Long.valueOf(s.trim()); } catch (Exception e) { return def; }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public JTextField getTxtCode() { return txtCode; }
    public JTextField getTxtTitle() { return txtTitle; }
    public JSpinner getSpnCfu() { return spnCfu; }
    public JSpinner getSpnMaxSeats() { return spnMaxSeats; }
    public JTable getTblCourses() { return tblCourses; }
    public DefaultTableModel getTableModel() { return tableModel; }
}
