package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.*;
import org.junit.runner.RunWith;

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
public class StudentSwingViewTest extends AssertJSwingJUnitTestCase {

  private static EntityManagerFactory emf;

  private StudentSwingView view;
  private FrameFixture window;

  @SuppressWarnings("unused")
private Long cs101Id;
  @SuppressWarnings("unused")
private Long math101Id;

  @BeforeClass
  public static void bootH2() {
    // Make sure we’re in GUI mode when running from Maven/CI too
    System.setProperty("java.awt.headless", "false");

    // Use ONE in-memory DB and set BOTH namespaces so Hibernate doesn’t mix URLs
    Map<String, String> p = new HashMap<>();
    String url = "jdbc:h2:mem:student_unit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

    // JPA namespace
    p.put("javax.persistence.jdbc.url", url);
    p.put("javax.persistence.jdbc.user", "sa");
    p.put("javax.persistence.jdbc.password", "");
    p.put("javax.persistence.jdbc.driver", "org.h2.Driver");

    // Hibernate namespace
    p.put("hibernate.connection.url", url);
    p.put("hibernate.connection.username", "sa");
    p.put("hibernate.connection.password", "");
    p.put("hibernate.connection.driver_class", "org.h2.Driver");

    p.put("hibernate.hbm2ddl.auto", "create-drop");
    p.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    p.put("hibernate.show_sql", "false");
    p.put("hibernate.format_sql", "false");
    p.put("hibernate.id.new_generator_mappings", "true");

    JpaUtil.rebuild(p);
    emf = JpaUtil.emf();
  }

  @AfterClass
  public static void shutdown() {
    if (emf != null) emf.close();
  }

  @Override
  protected void onSetUp() {
    clearDb();

    cs101Id  = preloadCourse("CS101", "Intro to CS", 6, 50);
    math101Id = preloadCourse("MATH101", "Math I", 6, 1);

    view = GuiActionRunner.execute(StudentSwingView::new);
    window = new FrameFixture(robot(), view);
    window.show(); // constructor already loads courses/students
    robot().waitForIdle();
  }

  @Override
  protected void onTearDown() {
    if (window != null) window.cleanUp();
    clearDb();
  }

  // ---------- helpers ----------

  private void clearDb() {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      em.createNativeQuery("DELETE FROM enrollments").executeUpdate();
      em.createNativeQuery("DELETE FROM students").executeUpdate();
      em.createNativeQuery("DELETE FROM courses").executeUpdate();
      tx.commit();
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }

  /**
   * Robust preloader that does NOT rely on IDENTITY generation (works even if H2
   * created a non-identity PK). We compute a next id and insert with native SQL.
   */
  private Long preloadCourse(String code, String title, int cfu, int maxSeats) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      Number next =
          (Number) em.createNativeQuery("SELECT COALESCE(MAX(id),0)+1 FROM courses")
                     .getSingleResult();
      Long id = next.longValue();
      em.createNativeQuery("INSERT INTO courses (id, code, title, cfu, maxSeats) VALUES (?,?,?,?,?)")
        .setParameter(1, id)
        .setParameter(2, code)
        .setParameter(3, title)
        .setParameter(4, cfu)
        .setParameter(5, maxSeats)
        .executeUpdate();
      tx.commit();
      return id;
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }

  private void uiAddStudent(String matricola, String name, String email, int courseIndexOrZero) {
    clickBtn("btnRefreshStudent");
    window.textBox("txtMatricola").setText(matricola);
    window.textBox("txtFullName").setText(name);
    window.textBox("txtEmail").setText(email);
    window.comboBox("cmbCourse").selectItem(courseIndexOrZero);
    clickBtn("btnAddStudent");
  }

  private void clickBtn(String name) {
    javax.swing.JButton btn = window.button(name).target();
    execute(() -> { btn.requestFocusInWindow(); btn.doClick(); });
    robot().waitForIdle();
  }

  // ---------- tests ----------

  @Test
  public void initialState_tableEmpty_andControlsPresent() {
    // Table starts empty
    assertThat(window.table("tblStudents").rowCount()).isEqualTo(0);

    // Text fields & combo exist and are enabled
    window.textBox("txtMatricola").requireEnabled();
    window.textBox("txtFullName").requireEnabled();
    window.textBox("txtEmail").requireEnabled();
    window.comboBox("cmbCourse").requireEnabled();

    // Buttons exist (always enabled in this view)
    window.button("btnAddStudent").requireEnabled();
    window.button("btnUpdateStudent").requireEnabled();
    window.button("btnDeleteStudent").requireEnabled();
    window.button("btnClearStudent").requireEnabled();
    window.button("btnRefreshStudent").requireEnabled();

    // Combo should contain placeholder + our 2 courses
    String[] contents = window.comboBox("cmbCourse").contents();
    assertThat(String.join("|", contents))
        .contains("CS101 - Intro to CS")
        .contains("MATH101 - Math I");
  }

  @Test
  public void addStudent_addsRow() {
    uiAddStudent("1111111", "Farrukh Aziz", "farrukh@example.com", 1); // 1 = CS101
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblStudents").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("1111111");
      assertThat(rows[0][2]).isEqualTo("Farrukh Aziz");
      assertThat(rows[0][4]).contains("CS101"); // courses CSV
    });
  }

  @Test
  public void addDuplicateMatricola_showsWarning_noExtraRow() {
    uiAddStudent("1234567", "John Doe", "john@example.com", 1);
    int before = window.table("tblStudents").rowCount();

    // Try duplicate
    window.textBox("txtMatricola").setText("1234567");
    window.textBox("txtFullName").setText("Dup Name");
    window.textBox("txtEmail").setText("dup@example.com");
    window.comboBox("cmbCourse").selectItem(1);
    clickBtn("btnAddStudent");

    await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(before)
    );
  }

  @Test
  public void updateSelectedRow_changesName() {
    uiAddStudent("2000001", "Alice Adams", "alice@example.com", 2); // 2 = MATH101

    window.table("tblStudents").selectRows(0);
    window.textBox("txtFullName").setText("Alice A.");
    clickBtn("btnUpdateStudent");

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblStudents").contents();
      assertThat(rows[0][2]).isEqualTo("Alice A.");
      assertThat(rows[0][4]).contains("MATH101");
    });
  }

  @Test
  public void deleteSelectedRow_removesRow() {
    uiAddStudent("3000001", "Bob Brown", "bob@example.com", 1);
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(1)
    );

    window.table("tblStudents").selectRows(0);
    clickBtn("btnDeleteStudent");

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(0)
    );
  }

  @Test
  public void capacityCheck_blocksSecondEnrollment_whenCourseIsFull() {
    // MATH101 has maxSeats = 1 (see preload)
    uiAddStudent("4000001", "First Student", "first@example.com", 2); // MATH101

    // Attempt to add second student into the same (now full) course
    window.textBox("txtMatricola").setText("4000002");
    window.textBox("txtFullName").setText("Second Student");
    window.textBox("txtEmail").setText("second@example.com");
    window.comboBox("cmbCourse").selectItem(2);
    clickBtn("btnAddStudent");

    await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(1)
    );
  }

  @Test
  public void clearButton_resetsForm_andKeepsTable() {
    uiAddStudent("5000001", "Charlie", "c@example.com", 1);
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(1)
    );

    window.textBox("txtMatricola").setText("temp");
    window.textBox("txtFullName").setText("temp");
    window.textBox("txtEmail").setText("temp@example.com");
    window.comboBox("cmbCourse").selectItem(0);

    clickBtn("btnClearStudent");

    // Form cleared, table unchanged
    assertThat(window.textBox("txtMatricola").text()).isEmpty();
    assertThat(window.textBox("txtFullName").text()).isEmpty();
    assertThat(window.textBox("txtEmail").text()).isEmpty();
    window.comboBox("cmbCourse").requireSelection(0); // placeholder selected
    assertThat(window.table("tblStudents").rowCount()).isEqualTo(1);
  }
}
