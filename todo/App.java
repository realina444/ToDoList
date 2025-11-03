package todo;

import controller.TaskController;
import model.TaskStore;
import view.TaskListView;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main class of the To-Do List application.
 * 
 * This class is responsible for:
 * 1. Loading tasks from the saved file.
 * 2. Displaying the main application window.
 * 3. Handling auto-save and saving data when the app closes.
 */
public class App {

    /** File path used to save and load all tasks. */
    private static final Path DATA_PATH = Paths.get("tasks.json");

    /** Delay before the first auto-save starts (in milliseconds). */
    private static final int AUTOSAVE_INITIAL_DELAY_MS = 2000;

    /** Time interval between automatic saves (in milliseconds). */
    private static final int AUTOSAVE_PERIOD_MS = 2000;

    /**
     * The main method that starts the To-Do List program.
     * It loads tasks, shows the user interface, and starts auto-save.
     */
    public static void main(String[] args) {
        // Create a single shared TaskStore and its controller
        TaskStore store = TaskStore.getInstance();
        TaskController controller = new TaskController(store);

        // Load saved tasks from file
        store.loadFromJson(DATA_PATH);

        // --- Create and show the main window ---
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("To-Do List");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.setLayout(new BorderLayout());
            f.add(new TaskListView(controller), BorderLayout.CENTER);
            f.setSize(500, 600);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });

        // --- Auto-save timer ---
        Timer autosave = new Timer(true);
        autosave.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                store.saveToJson(DATA_PATH);
            }
        }, AUTOSAVE_INITIAL_DELAY_MS, AUTOSAVE_PERIOD_MS);

        // --- Save data when the program closes ---
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            autosave.cancel();
            store.saveToJson(DATA_PATH);
        }));
    }
}
