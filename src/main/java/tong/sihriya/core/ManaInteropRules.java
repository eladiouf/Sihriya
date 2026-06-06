package tong.sihriya.core;

public final class ManaInteropRules {
    private ManaInteropRules() {
    }

    public static long remainingManaLockMs(long remainingTicks) {
        return Math.max(0L, remainingTicks) * 50L;
    }
}
