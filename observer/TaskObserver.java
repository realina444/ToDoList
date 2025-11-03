package observer;

/**
 * Interface used to observe changes in the task list.
 * 
 * Classes that implement this interface will be notified automatically
 * when tasks are added, edited, or deleted from the TaskStore.
 */
public interface TaskObserver {

    /**
     * This method runs automatically whenever the task list changes.
     */
    void onTasksChanged();
}
