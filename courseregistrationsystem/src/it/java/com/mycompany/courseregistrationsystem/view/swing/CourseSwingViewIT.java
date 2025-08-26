package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.controller.CourseController;
import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.finder.JOptionPaneFinder;
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
    // INJECT controller that uses THE SAME EMF
    GuiActionRunner.execute(() -> view.setController(new CourseController(emf)));
    window = new FrameFixture(robot(), view);
    window.show();
    robot().waitForIdle();
  }

  @Override
  protected void onTearDown() {
    if (window != null) window.cleanUp();
    clearDb();
  }

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

  private void preloadCourse(String code, String title, int cfu, int maxSeats) {
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
    } finally {
      if (tx.isActive()) tx.rollback();
      em.close();
    }
  }

  private void clickOkOnAnyDialog() {
    await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
      JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
      pane.requireVisible();
      pane.okButton().click();
    });
    robot().waitForIdle();
  }

  private void clickYesOnConfirm() {
    await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
      JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
      pane.requireVisible();
      pane.yesButton().click();
    });
    robot().waitForIdle();
  }

  private void clickNoOnConfirm() {
    await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
      JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
      pane.requireVisible();
      pane.noButton().click();
    });
    robot().waitForIdle();
  }

  // ---------------- TESTS (9) ----------------

  @Test
  public void t1_initialTableEmpty_onShow() {
    assertThat(window.table("tblCourses").rowCount()).isEqualTo(0);
  }

  @Test
  public void t2_addValidCourse_addsRowAndShowsDialog() {
    window.textBox("txtCode").enterText("CS101");
    window.textBox("txtTitle").enterText("Intro to CS");
    window.button("btnAddCourse").click();
    clickOkOnAnyDialog();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblCourses").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("CS101");
      assertThat(rows[0][2]).isEqualTo("Intro to CS");
    });
  }

  @Test
  public void t3_duplicateCode_showsWarning_noExtraRow() {
    preloadCourse("CS101","Intro to CS",6,30);
    window.button("btnRefreshCourse").click();

    int before = window.table("tblCourses").rowCount();

    window.textBox("txtCode").setText("CS101");
    window.textBox("txtTitle").setText("Anything");
    window.button("btnAddCourse").click();
    clickOkOnAnyDialog(); // warning

    assertThat(window.table("tblCourses").rowCount()).isEqualTo(before);
  }

  @Test
  public void t4_refreshShowsPreloadedCourse() {
    preloadCourse("MATH101","Math I",6,40);
    window.button("btnRefreshCourse").click();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblCourses").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("MATH101");
      assertThat(rows[0][2]).isEqualTo("Math I");
    });
  }

  @Test
  public void t5_selectRowLoadsFormFields() {
    preloadCourse("PHY101","Physics I",6,45);
    window.button("btnRefreshCourse").click();
    window.table("tblCourses").selectRows(0);

    assertThat(window.textBox("txtCode").text()).isEqualTo("PHY101");
    assertThat(window.textBox("txtTitle").text()).isEqualTo("Physics I");
  }

  @Test
  public void t6_updateCourse_editsRowAndShowsDialog() {
    preloadCourse("BIO101","Biology I",6,30);
    window.button("btnRefreshCourse").click();
    window.table("tblCourses").selectRows(0);

    window.textBox("txtTitle").setText("Biology I (Updated)");
    window.button("btnUpdateCourse").click();
    clickOkOnAnyDialog();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblCourses").contents();
      assertThat(rows[0][2]).isEqualTo("Biology I (Updated)");
    });
  }

  @Test
  public void t7_deleteCourse_chooseNo_keepsRow() {
    preloadCourse("CHEM101","Chem I",6,35);
    window.button("btnRefreshCourse").click();
    window.table("tblCourses").selectRows(0);

    window.button("btnDeleteCourse").click();
    clickNoOnConfirm();

    assertThat(window.table("tblCourses").rowCount()).isEqualTo(1);
  }

  @Test
  public void t8_deleteCourse_chooseYes_removesRow() {
    preloadCourse("HIST101","History I",6,20);
    window.button("btnRefreshCourse").click();
    window.table("tblCourses").selectRows(0);

    window.button("btnDeleteCourse").click();
    clickYesOnConfirm();
    clickOkOnAnyDialog();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblCourses").rowCount()).isEqualTo(0)
    );
  }

  @Test
  public void t9_addInvalid_showsValidationDialog_noRow() {
    window.button("btnAddCourse").click(); // empty -> IAE -> Warning
    clickOkOnAnyDialog();
    assertThat(window.table("tblCourses").rowCount()).isEqualTo(0);
  }
}
