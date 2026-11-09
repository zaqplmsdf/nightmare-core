package com.nightmarecore.jei;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.recipe.NightmareForgeRecipe;
import com.nightmarecore.registry.ModBlocks;
import com.nightmarecore.registry.ModItems;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class NightmareForgeRecipeCategory implements IRecipeCategory<NightmareForgeRecipe> {

    public static final RecipeType<NightmareForgeRecipe> TYPE =
            RecipeType.create(NightmareCore.MOD_ID, "forging", NightmareForgeRecipe.class);
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NightmareCore.MOD_ID, "textures/gui/jei/nightmare_forge.png");

    private final IDrawable background;
    private final IDrawable icon;

    public NightmareForgeRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 110, 54);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.NIGHTMARE_FORGE_ITEM.get()));
    }

    @Override
    public RecipeType<NightmareForgeRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.nightmare_core.forging");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, NightmareForgeRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            int col = i % 3;
            int row = i / 3;
            builder.addSlot(RecipeIngredientRole.INPUT, 1 + col * 18, 1 + row * 18)
                    .addIngredients(ingredients.get(i));
        }
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 58, 36)
                .addItemStack(new ItemStack(ModItems.NIGHTMARE_FRAGMENT.get(), recipe.getFragmentCost()))
                .addTooltipCallback((slot, tooltip) -> tooltip.add(
                        Component.translatable("jei.nightmare_core.fuel_cost", recipe.getFragmentCost())));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 18)
                .addItemStack(recipe.getResult());
    }

    @Override
    public void draw(NightmareForgeRecipe recipe, IRecipeSlotsView slotsView,
                     GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.nightmare_core.fuel_cost", recipe.getFragmentCost()),
                58, 40, 0xAAAAAA, false);
    }

    public static void addCatalyst(mezz.jei.api.registration.IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.NIGHTMARE_FORGE_ITEM.get()), TYPE);
    }

    public static void addRecipes(mezz.jei.api.registration.IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        List<NightmareForgeRecipe> recipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(com.nightmarecore.registry.ModRecipes.FORGING_TYPE.get());
        registration.addRecipes(TYPE, recipes);
    }

    public static void addCategory(mezz.jei.api.registration.IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new NightmareForgeRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));
    }
}
