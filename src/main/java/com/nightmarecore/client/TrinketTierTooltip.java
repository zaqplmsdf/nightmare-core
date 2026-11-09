package com.nightmarecore.client;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.util.TrinketTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TrinketTierTooltip {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        int tier = TrinketTier.tierOf(event.getItemStack().getItem());
        if (tier > 0) {
            event.getToolTip().add(Component.translatable("tooltip.nightmare_core.trinket_tier", tier)
                    .withStyle(tier == TrinketTier.TIER_1
                            ? ChatFormatting.GRAY
                            : tier == TrinketTier.TIER_2 ? ChatFormatting.BLUE : ChatFormatting.LIGHT_PURPLE));
        }
    }

    private TrinketTierTooltip() {
    }
}
