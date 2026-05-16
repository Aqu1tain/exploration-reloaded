package com.akitain.explorationreloaded.registry.other;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record BaitComponent(int level) implements TooltipProvider {
    public static final Codec<BaitComponent> CODEC = Codec.INT.xmap(BaitComponent::new, BaitComponent::level);
    public static final StreamCodec<ByteBuf, BaitComponent> PACKET_CODEC = ByteBufCodecs.VAR_INT.map(BaitComponent::new, BaitComponent::level);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
        textConsumer.accept(Component.translatable("component.exploration-reloaded.bait", this.level).withStyle(ChatFormatting.GRAY));
    }
}
