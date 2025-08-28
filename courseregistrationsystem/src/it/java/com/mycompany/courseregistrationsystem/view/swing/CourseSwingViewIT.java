package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.controller.CourseController;
import com.mycompany.courseregistrationsystem.controller.JpaUtil;
import com.mycompany.courseregistrationsystem.model.Course;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
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
import static org.assertj.swing.edt.GuiActionRunner.execute;
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
    view = GuiActionRunner.execute(CourseSwingView::new);
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

  @SuppressWarnings("unused")
private void clickYesOnConfirm() {
    await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
      JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
      pane.requireVisible();
      pane.yesButton().click();
    });
    robot().waitForIdle();
  }
  
  private void clickYesOnDialog() {
	    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
	    pane.pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);
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

  // ---------------- Tests ----------------

  @Test
  public void shows_empty_table_on_open() {
    assertThat(window.table("tblCourses").rowCount()).isEqualTo(0);
  }

  @Test
  public void add_adds_row_and_shows_info() {
    window.textBox("txtCode").enterText("CS101");
    window.textBox("txtTitle").enterText("Intro to CS");
    click(window, "btnAddCourse");
    clickOkOnAnyDialog();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblCourses").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("CS101");
      assertThat(rows[0][2]).isEqualTo("Intro to CS");
    });
  }

  @Test
  public void add_duplicate_code_shows_warning() {
    preloadCourse("CS101", "Intro to CS", 6, 30);
    click(window, "btnRefreshCourse");

    int before = window.table("tblCourses").rowCount();

    window.textBox("txtCode").setText("CS101");
    window.textBox("txtTitle").setText("Anything");
    click(window, "btnAddCourse");
    clickOkOnAnyDialog();

    assertThat(window.table("tblCourses").rowCount()).isEqualTo(before);
  }

  @Test
  public void refresh_shows_preloaded_course() {
    preloadCourse("MATH101", "Math I", 6, 40);
    click(window, "btnRefreshCourse");

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblCourses").contents();
      assertThat(rows.length).isEqualTo(1);
      assertThat(rows[0][1]).isEqualTo("MATH101");
      assertThat(rows[0][2]).isEqualTo("Math I");
    });
  }

  @Test
  public void selection_populates_form_fields() {
    preloadCourse("PHY101", "Physics I", 6, 45);
    click(window, "btnRefreshCourse");
    await()
    .atMost(5, TimeUnit.SECONDS)
    .untilAsserted(() -> assertThat(window.table("tblCourses").rowCount()).isEqualTo(1));
    org.assertj.swing.fixture.JTableFixture tbl = window.table("tblCourses");
    org.assertj.swing.data.TableCell first = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(first).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
    	tbl.selectRows(0);
    	robot().waitForIdle();
    }
    assertThat(tbl.target().isRowSelected(0)).isTrue();

    assertThat(window.textBox("txtCode").text()).isEqualTo("PHY101");
    assertThat(window.textBox("txtTitle").text()).isEqualTo("Physics I");
  }

  @Test
  public void update_edits_row_and_shows_info() {
    preloadCourse("CS202", "Data Structures", 6, 30);
    click(window, "btnRefreshCourse");

    await()
    .atMost(5, TimeUnit.SECONDS)
    .untilAsserted(() -> assertThat(window.table("tblCourses").rowCount()).isEqualTo(1));
    org.assertj.swing.fixture.JTableFixture tbl = window.table("tblCourses");
    org.assertj.swing.data.TableCell first = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(first).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
    	tbl.selectRows(0);
    	robot().waitForIdle();
    }
    assertThat(tbl.target().isRowSelected(0)).isTrue();

    window.textBox("txtTitle").setText("Data Structures (Updated)");
    click(window, "btnUpdateCourse");
    clickOkOnAnyDialog();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      String[][] rows = window.table("tblCourses").contents();
      assertThat(rows[0][2]).isEqualTo("Data Structures (Updated)");
    });
  }

  @Test
  public void delete_no_keeps_row() {
    preloadCourse("CHEM101", "Chem I", 6, 35);
    click(window, "btnRefreshCourse");
    await()
    .atMost(5, TimeUnit.SECONDS)
    .untilAsserted(() -> assertThat(window.table("tblCourses").rowCount()).isEqualTo(1));
    org.assertj.swing.fixture.JTableFixture tbl = window.table("tblCourses");
    org.assertj.swing.data.TableCell first = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(first).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
    	tbl.selectRows(0);
    	robot().waitForIdle();
    }
    assertThat(tbl.target().isRowSelected(0)).isTrue();

    click(window, "btnDeleteCourse");
    clickNoOnConfirm();

    assertThat(window.table("tblCourses").rowCount()).isEqualTo(1);
  }

  @Test
  public void delete_yes_removes_row() {
    preloadCourse("HIST101", "History I", 6, 20);
    click(window, "btnRefreshCourse");
    await()
    .atMost(5, TimeUnit.SECONDS)
    .untilAsserted(() -> assertThat(window.table("tblCourses").rowCount()).isEqualTo(1));
    org.assertj.swing.fixture.JTableFixture tbl = window.table("tblCourses");
    org.assertj.swing.data.TableCell first = org.assertj.swing.data.TableCell.row(0).column(0);
    tbl.cell(first).click();
    robot().waitForIdle();
    if (!tbl.target().isRowSelected(0)) {
    	tbl.selectRows(0);
    	robot().waitForIdle();
    }
    assertThat(tbl.target().isRowSelected(0)).isTrue();

    click(window, "btnDeleteCourse");
    clickYesOnDialog();

    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
        assertThat(window.table("tblCourses").rowCount()).isEqualTo(0)
    );
  }

  @Test
  public void add_invalid_shows_validation_and_no_row() {
    click(window, "btnAddCourse");
    clickOkOnAnyDialog();
    assertThat(window.table("tblCourses").rowCount()).isEqualTo(0);
  }
}
