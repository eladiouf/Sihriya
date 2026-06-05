/import bpy
import math
from mathutils import Vector, Quaternion

# ── Setup ──
obj = bpy.context.view_layer.objects.active
if not obj or obj.type != 'ARMATURE':
    obj = bpy.data.objects.get('Armature')
    if not obj:
        raise Exception("Selectionne l'armature d'abord.")
bpy.context.view_layer.objects.active = obj
bpy.ops.object.mode_set(mode='POSE')
bones = obj.pose.bones

if obj.animation_data:
    obj.animation_data_clear()

def point_y(bone_name, target_dir, frame):
    """Tourne l'os pour que son axe Y pointe vers target_dir (monde).
    La rotation est relative a la pose de repos — fonctionne
    quel que soit l'orientation de repos de l'os."""
    pbone = bones[bone_name]
    rest = pbone.bone.matrix_local          # 4x4 repos en espace armature
    rest_y = (rest @ Vector((0, 1, 0, 0))).xyz.normalized()
    target = Vector(target_dir).normalized()
    q = rest_y.rotation_difference(target)
    pbone.rotation_mode = 'QUATERNION'
    pbone.rotation_quaternion = q
    pbone.keyframe_insert('rotation_quaternion', frame=frame)

def euler(bone_name, xyz, frame):
    """Rotation Euler classique (relatif au repos)."""
    pbone = bones[bone_name]
    pbone.rotation_mode = 'XYZ'
    pbone.rotation_euler = xyz
    pbone.keyframe_insert('rotation_euler', frame=frame)

def loc(bone_name, xyz, frame):
    pbone = bones[bone_name]
    pbone.location = Vector(xyz)
    pbone.keyframe_insert('location', frame=frame)

# ────────────────────────────────────────────────────────
# Directions cibles (dans le monde : X=droite, Y=face, Z=ciel)
# ────────────────────────────────────────────────────────
UP    = (0, 0, 1)
DOWN  = (0, 0, -1)
FWD   = (0, 1, 0)
BACK  = (0, -1, 0)
RIGHT = (1, 0, 0)
LEFT  = (-1, 0, 0)

# ────────────────────────────────────────────────────────
# FRAME 0  —  REPOS (quaternion identite)
# ────────────────────────────────────────────────────────
for name in bones:
    name.rotation_mode = 'QUATERNION'
    name.rotation_quaternion = (1, 0, 0, 0)
    name.keyframe_insert('rotation_quaternion', frame=0)
loc('Root', (0, 0, 0), 0)

# ────────────────────────────────────────────────────────
# PHASE 1  —  INSPIRATION / ANTICIPATION  (0 → 12)
# ────────────────────────────────────────────────────────
point_y('Head',       (0, 0.2, 0.95),     12)   # leve le regard
point_y('Shoulder_R', (0.1, 0.2, 0.95),   12)   # epaules commencent a tourner
point_y('Shoulder_L', (-0.1, 0.2, 0.95),  12)
euler('Chest',        (0.06, 0, 0),       12)
euler('Torso',        (0.03, 0, 0),       12)
loc('Root',           (0, -0.005, 0.003), 12)

# ────────────────────────────────────────────────────────
# PHASE 2  —  BRAS MONTENT AU-DESSUS DE LA TETE  (12 → 30)
#   Les epaules pointent les bras vers le ciel.
#   Le torse se cambre, la tete suit.
# ────────────────────────────────────────────────────────
point_y('Shoulder_R', (0.15, 0.15, 0.98), 30)   # bras droit vers le haut
point_y('Shoulder_L', (-0.15, 0.15, 0.98), 30)  # bras gauche vers le haut
point_y('Head',       (0, 0, 1),          30)    # tete regarde le ciel
euler('Chest',        (0.2, 0, 0),        30)    # cambrure
euler('Torso',        (0.08, 0, 0),       30)    # bascule
loc('Root',           (-0.005, -0.01, 0.015), 30)

# Elbow — legere flexion naturelle
euler('Arm_R', (0, 0, 0.3),  30)
euler('Arm_L', (0, 0, -0.3), 30)

# Mains — paumes vers le ciel
euler('Hand_R', (math.pi/2, 0.2, 0.3), 30)
euler('Hand_L', (-math.pi/2, -0.2, -0.3), 30)

# ────────────────────────────────────────────────────────
# PHASE 3  —  DOIGTS S'OUVRENT VERS LE CIEL  (30 → 42)
# ────────────────────────────────────────────────────────
euler('Hand_R',  (math.pi/2, 0.3, 0.5),  42)
euler('Hand_L',  (-math.pi/2, -0.3, -0.5), 42)
euler('Tool_R',  (0.1, 0.7, -0.05),      42)
euler('Tool_L',  (-0.1, -0.7, 0.05),     42)
euler('Arm_R',   (0, 0, 0.25),           42)
euler('Arm_L',   (0, 0, -0.25),          42)

# ────────────────────────────────────────────────────────
# PHASE 4  —  ATTRAPE !  (42 → 52)
#   Les doigts se ferment, le corps reagit
# ────────────────────────────────────────────────────────
euler('Tool_R',  (0.05, 0.15, 0),       52)
euler('Tool_L',  (-0.05, -0.15, 0),     52)
euler('Hand_R',  (math.pi/2, 0.05, 0.2), 52)
euler('Hand_L',  (-math.pi/2, -0.05, -0.2), 52)
euler('Chest',   (0.15, 0.06, 0.05),    52)
point_y('Head',  (0.05, 0.05, 0.98),    52)
loc('Root',      (0.005, -0.008, 0.01), 52)

# ────────────────────────────────────────────────────────
# PHASE 5  —  RELACHE  (52 → 64)
# ────────────────────────────────────────────────────────
euler('Tool_R',  (0.15, 0.8, 0.05),      64)
euler('Tool_L',  (-0.15, -0.8, -0.05),   64)
euler('Hand_R',  (math.pi/2, 0.4, 0.5),  64)
euler('Hand_L',  (-math.pi/2, -0.4, -0.5), 64)

# ────────────────────────────────────────────────────────
# PHASE 6  —  DEUXIEME ATTRAPE  (64 → 76)
# ────────────────────────────────────────────────────────
euler('Tool_R',  (0.08, 0.2, 0),         76)
euler('Tool_L',  (-0.08, -0.2, 0),       76)
euler('Hand_R',  (math.pi/2, 0.1, 0.25), 76)
euler('Hand_L',  (-math.pi/2, -0.1, -0.25), 76)
euler('Chest',   (0.18, -0.04, -0.03),   76)
point_y('Head',  (-0.05, 0.1, 0.98),     76)

# ────────────────────────────────────────────────────────
# BRAISES  —  DANSE SUBTILE DU CORPS
# ────────────────────────────────────────────────────────
loc('Root', ( 0.012, -0.006,  0.008), 18)
loc('Root', (-0.018, -0.008, -0.004), 32)
loc('Root', ( 0.025, -0.004,  0.012), 44)
loc('Root', (-0.015, -0.01,  -0.01),  56)
loc('Root', ( 0.018, -0.007,  0.008), 68)
loc('Root', (-0.008, -0.006, -0.006), 80)
loc('Root', ( 0,     -0.006,  0),     92)

euler('Torso', (0.10, 0,  0.12), 30)
euler('Torso', (0.05, 0, -0.08), 45)
euler('Torso', (0.12, 0,  0.06), 58)
euler('Torso', (0.06, 0, -0.10), 72)
euler('Torso', (0.08, 0,  0),    88)

euler('Chest', (0.20,  0.05,  0.12), 30)
euler('Chest', (0.12, -0.04, -0.08), 45)
euler('Chest', (0.22,  0.03,  0.07), 58)
euler('Chest', (0.15, -0.05, -0.06), 72)
euler('Chest', (0.18,  0,     0),    88)

# Tete suit les braises
point_y('Head', (0.1, -0.1, 0.98), 30)
point_y('Head', (-0.15, 0.15, 0.96), 44)
point_y('Head', (0.1, -0.05, 0.98), 58)
point_y('Head', (-0.1, 0.1, 0.97), 72)
point_y('Head', (0, 0.05, 0.98), 88)

# Avant-bras flottent
euler('Arm_R', ( 0.15,  0.04, 0.3), 35)
euler('Arm_L', (-0.15, -0.04, -0.3), 35)
euler('Arm_R', (-0.06, -0.03, 0.35), 55)
euler('Arm_L', ( 0.06,  0.03, -0.35), 55)
euler('Arm_R', ( 0.10,  0.02, 0.28), 70)
euler('Arm_L', (-0.10, -0.02, -0.28), 70)

# ────────────────────────────────────────────────────────
# PHASE 7  —  TENUE SOUTENUE  (88 → 110)
# ────────────────────────────────────────────────────────
point_y('Shoulder_R', (0.12, 0.15, 0.98), 110)
point_y('Shoulder_L', (-0.12, 0.15, 0.98), 110)
point_y('Head',       (0, 0.05, 0.98),    110)
euler('Arm_R',   (0.05, 0, 0.25),        110)
euler('Arm_L',   (-0.05, 0, -0.25),      110)
euler('Hand_R',  (math.pi/2, 0.15, 0.25), 110)
euler('Hand_L',  (-math.pi/2, -0.15, -0.25), 110)
euler('Tool_R',  (0.05, 0.25, 0),        110)
euler('Tool_L',  (-0.05, -0.25, 0),      110)
euler('Chest',   (0.18, 0, 0),           110)
euler('Torso',   (0.06, 0, 0),           110)
loc('Root',      (0, -0.006, 0),         110)

# ────────────────────────────────────────────────────────
# Finalisation
# ────────────────────────────────────────────────────────
bpy.context.view_layer.update()
if obj.animation_data and obj.animation_data.action:
    action = obj.animation_data.action
    if hasattr(action, "fcurves"):
        for fc in action.fcurves:
            for kp in fc.keyframe_points:
                kp.interpolation = 'BEZIER'

print("Animation humaine generee : 0 -> 110 frames")
print("point_y() calcule les quaternions automatiquement")
print("quel que soit l'orientation de repos des os.")
