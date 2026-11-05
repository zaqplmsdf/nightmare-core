package com.nightmarecore.client;

import com.nightmarecore.CurseType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

/**
 * Builds the core tooltip's curse block. The suppressed annotation is read from
 * the local player's equipped blessings; Shift expands the short descriptions.
 */
public final class ClientCurseTooltip {

    public static void append(List<Component> tooltip) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        boolean shift = Screen.hasShiftDown();

        tooltip.add(Component.translatable("tooltip.nightmare_core.core.curses_header")
                .withStyle(ChatFormatting.DARK_PURPLE));

        for (CurseType curse : CurseType.values()) {
            boolean suppressed = player != null && curse.isSuppressed(player);
            MutableComponent line = Component.literal("• ")
                    .append(Component.translatable(curse.translationKey()));
            if (suppressed) {
                line.append(Component.translatable("tooltip.nightmare_core.core.suppressed")
                        .withStyle(ChatFormatting.GREEN));
            }
            tooltip.add(line.withStyle(suppressed ? ChatFormatting.DARK_GRAY : ChatFormatting.RED));

            if (shift) {
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable(curse.detailKey()))
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        if (!shift) {
            tooltip.add(Component.translatable("tooltip.nightmare_core.core.shift_hint")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private ClientCurseTooltip() {
    }
}
