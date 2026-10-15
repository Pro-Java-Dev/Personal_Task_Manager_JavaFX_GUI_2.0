package com.taskflow;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private final Path taskFile = Path.of(System.getProperty("user.home"), ".taskflow", "tasks.dat");
    private final Path categoryFile = Path.of(System.getProperty("user.home"), ".taskflow", "categories.dat");

    // Whitelist filter for safe ObjectInputStream deserialization including arrays
    private static final String ALLOWED_CLASSES_FILTER =
            "com.taskflow.**;" +
            "java.util.**;" +
            "java.time.**;" +
            "java.lang.**;" +
            "[*;" +
            "!*";

    @SuppressWarnings("unchecked")
    public List<Task> load() {
        if (Files.exists(taskFile)) {
            try (InputStream fis = Files.newInputStream(taskFile);
                 ObjectInputStream in = new ObjectInputStream(fis)) {
                in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(ALLOWED_CLASSES_FILTER));
                Object obj = in.readObject();
                if (obj instanceof List<?>) {
                    List<Task> result = new ArrayList<>();
                    for (Object item : (List<?>) obj) {
                        if (item instanceof Task task) {
                            result.add(task);
                        }
                    }
                    return result;
                }
            } catch (Exception ignored) { }
        }
        return starterTasks();
    }

    public void save(List<Task> tasks) {
        try {
            Files.createDirectories(taskFile.getParent());
            try (OutputStream fos = Files.newOutputStream(taskFile);
                 ObjectOutputStream out = new ObjectOutputStream(fos)) {
                out.writeObject(new ArrayList<>(tasks));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save tasks", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Category> loadCategories() {
        if (Files.exists(categoryFile)) {
            try (InputStream fis = Files.newInputStream(categoryFile);
                 ObjectInputStream in = new ObjectInputStream(fis)) {
                in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(ALLOWED_CLASSES_FILTER));
                Object obj = in.readObject();
                if (obj instanceof List<?>) {
                    List<Category> result = new ArrayList<>();
                    for (Object item : (List<?>) obj) {
                        if (item instanceof Category category) {
                            result.add(category);
                        }
                    }
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            } catch (Exception ignored) { }
        }
        return defaultCategories();
    }

    public void saveCategories(List<Category> categories) {
        try {
            Files.createDirectories(categoryFile.getParent());
            try (OutputStream fos = Files.newOutputStream(categoryFile);
                 ObjectOutputStream out = new ObjectOutputStream(fos)) {
                out.writeObject(new ArrayList<>(categories));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save categories", e);
        }
    }

    public List<Category> defaultCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Work", "#5d7ebd", true));
        categories.add(new Category("Personal", "#ae7c39", true));
        return categories;
    }

    private List<Task> starterTasks() {
        LocalDate today = LocalDate.now();
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Review Q3 product roadmap", "Align milestones with the team before the planning session.", "Work", "High", today));
        tasks.add(new Task("Send the launch notes", "Share the final release notes with the wider team.", "Work", "Medium", today.plusDays(1)));
        tasks.add(new Task("Book a table for Friday", "Somewhere relaxed, close to the office.", "Personal", "Low", today.plusDays(2)));
        tasks.add(new Task("Update portfolio case study", "Add the latest product screenshots and outcomes.", "Personal", "Medium", today.plusDays(4)));
        tasks.add(new Task("Prepare design handoff", "Check assets, spacing, and responsive states.", "Work", "High", today.plusDays(5)));
        return tasks;
    }
}
