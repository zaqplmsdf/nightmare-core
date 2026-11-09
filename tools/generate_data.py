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
    # Tier 1: direct material crafts. Fuel 16.
    "ember_heart": (["nightmare_core:ashen_cinder", "minecraft:blaze_powder", "minecraft:glistering_melon_slice", "minecraft:ghast_tear"], 16),
    "dreamrend_fang": (["nightmare_core:rift_shard", "minecraft:iron_sword", "minecraft:flint", "minecraft:phantom_membrane"], 16),
    "fleet_feather": (["nightmare_core:hollow_shell", "minecraft:feather", "minecraft:sugar", "minecraft:rabbit_foot"], 16),
    "stonebound_pendant": (["nightmare_core:ashen_cinder", "minecraft:stone", "minecraft:obsidian", "minecraft:iron_ingot"], 16),
    "vigil_carapace": (["nightmare_core:vigil_eye", "minecraft:iron_ingot", "minecraft:shield", "minecraft:scute"], 16),
    "echo_bell": (["nightmare_core:hollow_shell", "minecraft:amethyst_shard", "minecraft:gold_ingot", "minecraft:echo_shard"], 16),
    "dreamhunter_eye": (["nightmare_core:vigil_eye", "minecraft:spyglass", "minecraft:glowstone_dust", "minecraft:amethyst_shard"], 16),
    "moonlit_charm": (["nightmare_core:ashen_cinder", "minecraft:glowstone_dust", "minecraft:gold_ingot", "minecraft:white_wool"], 16),
    "bloodmoon_bracer": (["nightmare_core:tidal_tear", "minecraft:redstone_block", "minecraft:iron_ingot", "minecraft:leather"], 16),

    # Tier 2: requires the matching Tier 1 trinket. Fuel 32.
    "ember_crown": (["nightmare_core:ember_heart", "nightmare_core:ashen_cinder", "minecraft:gold_ingot", "minecraft:blaze_powder", "minecraft:nether_brick"], 32),
    "tidal_pendant": (["nightmare_core:stonebound_pendant", "nightmare_core:tidal_tear", "minecraft:prismarine_shard", "minecraft:heart_of_the_sea", "minecraft:lapis_lazuli"], 32),
    "rift_eye": (["nightmare_core:dreamhunter_eye", "nightmare_core:rift_shard", "minecraft:ender_pearl", "minecraft:amethyst_shard", "minecraft:glowstone_dust"], 32),
    "vigil_lantern": (["nightmare_core:vigil_carapace", "nightmare_core:vigil_eye", "minecraft:glowstone", "minecraft:gold_ingot", "minecraft:soul_lantern"], 32),
    "hollow_charm": (["nightmare_core:echo_bell", "nightmare_core:hollow_shell", "minecraft:bone", "minecraft:leather", "minecraft:iron_ingot"], 32),
    "moonshadow_cloak": (["nightmare_core:fleet_feather", "minecraft:phantom_membrane", "minecraft:blue_ice", "minecraft:feather", "minecraft:leather"], 32),
    "bloodpact_ring": (["nightmare_core:dreamrend_fang", "minecraft:gold_ingot", "minecraft:redstone_block", "minecraft:ghast_tear", "minecraft:lapis_lazuli"], 32),

    # Tier 3: requires the matching Tier 2 trinket. Fuel 64.
    "abyssal_crown": (["nightmare_core:ember_crown", "minecraft:nether_star", "nightmare_core:ashen_cinder", "nightmare_core:tidal_tear", "minecraft:obsidian", "minecraft:wither_skeleton_skull"], 64),
    "void_gauntlet": (["nightmare_core:hollow_charm", "minecraft:nether_star", "nightmare_core:rift_shard", "nightmare_core:hollow_shell", "minecraft:echo_shard", "minecraft:obsidian"], 64),
    "fate_dice": (["nightmare_core:rift_eye", "minecraft:nether_star", "nightmare_core:vigil_eye", "nightmare_core:hollow_shell", "minecraft:amethyst_shard", "minecraft:gold_block"], 64),
    "curse_reversal": (["nightmare_core:vigil_lantern", "minecraft:nether_star", "minecraft:echo_shard", "minecraft:amethyst_shard", "minecraft:gold_block", "minecraft:ghast_tear"], 64),
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
