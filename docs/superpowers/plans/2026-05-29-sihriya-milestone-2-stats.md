# Milestone 2 : STAT Mod Integration — Bridge, Mana & XP

> **Goal:** Bridge complet avec STAT Mod : mana piloté par MANA_POOL, XP aux stats à chaque action, scaling sorts basé sur les stats affinités.

**Architecture:** Remplacer SihriyaAPI.java (réflexion) par STATModIntegration.java (appels directs via l'API STAT Mod). ManaManager modifié pour lire MANA_POOL. Nouveau système d'XP lié aux sorts.

**Tech Stack:** Forge 1.20.1, Capabilities, STAT Mod API (PlayerStatsProvider, ActionXpHelper, StatCalculator)

---

### Task 2.1 : Réécrire STATModIntegration.java

**Files:**
- Modify: `src/main/java/tong/sihriya/integration/STATModIntegration.java`
- Delete: `src/main/java/tong/sihriya/integration/SihriyaAPI.java`

- [ ] **Step 1: Remplacer STATModIntegration.java avec l'API complète**

```java
package tong.sihriya.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.statmod.capability.PlayerStatsProvider;
import tong.statmod.progression.ActionXpHelper;
import tong.statmod.progression.ActionXpHelper.XpTier;
import tong.statmod.stats.StatCalculator;
import tong.statmod.stats.StatType;

public class STATModIntegration {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        try {
            Class.forName("tong.statmod.STATMod");
            Sihriya.LOGGER.info("STAT Mod détecté et intégré !");
            initialized = true;
        } catch (ClassNotFoundException e) {
            Sihriya.LOGGER.error("STAT Mod est requis mais introuvable !");
            throw new RuntimeException("STAT Mod manquant");
        }
    }

    public static boolean isInitialized() { return initialized; }

    /** Récupère le niveau d'une stat pour un joueur */
    public static int getStatLevel(Player player, StatType stat) {
        var opt = player.getCapability(PlayerStatsProvider.PLAYER_STATS).resolve();
        return opt.map(stats -> stats.getLevel(stat.index)).orElse(0);
    }

    /** Récupère le niveau d'une stat par son index */
    public static int getStatLevel(Player player, int statIndex) {
        var opt = player.getCapability(PlayerStatsProvider.PLAYER_STATS).resolve();
        return opt.map(stats -> stats.getLevel(statIndex)).orElse(0);
    }

    /** Calcule le multiplicateur de dégâts basé sur la stat affinité */
    public static float getDamageMultiplier(Player player, String schoolId) {
        StatType stat = schoolToStat(schoolId);
        if (stat == null) return 1.0f;
        int level = getStatLevel(player, stat);
        return 1.0f + level * 0.005f; // +0.5% par niveau
    }

    /** Récupère le mana max via MANA_POOL */
    public static float getMaxMana(Player player) {
        int manaPoolLevel = getStatLevel(player, StatType.MANA_POOL);
        return 50 + StatCalculator.getManaBonus(manaPoolLevel);
    }

    /** Calcule le temps d'incantation réduit par CASTING_SPEED */
    public static int getCastTime(Player player, int baseCastTime) {
        int castingSpeed = getStatLevel(player, StatType.CASTING_SPEED);
        float reduction = castingSpeed * 0.003f; // -0.3% par niveau
        return Math.max(5, (int)(baseCastTime * (1.0f - reduction)));
    }

    /** Donne de l'XP dans la stat correspondante à l'école */
    public static void awardSchoolXP(ServerPlayer player, String schoolId) {
        StatType stat = schoolToStat(schoolId);
        if (stat == null) return;
        var opt = player.getCapability(PlayerStatsProvider.PLAYER_STATS).resolve();
        opt.ifPresent(stats -> stats.addXp(stat.index, 10));
    }

    /** Mapping école → stat STAT Mod */
    public static StatType schoolToStat(String schoolId) {
        return switch (schoolId) {
            case "fire" -> StatType.FIRE_AFFINITY;
            case "water" -> StatType.WATER_AFFINITY;
            case "wind" -> StatType.AIR_AFFINITY;
            case "earth" -> StatType.EARTH_AFFINITY;
            case "lightning" -> StatType.ARCANE_POWER;
            case "ice" -> StatType.WATER_AFFINITY;
            case "lava" -> StatType.FIRE_AFFINITY;
            case "necromancy" -> StatType.ARCANE_POWER;
            case "lumagie" -> StatType.ERUDITION;
            default -> null;
        };
    }

    /** Récupère le bonus d'une stat secondaire pour une école */
    public static float getSecondaryBonus(Player player, String schoolId) {
        return switch (schoolId) {
            case "fire" -> getStatLevel(player, StatType.CASTING_SPEED) * 0.003f;
            case "water" -> getStatLevel(player, StatType.WILLPOWER) * 0.005f;
            case "wind" -> getStatLevel(player, StatType.AGILITY) * 0.002f;
            case "earth" -> getStatLevel(player, StatType.PHYSICAL_RESISTANCE) * 0.003f;
            case "lightning" -> getStatLevel(player, StatType.PRECISION) * 0.003f;
            case "ice" -> getStatLevel(player, StatType.MAGIC_RESISTANCE) * 0.003f;
            case "lava" -> getStatLevel(player, StatType.PHYSICAL_ENDURANCE) * 0.003f;
            case "necromancy" -> getStatLevel(player, StatType.WILLPOWER) * 0.003f;
            case "lumagie" -> getStatLevel(player, StatType.MANA_POOL) * 0.005f;
            default -> 0f;
        };
    }
}
```

- [ ] **Step 2: Supprimer l'ancien SihriyaAPI.java**

Delete: `src/main/java/tong/sihriya/integration/SihriyaAPI.java`

- [ ] **Step 3: Compiler et vérifier**

Run: `.\gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/java/tong/sihriya/integration/STATModIntegration.java
git rm src/main/java/tong/sihriya/integration/SihriyaAPI.java
git commit -m "feat: full STAT Mod bridge with mana, scaling, XP, and secondary stats"
```

---

### Task 2.2 : Refondre ManaManager pour utiliser MANA_POOL

**Files:**
- Modify: `src/main/java/tong/sihriya/core/ManaManager.java`
- Modify: `src/main/java/tong/sihriya/client/ClientManaData.java`

- [ ] **Step 1: Modifier ManaManager pour que maxMana soit piloté par MANA_POOL**

```java
package tong.sihriya.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import tong.sihriya.integration.STATModIntegration;

public class ManaManager implements INBTSerializable<CompoundTag> {
    private static final int BASE_MAX_MANA = 50;

    private float mana;
    private float maxMana = BASE_MAX_MANA;
    private long lockUntil = 0;
    private Player player;

    public ManaManager() {}

    public void setPlayer(Player player) { this.player = player; }

    public float getMana() { return mana; }

    public float getMaxMana() {
        if (player != null && player.level().isClientSide) return maxMana;
        if (player != null) {
            return STATModIntegration.getMaxMana(player);
        }
        return maxMana;
    }

    public float getManaPercent() {
        float max = getMaxMana();
        return max > 0 ? mana / max : 0;
    }

    public void setMana(float amount) { this.mana = Math.min(amount, getMaxMana()); }
    public void setMaxMana(float amount) { this.maxMana = Math.max(amount, BASE_MAX_MANA); }

    public boolean consumeMana(float amount) {
        if (mana < amount) return false;
        mana -= amount;
        return true;
    }

    public void regenMana(float amount) {
        if (isLocked()) return;
        mana = Math.min(mana + amount, getMaxMana());
    }

    public void lockMana(long durationMs) {
        this.lockUntil = System.currentTimeMillis() + durationMs;
    }

    public boolean isLocked() {
        return System.currentTimeMillis() < lockUntil;
    }

    public long getLockRemainingMs() {
        return Math.max(0, lockUntil - System.currentTimeMillis());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Mana", mana);
        tag.putFloat("MaxMana", maxMana);
        tag.putLong("LockUntil", lockUntil);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.mana = tag.getFloat("Mana");
        this.maxMana = tag.getFloat("MaxMana");
        this.lockUntil = tag.getLong("LockUntil");
    }
}
```

- [ ] **Step 2: Mettre à jour ManaProvider pour passer le joueur**

```java
// Ajouter ceci dans ManaProvider.getOrCreate()
private ManaManager getOrCreate(Player player) {
    if (this.manaManager == null) {
        this.manaManager = new ManaManager();
        this.manaManager.setPlayer(player);
    }
    return this.manaManager;
}
```

- [ ] **Step 3: Compiler et vérifier**

Run: `.\gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/java/tong/sihriya/core/ManaManager.java src/main/java/tong/sihriya/core/ManaProvider.java
git commit -m "feat: mana system now driven by STAT Mod MANA_POOL stat"
```

---

### Task 2.3 : Mettre à jour MeditationHandler pour l'XP STAT Mod

**Files:**
- Modify: `src/main/java/tong/sihriya/core/MeditationHandler.java`

- [ ] **Step 1: Ajouter un award XP MANA_POOL lors de la méditation**

```java
// Dans le tick de méditation, après regenMana :
if (serverPlayer.tickCount % 100 == 0) { // toutes les 5 secondes
    STATModIntegration.getStatLevel(serverPlayer, StatType.MANA_POOL); // just to keep ref
    var opt = serverPlayer.getCapability(PlayerStatsProvider.PLAYER_STATS).resolve();
    opt.ifPresent(stats -> stats.addXp(StatType.MANA_POOL.index, 1));
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/core/MeditationHandler.java
git commit -m "feat: meditation awards MANA_POOL XP every 5 seconds"
```
