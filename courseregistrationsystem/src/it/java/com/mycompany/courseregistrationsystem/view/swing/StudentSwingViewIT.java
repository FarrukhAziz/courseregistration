package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
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
import static org.awaitility.Awaitility.await;

@RunWith(GUITestRunner.class)
public class StudentSwingViewIT extends AssertJSwingJUnitTestCase {

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
    Map<String,String> p = new HashMap<>();
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
    preloadCourse("CS101","Intro to CS",6,30);
    preloadCourse("MATH101","Math I",6,1); // capacity=1 for capacity test
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

  private void uiAddStudent(String matricola, String name, String email, int courseIndexOrZero) {
    window.button("btnRefreshStudent").click();
    window.textBox("txtMatricola").setText(matricola);
    window.textBox("txtFullName").setText(name);
    window.textBox("txtEmail").setText(email);
    window.comboBox("cmbCourse").selectItem(courseIndexOrZero); // 0 = placeholder
    window.button("btnAddStudent").click();
    clickOkOnAnyDialog();
  }

  // ----------- Tests (10) -----------

  @Test
  public void t1_initialTableEmpty() {
    assertThat(window.table("tblStudents").rowCount()).isEqualTo(0);
  }

  @Test
  public void t2_addStudent_valid_addsRow() {
    uiAddStudent("1234567","John Doe","john@example.com",1); // CS101
    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblStudents").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("1234567");
      assertThat(rows[0][2]).isEqualTo("John Doe");
      assertThat(rows[0][4]).contains("CS101");
    });
  }

  @Test
  public void t3_addDuplicateMatricola_dialogNoExtraRow() {
    uiAddStudent("1234567","John Doe","john@example.com",1);
    int before = window.table("tblStudents").rowCount();

    window.textBox("txtMatricola").setText("1234567");
    window.textBox("txtFullName").setText("Dup Name");
    window.textBox("txtEmail").setText("dup@example.com");
    window.comboBox("cmbCourse").selectItem(1);
    window.button("btnAddStudent").click();
    clickOkOnAnyDialog();

    assertThat(window.table("tblStudents").rowCount()).isEqualTo(before);
  }

  @Test
  public void t4_updateName_changesValue() {
    uiAddStudent("1234567","John Doe","john@example.com",1);
    window.table("tblStudents").selectRows(0);
    window.textBox("txtFullName").setText("Johnathan Doe");
    window.button("btnUpdateStudent").click();
    clickOkOnAnyDialog();

    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblStudents").contents();
      assertThat(rows[0][2]).isEqualTo("Johnathan Doe");
    });
  }

  @Test
  public void t5_changeCourse_toMath101() {
    uiAddStudent("1234567","John Doe","john@example.com",1); // CS101
    window.table("tblStudents").selectRows(0);
    window.comboBox("cmbCourse").selectItem(2); // MATH101
    window.button("btnUpdateStudent").click();
    clickOkOnAnyDialog();

    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblStudents").contents();
      assertThat(rows[0][4]).contains("MATH101");
    });
  }

  @Test
  public void t6_capacityFull_preventsEnrollment() {
    uiAddStudent("1234567","Alice","alice@example.com",2); // fills MATH101 (cap 1)
    uiAddStudent("1234568","Bob","bob@example.com",1);     // Bob in CS101

    window.table("tblStudents").selectRows(1); // Bob
    window.comboBox("cmbCourse").selectItem(2); // try to move to MATH101
    window.button("btnUpdateStudent").click();
    clickOkOnAnyDialog(); // warning dialog

    String[][] rows = window.table("tblStudents").contents();
    assertThat(rows.length).isEqualTo(2);
    assertThat(rows[1][4]).contains("CS101"); // unchanged
  }

  @Test
  public void t7_deleteStudent_chooseNo_keepsRow() {
    uiAddStudent("1234567","John Doe","john@example.com",1);
    window.table("tblStudents").selectRows(0);
    window.button("btnDeleteStudent").click();
    window.dialog().button(JButtonMatcher.withText("No")).click();

    assertThat(window.table("tblStudents").rowCount()).isEqualTo(1);
  }

  @Test
  public void t8_deleteStudent_chooseYes_removesRow() {
    uiAddStudent("1234567","John Doe","john@example.com",1);
    window.table("tblStudents").selectRows(0);
    window.button("btnDeleteStudent").click();
    window.dialog().button(JButtonMatcher.withText("Yes")).click();
    clickOkOnAnyDialog();

    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(0)
    );
  }

  @Test
  public void t9_updateWithoutSelection_showsDialog() {
    window.button("btnUpdateStudent").click();
    clickOkOnAnyDialog();
  }

  @Test
  public void t10_addMissingFields_dialogAndNoRow() {
    window.textBox("txtMatricola").setText("");
    window.textBox("txtFullName").setText("");
    window.textBox("txtEmail").setText("");
    window.button("btnAddStudent").click();
    clickOkOnAnyDialog();
    assertThat(window.table("tblStudents").rowCount()).isEqualTo(0);
  }
}
