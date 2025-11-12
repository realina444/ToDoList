package controller;

import model.Task;
import model.TaskId;
import model.TaskStore;

import java.time.LocalDate;

/**
 * Handles the interaction between the View and the Model.
 * Provides methods for adding, deleting, editing, and updating tasks.
 */
public class TaskController {
    private final TaskStore store;

    /**
     * Creates a TaskController connected to the given TaskStore.
     *
     * @param store the TaskStore instance that manages all tasks
     */
    public TaskController(TaskStore store) { 
        this.store = store; 
    }

    /**
     * Adds a new task to the TaskStore.
     * The task requires a title; description and due date are optional.
     *
     * @param title the title of the new task (required)
     * @param  date of the task (optional)
     * @throws IllegalArgumentException if the title is null or blank
     */
    
    public void addTask(String title, LocalDate Date) {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Title is required");
        Task t = new Task(new TaskId(), title.trim(), Date, false);
        store.addTask(t);
    }

    /**
     * Deletes a task from the TaskStore.
     *
     * @param id the unique identifier (TaskId) of the task to delete
     */
    public void deleteTask(TaskId id) { 
        store.removeTask(id); 
    }

    /**
     * Toggles the completion status of a task.
     * Marks it as completed if not completed, or reopens it if already completed.
     *
     * @param id the unique identifier (TaskId) of the task to toggle
     */
    public void toggleCompleted(TaskId id) { 
        store.toggleCompleted(id); 
    }

    /**
     * Updates an existing task with new information.
     * Used for editing a task’s title or date.
     * @param updated the updated Task object containing new details
     */
    public void editTask(Task updated) { 
        store.updateTask(updated); 
    }
}