# Taskflow — JavaFX Task Manager

Taskflow is a desktop task manager built with Java 25, JavaFX, and Maven. Use it to capture tasks, organize them into lists, and keep track of priorities and due dates in a focused graphical workspace.

The application opens in a 1240 × 800 window with a dark navigation sidebar and a light task workspace. The window can be resized.

## Features

- Create, edit, complete, and delete tasks. Tasks can include details, a list, a priority, and an optional due date.
- Start with the **Work** and **Personal** lists, or create custom lists with an accent color. Delete custom lists when no longer needed; their tasks are reassigned to **Work**.
- Browse **All tasks**, **Today**, **Upcoming**, **Completed**, or an individual list.
- Search task titles and details as you type.
- Sort the current results by due date (earliest first), priority (high first), or due date (latest first).
- See due dates marked as overdue or due today.
- Collapse the sidebar to an icon-only navigation rail; tooltips show each destination.
- Get an empty state with a shortcut to create a task when there are no matching results.
- Save tasks and lists locally between launches.

When no saved task file exists, Taskflow loads a few example tasks so the workspace is ready to explore. You can edit or delete them like any other task.

## Requirements

- Java 25 or later
- Maven 3.8 or later
- A desktop environment supported by JavaFX

The JavaFX Maven plugin resolves the JavaFX dependencies for the current platform. A graphical desktop session is required to display the application window.

## Run the application

From the project directory, run:

```bash
mvn clean javafx:run
```

Maven resolves dependencies, compiles the application, and opens Taskflow.

To run the unit tests, use:

```bash
mvn test
```

To create a packaged JAR, run:

```bash
mvn package
```

The JAR is written to `target/task-manager-1.0.0.jar`. Since JavaFX needs platform-specific runtime components, `mvn javafx:run` is the simplest way to launch the app.

## Data storage

Taskflow stores task and list data under the `.taskflow` directory in your home folder:

```text
~/.taskflow/tasks.dat
~/.taskflow/categories.dat
```

On Windows, `~` refers to your user profile directory. Changes are saved as you work and when the application closes. If no saved task file exists, the example tasks are loaded; if no saved list file exists, the default **Work** and **Personal** lists are loaded.

## Project structure

```text
src/main/java/com/taskflow/
  Task.java              Task data model
  Category.java          Task list data model
  TaskRepository.java    Local task and list loading and saving
  TaskManagerApp.java    JavaFX application and interface
src/main/resources/com/taskflow/
  theme.css              Application styling
src/test/java/com/taskflow/
  *Test.java             Model and repository unit tests
pom.xml                  Maven build, dependencies, and JavaFX configuration
```

## Technology

- Java 25
- JavaFX Controls 21.0.2
- Maven
- JUnit 5.10.2
