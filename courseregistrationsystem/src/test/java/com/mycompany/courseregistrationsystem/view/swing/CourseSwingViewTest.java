package com.mycompany.courseregistrationsystem.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mycompany.courseregistrationsystem.controller.CourseController;
import com.mycompany.courseregistrationsystem.model.Course;

@RunWith(GUITestRunner.class)
public class CourseSwingViewTest extends AssertJSwingJUnitTestCase {

  private FrameFixture window;
  private CourseSwingView view;

  @Mock
  private CourseController controller;

  private AutoCloseable mocks;


  private static final Long   ID1    = 1L;
  private static final Long   ID2    = 2L;
  private static final String CODE1  = "CS101";
  private static final String TITLE1 = "Intro to CS";
  private static final int    CFU1   = 6;
  private static final int    MAX1   = 40;

  private static final String CODE2  = "MATH101";
  private static final String TITLE2 = "Math I";
  private static final int    CFU2   = 6;
  private static final int    MAX2   = 30;

  @Override
  protected void onSetUp() {
    mocks = MockitoAnnotations.openMocks(this);

    when(controller.loadAll()).thenReturn(Collections.emptyList());
    when(controller.enrolledCount(anyLong())).thenReturn(0);

    view = GuiActionRunner.execute(CourseSwingView::new);
    GuiActionRunner.execute(() -> view.setController(controller));

    window = new FrameFixture(robot(), view);
    window.show();
    robot().waitForIdle();
  }

  @Override
  protected void onTearDown() throws Exception {
    if (window != null) window.cleanUp();
    if (mocks != null) mocks.close();
  }

  // ----------------- helpers -----------------

  @SuppressWarnings("unused")
private void clickYesOnConfirm() {
    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.requireVisible();
    pane.yesButton().click();
    robot().waitForIdle();
  }
  
  private void clickYesOnDialog() {
	    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
	    pane.pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);
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

    // table present
    window.table("tblCourses");

    // buttons enabled by design
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

    click(window,"btnRefreshCourse");

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
  public void selectRow_loadsFormFields() {
    Course c1 = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    when(controller.loadAll()).thenReturn(Arrays.asList(c1));
    when(controller.enrolledCount(ID1)).thenReturn(0);

    click(window,"btnRefreshCourse");
    
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
    
    assertThat(window.textBox("txtCode").text()).isEqualTo(CODE1);
    assertThat(window.textBox("txtTitle").text()).isEqualTo(TITLE1);
    assertThat(window.spinner("spnCfu").target().getValue()).isEqualTo(CFU1);
    assertThat(window.spinner("spnMaxSeats").target().getValue()).isEqualTo(MAX1);
  }

  @Test
  @GUITest
  public void delete_chooseNo_keepsRow() {
    Course c1 = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    when(controller.loadAll()).thenReturn(Arrays.asList(c1));
    when(controller.enrolledCount(ID1)).thenReturn(0);
    
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
  @GUITest
  public void clearButton_resetsFormAndSelection() {
    Course c1 = courseMock(ID1, CODE1, TITLE1, CFU1, MAX1);
    when(controller.loadAll()).thenReturn(Arrays.asList(c1));
    when(controller.enrolledCount(ID1)).thenReturn(0);

    click(window,"btnRefreshCourse");
    
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
    
    click(window,"btnClearCourse");

    assertThat(window.textBox("txtCode").text()).isEmpty();
    assertThat(window.textBox("txtTitle").text()).isEmpty();
    assertThat(window.spinner("spnCfu").target().getValue()).isEqualTo(6);
    assertThat(window.spinner("spnMaxSeats").target().getValue()).isEqualTo(50);
    assertThat(window.table("tblCourses").target().getSelectedRow()).isEqualTo(-1);
  }

  @Test
  @GUITest
  public void update_withoutSelection_showsWarningDialog() {
    click(window,"btnUpdateCourse");

    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.requireVisible();
    pane.requireMessage("Select a row to update.");
    pane.okButton().click();
    robot().waitForIdle();
  }

  @Test
  @GUITest
  public void delete_withoutSelection_showsWarningDialog() {

    click(window,"btnDeleteCourse");

    JOptionPaneFixture pane = JOptionPaneFinder.findOptionPane().using(robot());
    pane.requireVisible();
    pane.requireMessage("Select a row to delete.");
    pane.okButton().click();
    robot().waitForIdle();
  }
}
