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
        return ENTITY_TYPES.register(name, () -> EntityType.Builder
                .of(NightmareEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(10)
                .build(new ResourceLocation(NightmareCore.MOD_ID, name).toString()));
    }

    public static NightmareEntity createRandom(net.minecraft.world.level.Level level, RandomSource random) {
        int eye = Math.max(0, Config.EYE.spawnWeight().get());
        int skull = Math.max(0, Config.SKULL.spawnWeight().get());
        int shadow = Math.max(0, Config.SHADOW.spawnWeight().get());
        int hand = Math.max(0, Config.HAND.spawnWeight().get());
        int heart = Math.max(0, Config.HEART.spawnWeight().get());
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
            return ModItems.EMBER_CROWN.get();
        }
        if (type == TIDAL_NIGHTMARE.get()) {
            return ModItems.TIDAL_PENDANT.get();
        }
        if (type == RIFT_NIGHTMARE.get()) {
            return ModItems.RIFT_EYE.get();
        }
        if (type == VIGIL_NIGHTMARE.get()) {
            return ModItems.VIGIL_LANTERN.get();
        }
        if (type == HOLLOW_NIGHTMARE.get()) {
            return ModItems.HOLLOW_CHARM.get();
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
