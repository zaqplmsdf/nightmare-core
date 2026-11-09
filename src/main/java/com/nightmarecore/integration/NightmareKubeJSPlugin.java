package com.nightmarecore.integration;

import com.nightmarecore.NightmareCore;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import net.minecraft.resources.ResourceLocation;

/**
 * KubeJS schema for nightmare_core:forging.
 *
 * Script example:
 * ServerEvents.recipes(event => {
 *   event.recipes.nightmare_core.forging(
 *     ['minecraft:diamond', 'minecraft:nether_star'],
 *     16,
 *     'minecraft:diamond_sword'
 *   )
 * })
 */
public class NightmareKubeJSPlugin extends KubeJSPlugin {

    private static final RecipeKey<InputItem[]> INGREDIENTS =
            ItemComponents.INPUT_ARRAY.key("ingredients");
    private static final RecipeKey<Integer> FRAGMENT_COST =
            NumberComponent.INT.key("fragment_cost");
    private static final RecipeKey<OutputItem> RESULT =
            ItemComponents.OUTPUT.key("result");

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(new ResourceLocation(NightmareCore.MOD_ID, "forging"),
                new RecipeSchema(INGREDIENTS, FRAGMENT_COST, RESULT));
    }
}
