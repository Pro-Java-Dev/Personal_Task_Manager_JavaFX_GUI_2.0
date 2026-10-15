package com.taskflow;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskRepositoryTest {

    @Test
    void testDefaultCategories() {
        TaskRepository repo = new TaskRepository();
        List<Category> defaults = repo.defaultCategories();

        assertNotNull(defaults);
        assertTrue(defaults.size() >= 2);
        assertTrue(defaults.stream().anyMatch(c -> c.getName().equals("Work")));
        assertTrue(defaults.stream().anyMatch(c -> c.getName().equals("Personal")));
    }

    @Test
    void testSaveAndLoadTasks() {
        TaskRepository repo = new TaskRepository();
        Task t = new Task("Test Repo Task", "Details", "Work", "High", LocalDate.now());

        repo.save(List.of(t));
        List<Task> loaded = repo.load();

        assertNotNull(loaded);
        assertTrue(loaded.stream().anyMatch(task -> task.getTitle().equals("Test Repo Task")));
    }

    @Test
    void testSaveAndLoadCategories() {
        TaskRepository repo = new TaskRepository();
        Category custom = new Category("CustomType", "#885ebd", false);

        List<Category> toSave = List.of(
                new Category("Work", "#5d7ebd", true),
                new Category("Personal", "#ae7c39", true),
                custom
        );

        repo.saveCategories(toSave);
        List<Category> loaded = repo.loadCategories();

        assertNotNull(loaded);
        assertTrue(loaded.stream().anyMatch(c -> c.getName().equals("CustomType")));
    }
}
