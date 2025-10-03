package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.controller.StudentController;
import com.mycompany.courseregistrationsystem.model.Course;
import com.mycompany.courseregistrationsystem.model.Student;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")
@RunWith(GUITestRunner.class)
public class StudentSwingViewTest extends AssertJSwingJUnitTestCase {

  private FrameFixture window;
  private StudentSwingView view;

  @Mock
  private StudentController controller;

  private AutoCloseable mocks;

  @Override
  protected void onSetUp() {
    mocks = MockitoAnnotations.openMocks(this);

    // Safe defaults so constructor refreshes don't explode
    when(controller.loadAllCourses()).thenReturn(Collections.emptyList());
    when(controller.findAllWithCourses()).thenReturn(Collections.emptyList());

    view = execute(StudentSwingView::new);
    execute(() -> view.setController(controller));

    window = new FrameFixture(robot(), view);
    window.show();
    robot().waitForIdle();
  }

  @Override
  protected void onTearDown() throws Exception {
    if (window != null) window.cleanUp();
    if (mocks != null) mocks.close();
  }
  
 
private void uiAddStudent(String matricola, String name, String email, int courseIndexOrZero) {
	    
	    window.textBox("txtMatricola").setText(matricola);
	    window.textBox("txtFullName").setText(name);
	    window.textBox("txtEmail").setText(email);
	    window.comboBox("cmbCourse").selectItem(courseIndexOrZero);
	    javax.swing.JButton addBtn = window.button("btnAddStudent").target();
	    execute(() -> { addBtn.requestFocusInWindow(); addBtn.doClick(); });
	  }

  // ---------------- tests ----------------

  @Test @GUITest
  public void initialControls_areVisibleAndEnabled() {
    window.label(JLabelMatcher.withText("Matricola:"));
    window.label(JLabelMatcher.withText("Full Name:"));
    window.label(JLabelMatcher.withText("Email:"));
    window.label(JLabelMatcher.withText("Course:"));

    window.textBox("txtMatricola").requireEnabled();
    window.textBox("txtFullName").requireEnabled();
    window.textBox("txtEmail").requireEnabled();
    window.comboBox("cmbCourse").requireEnabled();

    window.table("tblStudents");

    window.button(JButtonMatcher.withName("btnAddStudent")).requireEnabled();
    window.button(JButtonMatcher.withName("btnUpdateStudent")).requireEnabled();
    window.button(JButtonMatcher.withName("btnDeleteStudent")).requireEnabled();
    window.button(JButtonMatcher.withName("btnClearStudent")).requireEnabled();
    window.button(JButtonMatcher.withName("btnRefreshStudent")).requireEnabled();
  }

  @Test @GUITest
  public void update_withoutSelection_showsWarningDialog() {
    JButton update = window.button("btnUpdateStudent").target();
    execute(() -> { update.requestFocusInWindow(); update.doClick(); });

    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      JOptionPane pane = (JOptionPane) JOptionPaneFinder.findOptionPane().using(robot()).target();
      assertThat(pane.getMessage().toString()).contains("Select a row to update");
    });
  }

  @Test @GUITest
  public void delete_withoutSelection_showsWarningDialog() {
    JButton del = window.button("btnDeleteStudent").target();
    execute(() -> { del.requestFocusInWindow(); del.doClick(); });

    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      JOptionPane pane = (JOptionPane) JOptionPaneFinder.findOptionPane().using(robot()).target();
      assertThat(pane.getMessage().toString()).contains("Select a row to delete");
    });
  }

  @Test @GUITest
  public void add_with_missing_fields_showsWarning_and_no_row_added() {
    // Leave all fields empty and click Add
    JButton add = window.button("btnAddStudent").target();
    execute(() -> { add.requestFocusInWindow(); add.doClick(); });

    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
        JOptionPaneFinder.findOptionPane().using(robot()).requireVisible()
    );

    assertThat(window.table("tblStudents").rowCount()).isEqualTo(0);
    // No controller call expected (signature may vary; we just assert no row appeared)
  }
}
