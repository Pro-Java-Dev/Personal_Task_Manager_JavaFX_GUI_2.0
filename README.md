# Taskflow — JavaFX Task Manager

Taskflow is a desktop task manager built with Java 25, JavaFX, and Maven. It provides a focused graphical workspace for capturing tasks, organizing them into lists, and keeping track of due dates and priorities.

The interface uses a dark evergreen navigation sidebar and a spacious, light workspace with refined task cards and controls. The window opens at 1240 × 800 pixels and can be resized.

## Features

- Create, edit, and delete tasks.
- Add a task description, choose a **Work** or **Personal** list, set **Low**, **Medium**, or **High** priority, and select a due date.
- Mark tasks as complete and view them in the **Completed** section.
- Filter tasks by **Today**, **Upcoming**, or category.
- Click the Taskflow mark to collapse the sidebar into an icon-only navigation rail; destination tooltips remain available.
- Search task titles and descriptions as you type.
- Sort the current results by due date, priority, or newest due date.
- See due date labels, including overdue tasks and tasks due today.
- Get a clear empty state with a quick create action when a view has no matching tasks.
- Save tasks locally between launches.

The first launch starts with a few example tasks so the workspace is ready to explore. You can edit or delete these like any other task.

## Requirements

- Java 25 or later
- Maven 3.8 or later
- A desktop environment supported by JavaFX

The Maven JavaFX plugin downloads and configures the JavaFX dependencies for the current platform. A graphical desktop session is needed to display the application window.

## Run the application

From the project directory, run:

```bash
mvn clean javafx:run
```

Maven resolves the dependencies, compiles the application, and opens the Taskflow window.

To create a packaged JAR, run:

```bash
mvn package
```

The JAR is written to `target/task-manager-1.0.0.jar`. Because JavaFX applications need platform-specific runtime components, launching through `mvn javafx:run` is the simplest way to run the app.

## Where tasks are stored

Taskflow stores its task data in a file in your home directory:

```text
~/.taskflow/tasks.dat
```

On Windows, `~` refers to your user profile directory. The app loads this file at startup and saves changes as you make them and when the app closes. If no saved task file exists, the example tasks are loaded.

## Project structure

```text
src/main/java/com/taskflow/
  Task.java              Task data model
  TaskRepository.java    Local task loading and saving
  TaskManagerApp.java    JavaFX application and interface
src/main/resources/com/taskflow/
  theme.css              Application styling
pom.xml                  Maven build and JavaFX configuration
```

## Technology

- Java 25
- JavaFX Controls 21.0.2
- Maven
