package com.nightmarecore.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nightmarecore.block.NightmareForgeBlockEntity;
import com.nightmarecore.registry.ModBlocks;
import com.nightmarecore.registry.ModRecipes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class NightmareForgeRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final List<Ingredient> ingredients;
    private final int fragmentCost;
    private final ItemStack result;

    public NightmareForgeRecipe(ResourceLocation id, List<Ingredient> ingredients,
                                int fragmentCost, ItemStack result) {
        this.id = id;
        this.ingredients = List.copyOf(ingredients);
        this.fragmentCost = Math.max(1, fragmentCost);
        this.result = result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(this.ingredients);
        return list;
    }

    public int getFragmentCost() {
        return this.fragmentCost;
    }

    public ItemStack getResult() {
        return this.result;
    }

    @Override
    public boolean matches(Container container, Level level) {
        boolean[] used = new boolean[NightmareForgeBlockEntity.INPUT_COUNT];
        int inputCount = 0;
        for (int i = 0; i < NightmareForgeBlockEntity.INPUT_COUNT; i++) {
            if (!container.getItem(NightmareForgeBlockEntity.INPUT_START + i).isEmpty()) {
                inputCount++;
            }
        }
        if (inputCount != this.ingredients.size()) {
            return false;
        }
        for (Ingredient ingredient : this.ingredients) {
            boolean matched = false;
            for (int i = 0; i < NightmareForgeBlockEntity.INPUT_COUNT; i++) {
                if (!used[i] && ingredient.test(container.getItem(NightmareForgeBlockEntity.INPUT_START + i))) {
                    used[i] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return this.result.copy();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.NIGHTMARE_FORGE_ITEM.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FORGING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FORGING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<NightmareForgeRecipe> {

        @Override
        public NightmareForgeRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
            List<Ingredient> ingredients = new ArrayList<>();
            for (JsonElement element : array) {
                ingredients.add(Ingredient.fromJson(element));
            }
            int cost = GsonHelper.getAsInt(json, "fragment_cost", 4);
            JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
            ItemStack result = new ItemStack(
                    net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                            new ResourceLocation(GsonHelper.getAsString(resultJson, "item"))),
                    GsonHelper.getAsInt(resultJson, "count", 1));
            return new NightmareForgeRecipe(id, ingredients, cost, result);
        }

        @Override
        public NightmareForgeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<Ingredient> ingredients = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                ingredients.add(Ingredient.fromNetwork(buf));
            }
            int cost = buf.readVarInt();
            ItemStack result = buf.readItem();
            return new NightmareForgeRecipe(id, ingredients, cost, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, NightmareForgeRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buf);
            }
            buf.writeVarInt(recipe.fragmentCost);
            buf.writeItem(recipe.result);
        }
    }
}
