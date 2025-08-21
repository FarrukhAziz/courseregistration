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
public class CourseSwingViewIT extends AssertJSwingJUnitTestCase {

  @SuppressWarnings("resource")
@ClassRule
  public static PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("ui_course_it")
          .withUsername("user")
          .withPassword("pass");

  private static EntityManagerFactory emf;

  private CourseSwingView view;
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
    view = GuiActionRunner.execute(CourseSwingView::new);
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
  public void t1_initialTableEmpty_onShow() {
    assertThat(window.table("tblCourses").rowCount()).isEqualTo(0);
  }

  @Test
  public void t2_refreshShowsPreloadedCourse() {
    preloadCourse("CS101", "Intro to CS", 6, 30);
    window.button("btnRefreshCourse").click();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblCourses").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("CS101");
      assertThat(rows[0][2]).isEqualTo("Intro to CS");
      assertThat(Integer.parseInt(rows[0][5])).isEqualTo(0);
    });
  }

  @Test
  public void t3_selectRowLoadsFormFields() {
    preloadCourse("MATH101", "Math I", 6, 40);
    window.button("btnRefreshCourse").click();
    window.table("tblCourses").selectRows(0);

    assertThat(window.textBox("txtCode").text()).isEqualTo("MATH101");
    assertThat(window.textBox("txtTitle").text()).isEqualTo("Math I");
    // spinner text can vary across LAF; rely on table contents instead
    String[][] rows = window.table("tblCourses").contents();
    assertThat(Integer.parseInt(rows[0][3])).isEqualTo(6);
    assertThat(Integer.parseInt(rows[0][4])).isEqualTo(40);
  }

  @Test
  public void t4_updateCourseViaUI() {
    preloadCourse("PHY101", "Physics I", 6, 40);
    window.button("btnRefreshCourse").click();
    window.table("tblCourses").selectRows(0);

    window.textBox("txtTitle").setText("Physics I (Updated)");
    window.spinner("spnCfu").increment(1);      // 7
    window.spinner("spnMaxSeats").decrement(1); // 39
    window.button("btnUpdateCourse").click();
    window.dialog().button(JButtonMatcher.withText("OK")).click();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] afterUpdate = window.table("tblCourses").contents();
      assertThat(afterUpdate.length).isEqualTo(1);
      assertThat(afterUpdate[0][2]).isEqualTo("Physics I (Updated)");
      assertThat(Integer.parseInt(afterUpdate[0][3])).isEqualTo(7);
      assertThat(Integer.parseInt(afterUpdate[0][4])).isEqualTo(39);
    });
  }

  @Test
  public void t5_deleteCourseViaUI() {
    preloadCourse("CHEM101", "Chemistry I", 6, 35);
    window.button("btnRefreshCourse").click();
    window.table("tblCourses").selectRows(0);

    window.button("btnDeleteCourse").click();
    window.dialog().button(JButtonMatcher.withText("Yes")).click();
    window.dialog().button(JButtonMatcher.withText("OK")).click();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblCourses").rowCount()).isEqualTo(0)
    );
  }
}
