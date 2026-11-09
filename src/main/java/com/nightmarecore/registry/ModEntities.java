package com.nightmarecore.registry;

import com.nightmarecore.Config;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.entity.NightmareEntity;
import com.nightmarecore.entity.NightmareProjectile;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NightmareCore.MOD_ID);

    public static final RegistryObject<EntityType<NightmareEntity>> ASHEN_NIGHTMARE =
            register("ashen_nightmare");
    public static final RegistryObject<EntityType<NightmareEntity>> TIDAL_NIGHTMARE =
            register("tidal_nightmare");
    public static final RegistryObject<EntityType<NightmareEntity>> RIFT_NIGHTMARE =
            register("rift_nightmare");
    public static final RegistryObject<EntityType<NightmareEntity>> VIGIL_NIGHTMARE =
            register("vigil_nightmare");
    public static final RegistryObject<EntityType<NightmareEntity>> HOLLOW_NIGHTMARE =
            register("hollow_nightmare");

    public static final List<RegistryObject<EntityType<NightmareEntity>>> ALL = List.of(
            ASHEN_NIGHTMARE, TIDAL_NIGHTMARE, RIFT_NIGHTMARE, VIGIL_NIGHTMARE, HOLLOW_NIGHTMARE
    );

    public static final RegistryObject<EntityType<NightmareProjectile>> NIGHTMARE_PROJECTILE =
            ENTITY_TYPES.register("nightmare_projectile", () -> EntityType.Builder
                    .<NightmareProjectile>of(NightmareProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(6)
                    .updateInterval(10)
                    .build(new ResourceLocation(NightmareCore.MOD_ID, "nightmare_projectile").toString()));

    private static RegistryObject<EntityType<NightmareEntity>> register(String name) {
        boolean eye = "rift_nightmare".equals(name);
        return ENTITY_TYPES.register(name, () -> EntityType.Builder
                .of(NightmareEntity::new, MobCategory.MONSTER)
                .sized(eye ? 0.8F : 0.6F, eye ? 0.8F : 1.95F)
                .clientTrackingRange(10)
                .build(new ResourceLocation(NightmareCore.MOD_ID, name).toString()));
    }

    public static NightmareEntity createRandom(net.minecraft.world.level.Level level, RandomSource random) {
        int eye = Math.max(0, Config.EYE.spawnWeight().get());
        int skull = Math.max(0, Config.SKULL.spawnWeight().get());
        int shadow = Math.max(0, Config.SHADOW.spawnWeight().get());
        int hand = Math.max(0, Config.HAND.spawnWeight().get());
        int heart = Math.max(0, Config.HEART.spawnWeight().get());

        int favored = favoredVariantForPhase(level.getMoonPhase());
        if (favored >= 0) {
            double multiplier = Math.max(1.0D, Config.MOON_FAVORED_MULTIPLIER.get());
            switch (favored) {
                case 0 -> eye = (int) Math.round(eye * multiplier);
                case 1 -> skull = (int) Math.round(skull * multiplier);
                case 2 -> shadow = (int) Math.round(shadow * multiplier);
                case 3 -> hand = (int) Math.round(hand * multiplier);
                case 4 -> heart = (int) Math.round(heart * multiplier);
                default -> {
                }
            }
        }

        int total = eye + skull + shadow + hand + heart;
        if (total <= 0) {
            return null;
        }

        int roll = random.nextInt(total);
        EntityType<NightmareEntity> type;
        if (roll < eye) {
            type = RIFT_NIGHTMARE.get();
        } else if ((roll -= eye) < skull) {
            type = ASHEN_NIGHTMARE.get();
        } else if ((roll -= skull) < shadow) {
            type = VIGIL_NIGHTMARE.get();
        } else if ((roll -= shadow) < hand) {
            type = HOLLOW_NIGHTMARE.get();
        } else {
            type = TIDAL_NIGHTMARE.get();
        }
        return type.create(level);
    }

    /**
     * 0=full moon Eye, 1=waning gibbous Skull, 2=third quarter Shadow,
     * 3=waning crescent Hand, 4=new moon Heart, 5=waxing crescent Hand,
     * 6=first quarter Shadow, 7=waxing gibbous Skull.
     */
    public static int favoredVariantForPhase(int phase) {
        return switch (phase & 7) {
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            case 5 -> 3;
            case 6 -> 2;
            default -> 1;
        };
    }

    public static Item materialFor(EntityType<?> type) {
        if (type == ASHEN_NIGHTMARE.get()) {
            return ModItems.ASHEN_CINDER.get();
        }
        if (type == TIDAL_NIGHTMARE.get()) {
            return ModItems.TIDAL_TEAR.get();
        }
        if (type == RIFT_NIGHTMARE.get()) {
            return ModItems.RIFT_SHARD.get();
        }
        if (type == VIGIL_NIGHTMARE.get()) {
            return ModItems.VIGIL_EYE.get();
        }
        if (type == HOLLOW_NIGHTMARE.get()) {
            return ModItems.HOLLOW_SHELL.get();
        }
        return ModItems.NIGHTMARE_FRAGMENT.get();
    }

    public static Item blessingFor(EntityType<?> type) {
        if (type == ASHEN_NIGHTMARE.get()) {
            return ModItems.EMBER_HEART.get();
        }
        if (type == TIDAL_NIGHTMARE.get()) {
            return ModItems.MOONLIT_CHARM.get();
        }
        if (type == RIFT_NIGHTMARE.get()) {
            return ModItems.DREAMHUNTER_EYE.get();
        }
        if (type == VIGIL_NIGHTMARE.get()) {
            return ModItems.VIGIL_CARAPACE.get();
        }
        if (type == HOLLOW_NIGHTMARE.get()) {
            return ModItems.ECHO_BELL.get();
        }
        return ModItems.NIGHTMARE_FRAGMENT.get();
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(ModEntities::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        for (RegistryObject<EntityType<NightmareEntity>> type : ALL) {
            event.put(type.get(), Zombie.createAttributes().build());
        }
    }

    private ModEntities() {
    }
}
