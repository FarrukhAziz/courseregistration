package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import com.mycompany.courseregistrationsystem.model.Student;
import com.mycompany.courseregistrationsystem.repository.CourseRepository;
import com.mycompany.courseregistrationsystem.repository.StudentRepository;

import javax.persistence.EntityManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

public class StudentSwingView extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JTable tableStudents;
    private JTextField txtMatricola;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JComboBox<Course> cmbCourse;

    private final StudentRepository studentRepo = new StudentRepository();
    private final CourseRepository  courseRepo  = new CourseRepository();

    public StudentSwingView() {
        setTitle("Student's Portal");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 900, 560);
        setMinimumSize(new Dimension(820, 500));

        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        // ===== Top form (NORTH) =====
        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gc.gridx = 0; gc.gridy = row; panelForm.add(new JLabel("Matricola:"), gc);
        txtMatricola = new JTextField();
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1; panelForm.add(txtMatricola, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; panelForm.add(new JLabel("Full Name:"), gc);
        txtFullName = new JTextField();
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1; panelForm.add(txtFullName, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; panelForm.add(new JLabel("Email:"), gc);
        txtEmail = new JTextField();
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1; panelForm.add(txtEmail, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; panelForm.add(new JLabel("Course:"), gc);
        cmbCourse = new JComboBox<>();
        cmbCourse.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) {
                    Course crs = (Course) value;
                    setText(crs.getCode() + " - " + crs.getTitle());
                } else if (value == null) setText("-- Select Course --");
                return c;
            }
        });
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1; panelForm.add(cmbCourse, gc);

        contentPane.add(panelForm, BorderLayout.NORTH);

        // ===== Table (CENTER) =====
        tableStudents = new JTable();
        tableStudents.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Matricola", "Full Name", "Email", "Course(s)"}
        ) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        tableStudents.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scroll = new JScrollPane(tableStudents);
        contentPane.add(scroll, BorderLayout.CENTER);

        // ===== Buttons (SOUTH) =====
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnRefresh = new JButton("Refresh");
        panelButtons.add(btnAdd);
        panelButtons.add(btnUpdate);
        panelButtons.add(btnDelete);
        panelButtons.add(btnClear);
        panelButtons.add(btnRefresh);
        contentPane.add(panelButtons, BorderLayout.SOUTH);

        // ===== Listeners =====
        btnAdd.addActionListener(this::addStudent);
        btnUpdate.addActionListener(this::updateStudent);
        btnDelete.addActionListener(this::deleteStudent);
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> { refreshCourses(); loadStudents(); });

        tableStudents.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int rSel = tableStudents.getSelectedRow();
            if (rSel >= 0) {
                txtMatricola.setText(val(rSel, 1));
                txtFullName.setText(val(rSel, 2));
                txtEmail.setText(val(rSel, 3));
                selectCourseByOneOfCodes(val(rSel, 4));
            }
        });

        // Initial data
        refreshCourses();
        loadStudents();
    }

    // ---------- Data loading ----------

    private void refreshCourses() {
        DefaultComboBoxModel<Course> model = new DefaultComboBoxModel<>();
        model.addElement(null);
        for (Course c : courseRepo.findAll()) model.addElement(c);
        cmbCourse.setModel(model);
        cmbCourse.setSelectedIndex(0);
    }

    private void loadStudents() {
        DefaultTableModel model = (DefaultTableModel) tableStudents.getModel();
        model.setRowCount(0);

        // fetch-join to avoid LazyInitializationException
        List<Student> list = studentRepo.findAllWithCourses();

        for (Student s : list) {
            String coursesCol = (s.getCourses() == null || s.getCourses().isEmpty())
                    ? ""
                    : s.getCourses().stream()
                    .map(Course::getCode)
                    .sorted()
                    .collect(Collectors.joining(", "));
            model.addRow(new Object[]{ s.getId(), s.getMatricola(), s.getFullName(), s.getEmail(), coursesCol });
        }
    }

    // ---------- Actions ----------

    private void addStudent(ActionEvent e) {
        String m = txtMatricola.getText().trim();
        String n = txtFullName.getText().trim();
        String em = txtEmail.getText().trim();

        if (m.isEmpty() || n.isEmpty() || em.isEmpty()) {
            warn("Please fill Matricola, Full Name, and Email.");
            return;
        }
        if (studentRepo.findByMatricola(m).isPresent()) {
            warn("Matricola already exists: " + m);
            return;
        }

        Course selected = (Course) cmbCourse.getSelectedItem();
        if (selected != null) {
            // capacity check BEFORE creating the student
            if (!hasCapacity(selected.getId())) {
                warn("Course is full: " + selected.getCode());
                return;
            }
        }

        Student s = new Student();
        s.setMatricola(m);
        s.setFullName(n);
        s.setEmail(em);
        if (selected != null) s.getCourses().add(selected);

        studentRepo.save(s);
        clearForm();
        loadStudents();
        info("Student added.");
    }

    private void updateStudent(ActionEvent e) {
        int row = tableStudents.getSelectedRow();
        if (row < 0) { warn("Select a row to update."); return; }

        Long id = parseLong(val(row, 0), null);
        if (id == null) { warn("Invalid row selected."); return; }

        String m  = txtMatricola.getText().trim();
        String n  = txtFullName.getText().trim();
        String em = txtEmail.getText().trim();
        if (m.isEmpty() || n.isEmpty() || em.isEmpty()) {
            warn("Please fill Matricola, Full Name, and Email.");
            return;
        }

        Course selected = (Course) cmbCourse.getSelectedItem();
        Long targetCourseId = (selected == null ? null : selected.getId());

        // If changing course, enforce capacity.
        if (targetCourseId != null) {
            // Is the student already in this course? If yes, allow; if no, enforce capacity.
            boolean alreadyIn = isStudentInCourse(id, targetCourseId);
            if (!alreadyIn && !hasCapacity(targetCourseId)) {
                warn("Course is full: " + selected.getCode());
                return;
            }
        }

        try {
            // Uses your repository's transactional update method we added earlier
            studentRepo.updateStudent(id, m, n, em, targetCourseId);
            clearForm();
            loadStudents();
            info("Student updated.");
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Update failed:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteStudent(ActionEvent e) {
        int row = tableStudents.getSelectedRow();
        if (row < 0) { warn("Select a row to delete."); return; }
        Long id = parseLong(val(row, 0), null);
        if (id == null) { warn("Invalid row selected."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete selected student?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        studentRepo.deleteById(id);
        clearForm();
        loadStudents();
        info("Student deleted.");
    }

    // ---------- Capacity helpers (query DB fresh) ----------

    /** true if enrolled count < maxSeats */
    private boolean hasCapacity(Long courseId) {
        if (courseId == null) return true;
        int enrolled = enrolledCount(courseId);
        int max = findMaxSeats(courseId);
        return enrolled < max;
    }

    /** current enrolled count for a course (fresh from DB) */
    private int enrolledCount(Long courseId) {
        EntityManager em = JpaUtil.emf().createEntityManager();
        try {
            Long cnt = em.createQuery(
                    "select count(s) from Course c join c.students s where c.id = :cid", Long.class)
                .setParameter("cid", courseId)
                .getSingleResult();
            return cnt == null ? 0 : cnt.intValue();
        } finally {
            em.close();
        }
    }

    /** fetch maxSeats for the course */
    private int findMaxSeats(Long courseId) {
        EntityManager em = JpaUtil.emf().createEntityManager();
        try {
            Integer seats = em.createQuery(
                    "select c.maxSeats from Course c where c.id = :cid", Integer.class)
                .setParameter("cid", courseId)
                .getSingleResult();
            return seats == null ? 0 : seats;
        } finally {
            em.close();
        }
    }

    /** is the given student currently enrolled in that course? */
    private boolean isStudentInCourse(Long studentId, Long courseId) {
        EntityManager em = JpaUtil.emf().createEntityManager();
        try {
            Long cnt = em.createQuery(
                    "select count(s) from Course c join c.students s " +
                    "where c.id = :cid and s.id = :sid", Long.class)
                .setParameter("cid", courseId)
                .setParameter("sid", studentId)
                .getSingleResult();
            return cnt != null && cnt > 0;
        } finally {
            em.close();
        }
    }

    // ---------- UI helpers ----------

    private void clearForm() {
        txtMatricola.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        cmbCourse.setSelectedIndex(0);
        tableStudents.clearSelection();
    }

    private void selectCourseByOneOfCodes(String codesCsv) {
        if (codesCsv == null || codesCsv.trim().isEmpty()) { cmbCourse.setSelectedIndex(0); return; }
        String firstCode = codesCsv.split(",")[0].trim();
        ComboBoxModel<Course> model = cmbCourse.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Course c = model.getElementAt(i);
            if (c != null && firstCode.equalsIgnoreCase(c.getCode())) {
                cmbCourse.setSelectedIndex(i);
                return;
            }
        }
        cmbCourse.setSelectedIndex(0);
    }

    private String val(int row, int col) {
        Object v = tableStudents.getValueAt(row, col);
        return v == null ? "" : v.toString();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentSwingView().setVisible(true));
    }
}
