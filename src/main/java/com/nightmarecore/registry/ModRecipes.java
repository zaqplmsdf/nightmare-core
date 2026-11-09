package com.nightmarecore.registry;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.recipe.NightmareForgeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, NightmareCore.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, NightmareCore.MOD_ID);

    public static final RegistryObject<RecipeType<NightmareForgeRecipe>> FORGING_TYPE =
            RECIPE_TYPES.register("forging", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return NightmareCore.MOD_ID + ":forging";
                }
            });

    public static final RegistryObject<RecipeSerializer<NightmareForgeRecipe>> FORGING_SERIALIZER =
            RECIPE_SERIALIZERS.register("forging", NightmareForgeRecipe.Serializer::new);

    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }

    private ModRecipes() {
    }
}
