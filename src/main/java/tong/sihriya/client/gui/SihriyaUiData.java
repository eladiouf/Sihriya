package tong.sihriya.client.gui;

import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tong.sihriya.client.ClientSchoolData;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.data.SpellRegistry.SpellEffect;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class SihriyaUiData {
    public static final List<String> SCHOOL_ORDER = List.of(
        "fire", "water", "wind", "earth",
        "lightning", "ice", "lava", "necromancy", "lumamancy"
    );

    private static final int[] SCHOOL_COLORS = {
        0xFFFF4500, 0xFF3399FF, 0xFF90EE90, 0xFF8B4513,
        0xFFFFD700, 0xFFADD8E6, 0xFFFF2200, 0xFF8C14DC, 0xFFFFDC50
    };

    private static final int[] ALTERNATE_SCHOOL_COLORS = {
        0xFFE84A35, 0xFF2A78D4, 0xFF38B68A, 0xFF8A6A2A,
        0xFFE6C229, 0xFF7CD8F2, 0xFFD84C1F, 0xFF9B59D0, 0xFFF4E285
    };

    private SihriyaUiData() {}

    public static int schoolColor(String school) {
        int index = SCHOOL_ORDER.indexOf(school);
        if (index >= 0) {
            return ClientUiOptions.alternateColors ? ALTERNATE_SCHOOL_COLORS[index] : SCHOOL_COLORS[index];
        }

        var data = SchoolRegistry.get(school);
        if (data != null) {
            try {
                return 0xFF000000 | Integer.parseInt(data.color, 16);
            } catch (NumberFormatException ignored) {
                return 0xFFFFFFFF;
            }
        }
        return 0xFFFFFFFF;
    }

    public static String schoolName(String school) {
        return I18n.get("sihriya.school." + school);
    }

    public static String spellName(SpellData spell) {
        int separator = spell.id.indexOf('.');
        String shortId = separator >= 0 ? spell.id.substring(separator + 1) : spell.id;
        String key = "sihriya.spell." + shortId;
        String translated = I18n.get(key);
        if (!translated.equals(key)) return translated;
        return titleCase(shortId);
    }

    public static String spellTypeName(SpellData spell) {
        String key = "sihriya.spell_type." + spell.type.name().toLowerCase();
        String translated = I18n.get(key);
        return translated.equals(key) ? titleCase(spell.type.name().toLowerCase()) : translated;
    }

    public static String spellDescription(SpellData spell) {
        String shortId = shortSpellId(spell);
        String key = "sihriya.spell." + shortId + ".desc";
        String translated = I18n.get(key);
        if (!translated.equals(key)) return translated;

        List<String> parts = new ArrayList<>();
        for (SpellEffect effect : spell.effects) {
            parts.add(effectSummary(effect));
            if (parts.size() >= 3) break;
        }
        if (parts.isEmpty()) return I18n.get("screen.sihriya.grimoire.no_description");
        return I18n.get("screen.sihriya.grimoire.generated_description",
            spellTypeName(spell), String.join(", ", parts));
    }

    public static String effectName(String effectType) {
        String key = "sihriya.effect." + effectType;
        String translated = I18n.get(key);
        return translated.equals(key) ? titleCase(effectType) : translated;
    }

    public static String effectSummary(SpellEffect effect) {
        String name = effectName(effect.type);
        if (effect.duration > 0) {
            return I18n.get("screen.sihriya.grimoire.effect_timed", name, trim(effect.baseValue), effect.duration / 20.0f);
        }
        return I18n.get("screen.sihriya.grimoire.effect_instant", name, trim(effect.baseValue));
    }

    public static String unlockRequirementText(String school, String lockedFallback) {
        var data = SchoolRegistry.get(school);
        if (data == null || data.unlock == null) return lockedFallback;

        String joiner = "or".equals(data.unlock.type) ? " / " : " + ";
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < data.unlock.schoolIds.length; i++) {
            if (i > 0) text.append(joiner);
            String requiredSchool = data.unlock.schoolIds[i];
            text.append(schoolName(requiredSchool))
                .append(' ')
                .append(ClientSchoolData.getLevel(requiredSchool))
                .append('/')
                .append(data.unlock.levels[i]);
        }
        return text.toString();
    }

    private static String titleCase(String id) {
        String[] parts = id.replace('_', ' ').split(" ");
        StringBuilder text = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (text.length() > 0) text.append(' ');
            text.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) text.append(part.substring(1));
        }
        return text.toString();
    }

    private static String shortSpellId(SpellData spell) {
        int separator = spell.id.indexOf('.');
        return separator >= 0 ? spell.id.substring(separator + 1) : spell.id;
    }

    private static String trim(float value) {
        if (value == (int) value) return String.valueOf((int) value);
        return String.format("%.2f", value);
    }
}
