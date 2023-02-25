package com.minelittlepony.unicopia.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public class ZapAppleRecipe extends ShapelessRecipe {

    public ZapAppleRecipe(Identifier id, String group, ItemStack output, DefaultedList<Ingredient> input) {
        super(id, group, output, input);
    }

    public static class Serializer extends ShapelessRecipe.Serializer {
        @Override
        public ShapelessRecipe read(Identifier identifier, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");
            DefaultedList<Ingredient> ingredients = URecipes.getIngredients(JsonHelper.getArray(json, "ingredients"));

            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (ingredients.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            }

            Identifier id = new Identifier(JsonHelper.getString(json, "appearance"));

            return new ZapAppleRecipe(identifier, group, UItems.ZAP_APPLE.setAppearance(UItems.ZAP_APPLE.getDefaultStack(), Registry.ITEM.getOrEmpty(id).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown item '" + id + "'");
            }).getDefaultStack()), ingredients);
        }

        @Override
        public ShapelessRecipe read(Identifier identifier, PacketByteBuf input) {
            String group = input.readString(32767);

            DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(input.readVarInt(), Ingredient.EMPTY);

            for(int j = 0; j < ingredients.size(); ++j) {
                ingredients.set(j, Ingredient.fromPacket(input));
            }

            return new ZapAppleRecipe(identifier, group, input.readItemStack(), ingredients);
        }
    }
}
