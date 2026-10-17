package com.taskflow;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_DESC_LENGTH = 2000;
    private static final int MAX_CATEGORY_LENGTH = 50;

    private final String id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private LocalDate dueDate;
    private boolean completed;

    public Task(String title, String description, String category, String priority, LocalDate dueDate) {
        this.id = UUID.randomUUID().toString();
        setTitle(title);
        setDescription(description);
        setCategory(category);
        setPriority(priority);
        setDueDate(dueDate);
    }

    public String getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        this.title = sanitizeText(title.trim(), MAX_TITLE_LENGTH);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description == null ? "" : sanitizeText(description.trim(), MAX_DESC_LENGTH);
    }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = (category == null || category.trim().isEmpty()) ? "Work" : sanitizeText(category.trim(), MAX_CATEGORY_LENGTH);
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) {
        if (priority == null || (!priority.equalsIgnoreCase("High") && !priority.equalsIgnoreCase("Medium") && !priority.equalsIgnoreCase("Low"))) {
            this.priority = "Medium";
        } else {
            // Normalize casing
            if (priority.equalsIgnoreCase("High")) this.priority = "High";
            else if (priority.equalsIgnoreCase("Low")) this.priority = "Low";
            else this.priority = "Medium";
        }
    }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    private static String sanitizeText(String input, int maxLength) {
        if (input == null) return "";
        String cleaned = input.replaceAll("<[^>]*>", "")
                             .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        if (cleaned.length() > maxLength) {
            cleaned = cleaned.substring(0, maxLength);
        }
        return cleaned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
