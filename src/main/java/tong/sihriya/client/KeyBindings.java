package tong.sihriya.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyMapping SPELL_WHEEL = new KeyMapping(
        "key.sihriya.spell_wheel",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "key.sihriya.category"
    );
    public static final KeyMapping MEDITATE = new KeyMapping(
        "key.sihriya.meditate",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        "key.sihriya.category"
    );
}
