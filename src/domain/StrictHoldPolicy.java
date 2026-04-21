package domain;

/**
 * Policy B: Strict hold — all categories get 24 hours max.
 * Demonstrates REQ-2.6: same scenario, different outcome.
 */
public class StrictHoldPolicy implements HoldPolicy {

    @Override
    public int getHoldDurationHours(String category) {
        return 24; // strict: all categories get 24 hours only
    }

    @Override
    public String getPolicyName() {
        return "Policy B - Strict (All categories = 24h max)";
    }
}