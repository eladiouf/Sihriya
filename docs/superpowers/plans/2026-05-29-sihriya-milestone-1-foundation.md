# Milestone 1 : Foundation — Dépendances & Structure

> **Goal:** Le mod se charge avec Forge + Epic Fight + STAT Mod obligatoires, structure de base en place.

**Architecture:** Mise à jour de build.gradle, mods.toml, et classe principale Sihriya.java pour déclarer les dépendances obligatoires. Création des packages vides pour la refonte.

**Tech Stack:** Minecraft Forge 1.20.1, Java 17, Gradle

---

### Task 1.1 : Mettre à jour les dépendances Gradle

**Files:**
- Modify: `build.gradle`
- Modify: `gradle.properties`
- Modify: `settings.gradle`

- [ ] **Step 1: Ajouter STAT Mod en dépendance curse maven dans build.gradle**

```groovy
repositories {
    flatDir { dir 'libs' }
    maven { name = 'ParchmentMC'; url = 'https://maven.parchmentmc.org' }
    maven { url "https://cursemaven.com"; content { includeGroup "curse.maven" } }
    maven { name = "Modrinth"; url = "https://api.modrinth.com/maven"; content { includeGroup "maven.modrinth" } }
}

dependencies {
    minecraft "net.minecraftforge:forge:${minecraft_version}-${forge_version}"

    // Epic Fight (local JAR)
    implementation fg.deobf("blank:epic-fight-20.14.17-mc1.20.1-forge:20.14.17")

    // STAT Mod (sibling project)
    implementation project(':STAT_MOD')
}
```

- [ ] **Step 2: Mettre à jour settings.gradle pour inclure STAT Mod**

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { name = 'MinecraftForge'; url = 'https://maven.minecraftforge.net/' }
        maven { url = 'https://maven.parchmentmc.org' }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.7.0'
}

rootProject.name = 'sihriya'

// Include STAT Mod as a sub-project
include ':STAT_MOD'
project(':STAT_MOD').projectDir = file('../STAT_MOD')
```

- [ ] **Step 3: Vérifier que gradle.properties a les bonnes versions**

```properties
org.gradle.jvmargs=-Xmx3G
org.gradle.daemon=false

minecraft_version=1.20.1
minecraft_version_range=[1.20.1,1.21)
forge_version=47.4.20
forge_version_range=[47,)
loader_version_range=[47,)
mapping_channel=parchment
mapping_version=2023.09.03-1.20.1

mod_id=sihriya
mod_name=Sihriya
mod_license=All Rights Reserved
mod_version=1.0.0
mod_group_id=tong.sihriya
mod_authors=ela_juff
mod_description=Système de magie élémentaire — addon Epic Fight + STAT Mod
```

- [ ] **Step 4: Tester la compilation**

Run: `.\gradlew build`
Expected: BUILD SUCCESSFUL, JAR dans build/libs/

- [ ] **Step 5: Commit**

```bash
git add build.gradle settings.gradle gradle.properties
git commit -m "build: add Epic Fight and STAT Mod as mandatory dependencies"
```

---

### Task 1.2 : Mettre à jour mods.toml

**Files:**
- Modify: `src/main/resources/META-INF/mods.toml`

- [ ] **Step 1: Remplacer le contenu de mods.toml avec dépendances obligatoires**

```toml
modLoader="javafml"
loaderVersion="${loader_version_range}"
license="${mod_license}"

[[mods]]
modId="${mod_id}"
version="${mod_version}"
displayName="${mod_name}"
authors="${mod_authors}"
description='''${mod_description}'''

[[dependencies.${mod_id}]]
    modId="forge"
    mandatory=true
    versionRange="${forge_version_range}"
    ordering="NONE"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="minecraft"
    mandatory=true
    versionRange="${minecraft_version_range}"
    ordering="NONE"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="epicfight"
    mandatory=true
    versionRange="[20.14.17,)"
    ordering="NONE"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="statmod"
    mandatory=true
    versionRange="[1.0.0,)"
    ordering="NONE"
    side="BOTH"
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/META-INF/mods.toml
git commit -m "feat: make Epic Fight and STAT Mod mandatory dependencies"
```

---

### Task 1.3 : Nettoyer la classe principale Sihriya.java

**Files:**
- Modify: `src/main/java/tong/sihriya/Sihriya.java`
- Create: `src/main/java/tong/sihriya/integration/STATModIntegration.java`
- Create: `src/main/java/tong/sihriya/integration/EpicFightIntegration.java`

- [ ] **Step 1: Simplifier Sihriya.java — supprimer la détection optionnelle, configurer STAT Mod au load**

```java
package tong.sihriya;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tong.sihriya.data.DataLoader;
import tong.sihriya.integration.STATModIntegration;
import tong.sihriya.network.NetworkHandler;

@Mod(Sihriya.MODID)
public class Sihriya {
    public static final String MODID = "sihriya";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public Sihriya(FMLJavaModLoadingContext context) {
        var bus = context.getModEventBus();
        bus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DataLoader.loadAll();
            NetworkHandler.register();
            STATModIntegration.init();
            LOGGER.info("Sihriya chargé avec Epic Fight + STAT Mod !");
        });
    }
}
```

- [ ] **Step 2: Créer STATModIntegration.java (bridge vide pour l'instant)**

```java
package tong.sihriya.integration;

import tong.sihriya.Sihriya;

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
}
```

- [ ] **Step 3: Créer EpicFightIntegration.java (bridge vide pour l'instant)**

```java
package tong.sihriya.integration;

import tong.sihriya.Sihriya;

public class EpicFightIntegration {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        try {
            Class.forName("yesman.epicfight.main.EpicFightMod");
            Sihriya.LOGGER.info("Epic Fight détecté et intégré !");
            initialized = true;
        } catch (ClassNotFoundException e) {
            Sihriya.LOGGER.error("Epic Fight est requis mais introuvable !");
            throw new RuntimeException("Epic Fight manquant");
        }
    }

    public static boolean isInitialized() { return initialized; }
}
```

- [ ] **Step 4: Compiler et vérifier**

Run: `.\gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add src/main/java/tong/sihriya/Sihriya.java src/main/java/tong/sihriya/integration/STATModIntegration.java src/main/java/tong/sihriya/integration/EpicFightIntegration.java
git commit -m "feat: refactor main class with mandatory STAT Mod and Epic Fight bridges"
```

---

### Task 1.4 : Créer la structure de packages vide

**Files:**
- Create: `src/main/java/tong/sihriya/animation/package-info.java`
- Create: `src/main/java/tong/sihriya/projectile/package-info.java`

- [ ] **Step 1: Créer les nouveaux packages avec des fichiers package-info**

```java
// src/main/java/tong/sihriya/animation/package-info.java
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package tong.sihriya.animation;

import net.minecraft.MethodsReturnNonnullByDefault;
import javax.annotation.ParametersAreNonnullByDefault;
```

```java
// src/main/java/tong/sihriya/projectile/package-info.java
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package tong.sihriya.projectile;

import net.minecraft.MethodsReturnNonnullByDefault;
import javax.annotation.ParametersAreNonnullByDefault;
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/animation/ src/main/java/tong/sihriya/projectile/
git commit -m "chore: create animation and projectile package stubs"
```
