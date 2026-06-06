# Milestone 3 : Progression — Paliers & Déblocage d'Écoles

> **Goal:** Système de paliers (25/50/75/100) pour débloquer les tiers de sorts, et déblocage conditionnel des 9 écoles.

**Architecture:** Nouveau TierUnlockHandler.java qui écoute l'XP des écoles. Conditions de déblocage des écoles avancées lues depuis schools.json. Premier join lié à la stat STAT Mod la plus haute.

**Tech Stack:** Forge 1.20.1, Capabilities (SchoolProgression), Events (PlayerTickEvent, PlayerLoggedInEvent)

---

### Task 3.1 : Créer TierUnlockHandler.java

**Files:**
- Create: `src/main/java/tong/sihriya/core/TierUnlockHandler.java`
- Modify: `src/main/java/tong/sihriya/core/SchoolProgression.java`

- [ ] **Step 1: Créer TierUnlockHandler.java**

```java
package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.integration.STATModIntegration;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.SchoolSyncPacket;

import java.util.*;

@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class TierUnlockHandler {
    private static final int[] TIER_THRESHOLDS = {1, 25, 50, 75, 100};

    /** Vérifie les déblocages pour un joueur. Appelé après chaque gain d'XP d'école. */
    public static void checkUnlocks(ServerPlayer player, SchoolProgression prog) {
        boolean changed = false;

        // Vérifier les paliers de sorts pour chaque école
        for (var school : SchoolRegistry.getAll()) {
            int level = prog.getLevel(school.id);
            for (int tier = 1; tier <= 4; tier++) {
                int threshold = TIER_THRESHOLDS[tier];
                if (level >= threshold && !prog.isTierUnlocked(school.id, tier)) {
                    unlockTier(player, prog, school.id, tier);
                    changed = true;
                }
            }
            // Ultime au niveau 100
            if (level >= 100 && !prog.isSpellLearned(school.id + ".ultimate")) {
                grantUltimateSpell(player, prog, school.id);
                changed = true;
            }
        }

        // Vérifier le déblocage des écoles avancées
        for (var school : SchoolRegistry.getAll()) {
            if (prog.isSchoolUnlocked(school.id)) continue;
            if (school.unlock == null) continue;

            if (checkUnlockCondition(player, prog, school.unlock)) {
                prog.unlockSchool(school.id);
                // Donner 2 sorts T1 de cette école
                var t1Spells = SpellRegistry.getBySchoolAndTier(school.id, 1);
                var shuffled = new ArrayList<>(t1Spells);
                Collections.shuffle(shuffled, new Random());
                int count = Math.min(2, shuffled.size());
                for (int i = 0; i < count; i++) {
                    prog.learnSpell(shuffled.get(i).id);
                }
                Sihriya.LOGGER.info("Player {} unlocked school: {}", player.getName().getString(), school.id);
                changed = true;
            }
        }

        if (changed) {
            syncSchools(player, prog);
        }
    }

    private static boolean checkUnlockCondition(ServerPlayer player, SchoolProgression prog,
                                                  SchoolRegistry.UnlockCondition unlock) {
        if ("or".equals(unlock.type)) {
            for (int i = 0; i < unlock.schoolIds.length; i++) {
                if (prog.getLevel(unlock.schoolIds[i]) >= unlock.levels[i]) return true;
            }
            return false;
        } else if ("level".equals(unlock.type)) {
            if (unlock.schoolIds.length > 0) {
                return prog.getLevel(unlock.schoolIds[0]) >= unlock.levels[0];
            }
        }
        return false;
    }

    private static void unlockTier(ServerPlayer player, SchoolProgression prog, String schoolId, int tier) {
        var spells = SpellRegistry.getBySchoolAndTier(schoolId, tier);
        for (var spell : spells) {
            prog.learnSpell(spell.id);
        }
        Sihriya.LOGGER.debug("Player {} unlocked tier {} for school {}", player.getName().getString(), tier, schoolId);

        // Bonus STAT Mod : XP dans la stat correspondante
        STATModIntegration.awardSchoolXP(player, schoolId);
    }

    private static void grantUltimateSpell(ServerPlayer player, SchoolProgression prog, String schoolId) {
        var ultimates = SpellRegistry.getBySchoolAndTier(schoolId, 5); // T5 = ultime
        for (var spell : ultimates) {
            prog.learnSpell(spell.id);
        }
        Sihriya.LOGGER.info("Player {} unlocked ULTIMATE for school {}!", player.getName().getString(), schoolId);

        // Bonus STAT Mod massif
        var opt = player.getCapability(
            tong.statmod.capability.PlayerStatsProvider.PLAYER_STATS).resolve();
        opt.ifPresent(stats -> {
            var stat = STATModIntegration.schoolToStat(schoolId);
            if (stat != null) stats.addXp(stat.index, 500);
        });
    }

    private static void syncSchools(ServerPlayer player, SchoolProgression prog) {
        NetworkHandler.sendToPlayer(new SchoolSyncPacket(
            prog.getActiveSchool(), new HashMap<>(), prog.getUnlockedSchools(), prog.getLearnedSpells()
        ), player);
    }
}
```

- [ ] **Step 2: Ajouter isTierUnlocked à SchoolProgression.java**

```java
// Ajouter dans SchoolProgression.java
private final Map<String, Set<Integer>> unlockedTiers = new HashMap<>();

public boolean isTierUnlocked(String schoolId, int tier) {
    return unlockedTiers.getOrDefault(schoolId, Collections.emptySet()).contains(tier);
}

public void unlockTier(String schoolId, int tier) {
    unlockedTiers.computeIfAbsent(schoolId, k -> new HashSet<>()).add(tier);
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/tong/sihriya/core/TierUnlockHandler.java src/main/java/tong/sihriya/core/SchoolProgression.java
git commit -m "feat: add tier unlock system with milestone thresholds and advanced school requirements"
```

---

### Task 3.2 : Modifier PlayerLoginHandler pour le starter lié à STAT Mod

**Files:**
- Modify: `src/main/java/tong/sihriya/core/PlayerLoginHandler.java`

- [ ] **Step 1: Remplacer le random starter par la stat STAT Mod la plus haute**

```java
package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.integration.STATModIntegration;
import tong.sihriya.network.*;
import tong.statmod.capability.PlayerStatsProvider;
import tong.statmod.stats.StatType;

import java.util.*;

@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class PlayerLoginHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        serverPlayer.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).ifPresent(prog -> {
            if (prog.getUnlockedSchools().isEmpty()) {
                initStartingSchool(serverPlayer, prog);
            }
            syncSchools(serverPlayer, prog);
        });
    }

    private static void initStartingSchool(ServerPlayer player, SchoolProgression prog) {
        // Choisir l'école basée sur la stat STAT Mod la plus haute
        var startingSchools = SchoolRegistry.getStartingSchools();
        if (startingSchools.isEmpty()) return;

        String preferredSchool = null;
        int highestLevel = -1;

        var statsOpt = player.getCapability(PlayerStatsProvider.PLAYER_STATS).resolve();
        if (statsOpt.isPresent()) {
            var stats = statsOpt.get();
            for (var school : startingSchools) {
                StatType stat = STATModIntegration.schoolToStat(school.id);
                if (stat != null) {
                    int level = stats.getLevel(stat.index);
                    if (level > highestLevel) {
                        highestLevel = level;
                        preferredSchool = school.id;
                    }
                }
            }
        }

        if (preferredSchool == null) {
            // Fallback aléatoire si STAT Mod pas dispo
            var rand = player.getRandom();
            preferredSchool = startingSchools.get(rand.nextInt(startingSchools.size())).id;
        }

        prog.unlockSchool(preferredSchool);
        prog.setActiveSchool(preferredSchool);

        // Donner 2 sorts T1
        var t1Spells = SpellRegistry.getBySchoolAndTier(preferredSchool, 1);
        if (!t1Spells.isEmpty()) {
            var shuffled = new ArrayList<>(t1Spells);
            Collections.shuffle(shuffled, new Random());
            int count = Math.min(2, shuffled.size());
            for (int i = 0; i < count; i++) {
                prog.learnSpell(shuffled.get(i).id);
            }
        }

        Sihriya.LOGGER.info("Player {} started with school: {} (STAT Mod guided)", player.getName().getString(), preferredSchool);
    }

    private static void syncSchools(ServerPlayer player, SchoolProgression prog) {
        NetworkHandler.sendToPlayer(new SchoolSyncPacket(
            prog.getActiveSchool(), new HashMap<>(), prog.getUnlockedSchools(), prog.getLearnedSpells()
        ), player);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/core/PlayerLoginHandler.java
git commit -m "feat: starting school guided by highest STAT Mod affinity stat"
```

---

### Task 3.3 : Hook TierUnlockHandler dans SpellCastHandler

**Files:**
- Modify: `src/main/java/tong/sihriya/core/SpellCastHandler.java`

- [ ] **Step 1: Ajouter l'appel à TierUnlockHandler après chaque cast**

```java
// À la fin de castSpell(), remplacer l'ancien checkSchoolUnlocks :
// Supprimer l'appel à checkSchoolUnlocks(player, prog)
// Remplacer par :
TierUnlockHandler.checkUnlocks(player, prog);
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/core/SpellCastHandler.java
git commit -m "feat: hook tier unlock checks after each spell cast"
```
