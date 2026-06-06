"""
Pipeline d'integration des animations Blender exportees (1 par sort, 252 au total).

Usage:
    python pipeline_export_spells.py <source_dir> [--dry-run]

Ou <source_dir> contient les fichiers JSON nommes <school>_<spell>.json
(ex: fire_spark.json, water_water_jet.json, ...)

Le script:
  1. Copie les fichiers dans assets/sihriya/animmodels/animations/spells/
  2. Met a jour registerSpellAnimations() dans SihriyaAnimations.java
  3. Ajoute les mappings dans schools.json
"""

import json, os, shutil, sys, re

BASE = r"C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA"
SPELLS_JSON = os.path.join(BASE, "src", "main", "resources", "data", "sihriya", "spells.json")
ASSETS_SPELLS_DIR = os.path.join(BASE, "src", "main", "resources", "assets", "sihriya", "animmodels", "animations", "spells")
JAVA_FILE = os.path.join(BASE, "src", "main", "java", "tong", "sihriya", "animation", "SihriyaAnimations.java")
SCHOOLS_JSON = os.path.join(BASE, "src", "main", "resources", "data", "sihriya", "sihriya_spell_animations", "schools.json")


def spell_id_to_enum(spell_id):
    """'fire.spark' -> 'FIRE_SPARK'"""
    return spell_id.upper().replace(".", "_")


def spell_id_to_filename(spell_id):
    """'fire.spark' -> 'fire_spark.json'"""
    return spell_id.replace(".", "_") + ".json"


def read_spells():
    with open(SPELLS_JSON, "r", encoding="utf-8") as f:
        return json.load(f)


def find_exported(source_dir, spells):
    """Cherche les fichiers exportes dans source_dir et les associe aux sorts."""
    exported = {}
    for s in spells:
        sid = s["id"]
        filename = spell_id_to_filename(sid)
        src_path = os.path.join(source_dir, filename)
        if os.path.exists(src_path):
            exported[sid] = {"src": src_path, "school": s["school"]}
    return exported


def copy_animation_files(exported, dry_run):
    for sid, info in sorted(exported.items()):
        dst = os.path.join(ASSETS_SPELLS_DIR, spell_id_to_filename(sid))
        if dry_run:
            print(f"  [DRY-RUN] copie {info['src']} -> {dst}")
        else:
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy2(info["src"], dst)
            print(f"  OK {spell_id_to_filename(sid)}")


def update_java(exported, dry_run):
    with open(JAVA_FILE, "r", encoding="utf-8") as f:
        content = f.read()

    # Generer les lignes register() en gardant l'ordre des sorts
    register_lines = []
    for sid in sorted(exported):
        enum_name = spell_id_to_enum(sid)
        path = f"spells/{enum_name.lower()}"
        register_lines.append(f'        register(builder, "{enum_name}", "{path}", true);')
    new_body = "\n".join(register_lines)

    # Pattern pour trouver registerSpellAnimations
    pattern = r"(private static void registerSpellAnimations\(AnimationManager\.AnimationBuilder builder\) \{\n)(.*?)(\n    \})"
    match = re.search(pattern, content, re.DOTALL)
    if not match:
        print("ERREUR: Impossible de trouver registerSpellAnimations dans SihriyaAnimations.java")
        return False

    replacement = match.group(1) + new_body + "\n    }"
    new_content = content[:match.start()] + replacement + content[match.end():]

    if dry_run:
        print(f"  [DRY-RUN] registerSpellAnimations() mis a jour avec {len(exported)} entrees")
    else:
        with open(JAVA_FILE, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"  OK registerSpellAnimations() mis a jour ({len(exported)} entrees)")
    return True


def update_schools_json(exported, dry_run):
    with open(SCHOOLS_JSON, "r", encoding="utf-8") as f:
        schools = json.load(f)

    added = 0
    for sid in sorted(exported):
        enum_name = spell_id_to_enum(sid)
        if sid not in schools["spells"]:
            schools["spells"][sid] = {"cast_animation": enum_name}
            added += 1
        elif schools["spells"][sid].get("cast_animation") != enum_name:
            # Ne pas ecraser les overrides manuels (comme fire.blazing_sun -> BLADE_RUSH_COMBO3)
            print(f"  ATTENTION: {sid} a deja un cast_animation different dans schools.json, ignore")

    if dry_run:
        print(f"  [DRY-RUN] {added} nouvelles entrees dans schools.json")
    else:
        with open(SCHOOLS_JSON, "w", encoding="utf-8") as f:
            json.dump(schools, f, indent=2)
        print(f"  OK {added} nouvelles entrees dans schools.json")


def main():
    if len(sys.argv) < 2:
        print("Usage: python pipeline_export_spells.py <source_dir> [--dry-run]")
        sys.exit(1)

    source_dir = sys.argv[1]
    dry_run = "--dry-run" in sys.argv

    if not os.path.isdir(source_dir):
        print(f"ERREUR: {source_dir} n'est pas un dossier valide")
        sys.exit(1)

    spells = read_spells()
    exported = find_exported(source_dir, spells)

    if not exported:
        print(f"Aucun fichier d'animation trouve dans {source_dir}")
        print(f"Les fichiers doivent etre nommes selon le format: fire_spark.json, water_water_jet.json, ...")
        print(f"(252 fichiers attendus d'apres spells.json)")
        sys.exit(1)

    print(f"Trouve {len(exported)}/{len(spells)} animations exportees")
    print()

    print("1. Copie des fichiers JSON...")
    copy_animation_files(exported, dry_run)

    print("2. Mise a jour de SihriyaAnimations.java...")
    update_java(exported, dry_run)

    print("3. Mise a jour de schools.json...")
    update_schools_json(exported, dry_run)

    if dry_run:
        print(f"\n[DRY-RUN] Aucun fichier modifie. Passe --dry-run pour simuler.")
    else:
        print(f"\nTermine! {len(exported)} animations integrees.")
        print(f"  - Fichiers dans: {ASSETS_SPELLS_DIR}")
        print(f"  - Java: {JAVA_FILE}")
        print(f"  - Config: {SCHOOLS_JSON}")
        print(f"\nPour compiler: .\\gradlew.bat build")


if __name__ == "__main__":
    main()
