package com.taskflow;

import java.io.Serializable;
import java.util.Objects;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_NAME_LENGTH = 50;

    private String name;
    private String colorHex;
    private boolean defaultCategory;

    public Category(String name, String colorHex, boolean defaultCategory) {
        setName(name);
        this.colorHex = sanitizeColor(colorHex);
        this.defaultCategory = defaultCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        this.name = sanitizeText(name.trim(), MAX_NAME_LENGTH);
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = sanitizeColor(colorHex);
    }

    public boolean isDefaultCategory() {
        return defaultCategory;
    }

    public void setDefaultCategory(boolean defaultCategory) {
        this.defaultCategory = defaultCategory;
    }

    private static String sanitizeText(String input, int maxLength) {
        if (input == null) return "";
        String cleaned = input.replaceAll("<[^>]*>", "")
                             .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                             .trim();
        if (cleaned.length() > maxLength) {
            cleaned = cleaned.substring(0, maxLength);
        }
        return cleaned;
    }

    private static String sanitizeColor(String color) {
        if (color != null && color.matches("^#[0-9a-fA-F]{6}$")) {
            return color;
        }
        return "#39815d";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
