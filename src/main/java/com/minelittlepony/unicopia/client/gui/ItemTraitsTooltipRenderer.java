package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.spell.trait.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;

public class ItemTraitsTooltipRenderer implements Text, OrderedText, TooltipComponent {

    private final SpellTraits traits;

    public ItemTraitsTooltipRenderer(SpellTraits traits) {
        this.traits = traits;
    }

    @Override
    public int getHeight() {
        return getRows() * 17 + 2;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return getColumns() * 17 + 2;
    }

    private int getColumns() {
        return Math.min(traits.entries().size(), Math.max(6, (int)Math.ceil(Math.sqrt(traits.entries().size() + 1))));
    }

    private int getRows() {
        int columns = getColumns();
        if (columns == traits.entries().size()) {
            return 1;
        }
        return Math.max(1, (int)Math.ceil((float)(traits.entries().size() + 1) / getColumns()));
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        int columns = getColumns();
        int i = 0;

        for (var entry : traits) {
            renderTraitIcon(entry.getKey(), entry.getValue(), matrices,
                    x + (i % columns) * 17,
                    y + (i / columns) * 17
            );
            i++;
        }
    }

    @Override
    public boolean accept(CharacterVisitor visitor) {
        return false;
    }

    @Override
    public OrderedText asOrderedText() {
        return this;
    }

    @Override
    public MutableText copy() {
        return new LiteralText("");
    }

    public static void renderTraitIcon(Trait trait, float value, MatrixStack matrices, int xx, int yy) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        int size = 12;

        if (MinecraftClient.getInstance().player != null) {
            TraitDiscovery discoveries = Pony.of(MinecraftClient.getInstance().player).getDiscoveries();

            if (!discoveries.isKnown(trait)) {
                trait = Trait.values()[MinecraftClient.getInstance().player.getRandom().nextInt(Trait.all().size())];
            }
        }

        RenderSystem.setShaderTexture(0, trait.getSprite());
        DrawableHelper.drawTexture(matrices, xx + 2, yy + 1, 0, 0, 0, size, size, size, size);

        matrices.push();
        matrices.translate(xx + 9, yy + 3 + size / 2, itemRenderer.zOffset + 200.0F);
        matrices.scale(0.5F, 0.5F, 1);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        textRenderer.draw(value > 99 ? "99+" : Math.round(value) + "", 0, 0, 16777215, true, matrices.peek().getPositionMatrix(), immediate, false, 0, 15728880);
        immediate.draw();
        matrices.pop();
    }

    @Override
    public Style getStyle() {
        return Style.EMPTY;
    }

    @Override
    public List<Text> getSiblings() {
        return new ArrayList<>();
    }

    @Override
    public String asString() {
        return "";
    }

    @Override
    public MutableText shallowCopy() {
        return copy();
    }
}
