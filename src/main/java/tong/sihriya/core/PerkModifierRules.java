package tong.sihriya.core;

public final class PerkModifierRules {
    private PerkModifierRules() {}

    public record Modifier(float damageMult, float durationMult, int extraTargets) {
        public static final Modifier NONE = new Modifier(1f, 1f, 0);
    }

    public record StatLevels(
            int fire,
            int water,
            int air,
            int earth,
            int arcane,
            int magicResistance,
            int castingSpeed,
            int erudition) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private int fire;
            private int water;
            private int air;
            private int earth;
            private int arcane;
            private int magicResistance;
            private int castingSpeed;
            private int erudition;

            public Builder fire(int value) { fire = value; return this; }
            public Builder water(int value) { water = value; return this; }
            public Builder air(int value) { air = value; return this; }
            public Builder earth(int value) { earth = value; return this; }
            public Builder arcane(int value) { arcane = value; return this; }
            public Builder magicResistance(int value) { magicResistance = value; return this; }
            public Builder castingSpeed(int value) { castingSpeed = value; return this; }
            public Builder erudition(int value) { erudition = value; return this; }

            public StatLevels build() {
                return new StatLevels(fire, water, air, earth, arcane, magicResistance, castingSpeed, erudition);
            }
        }
    }

    public static Modifier modifierFor(StatLevels levels, String school, String effectType) {
        float damage = 1f;
        float duration = 1f;
        int extraTargets = 0;

        switch (school) {
            case "fire", "lava" -> {
                if (levels.fire >= 20 && ("burn".equals(effectType) || "damage".equals(effectType))) {
                    damage = 1.25f;
                }
            }
            case "water" -> {
                if (levels.water >= 20 && ("slow".equals(effectType) || "freeze".equals(effectType))) {
                    duration = 1.25f;
                }
            }
            case "wind" -> {
                if (levels.air >= 20 && ("knockback".equals(effectType) || "pull".equals(effectType))) {
                    damage = 1.5f;
                }
            }
            case "earth" -> {
                if (levels.earth >= 20 && "stun".equals(effectType)) {
                    duration = 1.25f;
                }
            }
            default -> {
            }
        }

        if (levels.arcane >= 20) damage *= 1.25f;
        if (levels.arcane >= 50 && "chain".equals(effectType)) extraTargets = 2;

        if (levels.magicResistance >= 20 && ("absorb".equals(effectType) || "damage_reduction".equals(effectType))) {
            duration += 0.15f;
        }
        if (levels.magicResistance >= 50 && "dispel".equals(effectType)) extraTargets = 1;

        if (levels.castingSpeed >= 20 && "dash".equals(effectType)) damage = 1.3f;
        if (levels.castingSpeed >= 50 && "heal".equals(effectType)) damage = 1.3f;

        if (levels.erudition >= 20 && ("speed".equals(effectType) || "flight".equals(effectType)
            || "melee_bonus".equals(effectType) || "thorns".equals(effectType))) {
            duration = 1.15f;
        }
        if (levels.erudition >= 50) damage *= 1.15f;

        return new Modifier(damage, duration, extraTargets);
    }
}
