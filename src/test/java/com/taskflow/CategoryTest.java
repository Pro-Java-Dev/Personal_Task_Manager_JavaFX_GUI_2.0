package com.taskflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void testCategoryCreationAndGetters() {
        Category cat = new Category("Marketing", "#5d7ebd", false);

        assertEquals("Marketing", cat.getName());
        assertEquals("#5d7ebd", cat.getColorHex());
        assertFalse(cat.isDefaultCategory());
    }

    @Test
    void testInvalidCategoryNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Category("", "#5d7ebd", false));
        assertThrows(IllegalArgumentException.class, () -> new Category("   ", "#5d7ebd", false));
        assertThrows(IllegalArgumentException.class, () -> new Category(null, "#5d7ebd", false));
    }

    @Test
    void testColorSanitizationFallback() {
        Category cat = new Category("Design", "invalidColor", false);
        assertEquals("#39815d", cat.getColorHex());

        cat.setColorHex("#123456");
        assertEquals("#123456", cat.getColorHex());
    }

    @Test
    void testCategoryEqualsAndHashCode() {
        Category cat1 = new Category("Work", "#111111", true);
        Category cat2 = new Category("Work", "#222222", false);

        assertEquals(cat1, cat2);
        assertEquals(cat1.hashCode(), cat2.hashCode());
    }
}
