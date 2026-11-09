package com.nightmarecore;

import com.nightmarecore.registry.ModItems;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import com.nightmarecore.util.CurioUtil;

/**
 * The seven curses and the blessing that suppresses each one. Only three
 * blessing slots exist, so the player must choose which three curses to keep
 * suppressed.
 */
public enum CurseType {

    SLEEP_NIGHTMARE("sleep_nightmare", ModItems.VIGIL_CARAPACE),
    HEALTH_CAP("health_cap", ModItems.EMBER_HEART),
    DAMAGE_PENALTY("damage_penalty", ModItems.DREAMREND_FANG),
    SPAWN_POINT("spawn_point", ModItems.STONEBOUND_PENDANT),
    AWAKE_DOUBLE_DAMAGE("awake_double_damage", ModItems.FLEET_FEATHER),
    DARKNESS("darkness", ModItems.DREAMHUNTER_EYE),
    INSANITY("insanity", ModItems.ECHO_BELL);

    private final String id;
    private final Supplier<Item> blessing;

    CurseType(String id, Supplier<Item> blessing) {
        this.id = id;
        this.blessing = blessing;
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return "curse.nightmare_core." + this.id;
    }

    public String detailKey() {
        return "curse.nightmare_core." + this.id + ".desc";
    }

    public boolean isSuppressed(Player player) {
        return CurioUtil.hasBlessing(player, this.blessing.get());
    }

    public static Optional<CurseType> byBlessing(Item item) {
        return Arrays.stream(values()).filter(curse -> curse.blessing.get() == item).findFirst();
    }

    public static boolean active(Player player, CurseType type) {
        return CurioUtil.hasNightmareCore(player) && !type.isSuppressed(player);
    }
}
