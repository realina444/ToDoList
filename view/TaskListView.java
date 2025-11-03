package view;

import controller.TaskController;
import model.Task;
import model.TaskStore;
import observer.TaskObserver;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * The TaskListView class represents the main user interface (UI)
 * for displaying and managing tasks in the To-Do List app.
 * 
 * It shows the list of tasks, search bar, filter options, and buttons
 * to add, delete, toggle, or edit tasks.
 */
public class TaskListView extends JPanel implements TaskObserver {

    /** Controller used to handle user actions like add, delete, and edit. */
    private final TaskController controller;

    /** Shared TaskStore instance that contains all tasks. */
    private final TaskStore store;

    /** List model and UI list that display all tasks. */
    private final DefaultListModel<Task> listModel = new DefaultListModel<>();
    private final JList<Task> list = new JList<>(listModel);

    /** Buttons for user actions. */
    private final JButton addBtn = new JButton("Add");
    private final JButton delBtn = new JButton("Delete");
    private final JButton toggleBtn = new JButton("Toggle");
    private final JButton editBtn = new JButton("Edit");

    /** Search field and filter options for tasks. */
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<TaskStore.Filter> filterBox =
            new JComboBox<>(TaskStore.Filter.values());

    /** Status label used to show small updates (e.g. "Task added"). */
    private final JLabel status = new JLabel("");

    /**
     * Creates the task list view and connects it with the controller and store.
     * It also sets up the layout and event listeners for the UI.
     *
     * @param controller The TaskController that handles logic for tasks.
     */
    public TaskListView(TaskController controller){
        this.controller = controller;
        this.store = TaskStore.getInstance();

        setLayout(new BorderLayout());

        // --- Top panel: buttons, filters, and search bar ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(addBtn);
        top.add(delBtn);
        top.add(toggleBtn);
        top.add(editBtn);
        top.add(new JLabel("View:"));
        top.add(filterBox);
        top.add(new JLabel("Search:"));
        top.add(searchField);

        // Add all UI parts to the layout
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new TodayRenderer());

        store.addObserver(this);

        reload();
        wireActions();
        wireSearch();
        wireFilter();
    }

    /**
     * Connects buttons (Add, Delete, Toggle, Edit) to their actions.
     */
    private void wireActions() {
        // Add new task
        addBtn.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(this, "Task title:", "New Task", JOptionPane.PLAIN_MESSAGE);
            if (title == null) return;
            title = title.trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                controller.addTask(title, "", LocalDate.now());
                status.setText("Added: " + title);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot add task", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Delete selected task
        delBtn.addActionListener(e -> {
            Task sel = list.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(this, "Select a task first."); return; }
            controller.deleteTask(sel.id());
            status.setText("Deleted: " + sel.title());
        });

        // Toggle task completion
        toggleBtn.addActionListener(e -> {
            Task sel = list.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(this, "Select a task first."); return; }
            boolean willBeCompleted = !sel.completed();
            controller.toggleCompleted(sel.id());
            status.setText(willBeCompleted ? ("Completed: " + sel.title()) : ("Reopened: " + sel.title()));
        });

        // Edit selected task title
        editBtn.addActionListener(e -> {
            Task sel = list.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(this, "Select a task first."); return; }
            String newTitle = (String) JOptionPane.showInputDialog(
                    this, "Edit title:", "Edit Task",
                    JOptionPane.PLAIN_MESSAGE, null, null, sel.title()
            );
            if (newTitle == null) return;
            newTitle = newTitle.trim();
            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Task updated = new Task(sel.id(), newTitle, sel.description(), sel.dueDate(), sel.completed());
                controller.editTask(updated);
                status.setText("Renamed to: " + newTitle);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot edit task", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * Updates the list of tasks whenever the search text changes.
     */
    private void wireSearch(){
        DocumentListener dl = new DocumentListener() {
            private void changed(){ reload(); }
            @Override public void insertUpdate(DocumentEvent e){ changed(); }
            @Override public void removeUpdate(DocumentEvent e){ changed(); }
            @Override public void changedUpdate(DocumentEvent e){ changed(); }
        };
        searchField.getDocument().addDocumentListener(dl);
    }

    /**
     * Updates the task list when the user changes the filter.
     */
    private void wireFilter(){ filterBox.addActionListener(e -> reload()); }

    /**
     * Reloads the displayed tasks according to the search and filter.
     */
    private void reload(){
        String q = (searchField == null) ? "" : searchField.getText();
        TaskStore.Filter f = (filterBox.getSelectedItem() != null)
                ? (TaskStore.Filter) filterBox.getSelectedItem()
                : TaskStore.Filter.ALL;
        refresh(store.query(q, f));
    }

    /**
     * Updates the list model with the provided tasks.
     */
    private void refresh(List<Task> tasks){
        listModel.clear();
        for (Task t: tasks) listModel.addElement(t);
    }

    /**
     * Called when the data in the store changes.
     */
    @Override public void onTasksChanged() { reload(); }

    /**
     * Highlights today’s tasks in blue and completed tasks in gray.
     */
    private static class TodayRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Task) {
                Task t = (Task) value;
                LocalDate due = t.dueDate();
                boolean isToday = (due != null && due.equals(LocalDate.now()));
                if (!isSelected) {
                    if (t.completed()) {
                        c.setForeground(Color.GRAY);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    } else if (isToday) {
                        c.setForeground(new Color(0, 102, 204));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(Color.BLACK);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
            }
            return c;
        }
    }
}
