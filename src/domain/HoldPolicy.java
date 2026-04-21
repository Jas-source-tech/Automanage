package domain;

/**
 * Configurable hold policy — Strategy pattern.
 * REQ-2.6: Two configurable policies that vary by type/config.
 * Policy A (Standard): hold duration varies by category.
 * Policy B (Strict):   all categories get a shorter fixed hold.
 */
public interface HoldPolicy {
    int getHoldDurationHours(String category);
    String getPolicyName();
}