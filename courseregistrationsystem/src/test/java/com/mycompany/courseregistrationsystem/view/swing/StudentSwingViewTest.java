package com.mycompany.courseregistrationsystem.view.swing;

import com.mycompany.courseregistrationsystem.controller.JpaUtil;
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
    public static void initializeDatabase() {
        System.setProperty("java.awt.headless", "false");
        Map<String, String> properties = new HashMap<>();
        String url = "jdbc:h2:mem:student_unit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        properties.put("javax.persistence.jdbc.url", url);
        properties.put("javax.persistence.jdbc.user", "sa");
        properties.put("javax.persistence.jdbc.password", "");
        properties.put("javax.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("hibernate.connection.url", url);
        properties.put("hibernate.connection.username", "sa");
        properties.put("hibernate.connection.password", "");
        properties.put("hibernate.connection.driver_class", "org.h2.Driver");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.id.new_generator_mappings", "true");
        JpaUtil.rebuild(properties);
        emf = JpaUtil.emf();
    }

    @AfterClass
    public static void closeDatabase() {
        if (emf != null) {
            emf.close();
        }
    }

    @Override
    protected void onSetUp() {
        clearDatabase();
        cs101Id = preloadCourse("CS101", "Intro to CS", 6, 50);
        math101Id = preloadCourse("MATH101", "Math I", 6, 1);

        view = GuiActionRunner.execute(StudentSwingView::new);
        window = new FrameFixture(robot(), view);
        window.show();
        robot().waitForIdle();
    }

    @Override
    protected void onTearDown() {
        if (window != null) {
            window.cleanUp();
        }
        clearDatabase();
    }

    private void clearDatabase() {
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

    private Long preloadCourse(String code, String title, int cfu, int maxSeats) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Number nextId = (Number) em.createNativeQuery("SELECT COALESCE(MAX(id),0)+1 FROM courses").getSingleResult();
            Long id = nextId.longValue();
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

    private void addStudentToUI(String matricola, String fullName, String email, int courseIndex) {
        window.button("btnRefreshStudent").click();
        window.textBox("txtMatricola").setText(matricola);
        window.textBox("txtFullName").setText(fullName);
        window.textBox("txtEmail").setText(email);
        window.comboBox("cmbCourse").selectItem(courseIndex);
        execute(() -> window.button("btnAddStudent").target().doClick());
    }

    private void clickButton(String buttonName) {
        execute(() -> window.button(buttonName).target().doClick());
        robot().waitForIdle();
    }

    @Test
    public void testInitialState_TableEmptyAndControlsEnabled() {
        assertThat(window.table("tblStudents").rowCount()).isZero();

        window.textBox("txtMatricola").requireEnabled();
        window.textBox("txtFullName").requireEnabled();
        window.textBox("txtEmail").requireEnabled();
        window.comboBox("cmbCourse").requireEnabled();

        window.button("btnAddStudent").requireEnabled();
        window.button("btnUpdateStudent").requireEnabled();
        window.button("btnDeleteStudent").requireEnabled();
        window.button("btnClearStudent").requireEnabled();
        window.button("btnRefreshStudent").requireEnabled();

        String[] comboContents = window.comboBox("cmbCourse").contents();
        assertThat(String.join("|", comboContents))
                .contains("CS101 - Intro to CS")
                .contains("MATH101 - Math I");
    }

    @Test
    public void testAddStudent_AddsStudentRow() {
        addStudentToUI("1111111", "Farrukh Aziz", "farrukh@example.com", 1);
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String[][] rows = window.table("tblStudents").contents();
            assertThat(rows[0][1]).isEqualTo("1111111");
            assertThat(rows[0][2]).isEqualTo("Farrukh Aziz");
            assertThat(rows[0][4]).contains("CS101");
        });
    }

    @Test
    public void testAddDuplicateMatricola_ShowsWarningAndDoesNotAdd() {
        addStudentToUI("1234567", "John Doe", "john@example.com", 1);
        int beforeCount = window.table("tblStudents").rowCount();

        window.textBox("txtMatricola").setText("1234567");
        window.textBox("txtFullName").setText("Duplicate Name");
        window.textBox("txtEmail").setText("dup@example.com");
        window.comboBox("cmbCourse").selectItem(1);
        clickButton("btnAddStudent");

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(window.table("tblStudents").rowCount()).isEqualTo(beforeCount);
        });
    }

    @Test
    public void testUpdateSelectedStudent_ChangesFullName() {
        addStudentToUI("2000001", "Alice Adams", "alice@example.com", 2);
        window.table("tblStudents").selectRows(0);
        window.textBox("txtFullName").setText("Alice A.");
        clickButton("btnUpdateStudent");

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String[][] rows = window.table("tblStudents").contents();
            assertThat(rows[0][2]).isEqualTo("Alice A.");
            assertThat(rows[0][4]).contains("MATH101");
        });
    }

    @Test
    public void testDeleteSelectedStudent_RemovesRow() {
        addStudentToUI("3000001", "Bob Brown", "bob@example.com", 1);
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(window.table("tblStudents").rowCount()).isEqualTo(1));

        window.table("tblStudents").selectRows(0);
        clickButton("btnDeleteStudent");

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(window.table("tblStudents").rowCount()).isZero());
    }

    @Test
    public void testCapacityCheck_PreventsAddingStudentWhenCourseIsFull() {
        // MATH101 has maxSeats = 1
        addStudentToUI("4000001", "First Student", "first@example.com", 2);

        window.textBox("txtMatricola").setText("4000002");
        window.textBox("txtFullName").setText("Second Student");
        window.textBox("txtEmail").setText("second@example.com");
        window.comboBox("cmbCourse").selectItem(2);
        clickButton("btnAddStudent");

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.table("tblStudents").rowCount()).isEqualTo(1));
    }

    @Test
    public void testClearButton_ResetsFormKeepsTableData() {
        addStudentToUI("5000001", "Charlie", "c@example.com", 1);
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(window.table("tblStudents").rowCount()).isEqualTo(1));

        window.textBox("txtMatricola").setText("temp");
        window.textBox("txtFullName").setText("temp");
        window.textBox("txtEmail").setText("temp@example.com");
        window.comboBox("cmbCourse").selectItem(0);

        clickButton("btnClearStudent");

        assertThat(window.textBox("txtMatricola").text()).isEmpty();
        assertThat(window.textBox("txtFullName").text()).isEmpty();
        assertThat(window.textBox("txtEmail").text()).isEmpty();
        window.comboBox("cmbCourse").requireSelection(0);
        assertThat(window.table("tblStudents").rowCount()).isEqualTo(1);
    }
}
