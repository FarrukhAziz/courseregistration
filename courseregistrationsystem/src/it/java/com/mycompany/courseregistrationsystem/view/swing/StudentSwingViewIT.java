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
    preloadCourse("CS101", "Intro to CS", 6, 30);
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

  // --- Tests ---

  @Test
  public void t1_addStudentViaUI() {
    window.button("btnRefreshStudent").click();

    window.textBox("txtMatricola").enterText("M12345");
    window.textBox("txtFullName").enterText("John Doe");
    window.textBox("txtEmail").enterText("john@example.com");
    window.comboBox("cmbCourse").selectItem(1); // 0 = placeholder
    window.button("btnAddStudent").click();
    window.dialog().button(JButtonMatcher.withText("OK")).click();

    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] table = window.table("tblStudents").contents();
      assertThat(table.length).isEqualTo(1);
      assertThat(table[0][1]).isEqualTo("M12345");
      assertThat(table[0][2]).isEqualTo("John Doe");
      assertThat(table[0][4]).contains("CS101");
    });
  }

  @Test
  public void t2_updateStudentViaUI() {
    t1_addStudentViaUI(); // reuse setup
    window.table("tblStudents").selectRows(0);
    window.textBox("txtFullName").setText("Johnathan Doe");
    window.button("btnUpdateStudent").click();
    window.dialog().button(JButtonMatcher.withText("OK")).click();

    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblStudents").contents();
      assertThat(rows[0][2]).isEqualTo("Johnathan Doe");
    });
  }

  @Test
  public void t3_deleteStudentViaUI() {
    t1_addStudentViaUI();
    window.table("tblStudents").selectRows(0);
    window.button("btnDeleteStudent").click();
    window.dialog().button(JButtonMatcher.withText("Yes")).click();
    window.dialog().button(JButtonMatcher.withText("OK")).click();

    await().atMost(7, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(0)
    );
  }

  @Test
  public void t4_updateWithoutSelection_showsWarning() {
    window.button("btnUpdateStudent").click();
    window.dialog().requireVisible(); // "Warning"
    window.dialog().button(JButtonMatcher.withText("OK")).click();
    assertThat(window.table("tblStudents").rowCount()).isEqualTo(0);
  }
}
