package domain;

/**
 * Observer pattern — IEventListener interface.
 * REQ-2.7: Behavioral pattern (Observer).
 */
public interface IEventListener {
    void onEvent(String eventType, String message);
}