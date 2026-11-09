"""Generate the Nightmare Forge recipes, tool recipes, and workstation recipe."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src" / "main" / "resources" / "data" / "nightmare_core"
RECIPES = DATA / "recipes"
FORGING = RECIPES / "forging"
ADVANCEMENTS = DATA / "advancements" / "recipes"
RECIPES.mkdir(parents=True, exist_ok=True)
FORGING.mkdir(parents=True, exist_ok=True)
ADVANCEMENTS.mkdir(parents=True, exist_ok=True)

RECIPE_UNLOCK = "minecraft:recipe_unlocked"
INVENTORY_CHANGED = "minecraft:inventory_changed"
FRAGMENT = "nightmare_core:nightmare_fragment"

# Old vanilla trinket recipes are intentionally removed. The Forge replaces them.
LEGACY_TRINKETS = [
    "ember_heart", "dreamrend_fang", "fleet_feather", "stonebound_pendant",
    "vigil_carapace", "echo_bell", "dreamhunter_eye", "ember_crown",
    "tidal_pendant", "rift_eye", "vigil_lantern", "hollow_charm",
    "abyssal_crown", "void_gauntlet", "fate_dice",
]

# name: (ingredients, fragment cost)
FORGING_RECIPES = {
    "ember_heart": (["nightmare_core:ashen_cinder", "minecraft:blaze_powder"], 4),
    "dreamrend_fang": (["nightmare_core:rift_shard", "minecraft:iron_sword"], 4),
    "fleet_feather": (["nightmare_core:hollow_shell", "minecraft:feather"], 4),
    "stonebound_pendant": (["nightmare_core:ashen_cinder", "minecraft:stone"], 4),
    "vigil_carapace": (["nightmare_core:vigil_eye", "minecraft:iron_ingot"], 4),
    "echo_bell": (["nightmare_core:hollow_shell", "minecraft:amethyst_shard"], 4),
    "dreamhunter_eye": (["nightmare_core:vigil_eye", "minecraft:spyglass"], 4),
    "ember_crown": (["nightmare_core:ashen_cinder", "minecraft:gold_ingot"], 8),
    "tidal_pendant": (["nightmare_core:tidal_tear", "minecraft:prismarine_shard"], 8),
    "rift_eye": (["nightmare_core:rift_shard", "minecraft:ender_pearl"], 8),
    "vigil_lantern": (["nightmare_core:vigil_eye", "minecraft:glowstone"], 8),
    "hollow_charm": (["nightmare_core:hollow_shell", "minecraft:bone"], 8),
    "abyssal_crown": (["nightmare_core:ashen_cinder", "nightmare_core:tidal_tear", "minecraft:nether_star"], 16),
    "void_gauntlet": (["nightmare_core:rift_shard", "nightmare_core:hollow_shell", "minecraft:nether_star"], 16),
    "fate_dice": (["nightmare_core:vigil_eye", "nightmare_core:hollow_shell", "minecraft:nether_star"], 16),
}


def item(value):
    return {"item": value}


def shaped(recipe_id, pattern, key, unlock_item):
    recipe = {
        "type": "minecraft:crafting_shaped",
        "pattern": pattern,
        "key": key,
        "result": {"item": f"nightmare_core:{recipe_id}"},
    }
    write_recipe(recipe_id, recipe, unlock_item)


def write_recipe(recipe_id, recipe, unlock_item):
    (RECIPES / f"{recipe_id}.json").write_text(
        json.dumps(recipe, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    advancement = {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_item": {
                "trigger": INVENTORY_CHANGED,
                "conditions": {"items": [{"items": [unlock_item]}]},
            },
            "has_the_recipe": {
                "trigger": RECIPE_UNLOCK,
                "conditions": {"recipe": f"nightmare_core:{recipe_id}"},
            },
        },
        "requirements": [["has_item", "has_the_recipe"]],
        "rewards": {"recipes": [f"nightmare_core:{recipe_id}"]},
    }
    (ADVANCEMENTS / f"{recipe_id}.json").write_text(
        json.dumps(advancement, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_forging(recipe_id, ingredients, fragment_cost):
    recipe = {
        "type": "nightmare_core:forging",
        "ingredients": [item(value) for value in ingredients],
        "fragment_cost": fragment_cost,
        "result": {"item": f"nightmare_core:{recipe_id}"},
    }
    (FORGING / f"{recipe_id}.json").write_text(
        json.dumps(recipe, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    for name in LEGACY_TRINKETS:
        for path in (RECIPES / f"{name}.json", ADVANCEMENTS / f"{name}.json"):
            if path.exists():
                path.unlink()

    shaped(
        "nightmare_prism",
        [" A ", "FGF", " A "],
        {"A": item("minecraft:amethyst_shard"), "F": item(FRAGMENT), "G": item("minecraft:glass")},
        FRAGMENT,
    )
    shaped(
        "dream_anchor",
        [" F ", "FCF", " O "],
        {"F": item(FRAGMENT), "C": item("minecraft:compass"), "O": item("minecraft:obsidian")},
        FRAGMENT,
    )
    shaped(
        "ember_blade",
        [" F ", "FSF", " B "],
        {"F": item(FRAGMENT), "S": item("minecraft:iron_sword"), "B": item("minecraft:blaze_powder")},
        FRAGMENT,
    )
    shaped(
        "nightmare_forge",
        ["FRF", "SBS", "FRF"],
        {"F": item(FRAGMENT), "R": item("nightmare_core:rift_shard"),
         "S": item("minecraft:stone"), "B": item("minecraft:crafting_table")},
        FRAGMENT,
    )

    for name, (ingredients, cost) in FORGING_RECIPES.items():
        write_forging(name, ingredients, cost)

    print(f"Generated {len(list(RECIPES.rglob('*.json')))} recipes, "
          f"{len(list(FORGING.glob('*.json')))} forging recipes, and "
          f"{len(list(ADVANCEMENTS.glob('*.json')))} recipe advancements.")


if __name__ == "__main__":
    main()
