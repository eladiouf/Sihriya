# Handoff — Crash runClient (après fix mixin)

## État actuel

✅ Le fix `mixin.env.remapRefMap` dans `build.gradle` fonctionne
✅ Plus aucun crash lié aux mixins d'EpicFight/Parcool
❌ Le jeu plante pendant le chargement des mods — **2 erreurs dans le code des mods**

---

## Erreur #1 — sihriya: ResourceLocation invalide

```
net.minecraft.ResourceLocationException:
Non [a-z0-9/._-] character in path of location: sihriya:sihriya:cast_projectile
```

**Fichier** : `SpellAnimationManager.java:34`  
**Cause** : Le `ResourceLocation` est construit avec `sihriya:sihriya:cast_projectile` au lieu de `sihriya:cast_projectile`.  
**Le namespace et le path sont séparés par UN seul `:`. Ici il y a deux `:`**

```java
// Bogue — deux-points en trop
new ResourceLocation("sihriya:sihriya:cast_projectile");

// Corrigé
new ResourceLocation("sihriya", "cast_projectile");
// ou
new ResourceLocation("sihriya:cast_projectile");
```

**Fichier à corriger** : `src/main/java/tong/sihriya/animation/SpellAnimationManager.java` ligne 34.

---

## Erreur #2 — statmod: NoSuchFieldError

```
java.lang.NoSuchFieldError: f_43602_
  at tong.statmod.STATMod.lambda$commonSetup$0(STATMod.java:72)
```

**Fichier** : `STATMod.java:72`  
**Cause** : Le champ `f_43602_` (nom Mojang/Obfuscated) n'existe pas dans la classe cible.  
**Raisons possibles** :
1. Le mod `statmod` a été compilé avec des mappings différents de ceux utilisés par le dev environment
2. Le champ a été renommé ou supprimé dans la version de Forge/MC utilisée
3. `statmod` est importé comme JAR compilé (`STAT_MOD/build/libs/statmod-1.0.0.jar`) — il est peut-être obfusqué

**Solutions** :
1. Recompiler `statmod` depuis le projet source `STAT_MOD` avec les mêmes mappings (Parchment 2023.09.03)
2. Si c'est un projet séparé, le référencer comme `project(':STAT_MOD')` au lieu de prendre le JAR compilé
3. Chercher le champ `f_43602_` et le remplacer par son vrai nom dans le code source

---

## Fichiers clés

| Fichier | Problème |
|---------|----------|
| `sihriya/.../SpellAnimationManager.java:34` | `ResourceLocation` double colon |
| `statmod/.../STATMod.java:72` | `NoSuchFieldError: f_43602_` |
| `build.gradle` | ✅ Fix mixin appliqué |

## Étapes pour le prochain agent

1. Lire `SpellAnimationManager.java` ligne ~34, corriger le `ResourceLocation`
2. Lire `STATMod.java` ligne ~72, remplacer `f_43602_` par le bon nom de champ
3. Relancer `runClient` pour vérifier
