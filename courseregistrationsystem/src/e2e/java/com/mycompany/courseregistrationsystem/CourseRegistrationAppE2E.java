package com.mycompany.courseregistrationsystem;

import com.mycompany.courseregistrationsystem.app.CourseRegistrationApp;
import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import com.mycompany.courseregistrationsystem.model.Student;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.awaitility.Awaitility.await;

public class CourseRegistrationAppE2E extends AssertJSwingJUnitTestCase {

  @SuppressWarnings("resource")
  @ClassRule
  public static PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("e2e_course")
          .withUsername("user")
          .withPassword("pass");

  private static EntityManagerFactory emf;

  private FrameFixture mainWin;
  private FrameFixture professorWin;
  private FrameFixture studentWin;

  @BeforeClass
  public static void bootDb() {
    Map<String, String> p = new HashMap<>();
    p.put("hibernate.connection.url", POSTGRES.getJdbcUrl());
    p.put("hibernate.connection.username", POSTGRES.getUsername());
    p.put("hibernate.connection.password", POSTGRES.getPassword());
    p.put("hibernate.connection.driver_class", "org.postgresql.Driver");
    p.put("hibernate.hbm2ddl.auto", "create-drop");
    p.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
    JpaUtil.rebuild(p);
    emf = JpaUtil.emf();
  }

  @AfterClass
  public static void shutdownDb() {
    if (emf != null) emf.close();
  }

  @Override
  protected void onSetUp() {
    clearDb();
    CourseRegistrationApp app = execute(CourseRegistrationApp::new);
    mainWin = new FrameFixture(robot(), app);
    mainWin.show();
    mainWin.requireVisible();
  }

  @Override
  protected void onTearDown() {
    if (studentWin != null) {
      try {
        studentWin.cleanUp();
      } catch (Exception ignore) {
      }
      studentWin = null;
    }
    if (professorWin != null) {
      try {
        professorWin.cleanUp();
      } catch (Exception ignore) {
      }
      professorWin = null;
    }
    if (mainWin != null) {
      try {
        mainWin.cleanUp();
      } catch (Exception ignore) {
      }
      mainWin = null;
    }
    clearDb();
  }

  private void clearDb() {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    try {
      em.createNativeQuery("DELETE FROM enrollments").executeUpdate();
    } catch (Exception ignore) {
    }
    em.createQuery("DELETE FROM Student").executeUpdate();
    em.createQuery("DELETE FROM Course").executeUpdate();
    tx.commit();
    em.close();
  }

  private Long preloadCourse(String code, String title, int cfu, int maxSeats) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    Course c = new Course();
    c.setCode(code);
    c.setTitle(title);
    c.setCfu(cfu);
    c.setMaxSeats(maxSeats);
    em.persist(c);
    tx.commit();
    Long id = c.getId();
    em.close();
    return id;
  }

  private Long preloadStudent(String m, String n, String email) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    Student s = new Student();
    s.setMatricola(m);
    s.setFullName(n);
    s.setEmail(email);
    em.persist(s);
    tx.commit();
    Long id = s.getId();
    em.close();
    return id;
  }

  private void click(FrameFixture win, String btnName) {
    JButtonFixture btn = win.button(btnName).requireVisible().requireEnabled();
    try {
      btn.focus();
      btn.click();
    } catch (Throwable ignored) {
      execute(() -> btn.target().doClick());
    }
    robot().waitForIdle();
  }

  private String okText() {
    String s = UIManager.getString("OptionPane.okButtonText");
    return s != null ? s : "OK";
  }

  private void clickOkOnDialog() {
    String text = okText();
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      try {
        JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
        pane.okButton().click();
      } catch (Throwable ignored) {
        org.assertj.swing.fixture.DialogFixture dlg =
            WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
              @Override
              protected boolean isMatching(JDialog d) {
                return d.isShowing();
              }
            }).using(robot());
        dlg.button(JButtonMatcher.withText(text)).click();
      }
    });
    robot().waitForIdle();
  }
  
  private void clickNoOnDialog() {
    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.pressAndReleaseKeys(java.awt.event.KeyEvent.VK_TAB);
    pane.pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);
    robot().waitForIdle();
  }

  @SuppressWarnings("unused")
private void clickYesOnDialog() {
    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);
    robot().waitForIdle();
  }
  
  private void openProfessorPortal() {
    try {
      mainWin.button(JButtonMatcher.withName("btnProfessorPortal")).requireEnabled().click();
    } catch (org.assertj.swing.exception.ComponentLookupException ignore) {
      mainWin.button(new GenericTypeMatcher<JButton>(JButton.class) {
        @Override
        protected boolean isMatching(JButton b) {
          return "Professor's Portal".equals(b.getText()) && b.isShowing() && b.isEnabled();
        }
      }).click();
    }
    robot().waitForIdle();
    professorWin = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
      @Override
      protected boolean isMatching(JFrame f) {
        return "Professor's Portal".equals(f.getTitle()) && f.isShowing();
      }
    }).using(robot());
    professorWin.requireVisible();
  }

  private void openStudentPortal() {
    try {
      mainWin.button(JButtonMatcher.withName("btnStudentPortal")).requireEnabled().click();
    } catch (org.assertj.swing.exception.ComponentLookupException ignore) {
      mainWin.button(new GenericTypeMatcher<JButton>(JButton.class) {
        @Override
        protected boolean isMatching(JButton b) {
          return "Student's Portal".equals(b.getText()) && b.isShowing() && b.isEnabled();
        }
      }).click();
    }
    robot().waitForIdle();
    studentWin = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
      @Override
      protected boolean isMatching(JFrame f) {
        return "Student's Portal".equals(f.getTitle()) && f.isShowing();
      }
    }).using(robot());
    studentWin.requireVisible();
  }

  @Test
  @GUITest
  public void mainWindow_is_visible() {
    assertThat(mainWin.target().getTitle()).isEqualTo("Course Registration System");
  }

  @Test
  @GUITest
  public void professor_refresh_shows_preloaded_courses() {
    preloadCourse("MATH101", "Math I", 6, 40);
    openProfessorPortal();
    click(professorWin, "btnRefreshCourse");
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = professorWin.table("tblCourses").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("MATH101");
      assertThat(rows[0][2]).isEqualTo("Math I");
    });
  }

  @Test
  @GUITest
  public void professor_add_course_adds_row() {
    openProfessorPortal();
    professorWin.textBox("txtCode").enterText("CS101");
    professorWin.textBox("txtTitle").enterText("Advanced Programming");
    click(professorWin, "btnAddCourse");
    clickOkOnDialog();
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = professorWin.table("tblCourses").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("CS101");
      assertThat(rows[0][2]).isEqualTo("Advanced Programming");
    });
  }

  @Test
  @GUITest
  public void professor_duplicate_code_shows_warning() {
    preloadCourse("CS101", "Intro to CS", 6, 30);
    openProfessorPortal();
    click(professorWin, "btnRefreshCourse");
    int before = professorWin.table("tblCourses").rowCount();
    professorWin.textBox("txtCode").deleteText().enterText("CS101");
    professorWin.textBox("txtTitle").deleteText().enterText("Anything");
    click(professorWin, "btnAddCourse");
    clickOkOnDialog();
    assertThat(professorWin.table("tblCourses").rowCount()).isEqualTo(before);
  }

  @Test
  @GUITest
  public void professor_update_course_updates_row() {
    preloadCourse("BIO101", "Biology I", 6, 30);
    openProfessorPortal();
    click(professorWin, "btnRefreshCourse");
    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(professorWin.table("tblCourses").rowCount()).isEqualTo(1));
    org.assertj.swing.fixture.JTableFixture tbl = professorWin.table("tblCourses");
    org.assertj.swing.data.TableCell first = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(first).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
      tbl.selectRows(0);
      robot().waitForIdle();
    }
    assertThat(tbl.target().isRowSelected(0)).isTrue();
    professorWin.textBox("txtTitle").deleteText().enterText("Biology I (Updated)");
    click(professorWin, "btnUpdateCourse");
    clickOkOnDialog();
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = professorWin.table("tblCourses").contents();
      assertThat(rows[0][2]).isEqualTo("Biology I (Updated)");
    });
  }

//  @Test
//  @GUITest
//  public void professor_delete_course_removes_row() {
//    preloadCourse("HIST101", "History I", 6, 20);
//    openProfessorPortal();
//    click(professorWin, "btnRefreshCourse");
//    await()
//        .atMost(5, TimeUnit.SECONDS)
//        .untilAsserted(() -> assertThat(professorWin.table("tblCourses").rowCount()).isEqualTo(1));
//    org.assertj.swing.fixture.JTableFixture tbl = professorWin.table("tblCourses");
//    org.assertj.swing.data.TableCell first = org.assertj.swing.data.TableCell.row(0).column(0);
//    tbl.cell(first).click();
//    robot().waitForIdle();
//    if (!tbl.target().isRowSelected(0)) {
//      tbl.selectRows(0);
//      robot().waitForIdle();
//    }
//    assertThat(tbl.target().isRowSelected(0)).isTrue();
//    click(professorWin, "btnDeleteCourse");
//    clickYesOnDialog();
//    await()
//        .atMost(5, TimeUnit.SECONDS)
//        .untilAsserted(() -> assertThat(tbl.rowCount()).isEqualTo(0));
//  }

  @Test
  @GUITest
  public void professor_cancel_delete_keeps_row() {
    preloadCourse("CHEM101", "Chem I", 6, 35);
    openProfessorPortal();
    click(professorWin, "btnRefreshCourse");
    professorWin.table("tblCourses").selectRows(0);
    click(professorWin, "btnDeleteCourse");
    clickNoOnDialog();
    assertThat(professorWin.table("tblCourses").rowCount()).isEqualTo(1);
  }

  @Test
  @GUITest
  public void student_add_creates_row() {
    preloadCourse("PHY101", "Physics I", 6, 45);
    openStudentPortal();

    studentWin.textBox("txtMatricola").enterText("1111111");
    studentWin.textBox("txtFullName").enterText("Farrukh Aziz");
    studentWin.textBox("txtEmail").enterText("farrukh@gmail.com");

    org.assertj.swing.fixture.JComboBoxFixture cmb = studentWin.comboBox("cmbCourse");
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(cmb.contents().length).isGreaterThanOrEqualTo(2) 
    );
    execute(() -> cmb.target().setSelectedIndex(1));
    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(cmb.selectedItem()).startsWith("PHY101")
    );

    javax.swing.JButton addBtn = studentWin.button("btnAddStudent").target();
    execute(() -> { addBtn.requestFocusInWindow(); addBtn.doClick(); });

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = studentWin.table("tblStudents").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("1111111");
      assertThat(rows[0][2]).isEqualTo("Farrukh Aziz");
      assertThat(rows[0][4]).contains("PHY101");
    });
  }

  @Test
  @GUITest
  public void student_update_changes_name() {
    preloadCourse("PHY101", "Physics I", 6, 45);
    preloadStudent("111111", "Farrukh", "F@gmail.com");
    openStudentPortal();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(studentWin.table("tblStudents").rowCount()).isEqualTo(1)
    );

    org.assertj.swing.fixture.JTableFixture tbl = studentWin.table("tblStudents");
    org.assertj.swing.data.TableCell firstCell = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(firstCell).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
      tbl.selectRows(0);
      robot().waitForIdle();
    }

    await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(studentWin.textBox("txtFullName").text()).isEqualTo("Farrukh")
    );

    studentWin.textBox("txtFullName").deleteText().enterText("Farrukh A.");

    javax.swing.JButton updateBtn = studentWin.button("btnUpdateStudent").target();
    execute(() -> { updateBtn.requestFocusInWindow(); updateBtn.doClick(); });
    robot().waitForIdle();

    clickOkOnDialog();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = studentWin.table("tblStudents").contents();
      assertThat(rows[0][2]).isEqualTo("Farrukh A.");
    });
  }


  @Test
  @GUITest
  public void student_delete_removes_row() {
    preloadStudent("111111", "Farrukh", "F@gmail.com");
    openStudentPortal();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(studentWin.table("tblStudents").rowCount()).isEqualTo(1)
    );

    org.assertj.swing.fixture.JTableFixture tbl = studentWin.table("tblStudents");
    org.assertj.swing.data.TableCell firstCell = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(firstCell).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
      tbl.selectRows(0);
      robot().waitForIdle();
    }

    await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(studentWin.textBox("txtFullName").text()).isEqualTo("Farrukh")
    );

    javax.swing.JButton deleteBtn = studentWin.button("btnDeleteStudent").target();
    execute(() -> { deleteBtn.requestFocusInWindow(); deleteBtn.doClick(); });
    robot().waitForIdle();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(studentWin.table("tblStudents").rowCount()).isEqualTo(0)
    );
  }


}
