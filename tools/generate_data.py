"""Generate recipes and recipe-unlock advancements for Nightmare Core."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src" / "main" / "resources" / "data" / "nightmare_core"
RECIPES = DATA / "recipes"
ADVANCEMENTS = DATA / "advancements" / "recipes"
RECIPES.mkdir(parents=True, exist_ok=True)
ADVANCEMENTS.mkdir(parents=True, exist_ok=True)

RECIPE_UNLOCK = "minecraft:recipe_unlocked"
INVENTORY_CHANGED = "minecraft:inventory_changed"


def shaped(recipe_id, pattern, key, unlock_item):
    recipe = {
        "type": "minecraft:crafting_shaped",
        "pattern": pattern,
        "key": key,
        "result": {"item": f"nightmare_core:{recipe_id}"},
    }
    write_recipe(recipe_id, recipe, unlock_item)


def shapeless(recipe_id, ingredients, unlock_item):
    recipe = {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [item(value) for value in ingredients],
        "result": {"item": f"nightmare_core:{recipe_id}"},
    }
    write_recipe(recipe_id, recipe, unlock_item)


def write_recipe(recipe_id, recipe, unlock_item):
    path = RECIPES / f"{recipe_id}.json"
    path.write_text(json.dumps(recipe, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

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
        json.dumps(advancement, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )


def item(value):
    return {"item": value}


def main():
    shaped(
        "ember_heart",
        [" F ", "FGF", " F "],
        {"F": item("nightmare_core:nightmare_fragment"),
         "G": item("minecraft:glistering_melon_slice")},
        "nightmare_core:nightmare_fragment",
    )
    shaped(
        "dreamrend_fang",
        [" F ", "FSF", " F "],
        {"F": item("nightmare_core:nightmare_fragment"),
         "S": item("minecraft:iron_sword")},
        "nightmare_core:nightmare_fragment",
    )
    shaped(
        "fleet_feather",
        [" F ", "FRF", " F "],
        {"F": item("nightmare_core:nightmare_fragment"),
         "R": item("minecraft:feather")},
        "nightmare_core:nightmare_fragment",
    )
    shaped(
        "vigil_carapace",
        [" F ", "FSF", " F "],
        {"F": item("nightmare_core:nightmare_fragment"),
         "S": item("minecraft:shield")},
        "nightmare_core:nightmare_fragment",
    )
    shaped(
        "stonebound_pendant",
        [" F ", "FOF", " F "],
        {"F": item("nightmare_core:nightmare_fragment"),
         "O": item("minecraft:obsidian")},
        "nightmare_core:nightmare_fragment",
    )
    shaped(
        "echo_bell",
        [" F ", "FAF", " F "],
        {"F": item("nightmare_core:nightmare_fragment"),
         "A": item("minecraft:amethyst_shard")},
        "nightmare_core:nightmare_fragment",
    )
    shaped(
        "dreamhunter_eye",
        [" F ", "FSF", " F "],
        {"F": item("nightmare_core:nightmare_fragment"),
         "S": item("minecraft:spyglass")},
        "nightmare_core:nightmare_fragment",
    )

    shaped(
        "nightmare_prism",
        [" A ", "FGF", " A "],
        {"A": item("minecraft:amethyst_shard"),
         "F": item("nightmare_core:nightmare_fragment"),
         "G": item("minecraft:glass")},
        "nightmare_core:nightmare_fragment",
    )
    shaped(
        "dream_anchor",
        [" F ", "FCF", " O "],
        {"F": item("nightmare_core:nightmare_fragment"),
         "C": item("minecraft:compass"),
         "O": item("minecraft:obsidian")},
        "nightmare_core:nightmare_fragment",
    )
    shaped(
        "ember_blade",
        [" F ", "FSF", " B "],
        {"F": item("nightmare_core:nightmare_fragment"),
         "S": item("minecraft:iron_sword"),
         "B": item("minecraft:blaze_powder")},
        "nightmare_core:nightmare_fragment",
    )

    for blessing, material in [
        ("ember_crown", "nightmare_core:ashen_cinder"),
        ("tidal_pendant", "nightmare_core:tidal_tear"),
        ("rift_eye", "nightmare_core:rift_shard"),
        ("vigil_lantern", "nightmare_core:vigil_eye"),
        ("hollow_charm", "nightmare_core:hollow_shell"),
    ]:
        shaped(
            blessing,
            [" F ", "FMF", " F "],
            {"F": item("nightmare_core:nightmare_fragment"),
             "M": item(material)},
            "nightmare_core:nightmare_fragment",
        )

    f = "nightmare_core:nightmare_fragment"
    n = "minecraft:nether_star"
    shapeless(
        "abyssal_crown",
        [f, f, f, f,
         "nightmare_core:ashen_cinder", "nightmare_core:ashen_cinder",
         "nightmare_core:tidal_tear", "nightmare_core:tidal_tear", n],
        f,
    )
    shapeless(
        "void_gauntlet",
        [f, f, f, f,
         "nightmare_core:rift_shard", "nightmare_core:rift_shard",
         "nightmare_core:hollow_shell", "nightmare_core:hollow_shell", n],
        f,
    )
    shapeless(
        "fate_dice",
        [f, f, f, f,
         "nightmare_core:vigil_eye", "nightmare_core:vigil_eye",
         "nightmare_core:hollow_shell", "nightmare_core:hollow_shell", n],
        f,
    )

    print(f"Generated {len(list(RECIPES.glob('*.json')))} recipes and "
          f"{len(list(ADVANCEMENTS.glob('*.json')))} recipe advancements.")


if __name__ == "__main__":
    main()
