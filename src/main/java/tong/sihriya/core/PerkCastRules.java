package tong.sihriya.core;

import java.util.ArrayList;
import java.util.List;

public final class PerkCastRules {
    private PerkCastRules() {
    }

    public enum ActionType {
        FIRE_AOE,
        FIRE_SPREAD,
        WATER_PUSH,
        TSUNAMI_SLOW,
        WIND_PULL,
        HURRICANE_LIFT,
        EARTH_RESISTANCE,
        EARTH_AOE_STUN,
        ARCANE_AOE_DAMAGE,
        MAGIC_RESISTANCE_ABSORB,
        CASTING_SPEED_BOOST,
        ERUDITION_REFUND,
        MANA_REFUND
    }

    public record Action(ActionType type, double radius, float strength, int duration, int amplifier, int manaRefund) {
        public static Action area(ActionType type, double radius, float strength, int duration, int amplifier) {
            return new Action(type, radius, strength, duration, amplifier, 0);
        }

        public static Action self(ActionType type, int duration, int amplifier) {
            return new Action(type, 0, 0, duration, amplifier, 0);
        }

        public static Action mana(int amount) {
            return new Action(ActionType.MANA_REFUND, 0, 0, 0, 0, amount);
        }
    }

    public record StatLevels(
            int fire,
            int water,
            int air,
            int earth,
            int arcane,
            int magicResistance,
            int castingSpeed,
            int manaPool,
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
            private int manaPool;
            private int erudition;

            public Builder fire(int value) { fire = value; return this; }
            public Builder water(int value) { water = value; return this; }
            public Builder air(int value) { air = value; return this; }
            public Builder earth(int value) { earth = value; return this; }
            public Builder arcane(int value) { arcane = value; return this; }
            public Builder magicResistance(int value) { magicResistance = value; return this; }
            public Builder castingSpeed(int value) { castingSpeed = value; return this; }
            public Builder manaPool(int value) { manaPool = value; return this; }
            public Builder erudition(int value) { erudition = value; return this; }

            public StatLevels build() {
                return new StatLevels(fire, water, air, earth, arcane, magicResistance,
                    castingSpeed, manaPool, erudition);
            }
        }
    }

    public static int manaRefundFor(int manaPoolLevel) {
        if (manaPoolLevel >= 80) return 8;
        if (manaPoolLevel >= 50) return 3;
        return 0;
    }

    public static List<Action> actionsFor(StatLevels levels, String school) {
        var actions = new ArrayList<Action>();

        if (levels.fire >= 50 && "fire".equals(school)) {
            actions.add(Action.area(ActionType.FIRE_AOE, 3, 0, 80, 0));
        }
        if (levels.fire >= 80 && "fire".equals(school)) {
            actions.add(Action.area(ActionType.FIRE_SPREAD, 5, 0, 60, 0));
        }

        if (levels.water >= 50 && "water".equals(school)) {
            actions.add(Action.area(ActionType.WATER_PUSH, 4, 0.8f, 0, 0));
        }
        if (levels.water >= 80 && "water".equals(school)) {
            actions.add(Action.area(ActionType.TSUNAMI_SLOW, 6, 0, 100, 2));
        }

        if (levels.air >= 50 && "wind".equals(school)) {
            actions.add(Action.area(ActionType.WIND_PULL, 6, 0.6f, 0, 0));
        }
        if (levels.air >= 80 && "wind".equals(school)) {
            actions.add(Action.area(ActionType.HURRICANE_LIFT, 8, 0.7f, 0, 0));
        }

        if (levels.earth >= 50 && "earth".equals(school)) {
            actions.add(Action.self(ActionType.EARTH_RESISTANCE, 100, 0));
        }
        if (levels.earth >= 80 && "earth".equals(school)) {
            actions.add(Action.area(ActionType.EARTH_AOE_STUN, 4, 0, 60, 3));
        }

        if (levels.arcane >= 80) {
            actions.add(Action.area(ActionType.ARCANE_AOE_DAMAGE, 5, 0.5f, 0, 0));
        }
        if (levels.magicResistance >= 80) {
            actions.add(Action.self(ActionType.MAGIC_RESISTANCE_ABSORB, 200, 0));
        }
        if (levels.castingSpeed >= 80) {
            actions.add(Action.self(ActionType.CASTING_SPEED_BOOST, 80, 0));
        }
        if (levels.erudition >= 80) {
            actions.add(new Action(ActionType.ERUDITION_REFUND, 0, 0, 0, 0, 5));
        }

        int manaRefund = manaRefundFor(levels.manaPool);
        if (manaRefund > 0) {
            actions.add(Action.mana(manaRefund));
        }

        return actions;
    }
}
