package tong.sihriya.data;

import java.util.Map;

public class SchoolColors {
    private static final Map<String, float[]> COLORS = Map.of(
        "fire",       new float[]{1.0f, 0.35f, 0.05f},
        "water",      new float[]{0.2f, 0.5f, 1.0f},
        "wind",       new float[]{0.8f, 0.85f, 1.0f},
        "earth",      new float[]{0.35f, 0.6f, 0.2f},
        "lightning",  new float[]{1.0f, 0.85f, 0.1f},
        "ice",        new float[]{0.5f, 0.8f, 1.0f},
        "lava",       new float[]{1.0f, 0.2f, 0.0f},
        "necromancy", new float[]{0.55f, 0.0f, 0.75f},
        "lumamancy",  new float[]{1.0f, 0.85f, 0.4f}
    );

    private static final Map<String, float[]> COLORS_SECONDARY = Map.of(
        "fire",       new float[]{1.0f, 0.8f, 0.2f},
        "water",      new float[]{0.6f, 0.85f, 1.0f},
        "wind",       new float[]{0.95f, 0.95f, 1.0f},
        "earth",      new float[]{0.6f, 0.8f, 0.4f},
        "lightning",  new float[]{1.0f, 1.0f, 0.5f},
        "ice",        new float[]{0.75f, 0.9f, 1.0f},
        "lava",       new float[]{1.0f, 0.6f, 0.1f},
        "necromancy", new float[]{0.75f, 0.3f, 1.0f},
        "lumamancy",  new float[]{1.0f, 0.95f, 0.6f}
    );

    public static float[] get(String schoolId) {
        return COLORS.getOrDefault(schoolId, new float[]{1, 1, 1});
    }

    public static float[] getSecondary(String schoolId) {
        return COLORS_SECONDARY.getOrDefault(schoolId, new float[]{1, 1, 1});
    }
}