package com.minelittlepony.unicopia.client;

import java.util.List;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class ModifierTooltipRenderer implements ItemTooltipCallback {

    @Override
    public void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines) {

        // TODO: Evaluate if we still need this
        /*int flags = stack.hasNbt() && stack.getNbt().contains("HideFlags", 99) ? stack.getNbt().getInt("HideFlags") : 0;

        if (isShowing(flags, ItemStack.TooltipSection.MODIFIERS)) {

            Enchantment s;
            Map<EquipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier>> modifiers = new HashMap<>();

            Equine.<PlayerEntity, Pony>of(MinecraftClient.getInstance().player).ifPresent(eq -> {
                getEnchantments(stack).filter(p -> p.getRight() instanceof AttributedEnchantment).forEach(pair -> {
                    ((AttributedEnchantment)pair.getRight()).getModifiers(eq, pair.getLeft(), modifiers);
                });
            });

            modifiers.forEach((slot, modifs) -> {

                List<Text> newLines = new ArrayList<>();

                modifs.entries().stream()
                    .filter(entry -> entry.getKey().equals(EntityAttributes.GENERIC_MOVEMENT_SPEED) || UEntityAttributes.REGISTRY.contains(entry.getKey()))
                    .forEach(entry -> describeModifiers(entry.getKey(), entry.getValue(), null, newLines));

                if (!newLines.isEmpty()) {
                    Text find = Text.translatable("item.modifiers." + slot.getName()).formatted(Formatting.GRAY);
                    int insertPosition = getInsertPosition(stack, find, flags, lines, tooltipType.isAdvanced());
                    if (insertPosition == -1) {
                        lines.add(ScreenTexts.EMPTY);
                        lines.add(find);
                        lines.addAll(newLines);
                    } else {
                        lines.addAll(insertPosition, newLines);
                    }
                }
            });
        }*/

        if (MinecraftClient.getInstance().player != null) {
            Pony.of(MinecraftClient.getInstance().player).getDiscoveries().appendTooltip(stack, MinecraftClient.getInstance().world, lines);
        }
    }
/*
    private int getInsertPosition(ItemStack stack, Text category, int flags, List<Text> lines, boolean advanced) {
        int insertPosition = lines.indexOf(category);

        if (insertPosition > -1) {
            return insertPosition + 1;
        }

        if (insertPosition == -1 && stack.hasNbt()) {
            if (isShowing(flags, ItemStack.TooltipSection.MODIFIERS) && stack.getNbt().getBoolean("Unbreakable")) {
                insertPosition = checkFor(lines, Text.translatable("item.unbreakable").formatted(Formatting.BLUE));
            }

            if (insertPosition == -1 && isShowing(flags, ItemStack.TooltipSection.CAN_DESTROY) && stack.getNbt().contains("CanDestroy", 9)) {
                insertPosition = checkFor(lines, Text.translatable("item.canBreak").formatted(Formatting.GRAY));
            }

            if (insertPosition == -1 && isShowing(flags, ItemStack.TooltipSection.CAN_PLACE) && stack.getNbt().contains("CanPlaceOn", 9)) {
                insertPosition = checkFor(lines, Text.translatable("item.canPlace").formatted(Formatting.GRAY));
            }
        }

        if (insertPosition == -1 && advanced) {
           if (stack.isDamaged()) {
               insertPosition = checkFor(lines, Text.translatable("item.durability", stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()));
           } else {
               insertPosition = checkFor(lines, Text.literal(Registries.ITEM.getId(stack.getItem()).toString()).formatted(Formatting.DARK_GRAY));
           }
        }

        return insertPosition;
    }

    private int checkFor(List<Text> lines, Text category) {
        return lines.indexOf(category);
    }

    private void describeModifiers(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, @Nullable PlayerEntity player, List<Text> lines) {
        double value = modifier.value();
        boolean baseAdjusted = false;
        if (player != null) {
            value += player.getAttributeBaseValue(attribute);
            baseAdjusted = true;
        }

        Operation op = modifier.operation();

        double displayValue;
        if (op != EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE && op != EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            displayValue = value;
        } else {
            displayValue = value * 100;
        }

        if (baseAdjusted) {
            lines.add(Text.literal(" ").append(getModifierLineBase("equals", displayValue, op, attribute, Formatting.DARK_GREEN)));
        } else if (value > 0) {
            lines.add(getModifierLineBase("plus", displayValue, op, attribute, attribute == UEntityAttributes.ENTITY_GRAVITY_MODIFIER ? Formatting.RED : Formatting.BLUE));
        } else if (value < 0) {
            lines.add(getModifierLineBase("take", -displayValue, op, attribute, attribute == UEntityAttributes.ENTITY_GRAVITY_MODIFIER ? Formatting.BLUE : Formatting.RED));
        }
    }

    private Text getModifierLineBase(String root, double displayValue, Operation op, EntityAttribute attribute, Formatting color) {
        return Text.translatable("attribute.modifier." + root + "." + op.getId(),
                ItemStack.MODIFIER_FORMAT.format(displayValue),
                Text.translatable(attribute.getTranslationKey())
            ).formatted(color);
    }


    private static boolean isShowing(int flags, ItemStack.TooltipSection section) {
        return (flags & section.getFlag()) == 0;
    }

    private static Stream<Pair<Integer, Enchantment>> getEnchantments(ItemStack stack) {
        if (!stack.isEmpty()) {
            return stack.getEnchantments()
                    .stream()
                    .map(t -> (NbtCompound)t)
                    .map(tag -> Registries.ENCHANTMENT.getOrEmpty(Identifier.tryParse(tag.getString("id")))
                                .map(ench -> new Pair<>(tag.getInt("lvl"), ench)))
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        }

        return Stream.empty();
    }
*/
}
