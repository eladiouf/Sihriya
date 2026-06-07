package tong.sihriya.compat;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import tong.sihriya.Sihriya;
import tong.sihriya.core.SpellCastHandler;
import tong.sihriya.data.SpellRegistry.SpellType;

public class SihriyaIronsSpell extends AbstractSpell {
    private final String spellId;
    private final String schoolId;
    private final int tier;
    private final int sihriyaManaCost;
    private final int sihriyaCooldownTicks;
    private final int sihriyaCastTimeTicks;
    private final SpellType sihriyaSpellType;
    private final DefaultConfig config;

    public SihriyaIronsSpell(String spellId, String schoolId, int tier,
                              int manaCost, int cooldownTicks, int castTimeTicks,
                              SpellType spellType) {
        super();
        this.spellId = spellId;
        this.schoolId = schoolId;
        this.tier = tier;
        this.sihriyaManaCost = manaCost;
        this.sihriyaCooldownTicks = cooldownTicks;
        this.sihriyaCastTimeTicks = castTimeTicks;
        this.sihriyaSpellType = spellType;
        this.baseManaCost = manaCost;
        this.castTime = castTimeTicks;
        this.config = buildConfig();
    }

    @Override
    public ResourceLocation getSpellResource() {
        return new ResourceLocation(Sihriya.MODID, spellId);
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return config;
    }

    @Override
    public CastType getCastType() {
        if (sihriyaCastTimeTicks <= 0) return CastType.INSTANT;
        return switch (sihriyaSpellType) {
            case PROJECTILE, ZONE, BUFF, SUMMON -> CastType.LONG;
            case ULTIMATE -> CastType.LONG;
        };
    }

    @Override
    public SchoolType getSchoolType() {
        var school = SchoolRegistry.getSchool(new ResourceLocation(Sihriya.MODID, schoolId));
        return school != null ? school : super.getSchoolType();
    }

    @Override
    public int getManaCost(int level) {
        return sihriyaManaCost;
    }

    @Override
    public int getCastTime(int level) {
        return sihriyaCastTimeTicks;
    }

    @Override
    public int getSpellCooldown() {
        return sihriyaCooldownTicks;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData magicData) {
        if (!(entity instanceof ServerPlayer sp)) return;
        SpellCastHandler.executeSpellEffects(sp, spellId);
    }

    private DefaultConfig buildConfig() {
        SpellRarity rarity = switch (tier) {
            case 1 -> SpellRarity.COMMON;
            case 2 -> SpellRarity.UNCOMMON;
            case 3 -> SpellRarity.RARE;
            case 4 -> SpellRarity.EPIC;
            default -> SpellRarity.LEGENDARY;
        };
        try {
            return new DefaultConfig()
                .setMaxLevel(1)
                .setMinRarity(rarity)
                .setSchoolResource(new ResourceLocation(Sihriya.MODID, schoolId))
                .setCooldownSeconds(sihriyaCooldownTicks / 20.0)
                .build();
        } catch (Exception e) {
            Sihriya.LOGGER.warn("SihriyaIronsSpell: DefaultConfig build failed for {}: {}", spellId, e.getMessage());
            return new DefaultConfig();
        }
    }
}
