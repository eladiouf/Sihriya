package tong.sihriya.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    // Touches 1-6 : cast le meilleur sort de l'école correspondante
    public static final KeyMapping SCHOOL_FIRE = new KeyMapping(
        "key.sihriya.school_fire", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_1, "key.sihriya.category");
    public static final KeyMapping SCHOOL_WATER = new KeyMapping(
        "key.sihriya.school_water", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_2, "key.sihriya.category");
    public static final KeyMapping SCHOOL_WIND = new KeyMapping(
        "key.sihriya.school_wind", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_3, "key.sihriya.category");
    public static final KeyMapping SCHOOL_EARTH = new KeyMapping(
        "key.sihriya.school_earth", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_4, "key.sihriya.category");
    public static final KeyMapping SCHOOL_LIGHTNING = new KeyMapping(
        "key.sihriya.school_lightning", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_5, "key.sihriya.category");
    public static final KeyMapping SCHOOL_ICE = new KeyMapping(
        "key.sihriya.school_ice", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_6, "key.sihriya.category");
    public static final KeyMapping MEDITATE = new KeyMapping(
        "key.sihriya.meditate", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.sihriya.category");
    public static final KeyMapping GRIMOIRE = new KeyMapping(
        "key.sihriya.grimoire", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.sihriya.category");
    public static final KeyMapping SPELL_WHEEL = new KeyMapping(
        "key.sihriya.spell_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.sihriya.category");

    public static final KeyMapping[] ALL_SCHOOLS = {
        SCHOOL_FIRE, SCHOOL_WATER, SCHOOL_WIND, SCHOOL_EARTH, SCHOOL_LIGHTNING, SCHOOL_ICE
    };

    public static final String[] SCHOOL_IDS = {
        "fire", "water", "wind", "earth", "lightning", "ice"
    };

    // Shift+1-3 : écoles avancées (Lave, Nécromancie, Lumagie)
    public static final String[] ADVANCED_SCHOOL_IDS = {
        "lava", "necromancy", "lumamancy"
    };
}
