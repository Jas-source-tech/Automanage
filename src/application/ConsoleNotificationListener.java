package application;

import domain.IEventListener;

/**
 * Console notification listener — subscribes to EventBus.
 * REQ-2.7: Demonstrates Observer behavioral pattern.
 */
public class ConsoleNotificationListener implements IEventListener {
    @Override
    public void onEvent(String eventType, String message) {
        System.out.println("  [NOTIFICATION] " + eventType + ": " + message);
    }
}