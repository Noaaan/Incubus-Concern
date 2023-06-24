package net.id.incubus_core.recipe;

import com.google.gson.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.recipe.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;

public class ItemDamagingRecipe<C extends RecipeInputInventory> extends ShapelessRecipe {

    public ItemDamagingRecipe(ShapelessRecipe parent) {
        super(parent.getId(), parent.getGroup(), parent.getCategory(), parent.getOutput(null), parent.getIngredients());
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory inventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem().isDamageable() && stack.getDamage() + 1 < stack.getMaxDamage()) { // Override damageable, fallback onto remainders
                stack = stack.copy();
                stack.setDamage(stack.getDamage() + 1); // Damage item by one
                defaultedList.set(i, stack);
            } else if (stack.getItem().hasRecipeRemainder()) {
                defaultedList.set(i, new ItemStack(stack.getItem().getRecipeRemainder()));
            }
        }
        return defaultedList;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ItemDamagingRecipe.Serializer.INSTANCE;
    }

    public static class Serializer extends ShapelessRecipe.Serializer {

        public static final ItemDamagingRecipe.Serializer INSTANCE = new ItemDamagingRecipe.Serializer();

        @Override
        public ShapelessRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new ItemDamagingRecipe<>(super.read(identifier, jsonObject));
        }

        @Override
        public ShapelessRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new ItemDamagingRecipe<>(super.read(identifier, packetByteBuf));
        }
    }
}

