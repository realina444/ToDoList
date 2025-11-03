package model;

import observer.TaskObserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskStore {

   
    private static TaskStore instance;
    private TaskStore() {}
    public static TaskStore getInstance() {
        if (instance == null) instance = new TaskStore();
        return instance;
    }

  
    private static final Path DEFAULT_JSON_PATH = java.nio.file.Paths.get("tasks.json");

    private static String esc(String s){
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    private static String norm(String s){ return (s == null) ? "" : s.trim().toLowerCase(); }

 
    private final Map<TaskId, Task> tasks = new LinkedHashMap<>();
    private final List<TaskObserver> observers = new ArrayList<>();
    private Path ioPath = DEFAULT_JSON_PATH;

   
    private Path getSavePath() { return (ioPath != null) ? ioPath : DEFAULT_JSON_PATH; }

    
    public void addObserver(TaskObserver o){ if (o != null && !observers.contains(o)) observers.add(o); }
    public void removeObserver(TaskObserver o){ observers.remove(o); }
    private void notifyObservers(){ for (TaskObserver o: observers) o.onTasksChanged(); }

/**
 * Returns all stored tasks.
 *
 * @return a list of all tasks
 */
    public List<Task> all(){ return new ArrayList<>(tasks.values()); }
    public enum Filter { ALL, TODAY, OVERDUE, COMPLETED } 
    /**
     * Searches for tasks whose titles contain the given query text.
     *
     * @param q the search text entered by the user
     * @return a list of tasks whose titles contain the query text (case-insensitive)
     *
     * If the query is null or empty, the method returns all tasks.
     * This method is used to enable keyword-based searching in the task list.
     */
    
    public List<Task> search(String q){
        if (q == null || q.isBlank()) return all();
        String needle = q.toLowerCase();
        List<Task> result = new ArrayList<>();
        for (Task t : tasks.values()){
            String title = (t.title() == null) ? "" : t.title();
            if (title.toLowerCase().contains(needle)) result.add(t);
        }
        return result;
    }
    /**
     * Returns a list of tasks that match a given search query and filter type.
     *
     * @param q the search text (part of the title to search for)
     * @param f the filter type (ALL, TODAY, OVERDUE, or COMPLETED)
     * @return a list of tasks that match both the search text and filter criteria
     *
     * If the search query is empty, all tasks are returned.
     * The method combines text search and date/status filters.
     */

    public List<Task> query(String q, Filter f){
        return applyFilter(search(q), f);
    }
    /**
     * Applies the given filter to a base list of tasks.
     *
     * @param base the list of tasks to filter
     * @param f the filter type (ALL, TODAY, OVERDUE, or COMPLETED)
     * @return a list of tasks that satisfy the given filter
     *
     * Filters tasks based on their due date and completion state:
     * - TODAY: tasks due today
     * - OVERDUE: tasks with past due dates and not completed
     * - COMPLETED: tasks marked as completed
     * - ALL: returns all tasks without filtering
     */
    private List<Task> applyFilter(List<Task> base, Filter f){
        if (f == null || f == Filter.ALL) return base;

        LocalDate today = LocalDate.now();
        List<Task> result = new ArrayList<>();
        for (Task t : base){
            LocalDate d = t.dueDate();
            switch (f){
                case TODAY:
                    if (d != null && d.equals(today)) result.add(t);
                    break;
                case OVERDUE:
                    if (d != null && d.isBefore(today) && !t.completed()) result.add(t);
                    break;
                case COMPLETED:
                    if (t.completed()) result.add(t);
                    break;
                default:
                    result.add(t);
            }
        }
        return result;
    }

    /**
    * Adds a new task to the store.
    *
    * @param t the task to add
    * @throws IllegalArgumentException if another task has the same title
    * Saves the task, notifies observers, and writes changes to file.
    */
    public Task addTask(Task t){
        if (t == null) return null;
        if (existsTitle(t.title()))
            throw new IllegalArgumentException("A task with the same title already exists.");
        tasks.put(t.id(), t);
        notifyObservers();
        saveToJson(getSavePath());
        return t;
    }

/**
 * Toggles the completion state of a task.
 *
 * @param id the ID of the task to toggle
 * Updates the task, notifies observers, and saves the change.
 */
    public void toggleCompleted(TaskId id){
        Task t = tasks.get(id);
        if (t != null){
            tasks.put(id, t.withCompleted(!t.completed()));
            notifyObservers();
            saveToJson(getSavePath());
        }
    }

/**
 * Removes a task with the given ID.
 *
 * @param id the ID of the task to remove
 * Removes the task if found, notifies observers, and updates the saved file.
 */
    public void removeTask(TaskId id) {
        tasks.remove(id);
        notifyObservers();
        saveToJson(getSavePath());
    }

    /**
     * Updates an existing task in the store.
     *
     * @param updated the updated Task object to replace the existing one
     * @throws IllegalArgumentException if another task with the same title already exists
     *
     * Replaces the old task with the new one (matched by ID),
     * notifies all observers, and saves the updated data to file.
     */
    public void updateTask(Task updated){
        if (updated == null) return;
        Task exists = tasks.get(updated.id());
        if (exists != null){
            if (existsTitleExcept(updated.title(), updated.id()))
                throw new IllegalArgumentException("A task with the same title already exists.");
            tasks.put(updated.id(), updated);
            notifyObservers();
            saveToJson(getSavePath());
        }
    }

    public synchronized String toJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Task t : tasks.values()){
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            sb.append("\"id\":\"").append(esc(t.id().value())).append("\",");
            sb.append("\"title\":\"").append(esc(t.title())).append("\",");
            sb.append("\"description\":\"").append(esc(t.description())).append("\",");
            sb.append("\"dueDate\":").append(t.dueDate()==null ? "null" : ("\""+t.dueDate()+"\"")).append(",");
            sb.append("\"completed\":").append(t.completed());
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
    /**
     * Saves all tasks as a JSON file.
     *
     * @param path the file path to save to
     */
    public synchronized boolean saveToJson(Path path){
        try{
            Files.writeString(
                path,
                toJson(),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.WRITE
            );
            return true;
        }catch(IOException e){
            System.err.println("Auto-save failed: " + e.getMessage());
            return false;
        }
    }

   
    private static String extractString(String obj, String key){
        Matcher m = Pattern.compile("\""+key+"\"\\s*:\\s*\"(.*?)\"").matcher(obj);
        return m.find() ? m.group(1) : null;
    }
    private static Boolean extractBool(String obj, String key){
        Matcher m = Pattern.compile("\""+key+"\"\\s*:\\s*(true|false)").matcher(obj);
        return m.find() ? Boolean.valueOf(m.group(1)) : null;
    }
    private static LocalDate extractDate(String obj, String key){
        String s = extractString(obj, key);
        return (s == null || s.equalsIgnoreCase("null")) ? null : LocalDate.parse(s);
    }


/**
 * Loads tasks from a JSON file.
 *
 * @param path the file path to load from
 * Loads all valid tasks and skips broken entries.
 */
    public synchronized void loadFromJson(Path path){
        try{
            if (path == null || !Files.exists(path)) return;
            String raw = Files.readString(path).trim();
            if (raw.isEmpty() || raw.equals("[]")) return;

            tasks.clear();

            Matcher m = Pattern.compile("\\{([^}]*)\\}").matcher(raw);
            while (m.find()){
                String obj = m.group();

                String idStr = extractString(obj, "id");
                String title = extractString(obj, "title");
                String desc  = extractString(obj, "description");
                LocalDate due = extractDate(obj, "dueDate");
                Boolean completed = extractBool(obj, "completed");
                if (completed == null) completed = false;
                if (title == null || title.isBlank()) continue;

                TaskId tid = (idStr == null || idStr.isBlank()) ? new TaskId() : new TaskId(idStr);
                Task t = new Task(tid, title, desc, due, completed);
                tasks.put(t.id(), t);
            }
               notifyObservers();
        }catch(Exception e){
            System.err.println("Load failed: " + e.getMessage());
        }
    }

  
    private boolean existsTitle(String title){
        String n = norm(title);
        for (Task t : tasks.values())
            if (norm(t.title()).equals(n)) return true;
        return false;
    }
    private boolean existsTitleExcept(String title, TaskId exclude){
        String n = norm(title);
        for (Task t : tasks.values())
            if (!t.id().equals(exclude) && norm(t.title()).equals(n)) return true;
        return false;
    }
}
