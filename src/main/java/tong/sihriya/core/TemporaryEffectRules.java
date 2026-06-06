package tong.sihriya.core;

public final class TemporaryEffectRules {
    public static final int DEFAULT_SUMMON_DURATION_TICKS = 600;
    public static final int MAX_SUMMON_DURATION_TICKS = 2400;
    public static final int MAX_SUMMON_COUNT = 12;
    public static final int DEFAULT_WALL_DURATION_TICKS = 1200;
    public static final int MAX_TEMPORARY_BLOCK_TICKS = 1200;
    public static final String TEMPORARY_SUMMON_TAG = "sihriya_temporary_summon";
    public static final String SUMMON_CASTER_TAG_PREFIX = "sihriya_caster:";

    private TemporaryEffectRules() {
    }

    public static int clampSummonCount(int requestedCount) {
        if (requestedCount <= 0) return 1;
        return Math.min(requestedCount, MAX_SUMMON_COUNT);
    }

    public static int clampSummonDuration(int requestedDurationTicks) {
        return clampDuration(
            requestedDurationTicks,
            DEFAULT_SUMMON_DURATION_TICKS,
            MAX_SUMMON_DURATION_TICKS
        );
    }

    public static int clampWallDuration(int requestedDurationTicks) {
        return clampDuration(
            requestedDurationTicks,
            DEFAULT_WALL_DURATION_TICKS,
            MAX_TEMPORARY_BLOCK_TICKS
        );
    }

    public static double summonAngle(int summonIndex, int summonCount) {
        if (summonCount <= 0) return 0.0;
        return (2 * Math.PI * summonIndex) / summonCount;
    }

    private static int clampDuration(int requestedDurationTicks, int defaultDurationTicks, int maxDurationTicks) {
        int duration = requestedDurationTicks <= 0 ? defaultDurationTicks : requestedDurationTicks;
        return Math.min(duration, maxDurationTicks);
    }
}
