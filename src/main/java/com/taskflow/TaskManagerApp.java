package com.taskflow;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskManagerApp extends Application {
    private final TaskRepository repository = new TaskRepository();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    private FilteredList<Task> filteredTasks;
    private SortedList<Task> sortedTasks;

    private final ListView<Task> taskList = new ListView<>();
    private final TextField searchField = new TextField();
    private final Label countLabel = new Label();
    private final Label greetingDate = new Label();
    private final ComboBox<String> sortBox = new ComboBox<>(FXCollections.observableArrayList("Due date", "Priority", "Newest"));

    private VBox sidebar;
    private VBox categoriesBox;
    private ScrollPane categoriesScrollPane;
    private HBox sidebarBrand;
    private Label sidebarBrandName;
    private Region sidebarBrandSpacer;
    private Button brandToggle;
    private VBox sidebarProfile;
    private Label profileTitle;
    private Label profileCopy;
    private Label listsCaption;
    private Button addCategoryBtn;

    private boolean sidebarCollapsed;
    private String currentFilter = "All tasks";
    private String currentCategory = "All";

    // Category Color Cache for high performance UI rendering
    private final Map<String, String> categoryColorCache = new HashMap<>();

    private final List<String> paletteColors = Arrays.asList(
            "#4c6cb3", // Blue
            "#b3742d", // Orange
            "#228b58", // Emerald Green
            "#8050b3", // Purple
            "#228285", // Teal
            "#b54e75", // Rose Pink
            "#b84343"  // Crimson
    );

    @Override
    public void start(Stage stage) {
        tasks.setAll(repository.load());
        categories.setAll(repository.loadCategories());
        updateCategoryColorCache();

        filteredTasks = new FilteredList<>(tasks, this::matchesFilterAndSearch);
        sortedTasks = new SortedList<>(filteredTasks, getComparator(sortBox.getValue()));

        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("app-shell");
        shell.setLeft(buildSidebar());
        shell.setCenter(buildMainContent());

        Scene scene = new Scene(shell, 1240, 800);
        scene.getStylesheets().add(getClass().getResource("/com/taskflow/theme.css").toExternalForm());
        stage.setTitle("Taskflow — Task Manager");
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.setScene(scene);

        // Keyboard Shortcuts: Ctrl+N (New Task), Ctrl+F (Search)
        KeyCombination newShortcut = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        KeyCombination searchShortcut = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        scene.setOnKeyPressed(event -> {
            if (newShortcut.match(event)) {
                editTask(null);
                event.consume();
            } else if (searchShortcut.match(event)) {
                searchField.requestFocus();
                searchField.selectAll();
                event.consume();
            }
        });

        stage.show();
        refresh();
    }

    private void updateCategoryColorCache() {
        categoryColorCache.clear();
        for (Category cat : categories) {
            categoryColorCache.put(cat.getName().toLowerCase(Locale.ROOT), cat.getColorHex());
        }
    }

    private VBox buildSidebar() {
        sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);

        brandToggle = new Button("✦");
        brandToggle.getStyleClass().add("brand-mark");
        brandToggle.setTooltip(new Tooltip("Collapse menu"));
        brandToggle.setOnAction(e -> setSidebarCollapsed(!sidebarCollapsed));

        sidebarBrandName = new Label("taskflow");
        sidebarBrandSpacer = new Region();
        HBox.setHgrow(sidebarBrandSpacer, Priority.ALWAYS);

        sidebarBrand = new HBox(11, brandToggle, sidebarBrandName, sidebarBrandSpacer);
        sidebarBrand.getStyleClass().add("brand");
        sidebarBrand.setMaxWidth(Double.MAX_VALUE);
        sidebarBrandName.getStyleClass().add("brand-name");
        sidebar.getChildren().add(sidebarBrand);

        Label workspace = new Label("WORKSPACE");
        workspace.getStyleClass().add("section-caption");
        sidebar.getChildren().add(workspace);
        sidebar.getChildren().add(navButton("▦", "All tasks", "All tasks"));
        sidebar.getChildren().add(navButton("◷", "Today", "Today"));
        sidebar.getChildren().add(navButton("▤", "Upcoming", "Upcoming"));
        sidebar.getChildren().add(navButton("✓", "Completed", "Completed"));

        HBox listsHeader = new HBox();
        listsHeader.setAlignment(Pos.CENTER_LEFT);
        listsHeader.getStyleClass().add("section-header");
        listsCaption = new Label("YOUR LISTS");
        listsCaption.getStyleClass().add("section-caption");
        Region flex = new Region();
        HBox.setHgrow(flex, Priority.ALWAYS);

        addCategoryBtn = new Button("+");
        addCategoryBtn.getStyleClass().add("add-category-btn");
        addCategoryBtn.setTooltip(new Tooltip("Create new task type"));
        addCategoryBtn.setOnAction(e -> createCategoryDialog());

        listsHeader.getChildren().addAll(listsCaption, flex, addCategoryBtn);
        sidebar.getChildren().add(listsHeader);

        categoriesBox = new VBox(2);
        categoriesScrollPane = new ScrollPane(categoriesBox);
        categoriesScrollPane.setFitToWidth(true);
        categoriesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        categoriesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        categoriesScrollPane.getStyleClass().add("categories-scroll");
        VBox.setVgrow(categoriesScrollPane, Priority.ALWAYS);

        sidebar.getChildren().add(categoriesScrollPane);
        rebuildCategoriesNav();

        profileTitle = new Label("YOUR SPACE");
        profileTitle.getStyleClass().add("sidebar-footer-title");
        profileCopy = new Label("Organized, clear, and focused.");
        profileCopy.getStyleClass().add("sidebar-footer-copy");
        sidebarProfile = new VBox(3, profileTitle, profileCopy);
        sidebarProfile.getStyleClass().add("sidebar-footer");
        sidebar.getChildren().add(sidebarProfile);

        return sidebar;
    }

    private Button navButton(String icon, String text, String filter) {
        Button button = new Button();
        button.getStyleClass().add("nav-button");
        configureNavButton(button, icon, text, null);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> {
            currentFilter = filter;
            currentCategory = "All";
            refresh();
            updateActiveNav();
        });
        button.setUserData(filter);
        return button;
    }

    private void rebuildCategoriesNav() {
        categoriesBox.getChildren().clear();
        for (Category cat : categories) {
            Button btn = createCategoryNavButton(cat);
            categoriesBox.getChildren().add(btn);
        }
    }

    private Button createCategoryNavButton(Category cat) {
        Button button = new Button();
        button.getStyleClass().addAll("nav-button", "category-nav");

        Circle dot = new Circle(4);
        try {
            dot.setFill(Color.web(cat.getColorHex()));
        } catch (Exception e) {
            dot.setFill(Color.web("#228b58"));
        }

        Label caption = new Label(cat.getName());
        caption.getStyleClass().add("nav-label");

        HBox graphic = new HBox(10, dot, caption);
        graphic.getStyleClass().add("nav-content");
        graphic.setAlignment(Pos.CENTER_LEFT);

        button.setGraphic(graphic);
        button.setTooltip(new Tooltip(cat.getName() + (cat.isDefaultCategory() ? " (Default)" : " (Custom Task Type)")));
        button.setUserData("category:" + cat.getName());
        button.getProperties().put("nav-label", caption);
        button.getProperties().put("category", cat);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(sidebarCollapsed ? Pos.CENTER : Pos.CENTER_LEFT);

        button.setOnAction(e -> {
            currentCategory = cat.getName();
            currentFilter = "All tasks";
            refresh();
            updateActiveNav();
        });

        // Context menu for category management / deletion
        ContextMenu cm = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete list \"" + cat.getName() + "\"");
        deleteItem.setOnAction(e -> deleteCategory(cat));
        cm.getItems().add(deleteItem);

        if (!cat.isDefaultCategory()) {
            button.setContextMenu(cm);
        } else {
            MenuItem defaultInfo = new MenuItem("Default List");
            defaultInfo.setDisable(true);
            cm.getItems().add(defaultInfo);
            button.setContextMenu(cm);
        }

        return button;
    }

    private void createCategoryDialog() {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("New Task Type");
        dialog.initOwner(sidebar.getScene().getWindow());
        dialog.setHeaderText("Create a custom list / task type");

        ButtonType createType = new ButtonType("Create List", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Finance, Marketing, Side Project");

        ToggleGroup colorGroup = new ToggleGroup();
        HBox colorPickerBox = new HBox(10);
        colorPickerBox.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < paletteColors.size(); i++) {
            String hex = paletteColors.get(i);
            RadioButton rb = new RadioButton();
            rb.setToggleGroup(colorGroup);
            rb.setUserData(hex);
            rb.setStyle("-fx-mark-color: " + hex + ";");
            if (i == 0) rb.setSelected(true);
            colorPickerBox.getChildren().add(rb);
        }

        VBox content = new VBox(12);
        content.setPadding(new Insets(10, 0, 10, 0));
        content.getChildren().addAll(
                new Label("Task Type Name"),
                nameField,
                new Label("Accent Color"),
                colorPickerBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStyleClass().add("task-dialog");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/taskflow/theme.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(420);

        dialog.getDialogPane().lookupButton(createType).disableProperty().bind(nameField.textProperty().isEmpty());

        dialog.setResultConverter(btn -> {
            if (btn != createType) return null;
            String name = nameField.getText().trim();
            RadioButton selectedRb = (RadioButton) colorGroup.getSelectedToggle();
            String color = selectedRb != null ? (String) selectedRb.getUserData() : paletteColors.get(0);
            return new Category(name, color, false);
        });

        dialog.showAndWait().ifPresent(cat -> {
            boolean exists = categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(cat.getName()));
            if (exists) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "A list with this name already exists.", ButtonType.OK);
                alert.initOwner(sidebar.getScene().getWindow());
                alert.showAndWait();
                return;
            }
            categories.add(cat);
            updateCategoryColorCache();
            repository.saveCategories(categories);
            rebuildCategoriesNav();
            currentCategory = cat.getName();
            currentFilter = "All tasks";
            refresh();
            updateActiveNav();
        });
    }

    private void deleteCategory(Category category) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Task Type");
        confirm.setHeaderText("Delete list \"" + category.getName() + "\"?");
        confirm.setContentText("Tasks under this list will be reassigned to \"Work\".");
        confirm.initOwner(sidebar.getScene().getWindow());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            categories.remove(category);
            updateCategoryColorCache();
            repository.saveCategories(categories);

            // Reassign affected tasks
            for (Task task : tasks) {
                if (task.getCategory().equalsIgnoreCase(category.getName())) {
                    task.setCategory("Work");
                }
            }
            repository.save(tasks);

            if (currentCategory.equalsIgnoreCase(category.getName())) {
                currentCategory = "All";
                currentFilter = "All tasks";
            }

            rebuildCategoriesNav();
            refresh();
            updateActiveNav();
        }
    }

    private void configureNavButton(Button button, String icon, String text, String iconStyle) {
        Label glyph = new Label(icon);
        glyph.getStyleClass().add("nav-icon");
        if (iconStyle != null) glyph.getStyleClass().add(iconStyle);
        Label caption = new Label(text);
        caption.getStyleClass().add("nav-label");
        HBox graphic = new HBox(12, glyph, caption);
        graphic.getStyleClass().add("nav-content");
        graphic.setAlignment(Pos.CENTER_LEFT);
        button.setGraphic(graphic);
        button.setTooltip(new Tooltip(text));
        button.getProperties().put("nav-label", caption);
    }

    private void setSidebarCollapsed(boolean collapsed) {
        sidebarCollapsed = collapsed;
        sidebar.setPrefWidth(collapsed ? 88 : 250);
        sidebar.setMinWidth(collapsed ? 88 : 250);
        sidebar.setMaxWidth(collapsed ? 88 : 250);
        if (collapsed) sidebar.getStyleClass().add("sidebar-collapsed");
        else sidebar.getStyleClass().remove("sidebar-collapsed");

        sidebarBrand.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
        sidebarBrandName.setVisible(!collapsed);
        sidebarBrandName.setManaged(!collapsed);
        sidebarBrandSpacer.setVisible(!collapsed);
        sidebarBrandSpacer.setManaged(!collapsed);
        brandToggle.setTooltip(new Tooltip(collapsed ? "Expand menu" : "Collapse menu"));

        listsCaption.setVisible(!collapsed);
        listsCaption.setManaged(!collapsed);
        addCategoryBtn.setVisible(!collapsed);
        addCategoryBtn.setManaged(!collapsed);

        profileTitle.setVisible(!collapsed);
        profileTitle.setManaged(!collapsed);
        profileCopy.setVisible(!collapsed);
        profileCopy.setManaged(!collapsed);
        sidebarProfile.setVisible(!collapsed);
        sidebarProfile.setManaged(!collapsed);

        for (var node : sidebar.getChildren()) {
            if (node instanceof Label label && label.getStyleClass().contains("section-caption")) {
                label.setVisible(!collapsed);
                label.setManaged(!collapsed);
            } else if (node instanceof Button button) {
                Label caption = (Label) button.getProperties().get("nav-label");
                if (caption != null) {
                    caption.setVisible(!collapsed);
                    caption.setManaged(!collapsed);
                }
                button.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
                HBox graphic = (HBox) button.getGraphic();
                graphic.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
            }
        }

        for (var node : categoriesBox.getChildren()) {
            if (node instanceof Button button) {
                Label caption = (Label) button.getProperties().get("nav-label");
                if (caption != null) {
                    caption.setVisible(!collapsed);
                    caption.setManaged(!collapsed);
                }
                button.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
                HBox graphic = (HBox) button.getGraphic();
                graphic.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
            }
        }
    }

    private void updateActiveNav() {
        for (var node : sidebar.getChildren()) {
            if (node instanceof Button b) {
                boolean active = b.getUserData() != null && b.getUserData().equals(currentFilter) && currentCategory.equals("All");
                b.getStyleClass().remove("nav-active");
                if (active) b.getStyleClass().add("nav-active");
            }
        }

        for (var node : categoriesBox.getChildren()) {
            if (node instanceof Button b) {
                boolean active = b.getUserData() != null && b.getUserData().equals("category:" + currentCategory);
                b.getStyleClass().remove("nav-active");
                if (active) b.getStyleClass().add("nav-active");
            }
        }
    }

    private VBox buildMainContent() {
        VBox main = new VBox(0);
        main.getStyleClass().add("main-content");

        HBox topbar = new HBox();
        topbar.setAlignment(Pos.CENTER_RIGHT);
        topbar.getStyleClass().add("topbar");
        Label date = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)));
        date.getStyleClass().add("top-date");
        Label avatar = new Label("TF");
        avatar.getStyleClass().add("avatar");
        topbar.getChildren().addAll(date, avatar);

        VBox heading = new VBox(7);
        greetingDate.getStyleClass().add("eyebrow");
        greetingDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)).toUpperCase(Locale.ROOT));
        Label title = new Label("Make room for\nwhat matters.");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("A clear mind starts with a clear plan.");
        subtitle.getStyleClass().add("page-subtitle");
        heading.getChildren().addAll(greetingDate, title, subtitle);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.getStyleClass().add("toolbar");
        searchField.setPromptText("⌕   Search tasks...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((o, old, value) -> refresh());

        Button add = new Button("+  New task");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editTask(null));

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        actions.getChildren().addAll(searchField, push, add);

        HBox listHead = new HBox();
        listHead.setAlignment(Pos.CENTER_LEFT);
        listHead.getStyleClass().add("list-heading");
        Label listTitle = new Label("Your tasks");
        listTitle.getStyleClass().add("list-title");
        countLabel.getStyleClass().add("task-count");
        Region headPush = new Region();
        HBox.setHgrow(headPush, Priority.ALWAYS);

        sortBox.setValue("Due date");
        sortBox.getStyleClass().add("sort-box");
        sortBox.valueProperty().addListener((o, old, value) -> {
            sortedTasks.setComparator(getComparator(value));
            refreshCountAndNav();
        });
        Label sortLabel = new Label("Sort by");
        sortLabel.getStyleClass().add("sort-label");
        listHead.getChildren().addAll(listTitle, countLabel, headPush, sortLabel, sortBox);

        taskList.setItems(sortedTasks);
        taskList.setCellFactory(list -> new TaskCell());
        taskList.getStyleClass().add("task-list");
        VBox.setVgrow(taskList, Priority.ALWAYS);

        Label emptyIcon = new Label("✓");
        emptyIcon.getStyleClass().add("empty-icon");
        Label emptyTitle = new Label("Nothing on your list");
        emptyTitle.getStyleClass().add("empty-title");
        Label emptyMessage = new Label("A little breathing room. Add a task whenever you're ready.");
        emptyMessage.getStyleClass().add("empty-message");
        Button emptyAdd = new Button("+  Create a task");
        emptyAdd.getStyleClass().add("primary-button");
        emptyAdd.setOnAction(e -> editTask(null));

        VBox emptyState = new VBox(11, emptyIcon, emptyTitle, emptyMessage, emptyAdd);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);
        taskList.setPlaceholder(emptyState);

        VBox.setMargin(taskList, new Insets(0, 0, 0, 0));
        main.getChildren().addAll(topbar, heading, actions, listHead, taskList);
        return main;
    }

    private void refresh() {
        if (filteredTasks != null) {
            filteredTasks.setPredicate(this::matchesFilterAndSearch);
            refreshCountAndNav();
        }
    }

    private void refreshCountAndNav() {
        if (taskList.getScene() != null && filteredTasks != null) {
            int size = filteredTasks.size();
            countLabel.setText(size + (size == 1 ? " task" : " tasks"));
            updateActiveNav();
        }
    }

    private boolean matchesFilterAndSearch(Task task) {
        if (task == null) return false;
        if (!currentCategory.equals("All") && !task.getCategory().equalsIgnoreCase(currentCategory)) return false;

        boolean matchesFilter = switch (currentFilter) {
            case "Today" -> !task.isCompleted() && task.getDueDate() != null && task.getDueDate().equals(LocalDate.now());
            case "Upcoming" -> !task.isCompleted() && task.getDueDate() != null && task.getDueDate().isAfter(LocalDate.now());
            case "Completed" -> task.isCompleted();
            default -> !task.isCompleted();
        };

        if (!matchesFilter) return false;

        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        return q.isEmpty() || task.getTitle().toLowerCase(Locale.ROOT).contains(q) || task.getDescription().toLowerCase(Locale.ROOT).contains(q);
    }

    private Comparator<Task> getComparator(String sortOption) {
        if (sortOption == null) sortOption = "Due date";
        return switch (sortOption) {
            case "Priority" -> Comparator.comparingInt((Task t) -> priorityRank(t.getPriority())).thenComparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "Newest" -> Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private int priorityRank(String priority) {
        return switch (priority) {
            case "High" -> 0;
            case "Medium" -> 1;
            default -> 2;
        };
    }

    private void editTask(Task existing) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create a task" : "Edit task");
        dialog.initOwner(taskList.getScene().getWindow());
        dialog.setHeaderText(existing == null ? "A small step toward a clearer day." : "Make a quick update.");

        ButtonType saveType = new ButtonType(existing == null ? "Create task" : "Save changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField title = new TextField(existing == null ? "" : existing.getTitle());
        title.setPromptText("e.g. Plan the week");

        TextArea description = new TextArea(existing == null ? "" : existing.getDescription());
        description.setPromptText("Add a few details...");
        description.setPrefRowCount(3);
        description.setWrapText(true);

        List<String> categoryNames = categories.stream().map(Category::getName).toList();
        ComboBox<String> category = new ComboBox<>(FXCollections.observableArrayList(categoryNames));
        category.setValue(existing == null ? (categories.isEmpty() ? "Work" : categories.get(0).getName()) : existing.getCategory());

        ComboBox<String> priority = new ComboBox<>(FXCollections.observableArrayList("Low", "Medium", "High"));
        priority.setValue(existing == null ? "Medium" : existing.getPriority());

        DatePicker due = new DatePicker(existing == null ? LocalDate.now() : existing.getDueDate());
        due.setPromptText("No due date");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("task-form");
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(12, 2, 4, 2));

        grid.add(new Label("Task name"), 0, 0);
        grid.add(title, 1, 0);
        grid.add(new Label("Details"), 0, 1);
        grid.add(description, 1, 1);
        grid.add(new Label("Task Type"), 0, 2);
        grid.add(category, 1, 2);
        grid.add(new Label("Priority"), 0, 3);
        grid.add(priority, 1, 3);
        grid.add(new Label("Due date"), 0, 4);
        grid.add(due, 1, 4);

        ColumnConstraints first = new ColumnConstraints();
        first.setMinWidth(80);
        ColumnConstraints second = new ColumnConstraints();
        second.setHgrow(Priority.ALWAYS);
        second.setFillWidth(true);
        second.setPrefWidth(340);

        grid.getColumnConstraints().addAll(first, second);

        title.setMaxWidth(Double.MAX_VALUE);
        description.setMaxWidth(Double.MAX_VALUE);
        category.setMaxWidth(Double.MAX_VALUE);
        priority.setMaxWidth(Double.MAX_VALUE);
        due.setMaxWidth(Double.MAX_VALUE);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStyleClass().add("task-dialog");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/taskflow/theme.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(540);

        dialog.getDialogPane().lookupButton(saveType).disableProperty().bind(title.textProperty().isEmpty());

        dialog.setResultConverter(button -> {
            if (button != saveType) return null;
            if (existing == null) return new Task(title.getText().trim(), description.getText().trim(), category.getValue(), priority.getValue(), due.getValue());
            existing.setTitle(title.getText().trim());
            existing.setDescription(description.getText().trim());
            existing.setCategory(category.getValue());
            existing.setPriority(priority.getValue());
            existing.setDueDate(due.getValue());
            return existing;
        });

        dialog.showAndWait().ifPresent(task -> {
            if (!tasks.contains(task)) tasks.add(task);
            persist();
            refresh();
        });
    }

    private void persist() {
        repository.save(tasks);
        repository.saveCategories(categories);
    }

    /**
     * Highly optimized TaskCell class.
     * Reuses Node hierarchies across scrolling/virtualization instead of re-instantiating.
     */
    private class TaskCell extends ListCell<Task> {
        private final HBox card = new HBox(14);
        private final CheckBox done = new CheckBox();
        private final VBox content = new VBox(6);
        private final Label title = new Label();
        private final Label description = new Label();
        private final HBox meta = new HBox(8);
        private final Label categoryPill = new Label();
        private final Label priorityPill = new Label();

        private final VBox due = new VBox(3);
        private final Label dueLabel = new Label();
        private final Label dueHint = new Label();

        private final Button edit = new Button("···");

        private Task currentTask;

        public TaskCell() {
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("task-card");

            done.getStyleClass().add("task-check");
            done.setOnAction(e -> {
                if (currentTask != null) {
                    currentTask.setCompleted(done.isSelected());
                    persist();
                    refresh();
                }
            });

            HBox.setHgrow(content, Priority.ALWAYS);
            title.getStyleClass().add("task-name");
            description.getStyleClass().add("task-description");

            meta.setAlignment(Pos.CENTER_LEFT);
            categoryPill.getStyleClass().add("category-pill");
            priorityPill.getStyleClass().add("priority-pill");
            meta.getChildren().addAll(categoryPill, priorityPill);

            content.getChildren().addAll(title, description, meta);

            due.setAlignment(Pos.CENTER_RIGHT);
            due.getStyleClass().add("due-box");
            dueLabel.getStyleClass().add("due-date");
            dueHint.getStyleClass().add("due-hint");
            due.getChildren().addAll(dueLabel, dueHint);

            edit.getStyleClass().add("more-button");
            edit.setOnAction(e -> {
                if (currentTask != null) {
                    MenuItem editItem = new MenuItem("Edit task");
                    editItem.setOnAction(ev -> editTask(currentTask));
                    MenuItem deleteItem = new MenuItem("Delete task");
                    deleteItem.setOnAction(ev -> {
                        tasks.remove(currentTask);
                        persist();
                        refresh();
                    });
                    new ContextMenu(editItem, deleteItem).show(edit, javafx.geometry.Side.BOTTOM, 0, 0);
                }
            });

            card.getChildren().addAll(done, content, due, edit);
        }

        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            this.currentTask = task;

            if (empty || task == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            // Update state
            done.setSelected(task.isCompleted());

            title.setText(task.getTitle());
            title.getStyleClass().remove("task-done");
            if (task.isCompleted()) {
                title.getStyleClass().add("task-done");
            }

            description.setText(task.getDescription().isBlank() ? "No details added" : task.getDescription());

            // Category Pill optimization
            updateCategoryPill(task.getCategory());

            // Priority Pill optimization
            String prio = task.getPriority().toLowerCase(Locale.ROOT);
            priorityPill.setText("●  " + task.getPriority());
            priorityPill.getStyleClass().setAll("label", "priority-pill", "priority-" + prio);

            // Due Date optimization
            LocalDate dueDateVal = task.getDueDate();
            if (dueDateVal == null) {
                dueLabel.setText("No date");
                dueHint.setText("");
                dueHint.getStyleClass().setAll("label", "due-hint");
            } else {
                dueLabel.setText(dueDateVal.format(DateTimeFormatter.ofPattern("MMM d")));
                boolean isOverdue = dueDateVal.isBefore(LocalDate.now()) && !task.isCompleted();
                boolean isToday = dueDateVal.equals(LocalDate.now());

                if (isOverdue) {
                    dueHint.setText("OVERDUE");
                    dueHint.getStyleClass().setAll("label", "due-hint", "overdue");
                } else if (isToday) {
                    dueHint.setText("TODAY");
                    dueHint.getStyleClass().setAll("label", "due-hint");
                } else {
                    dueHint.setText("DUE DATE");
                    dueHint.getStyleClass().setAll("label", "due-hint");
                }
            }

            setGraphic(card);
        }

        private void updateCategoryPill(String categoryName) {
            categoryPill.setText(categoryName);
            categoryPill.getStyleClass().setAll("label", "category-pill");

            String hex = categoryColorCache.get(categoryName.toLowerCase(Locale.ROOT));
            if (hex != null) {
                categoryPill.setStyle("-fx-background-color: " + hex + "1A; -fx-text-fill: " + hex + "; -fx-font-weight: 600;");
            } else if (categoryName.equalsIgnoreCase("Work")) {
                categoryPill.getStyleClass().add("work-pill");
                categoryPill.setStyle(null);
            } else if (categoryName.equalsIgnoreCase("Personal")) {
                categoryPill.getStyleClass().add("personal-pill");
                categoryPill.setStyle(null);
            } else {
                categoryPill.setStyle("-fx-background-color: #e4ebe6; -fx-text-fill: #5a6d63; -fx-font-weight: 600;");
            }
        }
    }

    @Override
    public void stop() {
        persist();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
