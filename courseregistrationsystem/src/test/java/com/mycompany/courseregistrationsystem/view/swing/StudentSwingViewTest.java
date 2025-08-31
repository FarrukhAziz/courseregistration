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
    Map<String, String> p = new HashMap<>();
    p.put("hibernate.connection.url", "jdbc:h2:mem:student_unit;DB_CLOSE_DELAY=-1");
    p.put("hibernate.connection.username", "sa");
    p.put("hibernate.connection.password", "");
    p.put("hibernate.connection.driver_class", "org.h2.Driver");
    p.put("hibernate.hbm2ddl.auto", "create-drop");
    p.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

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

    cs101Id = preloadCourse("CS101", "Intro to CS", 6, 50);
    math101Id = preloadCourse("MATH101", "Math I", 6, 1);

    view = GuiActionRunner.execute(StudentSwingView::new);
    window = new FrameFixture(robot(), view);
    window.show();
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
      em.createQuery("DELETE FROM Student").executeUpdate();
      em.createQuery("DELETE FROM Course").executeUpdate();
      tx.commit();
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }

  private Long preloadCourse(String code, String title, int cfu, int maxSeats) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      Course c = new Course();
      c.setCode(code);
      c.setTitle(title);
      c.setCfu(cfu);
      c.setMaxSeats(maxSeats);
      em.persist(c);
      tx.commit();
      return c.getId();
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
    // contents are rendered as "-- Select Course --" or "CODE - Title"
    assertThat(contents.length).isGreaterThanOrEqualTo(3);
    assertThat(String.join("|", contents)).contains("CS101 - Intro to CS")
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
      // column 4 contains course codes CSV
      assertThat(rows[0][4]).contains("CS101");
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

    // Warn dialog in view auto-dismisses; we only assert table unchanged
    await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(before)
    );
  }

  @Test
  public void updateSelectedRow_changesName() {
    uiAddStudent("2000001", "Alice Adams", "alice@example.com", 2); // 2 = MATH101

    // Select first (and only) row
    window.table("tblStudents").selectRows(0);

    // Edit name and update
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
    // MATH101 has maxSeats = 1 (see setUp)
    uiAddStudent("4000001", "First Student", "first@example.com", 2); // enroll into MATH101

    // Attempt to add second student into the same (now full) course
    window.textBox("txtMatricola").setText("4000002");
    window.textBox("txtFullName").setText("Second Student");
    window.textBox("txtEmail").setText("second@example.com");
    window.comboBox("cmbCourse").selectItem(2); // MATH101
    clickBtn("btnAddStudent"); // should warn "Course is full" and not add

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
    window.comboBox("cmbCourse").selectItem(0); // set something

    clickBtn("btnClearStudent");

    // Form cleared, table unchanged
    assertThat(window.textBox("txtMatricola").text()).isEmpty();
    assertThat(window.textBox("txtFullName").text()).isEmpty();
    assertThat(window.textBox("txtEmail").text()).isEmpty();
    assertThat(window.comboBox("cmbCourse").selectedItem()).isEqualTo(0);
    assertThat(window.table("tblStudents").rowCount()).isEqualTo(1);
  }
}
