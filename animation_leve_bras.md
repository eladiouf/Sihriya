# Animation : Lanceur bras au ciel, paumes ouvertes, braises dansantes

## Coordinate System

| Axe | Direction | Dans le rig |
|-----|-----------|-------------|
| **X** | Droite/Gauche | Épaules alignées sur X |
| **Y** | Avant/Arrière | Le personnage fait face à +Y |
| **Z** | Haut/Bas | Gravité = −Z, ciel = +Z |

## Structure du Rig (20 os)

```
Root
├── Thigh_R          (hanche droite, X positif)
│   ├── Leg_R        (cuisse → arrière)
│   └── Knee_R       (os helper, même position que Leg_R)
├── Thigh_L          (hanche gauche, X négatif)
│   ├── Leg_L
│   └── Knee_L
└── Torso            (tronc, Y=0.05 de Root)
    └── Chest        (torse, Y=0.3 de Torso)
        ├── Head     (tête, Y=0.4 de Chest)
        ├── Shoulder_R  (épaule droite, enfant de Chest)
        │   └── Arm_R
        │       ├── Hand_R → Tool_R (outil tenu)
        │       └── Elbow_R (helper)
        └── Shoulder_L
            └── Arm_L
                ├── Hand_L → Tool_L
                └── Elbow_L
```

- Les os **Knee_R/L** et **Elbow_R/L** sont des helpers (déformation/IK) — on ne les anime pas directement.
- **Tool_R/L** représentent ce que les mains tiennent. On les utilise pour simuler l'ouverture/fermeture des doigts (pas de phalanges dans le rig).

## Principe d'humanisation

| Principe | Application |
|----------|-------------|
| **Respiration** | Chest se soulève (rx) avant et pendant le mouvement |
| **Anticipation** | Les épaules commencent à tourner avant que les bras ne montent |
| **Arc naturel** | Les bras suivent une trajectoire courbe, pas linéaire |
| **Suivi de tête** | La tête bascule en arrière pour regarder les mains |
| **Équilibre** | Le Root se décale pour compenser le changement de centre de masse |
| **Rebond** | Les outils "doigts" s'ouvrent/ferment avec un mouvement parenthésé (ease-in/ease-out) |
| **Braises** | Head + Torso + Chest pivotent subtilement pour suivre des particules imaginaires |

## Script Blender

```python
"""
Animation : Bras levés au ciel, paumes ouvertes, attrape, braises dansantes.
Coordinate system : X=épaules  Y=face  Z=ciel

Exécute dans Blender :
  1. Sélectionne l'armature
  2. Ouvre ce script dans l'éditeur texte
  3. Run Script
"""

import bpy
import math
from mathutils import Euler, Vector


# ──────────────────────────────────────────────
# CONFIG — Ajuste ces angles si le rendu visuel
#          ne correspond pas à ton armature.
#          Les valeurs sont en RADIANS.
#          Dans Blender pose mode, rotation_euler
#          est RELATIF à la pose de repos.
# ──────────────────────────────────────────────
CFG = {
    # Bras levés : angle X des épaules (positif = bras vers le haut)
    "shoulder_raise": 1.5,
    # Bras levés : angle Z (twist/ajustement fin)
    "shoulder_twist": 0.2,
    # Coude : flexion (Z pour la plupart des rigs)
    "elbow_bend": 0.35,
    # Main : paume vers le ciel (angle X)
    "palm_open": math.pi / 2,
    # Main : rotation secondaire (poignet)
    "wrist_twist": 0.2,
    # "Doigts" (Tool) : angle d'ouverture
    "fingers_open": 0.65,
    # "Doigts" (Tool) : angle de fermeture
    "fingers_close": 0.15,
    # Tête : bascule arrière (X négatif = regard vers le haut)
    "head_tilt": -0.55,
    # Torse cambré (X positif = extension)
    "chest_arch": 0.2,
    # Taille rotation
    "torso_bend": 0.08,
    # FPS
    "fps": 60,
}


# ── Setup ──
obj = bpy.context.view_layer.objects.active
if not obj or obj.type != 'ARMATURE':
    obj = bpy.data.objects.get('Armature')
    if not obj:
        raise Exception("Sélectionne l'armature d'abord.")
bpy.context.view_layer.objects.active = obj
bpy.ops.object.mode_set(mode='POSE')
bones = obj.pose.bones
for b in bones:
    b.rotation_mode = 'XYZ'

if obj.animation_data:
    obj.animation_data_clear()

FPS = CFG["fps"]


def rot(name, xyz, frame):
    bones[name].rotation_euler = Euler(xyz)
    bones[name].keyframe_insert('rotation_euler', frame=frame)


def loc(name, xyz, frame):
    bones[name].location = Vector(xyz)
    bones[name].keyframe_insert('location', frame=frame)


ALL = [
    'Root', 'Thigh_R', 'Leg_R', 'Knee_R',
    'Thigh_L', 'Leg_L', 'Knee_L',
    'Torso', 'Chest', 'Head',
    'Shoulder_R', 'Arm_R', 'Hand_R', 'Tool_R', 'Elbow_R',
    'Shoulder_L', 'Arm_L', 'Hand_L', 'Tool_L', 'Elbow_L',
]

SR = CFG["shoulder_raise"]
ST = CFG["shoulder_twist"]
EB = CFG["elbow_bend"]
PO = CFG["palm_open"]
WT = CFG["wrist_twist"]
FO = CFG["fingers_open"]
FC = CFG["fingers_close"]
HT = CFG["head_tilt"]
CA = CFG["chest_arch"]
TB = CFG["torso_bend"]

# ────────────────────────────────────────
# FRAME 0 — POSE DE REPOS
# ────────────────────────────────────────
for n in ALL:
    rot(n, (0, 0, 0), 0)
loc('Root', (0, 0, 0), 0)

# ────────────────────────────────────────
# PHASE 1 — INSPIRATION / ANTICIPATION  (0→12)
# ────────────────────────────────────────
rot('Chest',      (CA * 0.3, 0, 0), 12)
rot('Torso',      (TB * 0.4, 0, 0), 12)
rot('Head',       (HT * 0.15, 0, 0), 12)
rot('Shoulder_R', (SR * 0.15, 0, -ST * 0.3), 12)
rot('Shoulder_L', (SR * 0.15, 0,  ST * 0.3), 12)
loc('Root',       (0, -0.005, 0.003), 12)  # léger recul

# ────────────────────────────────────────
# PHASE 2 — MONTÉE DES BRAS  (12→28)
# ────────────────────────────────────────
rot('Shoulder_R', (SR, 0, -ST),   28)
rot('Shoulder_L', (SR, 0,  ST),   28)
rot('Arm_R',      (0.08, 0,  EB), 28)
rot('Arm_L',      (-0.08, 0, -EB), 28)
rot('Hand_R',     (PO * 0.6, 0, WT * 0.5), 28)
rot('Hand_L',     (-PO * 0.6, 0, -WT * 0.5), 28)
rot('Head',       (HT * 0.85, 0, 0), 28)
rot('Chest',      (CA, 0, 0), 28)
rot('Torso',      (TB, 0, 0), 28)
loc('Root',       (-0.005, -0.01, 0.012), 28)

# ────────────────────────────────────────
# PHASE 3 — PAUMES AU CIEL + DOIGTS OUVERTS  (28→38)
# ────────────────────────────────────────
rot('Hand_R',      (PO, WT * 0.4,  WT),   38)
rot('Hand_L',      (-PO, -WT * 0.4, -WT), 38)
rot('Tool_R',      (0.1,  FO, -0.05),     38)
rot('Tool_L',      (-0.1, -FO, 0.05),     38)
rot('Shoulder_R',  (SR * 0.9, 0, -ST),    38)  # léger abaissement
rot('Shoulder_L',  (SR * 0.9, 0,  ST),    38)
rot('Arm_R',       (0.02, 0, EB * 0.85),  38)
rot('Arm_L',       (-0.02, 0, -EB * 0.85), 38)

# ────────────────────────────────────────
# PHASE 4 — ATTRAPE !  (38→46)
# ────────────────────────────────────────
rot('Tool_R',      (0.05, FC, 0),     46)
rot('Tool_L',      (-0.05, -FC, 0),   46)
rot('Hand_R',      (PO, WT * 0.15, WT * 0.3), 46)
rot('Hand_L',      (-PO, -WT * 0.15, -WT * 0.3), 46)
rot('Chest',       (CA * 0.85, 0.06, 0.05), 46)
rot('Head',        (HT * 0.9,  0.06, 0.03), 46)
loc('Root',        (0.005, -0.008, 0.008),  46)  # rebond

# ────────────────────────────────────────
# PHASE 5 — RELÂCHE  (46→56)
# ────────────────────────────────────────
rot('Tool_R',      (0.15, FO * 1.15, 0.05),  56)
rot('Tool_L',      (-0.15, -FO * 1.15, -0.05), 56)
rot('Hand_R',      (PO, WT * 0.6, WT * 0.2),   56)
rot('Hand_L',      (-PO, -WT * 0.6, -WT * 0.2), 56)

# ────────────────────────────────────────
# PHASE 6 — DEUXIÈME ATTRAPE  (56→66)
# ────────────────────────────────────────
rot('Tool_R',      (0.08, FC, 0),      66)
rot('Tool_L',      (-0.08, -FC, 0),    66)
rot('Hand_R',      (PO, WT * 0.2, WT * 0.4), 66)
rot('Hand_L',      (-PO, -WT * 0.2, -WT * 0.4), 66)
rot('Chest',       (CA * 0.9, -0.04, -0.03), 66)
rot('Head',        (HT * 0.85, -0.05, 0.02), 66)

# ────────────────────────────────────────
# BRAISES — DANSE SUBTILE DU CORPS
# ────────────────────────────────────────

# Root — balancement du poids
loc('Root', ( 0.012, -0.006,  0.008), 18)
loc('Root', (-0.018, -0.008, -0.004), 32)
loc('Root', ( 0.025, -0.004,  0.012), 44)
loc('Root', (-0.015, -0.01,  -0.01),  55)
loc('Root', ( 0.018, -0.007,  0.008), 68)
loc('Root', (-0.008, -0.006, -0.006), 80)
loc('Root', ( 0,     -0.006,  0),     92)

# Torso — rotation douce AX (bascule) + AZ (torque)
rot('Torso', (TB * 1.2, 0,  0.10), 30)
rot('Torso', (TB * 0.6, 0, -0.08), 44)
rot('Torso', (TB * 1.3, 0,  0.06), 58)
rot('Torso', (TB * 0.8, 0, -0.09), 72)
rot('Torso', (TB,       0,  0),    88)

# Chest — suit les braises + respiration
rot('Chest', (CA,       0.05,  0.10), 30)
rot('Chest', (CA * 0.7, -0.04, -0.08), 44)
rot('Chest', (CA * 1.1,  0.03,  0.07), 58)
rot('Chest', (CA * 0.8, -0.05, -0.06), 72)
rot('Chest', (CA,        0,     0),    88)

# Head — regarde les braises flotter autour
rot('Head', (HT * 0.9, -0.15,  0.10), 30)
rot('Head', (HT * 0.7,  0.20, -0.06), 44)
rot('Head', (HT * 1.0, -0.12,  0.12), 58)
rot('Head', (HT * 0.8,  0.18, -0.10), 72)
rot('Head', (HT * 0.9,  0,     0),    88)

# Avant-bras flottent avec les braises
rot('Arm_R', ( 0.15,  0.04, EB),      34)
rot('Arm_L', (-0.15, -0.04, -EB),     34)
rot('Arm_R', (-0.06, -0.03, EB * 1.15), 52)
rot('Arm_L', ( 0.06,  0.03, -EB * 1.15), 52)
rot('Arm_R', ( 0.12,  0.02, EB * 0.9), 68)
rot('Arm_L', (-0.12, -0.02, -EB * 0.9), 68)

# ────────────────────────────────────────
# PHASE 7 — TENUE SOUTENUE  (88→110)
# ────────────────────────────────────────
rot('Shoulder_R', (SR * 0.9, 0, -ST),     110)
rot('Shoulder_L', (SR * 0.9, 0,  ST),     110)
rot('Arm_R',      (0.05, 0, EB * 0.85),   110)
rot('Arm_L',      (-0.05, 0, -EB * 0.85), 110)
rot('Hand_R',     (PO, WT * 0.3, WT),     110)
rot('Hand_L',     (-PO, -WT * 0.3, -WT),  110)
rot('Tool_R',     (0.05, FC, 0),          110)
rot('Tool_L',     (-0.05, -FC, 0),        110)
rot('Head',       (HT * 0.8, 0, 0),       110)
rot('Chest',      (CA * 0.9, 0, 0),       110)
rot('Torso',      (TB * 0.75, 0, 0),      110)
rot('Root',       (0, 0, 0),              110)
loc('Root',       (0, -0.006, 0),         110)

# ────────────────────────────────────────
# INTERPOLATION BEZIER
# ────────────────────────────────────────
if obj.animation_data and obj.animation_data.action:
    for fc in obj.animation_data.action.fcurves:
        for kf in fc.keyframe_points:
            kf.interpolation = 'BEZIER'

# ────────────────────────────────────────
# RAPPORT
# ────────────────────────────────────────
durée = 110 / FPS
print(f"✔ Animation générée : 0→110 frames à {FPS}fps = {durée:.1f}s")
print(f"  Bras levés  (Shoulder X = {SR:.2f} rad)")
print(f"  Paumes ciel (Hand X = {PO:.2f} rad)")
print(f"  Doigts ouv/ferm (Tool Y = {FO:.2f} / {FC:.2f} rad)")
print(f"  Tête bascule (Head X = {HT:.2f} rad)")
print("  Braises : Root sways + Torso/Chest/Head track")
```

## Ajustement rapide

Si les rotations ne correspondent pas aux axes locaux de tes os, édite le dictionnaire `CFG` en haut du script :

| Variable | Effet | Valeur typique |
|----------|-------|----------------|
| `shoulder_raise` | Monte/descend les bras | `1.5` |
| `shoulder_twist` | Torsion de l'épaule | `0.2` |
| `elbow_bend` | Plie le coude | `0.35` |
| `palm_open` | Rotation paume ciel | `π/2` |
| `fingers_open` | Écart des "doigts" | `0.65` |
| `fingers_close` | Fermeture poing | `0.15` |
| `head_tilt` | Bascule tête (regard haut) | `-0.55` |
| `chest_arch` | Cambrure du torse | `0.2` |

### Trouver le bon axe

1. Passe l'armature en **Pose Mode**
2. Sélectionne un os (ex: Shoulder_R)
3. Dans le panneau *Item* (touche N), fais tourner manuellement autour de X, Y, Z
4. Observe quel axe monte le bras → mets cette valeur dans `shoulder_raise`
5. Idem pour chaque os

## Timeline résumée

```
Frame: 0        12        28        38   46   56   66        88       110
       |    Resp |  Bras   | Paumes |Catch|Open |Catch| ... | Tenue   |
       |    Anti |  montent| ciel   |  #1 |  #2 |  #2 |     |soutenue |
       |         |         | Doigts |     |     |     |     |         |
       |◄━━━━━ Braises : Root/Torso/Chest/Head sway continu ━━━━━━━━━►|
```
