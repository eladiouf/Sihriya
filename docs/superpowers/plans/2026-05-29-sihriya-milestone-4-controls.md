# Milestone 4 : Contrôles & Casting — Touches 1-6

> **Goal:** Remplacer le système de cast actuel (clic droit + roue) par les touches 1-6. Chaque touche lance le meilleur sort connu de l'école correspondante.

**Architecture:** Nouveau SchoolKeyHandler (client) + nouveau SchoolCastPacket (réseau) + SpellCastHandler modifié pour recevoir les schoolId au lieu des spellId. KeyBindings réécrit.

**Tech Stack:** Forge 1.20.1, KeyMapping, SimpleChannel réseau, Capabilities

---

### Task 4.1 : Réécrire KeyBindings.java

**Files:**
- Modify: `src/main/java/tong/sihriya/client/KeyBindings.java`

- [ ] **Step 1: Remplacer les touches (R, V) par (1-6, V, G)**

```java
package tong.sihriya.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
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
    public static final KeyMapping SCHOOL_WHEEL = new KeyMapping(
        "key.sihriya.advanced_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.sihriya.category");

    public static final KeyMapping[] SCHOOL_KEYS = {
        SCHOOL_FIRE, SCHOOL_WATER, SCHOOL_WIND, SCHOOL_EARTH,
        SCHOOL_LIGHTNING, SCHOOL_ICE
    };

    public static final String[] SCHOOL_IDS = {
        "fire", "water", "wind", "earth", "lightning", "ice"
    };
}
```

- [ ] **Step 2: Mettre à jour en_us.json et fr_fr.json**

```json
// Ajouter à en_us.json
{
  "key.sihriya.school_fire": "Fire School",
  "key.sihriya.school_water": "Water School",
  "key.sihriya.school_wind": "Wind School",
  "key.sihriya.school_earth": "Earth School",
  "key.sihriya.school_lightning": "Lightning School",
  "key.sihriya.school_ice": "Ice School",
  "key.sihriya.advanced_wheel": "Advanced School Wheel",
  "key.sihriya.category": "Sihriya Magic"
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/tong/sihriya/client/KeyBindings.java src/main/resources/assets/sihriya/lang/
git commit -m "feat: rework keybinds to 1-6 school keys + G wheel + V meditate"
```

---

### Task 4.2 : Créer SchoolKeyHandler.java

**Files:**
- Create: `src/main/java/tong/sihriya/client/SchoolKeyHandler.java`
- Create: `src/main/java/tong/sihriya/network/SchoolCastPacket.java`

- [ ] **Step 1: Créer SchoolKeyHandler.java**

```java
package tong.sihriya.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.SchoolCastPacket;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT)
public class SchoolKeyHandler {
    private static final boolean[] lastStates = new boolean[6];

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        for (int i = 0; i < 6; i++) {
            boolean isPressed = KeyBindings.SCHOOL_KEYS[i].isDown();
            if (isPressed && !lastStates[i]) {
                // Envoyer la demande de cast au serveur
                NetworkHandler.CHANNEL.sendToServer(
                    new SchoolCastPacket(KeyBindings.SCHOOL_IDS[i]));
            }
            lastStates[i] = isPressed;
        }
    }
}
```

- [ ] **Step 2: Créer SchoolCastPacket.java**

```java
package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import tong.sihriya.core.SpellCastHandler;

public class SchoolCastPacket {
    private final String schoolId;

    public SchoolCastPacket(String schoolId) {
        this.schoolId = schoolId;
    }

    public static void encode(SchoolCastPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.schoolId);
    }

    public static SchoolCastPacket decode(FriendlyByteBuf buf) {
        return new SchoolCastPacket(buf.readUtf(32));
    }

    public static void handle(SchoolCastPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getSender() instanceof ServerPlayer player) {
                SpellCastHandler.castBestSpell(player, packet.schoolId);
            }
        });
        context.setPacketHandled(true);
    }
}
```

- [ ] **Step 3: Enregistrer le packet dans NetworkHandler.java**

```java
// Ajouter dans NetworkHandler.register()
CHANNEL.registerMessage(packetId++, SchoolCastPacket.class,
    SchoolCastPacket::encode, SchoolCastPacket::decode, SchoolCastPacket::handle);
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/tong/sihriya/client/SchoolKeyHandler.java src/main/java/tong/sihriya/network/SchoolCastPacket.java src/main/java/tong/sihriya/network/NetworkHandler.java
git commit -m "feat: school key handler sends cast packet to server on key press"
```

---

### Task 4.3 : Modifier SpellCastHandler pour castBestSpell

**Files:**
- Modify: `src/main/java/tong/sihriya/core/SpellCastHandler.java`

- [ ] **Step 1: Ajouter la méthode castBestSpell + intégrer le scaling STAT Mod**

```java
// Ajouter dans SpellCastHandler.java :

public static boolean castBestSpell(ServerPlayer player, String schoolId) {
    var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
    if (progOpt.isEmpty()) return false;
    var prog = progOpt.get();

    if (!prog.isSchoolUnlocked(schoolId)) return false;

    // Trouver le meilleur sort connu de cette école (tier le plus haut)
    var spells = prog.getLearnedSpells().stream()
        .filter(s -> s.startsWith(schoolId + "."))
        .sorted((a, b) -> {
            var sa = SpellRegistry.get(a);
            var sb = SpellRegistry.get(b);
            if (sa == null || sb == null) return 0;
            return Integer.compare(sb.tier, sa.tier); // plus haut tier d'abord
        })
        .toList();

    if (spells.isEmpty()) return false;

    String bestSpellId = spells.get(0);
    return castSpell(player, bestSpellId);
}
```

- [ ] **Step 2: Modifier executeEffects pour utiliser STAT Mod scaling**

```java
private static void executeEffects(ServerPlayer player, SpellData spell) {
    float schoolLevel = 0;
    var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
    if (progOpt.isPresent()) {
        schoolLevel = progOpt.get().getLevel(spell.school);
    }

    // STAT Mod damage multiplier
    float statMultiplier = STATModIntegration.getDamageMultiplier(player, spell.school);
    float secondaryBonus = STATModIntegration.getSecondaryBonus(player, spell.school);

    for (SpellEffect effect : spell.effects) {
        float value = (effect.baseValue + (schoolLevel * effect.scaling)) * statMultiplier;

        // Bonus secondaire selon le type d'effet
        if (("slow".equals(effect.type) || "freeze".equals(effect.type)) && secondaryBonus > 0) {
            value *= (1 + secondaryBonus);
        }

        switch (effect.type) {
            case "damage" -> applyDamage(player, value);
            case "burn" -> applyBurn(player, (int) value, effect.duration);
            case "slow" -> applySlow(player, effect.duration);
            case "knockback" -> applyKnockback(player, value);
            case "stun" -> applyStun(player, effect.duration);
            case "freeze" -> applyFreeze(player, effect.duration);
            case "chain" -> applyChain(player, value, effect.duration);
            case "heal" -> applyHeal(player, value);
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/tong/sihriya/core/SpellCastHandler.java
git commit -m "feat: castBestSpell method + STAT Mod damage scaling in spell effects"
```

---

### Task 4.4 : Supprimer l'ancien RightClickCastHandler

**Files:**
- Delete: `src/main/java/tong/sihriya/core/RightClickCastHandler.java`

- [ ] **Step 1: Supprimer le fichier et nettoyer les références**

```bash
git rm src/main/java/tong/sihriya/core/RightClickCastHandler.java
```

- [ ] **Step 2: Commit**

```bash
git commit -m "remove: RightClickCastHandler replaced by school key system (1-6)"
```

---

### Task 4.5 : Mettre à jour ClientSetup.java

**Files:**
- Modify: `src/main/java/tong/sihriya/client/ClientSetup.java`

- [ ] **Step 1: Enregistrer les nouvelles touches et le SchoolKeyHandler**

```java
package tong.sihriya.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tong.sihriya.Sihriya;
import tong.sihriya.client.gui.ManaOverlay;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(ManaOverlay.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new SchoolKeyHandler());
    }

    @SubscribeEvent
    public static void onKeyMappings(RegisterKeyMappingsEvent event) {
        for (var key : KeyBindings.SCHOOL_KEYS) event.register(key);
        event.register(KeyBindings.MEDITATE);
        event.register(KeyBindings.SCHOOL_WHEEL);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "sihriya_mana", ManaOverlay.INSTANCE);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/client/ClientSetup.java
git commit -m "feat: register school keys and SchoolKeyHandler in client setup"
```
