package com.nightmarecore.util;

import com.nightmarecore.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.Item;

/**
 * Trinket tiers used by the Blessing Bag and the Nightmare Forge.
 * The Nightmare Core and tools are intentionally not part of this list.
 */
public final class TrinketTier {

    public static final int TIER_1 = 1;
    public static final int TIER_2 = 2;
    public static final int TIER_3 = 3;

    private static final List<Item> TIER_1_ITEMS = List.of(
            ModItems.EMBER_HEART.get(),
            ModItems.DREAMREND_FANG.get(),
            ModItems.FLEET_FEATHER.get(),
            ModItems.VIGIL_CARAPACE.get(),
            ModItems.STONEBOUND_PENDANT.get(),
            ModItems.ECHO_BELL.get(),
            ModItems.DREAMHUNTER_EYE.get()
    );

    private static final List<Item> TIER_2_ITEMS = List.of(
            ModItems.EMBER_CROWN.get(),
            ModItems.TIDAL_PENDANT.get(),
            ModItems.RIFT_EYE.get(),
            ModItems.VIGIL_LANTERN.get(),
            ModItems.HOLLOW_CHARM.get()
    );

    private static final List<Item> TIER_3_ITEMS = List.of(
            ModItems.ABYSSAL_CROWN.get(),
            ModItems.VOID_GAUNTLET.get(),
            ModItems.FATE_DICE.get()
    );

    public static List<Item> tier1() {
        return TIER_1_ITEMS;
    }

    public static int tierOf(Item item) {
        if (TIER_1_ITEMS.contains(item)) {
            return TIER_1;
        }
        if (TIER_2_ITEMS.contains(item)) {
            return TIER_2;
        }
        if (TIER_3_ITEMS.contains(item)) {
            return TIER_3;
        }
        return 0;
    }

    public static int fuelCost(Item item) {
        return switch (tierOf(item)) {
            case TIER_1 -> 4;
            case TIER_2 -> 8;
            case TIER_3 -> 16;
            default -> 0;
        };
    }

    private TrinketTier() {
    }
}
