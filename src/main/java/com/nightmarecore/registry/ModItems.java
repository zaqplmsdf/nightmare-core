package com.nightmarecore.registry;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.item.AttributeBlessingItem;
import com.nightmarecore.item.BlessingBagItem;
import com.nightmarecore.item.BlessingItem;
import com.nightmarecore.item.DreamAnchorItem;
import com.nightmarecore.item.DreamhunterEyeItem;
import com.nightmarecore.item.EmberBladeItem;
import com.nightmarecore.item.FateDiceItem;
import com.nightmarecore.item.NightmareCoreItem;
import com.nightmarecore.item.NightmareFragmentItem;
import com.nightmarecore.item.NightmareMaterialItem;
import com.nightmarecore.item.NightmarePrismItem;
import java.util.function.Supplier;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NightmareCore.MOD_ID);

    public static final RegistryObject<Item> NIGHTMARE_CORE = register("nightmare_core",
            () -> new NightmareCoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NIGHTMARE_FRAGMENT = register("nightmare_fragment",
            () -> new NightmareFragmentItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ASHEN_CINDER = register("ashen_cinder",
            () -> new NightmareMaterialItem("ashen_cinder", new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> TIDAL_TEAR = register("tidal_tear",
            () -> new NightmareMaterialItem("tidal_tear", new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RIFT_SHARD = register("rift_shard",
            () -> new NightmareMaterialItem("rift_shard", new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> VIGIL_EYE = register("vigil_eye",
            () -> new NightmareMaterialItem("vigil_eye", new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> HOLLOW_SHELL = register("hollow_shell",
            () -> new NightmareMaterialItem("hollow_shell", new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> EMBER_HEART = blessing("ember_heart");
    public static final RegistryObject<Item> DREAMREND_FANG = blessing("dreamrend_fang");
    public static final RegistryObject<Item> FLEET_FEATHER = blessing("fleet_feather");
    public static final RegistryObject<Item> VIGIL_CARAPACE = blessing("vigil_carapace");
    public static final RegistryObject<Item> STONEBOUND_PENDANT = blessing("stonebound_pendant");
    public static final RegistryObject<Item> ECHO_BELL = blessing("echo_bell");
    public static final RegistryObject<Item> DREAMHUNTER_EYE = register("dreamhunter_eye",
            () -> new DreamhunterEyeItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> EMBER_CROWN = specialBlessing("ember_crown",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.ARMOR, 2.0D),
                    new AttributeBlessingItem.AttributeBonus(Attributes.ARMOR_TOUGHNESS, 0.5D)),
            List.of(MobEffects.FIRE_RESISTANCE));
    public static final RegistryObject<Item> TIDAL_PENDANT = specialBlessing("tidal_pendant",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.MAX_HEALTH, 4.0D)),
            List.of(MobEffects.WATER_BREATHING));
    public static final RegistryObject<Item> RIFT_EYE = specialBlessing("rift_eye",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.MOVEMENT_SPEED, 0.01D)),
            List.of(MobEffects.NIGHT_VISION));
    public static final RegistryObject<Item> VIGIL_LANTERN = specialBlessing("vigil_lantern",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.ARMOR, 2.0D)),
            List.of(MobEffects.DAMAGE_RESISTANCE));
    public static final RegistryObject<Item> HOLLOW_CHARM = specialBlessing("hollow_charm",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.ATTACK_DAMAGE, 2.0D),
                    new AttributeBlessingItem.AttributeBonus(Attributes.KNOCKBACK_RESISTANCE, 0.5D)),
            List.of());
    public static final RegistryObject<Item> ABYSSAL_CROWN = specialBlessing("abyssal_crown",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.ARMOR, 6.0D),
                    new AttributeBlessingItem.AttributeBonus(Attributes.ARMOR_TOUGHNESS, 3.0D),
                    new AttributeBlessingItem.AttributeBonus(Attributes.MAX_HEALTH, 8.0D)),
            List.of(MobEffects.DAMAGE_RESISTANCE));
    public static final RegistryObject<Item> VOID_GAUNTLET = specialBlessing("void_gauntlet",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.ATTACK_DAMAGE, 5.0D),
                    new AttributeBlessingItem.AttributeBonus(Attributes.ATTACK_SPEED, 0.8D)),
            List.of(MobEffects.DAMAGE_BOOST));
    public static final RegistryObject<Item> FATE_DICE = register("fate_dice",
            () -> new FateDiceItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> MOONLIT_CHARM = specialBlessing("moonlit_charm",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.ARMOR, 1.0D),
                    new AttributeBlessingItem.AttributeBonus(Attributes.MOVEMENT_SPEED, 0.005D)),
            List.of(), Rarity.UNCOMMON);
    public static final RegistryObject<Item> BLOODMOON_BRACER = specialBlessing("bloodmoon_bracer",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.ATTACK_DAMAGE, 1.0D)),
            List.of(), Rarity.UNCOMMON);
    public static final RegistryObject<Item> MOONSHADOW_CLOAK = specialBlessing("moonshadow_cloak",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.ARMOR, 2.0D),
                    new AttributeBlessingItem.AttributeBonus(Attributes.MOVEMENT_SPEED, 0.01D)),
            List.of(MobEffects.SLOW_FALLING), Rarity.RARE);
    public static final RegistryObject<Item> BLOODPACT_RING = specialBlessing("bloodpact_ring",
            List.of(new AttributeBlessingItem.AttributeBonus(Attributes.MAX_HEALTH, 4.0D),
                    new AttributeBlessingItem.AttributeBonus(Attributes.ATTACK_DAMAGE, 1.5D)),
            List.of(MobEffects.REGENERATION), Rarity.RARE);
    public static final RegistryObject<Item> CURSE_REVERSAL = register("curse_reversal",
            () -> new BlessingItem("curse_reversal",
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> NIGHTMARE_PRISM = register("nightmare_prism",
            () -> new NightmarePrismItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> DREAM_ANCHOR = register("dream_anchor",
            () -> new DreamAnchorItem(new Item.Properties().stacksTo(1).durability(8).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> EMBER_BLADE = register("ember_blade",
            () -> new EmberBladeItem(new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BLESSING_BAG = register("blessing_bag",
            () -> new BlessingBagItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> ASHEN_NIGHTMARE_SPAWN_EGG = register("ashen_nightmare_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ASHEN_NIGHTMARE, 0x3A2B2B, 0xE07B3A,
                    new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> TIDAL_NIGHTMARE_SPAWN_EGG = register("tidal_nightmare_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TIDAL_NIGHTMARE, 0x1E3A5F, 0x62C8D9,
                    new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RIFT_NIGHTMARE_SPAWN_EGG = register("rift_nightmare_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.RIFT_NIGHTMARE, 0x3B2456, 0xB56BE0,
                    new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> VIGIL_NIGHTMARE_SPAWN_EGG = register("vigil_nightmare_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.VIGIL_NIGHTMARE, 0x2B2A33, 0xD8B45A,
                    new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> HOLLOW_NIGHTMARE_SPAWN_EGG = register("hollow_nightmare_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HOLLOW_NIGHTMARE, 0x4A4A50, 0xB8B8C0,
                    new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));

    private static RegistryObject<Item> blessing(String id) {
        return register(id, () -> new BlessingItem(id,
                new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    }

    private static RegistryObject<Item> specialBlessing(
            String id, List<AttributeBlessingItem.AttributeBonus> bonuses,
            List<net.minecraft.world.effect.MobEffect> effects) {
        return specialBlessing(id, bonuses, effects, Rarity.RARE);
    }

    private static RegistryObject<Item> specialBlessing(
            String id, List<AttributeBlessingItem.AttributeBonus> bonuses,
            List<net.minecraft.world.effect.MobEffect> effects, Rarity rarity) {
        return register(id, () -> new AttributeBlessingItem(id, bonuses, effects,
                new Item.Properties().stacksTo(1).rarity(rarity)));
    }

    private static RegistryObject<Item> register(String name, Supplier<? extends Item> supplier) {
        return ITEMS.register(name, supplier);
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private ModItems() {
    }
}
