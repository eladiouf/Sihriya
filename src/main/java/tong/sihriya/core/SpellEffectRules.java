package tong.sihriya.core;

public final class SpellEffectRules {
    private SpellEffectRules() {
    }

    public static int rangeBonusAmplifier(float bonus) {
        return Math.max(0, Math.round(bonus * 10.0f) - 1);
    }

    public static double rangeMultiplierFromAmplifier(int amplifier) {
        if (amplifier < 0) return 1.0;
        return 1.0 + (amplifier + 1) / 10.0;
    }

    public static int orbitDamageAmplifier(float damage) {
        return Math.max(0, Math.round(damage * 2.0f) - 1);
    }

    public static float orbitDamageFromAmplifier(int amplifier) {
        if (amplifier < 0) return 0.0f;
        return (amplifier + 1) / 2.0f;
    }

    public static boolean shouldRevokeMagicFlight(boolean creative, boolean spectator) {
        return !creative && !spectator;
    }
}
