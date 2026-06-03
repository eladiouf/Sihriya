"""
Pipeline d'importation des animations EpicFight vers Sihriya.
1. Convertit les poses efiscompat (format armature) → format flat animation[]
2. Copie les animations epicfight/* et Battle-Arts/* dans assets/sihriya/animations/
3. Génère la classe Java SihriyaAnimations.java
4. Génère les configs JSON par école + sort
"""

import json, os, shutil, glob

ANIM_SRC = r"C:\Users\El Hadji\Downloads\Compressed\EpicFight-Files-Blender-Armor\EpicFight-Files-Blender-Armor\workspace\animations"
ASSETS_DIR = r"C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA\src\main\resources\assets\sihriya\animmodels\animations"
JAVA_ANIM = r"C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA\src\main\java\tong\sihriya\animation\SihriyaAnimations.java"
CONFIG_DIR = r"C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA\src\main\resources\data\sihriya\sihriya_spell_animations"

# Mapping des types pour les data/ companion files
DATA_PRIORITIES = {
    "MIDDLE": "MIDDLE",
    "HIGH": "HIGH",
    "LOWEST": "LOWEST",
    "HIGHEST": "HIGHEST",
}

def ensure_dir(path):
    os.makedirs(path, exist_ok=True)

def convert_armature_to_animation(data):
    """Convertit format armature {joints, hierarchy} → format flat animation[]."""
    if "animation" in data:
        return data  # déjà au bon format
    if "armature" not in data:
        return None

    arm = data["armature"]
    joints = arm.get("joints", [])
    hierarchy = arm.get("hierarchy", [])

    # Extraire les transforms de la hiérarchie
    joint_transforms = {}

    def walk(node, parent_transform=None):
        name = node["name"]
        t = node["transform"]
        # Appliquer le transform parent si présent
        joint_transforms[name] = t
        for child in node.get("children", []):
            walk(child, t)

    for root in hierarchy:
        walk(root)

    animation = []
    for j in joints:
        if j in joint_transforms:
            animation.append({
                "name": j,
                "time": [0.0],
                "transform": [joint_transforms[j]]
            })

    return {"animation": animation}

def copy_animation(src_path, rel_path):
    """Copie une animation en convertissant si nécessaire."""
    dst_path = os.path.join(ASSETS_DIR, rel_path)
    ensure_dir(os.path.dirname(dst_path))

    with open(src_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    converted = convert_armature_to_animation(data)
    if converted is None:
        return False

    with open(dst_path, "w", encoding="utf-8") as f:
        json.dump(converted, f, indent=2)
    return True

def collect_animations():
    """Parcourt tous les dossiers et collecte les animations avec leur catégorie."""
    anims = []

    # epicfight/biped/*
    for root, dirs, files in os.walk(os.path.join(ANIM_SRC, "epicfight", "biped")):
        # Skip data/ et pov/ dossiers
        rel = os.path.relpath(root, ANIM_SRC)
        if "/data" in rel or "\\data" in rel or "/pov" in rel or "\\pov" in rel:
            continue
        for f in files:
            if f.endswith(".json"):
                src = os.path.join(root, f)
                # Chemin relatif: epicfight/biped/combat/xxx.json → epicfight/biped/combat/xxx.json
                rel_path = os.path.relpath(src, ANIM_SRC)
                # Vérifier si c'est un data file (souvent dans un sous-dossier data/)
                if rel_path.startswith("epicfight\\biped\\emote"):
                    category = "emote"
                elif rel_path.startswith("epicfight\\biped\\living"):
                    category = "living"
                elif rel_path.startswith("epicfight\\biped\\combat"):
                    category = "combat"
                elif rel_path.startswith("epicfight\\biped\\skill"):
                    category = "skill"
                elif rel_path.startswith("epicfight\\biped\\interact"):
                    category = "interact"
                else:
                    continue
                name = os.path.splitext(f)[0]
                anims.append((src, rel_path, category, name))

    # epicfight/mobs
    for mob_dir in ["creeper", "dragon", "enderman", "hoglin", "illager", "iron_golem",
                     "piglin", "ravager", "spider", "vex", "witch", "wither",
                     "wither_skeleton", "zombie"]:
        mob_path = os.path.join(ANIM_SRC, "epicfight", mob_dir)
        if os.path.isdir(mob_path):
            for f in os.listdir(mob_path):
                if f.endswith(".json") and f != "data":
                    src = os.path.join(mob_path, f)
                    rel_path = f"epicfight\\{mob_dir}\\{f}"
                    name = os.path.splitext(f)[0]
                    anims.append((src, rel_path, "mob", name))

    # efiscompat/biped/living/
    efi_root = os.path.join(ANIM_SRC, "efiscompat", "biped", "living")
    if os.path.isdir(efi_root):
        for f in os.listdir(efi_root):
            if f.endswith(".json") and f != "data":
                src = os.path.join(efi_root, f)
                # Registrer sous un path qui a du sens
                rel_path = f"efiscompat\\{f}"
                name = os.path.splitext(f)[0]
                anims.append((src, rel_path, "casting", name))

    # Battle-Arts/
    ba_root = os.path.join(ANIM_SRC, "Battle-Arts")
    if os.path.isdir(ba_root):
        for root, dirs, files in os.walk(ba_root):
            if "data" in root.split(os.sep):
                continue
            rel = os.path.relpath(root, ANIM_SRC)
            for f in files:
                if f.endswith(".json"):
                    src = os.path.join(root, f)
                    rel_path = os.path.relpath(src, ANIM_SRC)
                    name = os.path.splitext(f)[0]
                    # Extraire le style du chemin
                    parts = rel_path.split(os.sep)
                    if len(parts) >= 3:
                        anims.append((src, rel_path, "battle_arts", name))

    return anims

def generate_java_class(anims):
    """Génère la classe SihriyaAnimations.java mise à jour."""
    casting_anims = [a for a in anims if a[2] == "casting"]
    skill_anims = [a for a in anims if a[2] == "skill"]
    combat_anims = [a for a in anims if a[2] == "combat"]
    living_anims = [a for a in anims if a[2] == "living"]
    ba_anims = [a for a in anims if a[2] == "battle_arts"]

    # Inclure tous les efiscompat (casting + chanting + continuous = 38)
    # + les skills/combat sélectionnés
    selected = cast_select(casting_anims, 99) + cast_select(skill_anims[:30], 99) + cast_select(combat_anims[:20], 99)

    # Générer le code Java
    fields = []
    registry = []

    for src, rel_path, cat, name in selected:
        field_name = name.upper().replace("-", "_").replace(" ", "_")
        anim_path = rel_path.replace("\\", "/").replace(".json", "")

        is_looping = cat in ("casting", "living", "emote")
        if is_looping:
            fields.append(f'    public static AnimationAccessor<StaticAnimation> {field_name};')
            registry.append(
                f'        {field_name} = builder.nextAccessor("{anim_path}",\n'
                f'            a -> new StaticAnimation(true, a, Armatures.BIPED));')
        else:
            fields.append(f'    public static AnimationAccessor<StaticAnimation> {field_name};')
            registry.append(
                f'        {field_name} = builder.nextAccessor("{anim_path}",\n'
                f'            a -> new StaticAnimation(false, a, Armatures.BIPED));')

    # Générer getByName()
    cases = []
    for src, rel_path, cat, name in selected:
        field_name = name.upper().replace("-", "_").replace(" ", "_")
        cases.append(f'            case "{field_name}" -> {field_name};')

    java = f"""package tong.sihriya.animation;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Armatures;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SihriyaAnimations {{\n"""
    java += "\n".join(fields)
    java += """

    @SubscribeEvent
    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(Sihriya.MODID, SihriyaAnimations::build);
    }

    public static void build(AnimationManager.AnimationBuilder builder) {
"""
    java += "\n".join(registry)
    java += """
    }

    public static AnimationAccessor<?> getByName(String name) {
        if (name == null) return null;
        return switch (name) {
"""
    java += "\n".join(cases)
    java += """
            default -> null;
        };
    }
}
"""
    return java

def cast_select(anims, max_count=30):
    """Sélectionne jusqu'à max_count animations, priorité aux plus pertinentes."""
    seen = set()
    result = []
    for a in anims:
        if a[3] not in seen:
            seen.add(a[3])
            result.append(a)
        if len(result) >= max_count:
            break
    return result

def generate_java(casting_anims, skill_anims, combat_anims):
    """Génère la classe Java avec ~50 animations sélectionnées."""
    # Toutes les efiscompat (casting/chanting/continuous — ~37)
    all_efi = [a for a in casting_anims if a[3].startswith("casting") or a[3].startswith("chanting") or a[3].startswith("continuous")]
    all_efi.sort(key=lambda a: a[3])

    # Les skills EpicFight les plus utiles pour les sorts (~15)
    key_skills = ["battojutsu", "blade_rush_combo1", "blade_rush_combo2", "blade_rush_combo3",
                  "blade_rush_execute", "dancing_edge", "demolition_leap", "eviscerate_first",
                  "heartpiercer", "relentless_combo", "sweeping_edge", "steel_whirlwind",
                  "tsunami", "wrathful_lighting", "the_guillotine", "grasping_spire_second",
                  "phantom_ascent_forward", "rushing_tempo1", "rushing_tempo2", "rushing_tempo3"]
    all_skills = [a for a in skill_anims if a[3] in key_skills]

    # Combats utiles (~10)
    key_combat = ["sword_auto1", "sword_auto2", "greatsword_auto1", "dagger_auto1",
                  "fist_auto1", "spear_twohand_auto1", "tachi_auto1", "knockdown",
                  "guard_sword", "guard_greatsword"]
    all_combat = [a for a in combat_anims if a[3] in key_combat]

    selected = all_efi + all_skills + all_combat
    return selected

def generate_spell_configs(anims):
    """Génère les JSONs de config d'animations par école."""
    casting = [a for a in anims if a[2] == "casting"]
    skills = [a for a in anims if a[2] == "skill"][:10]

    # Mapping school → animation set
    # Chaque école aura un style d'incantation différent
    school_animations = {
        "fire": {
            "chant_animation": "CASTING_TWO_HAND_EXPLOSION",
            "cast_animation": "CASTING_TWO_HAND_EXPLOSION",
            "continuous_animation": "CONTINUOUS_TWO_HAND_FRONT",
        },
        "water": {
            "chant_animation": "CASTING_TWO_HAND_BOW",
            "cast_animation": "CASTING_TWO_HAND_BOW",
            "continuous_animation": "CONTINUOUS_TWO_HAND_FRONT",
        },
        "wind": {
            "chant_animation": "CASTING_ONE_HAND_TOP",
            "cast_animation": "CASTING_ONE_HAND_TOP",
            "continuous_animation": "CONTINUOUS_ONE_HAND_STAFF_LEFT",
        },
        "earth": {
            "chant_animation": "CASTING_TWO_HAND_STOMP",
            "cast_animation": "CASTING_TWO_HAND_STOMP",
            "continuous_animation": "CONTINUOUS_TWO_HAND_FRONT",
        },
        "lightning": {
            "chant_animation": "CASTING_TWO_HAND_TOP",
            "cast_animation": "CASTING_TWO_HAND_TOP",
            "continuous_animation": "CONTINUOUS_TWO_HAND_FRONT",
        },
        "ice": {
            "chant_animation": "CASTING_ONE_HAND_INWARD",
            "cast_animation": "CASTING_ONE_HAND_INWARD",
            "continuous_animation": "CONTINUOUS_TWO_HAND_FRONT",
        },
        "lava": {
            "chant_animation": "CASTING_TWO_HAND_EXPLOSION",
            "cast_animation": "CASTING_TWO_HAND_EXPLOSION",
            "continuous_animation": "CONTINUOUS_ONE_HAND_STAFF_RIGHT",
        },
        "necromancy": {
            "chant_animation": "CHANTING_TWO_HAND_BACK",
            "cast_animation": "CASTING_TWO_HAND_BACK",
            "continuous_animation": "CONTINUOUS_ONE_HAND_STAFF_LEFT",
        },
        "lumamancy": {
            "chant_animation": "CASTING_TWO_HAND_ASCENSION",
            "cast_animation": "CASTING_TWO_HAND_ASCENSION",
            "continuous_animation": "CONTINUOUS_TWO_HAND_FRONT",
        },
    }

    # Sorts qui ont des animations spéciales
    signature_spells = {
        "fire.blazing_sun": {"cast_animation": "BLADE_RUSH_COMBO3"},
        "fire.meteor_shower": {"cast_animation": "STEEL_WHIRLWIND"},
        "water.tidal_wave": {"cast_animation": "TSUNAMI"},
        "wind.eye_of_storm": {"cast_animation": "STEEL_WHIRLWIND"},
        "earth.titan_awakening": {"cast_animation": "DEMOLITION_LEAP"},
        "lightning.celestial_judgment": {"cast_animation": "WRATHFUL_LIGHTING"},
        "ice.ice_age": {"cast_animation": "TSUNAMI"},
        "lava.volcanic_awakening": {"cast_animation": "DEMOLITION_LEAP"},
        "necromancy.the_void": {"cast_animation": "CASTING_TWO_HAND_BACK"},
        "lumamancy.paradise_found": {"cast_animation": "CASTING_TWO_HAND_ASCENSION"},
    }

    # Générer le JSON principal
    result = {"spells": {}}

    # Ajouter le default (fallback)
    result["spells"]["default"] = school_animations["fire"]

    # Générer par école
    for school, anims in school_animations.items():
        config_key = f"__{school}__"
        result["spells"][config_key] = anims

    # Ajouter les sorts signatures
    for spell_id, anims in signature_spells.items():
        result["spells"][spell_id] = anims

    return result


def main():
    print("=== Pipeline d'animations Sihriya ===")
    ensure_dir(ASSETS_DIR)
    ensure_dir(CONFIG_DIR)

    print("1. Collecte des animations...")
    all_anims = collect_animations()
    print(f"   {len(all_anims)} animations trouvées")

    print("2. Copie et conversion dans assets/sihriya/animations/...")
    copied = 0
    errors = 0
    for src, rel_path, cat, name in all_anims:
        # Nettoyer le séparateur
        clean_path = rel_path.replace("\\", "/")
        if copy_animation(src, clean_path):
            copied += 1
        else:
            errors += 1

    print(f"   {copied} copiées, {errors} erreurs")

    print("3. Sélection des meilleures animations...")
    casting_anims = [a for a in all_anims if a[2] == "casting"]
    skill_anims = [a for a in all_anims if a[2] == "skill"]
    combat_anims = [a for a in all_anims if a[2] == "combat"]
    selected = generate_java(casting_anims, skill_anims, combat_anims)
    print(f"   {len(selected)} animations sélectionnées pour la classe Java")

    print("4. Génération de SihriyaAnimations.java...")
    # Convertir pour generate_java_class qui attend le bon format
    java_code = generate_java_class(selected)
    with open(JAVA_ANIM, "w", encoding="utf-8") as f:
        f.write(java_code)

    print("5. Génération des configs JSON...")
    configs = generate_spell_configs(all_anims)
    config_path = os.path.join(CONFIG_DIR, "schools.json")
    with open(config_path, "w", encoding="utf-8") as f:
        json.dump(configs, f, indent=2)

    print("\n=== Terminé ! ===")
    print(f"Animations dans: {ASSETS_DIR}")
    print(f"Configs dans: {config_path}")

if __name__ == "__main__":
    main()
