package model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an immutable unique identifier for a Task.
 * Each TaskId wraps a UUID string value.
 */
public final class TaskId {
    private final String value;

    /**
     * Creates a new TaskId with a randomly generated UUID value.
     */
    public TaskId() {
        this.value = UUID.randomUUID().toString();
    }

    /**
     * Creates a TaskId with a specified string value.
     *
     * @param value the string representation of the task identifier
     */
    public TaskId(String value) {
        this.value = value;
    }

    /**
     * Returns the string value of this TaskId.
     *
     * @return the task identifier as a string
     */
    public String value() {
        return value;
    }

    /**
     * Checks if this TaskId is equal to another object.
     * Two TaskId objects are equal if they have the same value.
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof TaskId && Objects.equals(value, ((TaskId) o).value);
    }

    /**
     * Returns the hash code of this TaskId.
     *
     * @return the hash code based on the value
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Returns a string representation of this TaskId.
     *
     * @return the string value of the identifier
     */
    @Override
    public String toString() {
        return value;
    }
}