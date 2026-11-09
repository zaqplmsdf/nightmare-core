package com.nightmarecore.jei;

import com.nightmarecore.NightmareCore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class NightmareCoreJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID =
            new ResourceLocation(NightmareCore.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        NightmareForgeRecipeCategory.addCategory(registration);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        NightmareForgeRecipeCategory.addRecipes(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        NightmareForgeRecipeCategory.addCatalyst(registration);
    }
}
