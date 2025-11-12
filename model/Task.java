package model;

import java.time.LocalDate;

/**
 * Represents an immutable task entity with an ID, title, description,
 * due date, and completion status.
 */
public final class Task {
    private final TaskId id;
    private final String title;
    private final LocalDate dueDate;
    private final boolean completed;

    /**
     * Constructs a new Task instance.
     *
     * @param id the unique identifier of the task; a new one is generated if null
     * @param title the title of the task (must not be null or blank)
     * @param description the description of the task (may be null)
     * @param dueDate the due date of the task (may be null)
     * @param completed the completion status of the task
     * @throws IllegalArgumentException if the title is null or blank
     */
    public Task(TaskId id, String title, LocalDate dueDate, boolean completed) {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("title required");
        this.id = (id == null ? new TaskId() : id);
        this.title = title;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    /**
     * Returns the unique identifier of this task.
     *
     * @return the task ID
     */
    public TaskId id() { return id; }

    /**
     * Returns the title of this task.
     *
     * @return the task title
     */
    public String title() { return title; }

    /**
     * Returns the due date of this task.
     *
     * @return the due date, or null if not set
     */
    public LocalDate dueDate() { return dueDate; }

    /**
     * Returns the completion status of this task.
     *
     * @return true if completed, false otherwise
     */
    public boolean completed() { return completed; }

    /**
     * Creates a new Task with the same properties but a different completion status.
     *
     * @param c the new completion status
     * @return a new Task instance with the updated completion flag
     */
    public Task withCompleted(boolean c) {
        return new Task(id, title, dueDate, c);
    }

    /**
     * Returns a string representation of this task,
     * including its status, title, and due date if available.
     *
     * @return a string representing the task
     */
    @Override
    public String toString() {
        String base = (completed ? "[✓] " : "[ ] ") + title;
        return dueDate == null ? base : base + " (" + dueDate + ")";
    }
}