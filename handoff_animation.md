# Handoff — Animation EpicFight Rig (Bras au ciel / Mage)

## Contexte

L'utilisateur a un rig bipède **20 os** importé d'un glTF vers Blender (via EpicFight addon). Le format des données sources est une hiérarchie JSON avec matrices 4×4 row-major.

### Problème initial
- Le rig NE supporte PAS les F-Curves (sans doute export vers moteur de jeu)
- L'utilisateur veut un script Blender Python qui génère l'animation par keyframes
- Le script doit être exécutable dans l'éditeur texte de Blender

### Coordinate System (confirmé par l'utilisateur)
- **X** = épaules (droite/gauche)
- **Y** = face (avant/arrière)
- **Z** = ciel (haut/bas)

Le personnage fait face à +Y, la gravité est en -Z.

## Structure du Rig

```
Root (hanche, à Z≈0.76)
├── Thigh_R (x=+0.125) → Leg_R, Knee_R (helper)
├── Thigh_L (x=-0.125) → Leg_L, Knee_L (helper)
└── Torso (y=0.05) → Chest (y=0.3)
    ├── Head (y=0.4)
    ├── Shoulder_R → Arm_R → Hand_R → Tool_R
    │                     └── Elbow_R (helper)
    └── Shoulder_L → Arm_L → Hand_L → Tool_L
                      └── Elbow_L (helper)
```

- **Knee_R/L** et **Elbow_R/L** sont des os helpers (IK/déformation) — on ne les anime pas
- **Tool_R/L** = objets tenus dans les mains — utilisés pour simuler l'ouverture/fermeture des doigts
- Le rig n'a **pas d'os de doigts individuels**

## Animation demandée

"Le lanceur lève les deux bras au-dessus de la tête, paumes face au ciel. Les doigts s'ouvrent et se ferment comme pour attraper quelque chose. Des braises dansent autour du corps."

### Principes d'humanisation (mage)
- Respiration (Chest se soulève avant le mouvement)
- Anticipation (épaules commencent à tourner avant que les bras ne montent)
- Arc naturel (trajectoire courbe, pas linéaire)
- Suivi de tête (regarde les mains)
- Overlap/Follow-through (différentes parties bougent à des temps différents)
- Contre-poids (Root se décale)
- Les braises = sway continu du corps (Root, Torso, Chest, Head)

## Ce qui a été essayé / Résultats

### Version 1 — Euler Y-up
Script avec `rotation_mode='XYZ'` et angles Euler codés en dur. Assumait Y-up. ❌ Pas testé, axes faux.

### Version 2 — Euler Z-up avec CONFIG
Script avec angles configurables en haut. Ajouté try/except pour l'erreur `fcurves`.  
❌ L'utilisateur a signalé que l'animation "n'est pas bonne" — angles pas naturels.

### Version 3 — Quaternion direction targeting (actuelle)
Fichier : `C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA\animation_leve_bras.py`

Utilise `point_y()` qui calcule un quaternion pour orienter l'axe Y de l'os vers une direction cible monde.  
✅ Mathématiquement correct pour toute orientation de repos  
✅ Plus besoin de deviner les axes locaux  
⏳ Pas encore testé par l'utilisateur

## Problèmes rencontrés

### 1. `'Action' object has no attribute 'fcurves'`
- Solution : `hasattr(action, "fcurves")` + `bpy.context.view_layer.update()` avant
- Le `.blend` utilise peut-être un type Action personnalisé (EpicFight addon)

### 2. Orientation des os inconnue
- Les matrices 4×4 du JSON sont en row-major
- L'axe Y de chaque os pointe "le long" de l'os
- Impossible de prédire les axes Euler sans test visuel
- Solution : `point_y()` avec quaternion cible

### 3. Pas de phalanges
- Tool_R/L utilisés comme "doigts" (rotation autour de leur axe Y pour ouvrir/fermer)

## Script actuel

**Fichier** : `C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA\animation_leve_bras.py`  
**Fonctions clés** :

```python
point_y(bone_name, target_dir, frame)
  # Calcule le quaternion pour que l'axe Y de l'os pointe vers target_dir
  # target_dir = (x, y, z) dans le monde
  # Ex: (0, 0, 1) = tout droit vers le haut

euler(bone_name, xyz, frame)
  # Rotation Euler classique pour le contrôle fin

loc(bone_name, xyz, frame)
  # Translation / position
```

**Os utilisés avec point_y()** : Shoulder_R/L, Head  
**Os en Euler** : Arm_R/L (coude), Hand_R/L (poignet), Tool_R/L (doigts), Chest, Torso  
**Os en location** : Root

## Ce qu'il reste à faire / Améliorer

1. **Tester la version quaternion** — demander à l'utilisateur de lancer le script
2. **Ajuster les directions cibles** si les bras ne pointent pas correctement :
   - `target_dir` de Shoulder_R = `(dx, dy, dz)` où :
     - dx = composante latérale (+ = droite)
     - dy = composante avant (+ = devant)
     - dz = composante verticale (+ = haut)
   - Pour des bras vers le ciel : `(0.15, 0.15, 0.98)` = léger droite, léger avant, surtout haut
3. **Peut-être changer les FPS** (actuellement 60)
4. **Ajouter plus de frames** si le mouvement est trop rapide
5. **Améliorer la danse des braises** — actuellement c'est un sway sinusoidal simple
6. **Ajouter des clés de respiration** plus marquées
7. **Gérer le cas où le rig a des contraintes** (EpicFight utilise peut-être des contraintes qui écrasent les keyframes)

## Pour le prochain agent

Si tu reprends ce projet :

1. Lis d'abord `animation_leve_bras.py` pour voir l'état actuel
2. Demande à l'utilisateur de tester et de décrire CE QUI NE VA PAS précisément :
   - Les bras vont dans quelle direction ?
   - Quelle partie du corps bouge mal ?
   - Est-ce trop rapide / trop lent ?
3. Ajuste les `target_dir` dans les appels à `point_y()` pour corriger la direction
4. Ajuste les angles Euler pour les mains/doigts si nécessaire
5. Vérifie que l'interpolation Bezier fonctionne (sinon les keyframes sont linéaires)

**Si `point_y()` ne donne pas le bon résultat**, vérifie que `pbone.bone.matrix_local` donne bien la matrice de repos correcte dans l'espace armature. Sur certains rigs, il faut peut-être utiliser `pbone.bone.matrix` en edit mode ou une autre matrice.

## Fichiers

| Fichier | Description |
|---------|-------------|
| `animation_leve_bras.py` | Script Blender actuel (quaternion) |
| `animation_leve_bras.md` | Documentation markdown (obsolète) |
| `handoff_animation.md` | Ce fichier — instructions pour le prochain agent |
