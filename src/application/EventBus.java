package application;

import domain.IEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EventBus — Observer pattern (behavioral).
 * REQ-2.7: Publishes domain events to subscribed listeners.
 * New notification channels can be added without modifying
 * Vehicle or Appointment — Open/Closed Principle.
 */
public class EventBus {
    private static EventBus instance; // Singleton
    private Map<String, List<IEventListener>> listeners = new HashMap<>();

    private EventBus() {}

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void subscribe(String eventType, IEventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public void publish(String eventType, String message) {
        List<IEventListener> subs = listeners.getOrDefault(eventType, new ArrayList<>());
        for (IEventListener listener : subs) {
            try {
                listener.onEvent(eventType, message);
            } catch (Exception e) {
                System.out.println("  [EventBus] Listener error: " + e.getMessage());
            }
        }
    }
}