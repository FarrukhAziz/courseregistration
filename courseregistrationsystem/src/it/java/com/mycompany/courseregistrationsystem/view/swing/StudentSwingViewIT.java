package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.*;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.awaitility.Awaitility.await;

@SuppressWarnings("unused")
@RunWith(GUITestRunner.class)
public class StudentSwingViewIT extends AssertJSwingJUnitTestCase {

  @SuppressWarnings("resource")
  @ClassRule
  public static PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("ui_student_it")
          .withUsername("user")
          .withPassword("pass");

  private static EntityManagerFactory emf;

  private StudentSwingView view;
  private FrameFixture window;

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
    preloadCourse("CS101", "Intro to CS", 6, 30);
    preloadCourse("MATH101", "Math I", 6, 1);
    view = GuiActionRunner.execute(StudentSwingView::new);
    window = new FrameFixture(robot(), view);
    window.show();
  }

  @Override
  protected void onTearDown() {
    window.cleanUp();
    clearDb();
  }

  private void clearDb() {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    em.createNativeQuery("DELETE FROM enrollments").executeUpdate();
    em.createQuery("DELETE FROM Student").executeUpdate();
    em.createQuery("DELETE FROM Course").executeUpdate();
    tx.commit();
    em.close();
  }

  private void preloadCourse(String code, String title, int cfu, int maxSeats) {
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
    em.close();
  }

  private void clickOkOnAnyDialog() {
    window.dialog().requireVisible();
    window.dialog().button(JButtonMatcher.withText("OK")).click();
  }

  private void clickNoOnDialog() {
    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.pressAndReleaseKeys(java.awt.event.KeyEvent.VK_TAB);
    pane.pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);
    robot().waitForIdle();
  }

  private void uiAddStudent(String matricola, String name, String email, int courseIndexOrZero) {
    window.button("btnRefreshStudent").click();
    window.textBox("txtMatricola").setText(matricola);
    window.textBox("txtFullName").setText(name);
    window.textBox("txtEmail").setText(email);
    window.comboBox("cmbCourse").selectItem(courseIndexOrZero);
    javax.swing.JButton addBtn = window.button("btnAddStudent").target();
    execute(() -> { addBtn.requestFocusInWindow(); addBtn.doClick(); });
  }

  // ---------------- Tests ----------------

  @Test
  public void initial_table_empty() {
    assertThat(window.table("tblStudents").rowCount()).isEqualTo(0);
  }

  @Test
  public void add_valid_student_adds_row() {
    uiAddStudent("1234567", "John Doe", "john@example.com", 1);
    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblStudents").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("1234567");
      assertThat(rows[0][2]).isEqualTo("John Doe");
    });
  }

  @Test
  public void add_duplicate_matricola_no_extra_row() {
    uiAddStudent("1234567", "John Doe", "john@example.com", 1);
    int before = window.table("tblStudents").rowCount();

    window.textBox("txtMatricola").setText("1234567");
    window.textBox("txtFullName").setText("Dup Name");
    window.textBox("txtEmail").setText("dup@example.com");
    window.comboBox("cmbCourse").selectItem(1);
    javax.swing.JButton addBtn = window.button("btnAddStudent").target();
    execute(() -> { addBtn.requestFocusInWindow(); addBtn.doClick(); });

    assertThat(window.table("tblStudents").rowCount()).isEqualTo(before);
  }

  @Test
  public void update_full_name_updates_row() {
    uiAddStudent("1234567", "John Doe", "john@example.com", 1);

    org.assertj.swing.fixture.JTableFixture tbl = window.table("tblStudents");
    org.assertj.swing.data.TableCell firstCell = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(firstCell).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
      tbl.selectRows(0);
      robot().waitForIdle();
    }

    window.textBox("txtFullName").setText("Johnathan Doe");
    javax.swing.JButton updateBtn = window.button("btnUpdateStudent").target();
    execute(() -> { updateBtn.requestFocusInWindow(); updateBtn.doClick(); });
    clickOkOnAnyDialog();

    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblStudents").contents();
      assertThat(rows[0][2]).isEqualTo("Johnathan Doe");
    });
  }

  @Test
  public void update_course_to_MATH101_updates_cell() {
    uiAddStudent("1234567", "John Doe", "john@example.com", 1);

    org.assertj.swing.fixture.JTableFixture tbl = window.table("tblStudents");
    org.assertj.swing.data.TableCell firstCell = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(firstCell).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
      tbl.selectRows(0);
      robot().waitForIdle();
    }

    window.comboBox("cmbCourse").selectItem(2); // MATH101
    javax.swing.JButton updateBtn = window.button("btnUpdateStudent").target();
    execute(() -> { updateBtn.requestFocusInWindow(); updateBtn.doClick(); });
    clickOkOnAnyDialog();

    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblStudents").contents();
      assertThat(rows[0][4]).contains("MATH101");
    });
  }

  @Test
  public void delete_student_removes_row() {
    uiAddStudent("1234567", "John Doe", "john@example.com", 1);

    org.assertj.swing.fixture.JTableFixture tbl = window.table("tblStudents");
    org.assertj.swing.data.TableCell firstCell = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(firstCell).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
      tbl.selectRows(0);
      robot().waitForIdle();
    }

    javax.swing.JButton deleteBtn = window.button("btnDeleteStudent").target();
    execute(() -> { deleteBtn.requestFocusInWindow(); deleteBtn.doClick(); });

    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(0)
    );
  }

  @Test
  public void add_missing_fields_no_row() {
    window.textBox("txtMatricola").setText("");
    window.textBox("txtFullName").setText("");
    window.textBox("txtEmail").setText("");

    javax.swing.JButton addBtn = window.button("btnAddStudent").target();
    execute(() -> { addBtn.requestFocusInWindow(); addBtn.doClick(); });

    assertThat(window.table("tblStudents").rowCount()).isEqualTo(0);
  }
}
