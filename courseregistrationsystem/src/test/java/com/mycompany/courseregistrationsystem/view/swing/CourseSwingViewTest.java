package com.mycompany.courseregistrationsystem.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.courseregistrationsystem.controller.CourseController;
import com.mycompany.courseregistrationsystem.model.Course;

@SuppressWarnings("unused")
@RunWith(GUITestRunner.class)
public class CourseSwingViewTest extends AssertJSwingJUnitTestCase {

  private FrameFixture window;
  private CourseSwingView view;

  @Mock
  private CourseController controller;

  private AutoCloseable mocks;

  // Test constants
  private static final Long   ID1     = 1L;
  private static final Long   ID2     = 2L;
  private static final String CODE1   = "CS101";
  private static final String TITLE1  = "Intro to CS";
  private static final int    CFU1    = 6;
  private static final int    MAX1    = 40;

  private static final String CODE2   = "MATH101";
  private static final String TITLE2  = "Math I";
  private static final int    CFU2    = 6;
  private static final int    MAX2    = 30;

  @Before @Override
  public void onSetUp() {
    mocks = MockitoAnnotations.openMocks(this);


    when(controller.loadAll()).thenReturn(java.util.Collections.emptyList());
    when(controller.enrolledCount(org.mockito.ArgumentMatchers.anyLong())).thenReturn(0);

    view = org.assertj.swing.edt.GuiActionRunner.execute(CourseSwingView::new);
    org.assertj.swing.edt.GuiActionRunner.execute(() -> view.setController(controller));
    window = new org.assertj.swing.fixture.FrameFixture(robot(), view);
    window.show();
    robot().waitForIdle();
  }

  @After @Override
  public void onTearDown() throws Exception {
    if (window != null) window.cleanUp();
    if (mocks != null) mocks.close();
  }

  // ----------------- helpers -----------------

  private void clickOkOnAnyDialog() {
    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.requireVisible();
    pane.okButton().click();
    robot().waitForIdle();
  }

  private void clickYesOnConfirm() {
    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.requireVisible();
    pane.yesButton().click();
    robot().waitForIdle();
  }

  private void clickNoOnConfirm() {
    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.requireVisible();
    pane.noButton().click();
    robot().waitForIdle();
  }

  private Course courseMock(Long id, String code, String title, int cfu, int maxSeats) {
    Course c = mock(Course.class);
    when(c.getId()).thenReturn(id);
    when(c.getCode()).thenReturn(code);
    when(c.getTitle()).thenReturn(title);
    when(c.getCfu()).thenReturn(cfu);
    when(c.getMaxSeats()).thenReturn(maxSeats);
    return c;
  }

  // ----------------- tests -----------------

  @Test
  @GUITest
  public void initialControls_areVisibleAndEnabled() {
    // labels
    window.label(JLabelMatcher.withText("Course Management"));
    window.label(JLabelMatcher.withText("Code:"));
    window.label(JLabelMatcher.withText("Title:"));
    window.label(JLabelMatcher.withText("CFU:"));
    window.label(JLabelMatcher.withText("Max Seats:"));

    // fields/spinners
    window.textBox("txtCode").requireEnabled();
    window.textBox("txtTitle").requireEnabled();
    window.spinner("spnCfu").requireEnabled();
    window.spinner("spnMaxSeats").requireEnabled();

    // table
    window.table("tblCourses");

    // buttons (view has them enabled by default)
    window.button(JButtonMatcher.withName("btnAddCourse")).requireEnabled();
    window.button(JButtonMatcher.withName("btnUpdateCourse")).requireEnabled();
    window.button(JButtonMatcher.withName("btnDeleteCourse")).requireEnabled();
    window.button(JButtonMatcher.withName("btnClearCourse")).requireEnabled();
    window.button(JButtonMatcher.withName("btnRefreshCourse")).requireEnabled();
  }

  @Test
  @GUITest
  public void refresh_showsCoursesInTable() {
    Course c1 = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    Course c2 = courseMock(ID2, CODE2, TITLE2, CFU2, MAX2);

    when(controller.loadAll()).thenReturn(Arrays.asList(c1, c2));
    when(controller.enrolledCount(ID1)).thenReturn(0);
    when(controller.enrolledCount(ID2)).thenReturn(3);

    window.button("btnRefreshCourse").click();

    String[][] rows = window.table("tblCourses").contents();
    assertThat(rows.length).isEqualTo(2);
    assertThat(rows[0][1]).isEqualTo(CODE1);
    assertThat(rows[0][2]).isEqualTo(TITLE1);
    assertThat(rows[0][3]).isEqualTo(String.valueOf(CFU1));
    assertThat(rows[0][4]).isEqualTo(String.valueOf(MAX1));

    assertThat(rows[1][1]).isEqualTo(CODE2);
    assertThat(rows[1][2]).isEqualTo(TITLE2);
  }

  @Test
  @GUITest
  public void add_delegatesToController_andTableShowsNewRow() {
    Course created = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    when(controller.add(CODE1, TITLE1, CFU1, MAX1)).thenReturn(created);
    when(controller.loadAll()).thenReturn(Arrays.asList(created));
    when(controller.enrolledCount(ID1)).thenReturn(0);

    window.textBox("txtCode").enterText(CODE1);
    window.textBox("txtTitle").enterText(TITLE1);
    window.spinner("spnCfu").target().setValue(CFU1);
    window.spinner("spnMaxSeats").target().setValue(MAX1);

    window.button("btnAddCourse").click();

    verify(controller).add(CODE1, TITLE1, CFU1, MAX1);

    String[][] rows = window.table("tblCourses").contents();
    assertThat(rows.length).isEqualTo(1);
    assertThat(rows[0][1]).isEqualTo(CODE1);
    assertThat(rows[0][2]).isEqualTo(TITLE1);
  }

  @Test
  @GUITest
  public void selectRow_loadsFormFields() {
    Course c1 = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    when(controller.loadAll()).thenReturn(Arrays.asList(c1));
    when(controller.enrolledCount(ID1)).thenReturn(0);

    window.button("btnRefreshCourse").click();
    window.table("tblCourses").cell(org.assertj.swing.data.TableCell.row(0).column(0)).click();

    assertThat(window.textBox("txtCode").text()).isEqualTo(CODE1);
    assertThat(window.textBox("txtTitle").text()).isEqualTo(TITLE1);
    assertThat(window.spinner("spnCfu").target().getValue()).isEqualTo(CFU1);
    assertThat(window.spinner("spnMaxSeats").target().getValue()).isEqualTo(MAX1);
  }

  @SuppressWarnings("unchecked")
@Test
  @GUITest
  public void update_delegatesAndRefreshesTable() {
    Course original = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    Course updated  = courseMock(ID1, CODE1, "Intro to CS (Updated)", 8, 50);

    // first refresh shows original, update refresh shows updated
    when(controller.loadAll()).thenReturn(Arrays.asList(original), Arrays.asList(updated));
    when(controller.enrolledCount(ID1)).thenReturn(0);

    window.button("btnRefreshCourse").click();
    window.table("tblCourses").cell(org.assertj.swing.data.TableCell.row(0).column(0)).click();

    // change fields
    window.textBox("txtTitle").setText("Intro to CS (Updated)");
    window.spinner("spnCfu").target().setValue(8);
    window.spinner("spnMaxSeats").target().setValue(50);

    when(controller.update(ID1, CODE1, "Intro to CS (Updated)", 8, 50)).thenReturn(updated);

    window.button("btnUpdateCourse").click();

    verify(controller).update(ID1, CODE1, "Intro to CS (Updated)", 8, 50);

    String[][] rows = window.table("tblCourses").contents();
    assertThat(rows[0][2]).isEqualTo("Intro to CS (Updated)");
    assertThat(rows[0][3]).isEqualTo("8");
    assertThat(rows[0][4]).isEqualTo("50");
  }

  @Test
  @GUITest
  public void delete_chooseNo_keepsRow() {
    Course c1 = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    when(controller.loadAll()).thenReturn(Arrays.asList(c1));
    when(controller.enrolledCount(ID1)).thenReturn(0);

    window.button("btnRefreshCourse").click();
    window.table("tblCourses").selectRows(0);

    window.button("btnDeleteCourse").click();
    clickNoOnConfirm();

    // still 1 row
    assertThat(window.table("tblCourses").rowCount()).isEqualTo(1);
  }

  @SuppressWarnings("unchecked")
@Test
  @GUITest
  public void delete_chooseYes_delegatesAndRemovesRow() {
    Course c1 = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    when(controller.loadAll()).thenReturn(Arrays.asList(c1), Collections.emptyList());
    when(controller.enrolledCount(ID1)).thenReturn(0);

    window.button("btnRefreshCourse").click();
    window.table("tblCourses").selectRows(0);

    doNothing().when(controller).delete(ID1);

    window.button("btnDeleteCourse").click();
    clickYesOnConfirm();

    verify(controller).delete(ID1);
    // The view then shows a non-modal Info dialog ("Course deleted.") that auto-closes; no need to click OK.

    String[][] rows = window.table("tblCourses").contents();
    assertThat(rows.length).isEqualTo(0);
  }

  @Test
  @GUITest
  public void clearButton_resetsFormAndSelection() {
    // preload something and select it
    Course c1 = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    when(controller.loadAll()).thenReturn(Arrays.asList(c1));
    when(controller.enrolledCount(ID1)).thenReturn(0);

    window.button("btnRefreshCourse").click();
    window.table("tblCourses").cell(org.assertj.swing.data.TableCell.row(0).column(0)).click();

    // change fields to non-defaults
    window.textBox("txtCode").setText("X");
    window.textBox("txtTitle").setText("Y");
    window.spinner("spnCfu").target().setValue(7);
    window.spinner("spnMaxSeats").target().setValue(77);

    window.button("btnClearCourse").click();

    assertThat(window.textBox("txtCode").text()).isEmpty();
    assertThat(window.textBox("txtTitle").text()).isEmpty();
    assertThat(window.spinner("spnCfu").target().getValue()).isEqualTo(6);
    assertThat(window.spinner("spnMaxSeats").target().getValue()).isEqualTo(50);
    assertThat(window.table("tblCourses").target().getSelectedRow()).isEqualTo(-1);
  }

  @Test
  @GUITest
  public void update_withoutSelection_showsWarningDialog() {
    window.button("btnUpdateCourse").click();

    // Warning dialog "Select a row to update."
    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.requireVisible();
    pane.requireMessage("Select a row to update.");
    pane.okButton().click();
    robot().waitForIdle();
  }

  @Test
  @GUITest
  public void delete_withoutSelection_showsWarningDialog() {
    window.button("btnDeleteCourse").click();

    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.requireVisible();
    pane.requireMessage("Select a row to delete.");
    pane.okButton().click();
    robot().waitForIdle();
  }
}
