package domain;

/**
 * Policy A: Standard hold durations vary by vehicle category.
 * New = 72 hrs, Used = 48 hrs, CPO = 96 hrs.
 */
public class StandardHoldPolicy implements HoldPolicy {

    @Override
    public int getHoldDurationHours(String category) {
        switch (category.toUpperCase()) {
            case "NEW":  return 72;
            case "CPO":  return 96;
            case "USED":
            default:     return 48;
        }
    }

    @Override
    public String getPolicyName() {
        return "Policy A - Standard (New=72h, Used=48h, CPO=96h)";
    }
}