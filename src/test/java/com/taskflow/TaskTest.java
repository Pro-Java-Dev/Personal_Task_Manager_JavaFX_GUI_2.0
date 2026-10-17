package com.taskflow;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testTaskCreationAndGetters() {
        LocalDate now = LocalDate.now();
        Task task = new Task("Test Task", "Test Description", "Work", "High", now);

        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals("Work", task.getCategory());
        assertEquals("High", task.getPriority());
        assertEquals(now, task.getDueDate());
        assertFalse(task.isCompleted());
        assertNotNull(task.getId());
    }

    @Test
    void testInvalidTitleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Task("", "Desc", "Work", "Medium", LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> new Task("   ", "Desc", "Work", "Medium", LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> new Task(null, "Desc", "Work", "Medium", LocalDate.now()));
    }

    @Test
    void testPriorityNormalization() {
        Task task = new Task("Task", "Desc", "Work", "high", LocalDate.now());
        assertEquals("High", task.getPriority());

        task.setPriority("LOW");
        assertEquals("Low", task.getPriority());

        task.setPriority("invalid");
        assertEquals("Medium", task.getPriority());
    }

    @Test
    void testSanitizationAndTruncation() {
        String longTitle = "A".repeat(300);
        Task task = new Task(longTitle, "Desc", "Work", "High", LocalDate.now());
        assertEquals(200, task.getTitle().length());
    }

    @Test
    void testHtmlTagStripping() {
        Task task = new Task("<b>Title</b><script>alert(1)</script>", "<p>Hello <span>World</span></p>", "Work", "High", LocalDate.now());
        assertEquals("Titlealert(1)", task.getTitle());
        assertEquals("Hello World", task.getDescription());
    }

    @Test
    void testTaskEqualsAndHashCode() {
        Task t1 = new Task("Task 1", "Desc", "Work", "High", LocalDate.now());
        Task t2 = new Task("Task 2", "Desc", "Work", "High", LocalDate.now());

        assertNotEquals(t1, t2);
        assertEquals(t1, t1);
    }
}
