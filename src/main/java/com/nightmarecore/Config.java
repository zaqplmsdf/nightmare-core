package com.nightmarecore;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common config. Drop values are intentionally independent from Looting and Fortune.
 * Comments are written in Simplified Chinese for the pack's generated TOML file;
 * translation keys are also provided for config UIs.
 */
public final class Config {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_FRAGMENT_DROPS;
    public static final ForgeConfigSpec.DoubleValue BASE_CHANCE_PERCENT;
    public static final ForgeConfigSpec.DoubleValue CHANCE_PER_LUCK_LEVEL;
    public static final ForgeConfigSpec.DoubleValue CHARM_BONUS_PERCENT;
    public static final ForgeConfigSpec.IntValue MIN_FRAGMENTS;
    public static final ForgeConfigSpec.IntValue MAX_FRAGMENTS;
    public static final ForgeConfigSpec.BooleanValue DROP_FROM_MOBS;
    public static final ForgeConfigSpec.BooleanValue DROP_FROM_BLOCKS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PASSIVE_MOBS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PLAYER_KILLS;
    public static final ForgeConfigSpec.BooleanValue KEEP_CORE_ON_DEATH;
    public static final ForgeConfigSpec.BooleanValue GIVE_CORE_ON_FIRST_SLEEP;
    public static final ForgeConfigSpec.DoubleValue NIGHTMARE_SPAWN_CHANCE_PERCENT;
    public static final ForgeConfigSpec.DoubleValue BLESSING_BAG_CHANCE_PERCENT;

    public static final VariantStats EYE;
    public static final VariantStats SKULL;
    public static final VariantStats SHADOW;
    public static final VariantStats HEART;
    public static final VariantStats HAND;

    public static final ForgeConfigSpec.DoubleValue EYE_RANGED_DISTANCE;
    public static final ForgeConfigSpec.IntValue EYE_RANGED_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue EYE_PROJECTILE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue EYE_PROJECTILE_SPEED;
    public static final ForgeConfigSpec.DoubleValue SHADOW_DAMAGE_REDUCTION_PERCENT;
    public static final ForgeConfigSpec.DoubleValue HEART_AURA_RADIUS;
    public static final ForgeConfigSpec.IntValue HAND_EFFECT_DURATION_TICKS;

    public static final ForgeConfigSpec.DoubleValue CURSE_HEALTH_CAP_DEFAULT_PERCENT;
    public static final ForgeConfigSpec.IntValue CURSE_HEALTH_CAP_LEVELS_PER_STEP;
    public static final ForgeConfigSpec.DoubleValue CURSE_HEALTH_CAP_STEP_PERCENT;
    public static final ForgeConfigSpec.DoubleValue CURSE_HEALTH_CAP_MAX_PERCENT;
    public static final ForgeConfigSpec.DoubleValue CURSE_DAMAGE_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue CURSE_DAMAGE_REDUCTION_PERCENT;
    public static final ForgeConfigSpec.DoubleValue CURSE_SPAWN_HOSTILE_RADIUS;
    public static final ForgeConfigSpec.DoubleValue CURSE_DARKNESS_TRIGGER_SECONDS;
    public static final ForgeConfigSpec.DoubleValue CURSE_DARKNESS_DAMAGE_PERCENT;
    public static final ForgeConfigSpec.DoubleValue CURSE_INSANITY_DAYS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("fragment_drops");
        builder.comment("梦魇碎片掉落相关设置。");
        builder.translation("nightmare_core.config.fragment_drops");

        ENABLE_FRAGMENT_DROPS = builder
                .comment("碎片掉落总开关。",
                        "只有把梦魇之核佩戴在梦魇槽中的玩家才能触发掉落。")
                .translation("nightmare_core.config.enable_fragment_drops")
                .define("enableFragmentDrops", true);
        BASE_CHANCE_PERCENT = builder
                .comment("每次成功判定的基础概率（百分比）。")
                .translation("nightmare_core.config.base_chance")
                .defineInRange("baseChancePercent", 5.0D, 0.0D, 100.0D);
        CHANCE_PER_LUCK_LEVEL = builder
                .comment("每 1 级幸运属性增加的概率（百分比）。",
                        "最终概率 = 基础概率 + 本值 × floor(幸运)。",
                        "抢夺、时运和工具附魔不会参与计算。")
                .translation("nightmare_core.config.chance_per_luck")
                .defineInRange("chancePerLuckLevel", 5.0D, 0.0D, 100.0D);
        CHARM_BONUS_PERCENT = builder
                .comment("佩戴「猎梦之眼」时额外增加的概率（百分比）。",
                        "同种赐福只能佩戴一件，因此该加成最多生效一次。")
                .translation("nightmare_core.config.charm_bonus")
                .defineInRange("charmBonusPercent", 5.0D, 0.0D, 100.0D);
        MIN_FRAGMENTS = builder
                .comment("一次成功判定掉落的最少碎片数量。")
                .translation("nightmare_core.config.min_fragments")
                .defineInRange("minFragments", 1, 1, 64);
        MAX_FRAGMENTS = builder
                .comment("一次成功判定掉落的最多碎片数量。")
                .translation("nightmare_core.config.max_fragments")
                .defineInRange("maxFragments", 3, 1, 64);
        DROP_FROM_MOBS = builder
                .comment("允许在玩家击杀生物时触发掉落。")
                .translation("nightmare_core.config.drop_from_mobs")
                .define("dropFromMobs", true);
        DROP_FROM_BLOCKS = builder
                .comment("允许在玩家破坏方块时触发掉落。")
                .translation("nightmare_core.config.drop_from_blocks")
                .define("dropFromBlocks", true);
        ALLOW_PASSIVE_MOBS = builder
                .comment("允许被动生物和其他非敌对生物计入击杀掉落。")
                .translation("nightmare_core.config.allow_passive_mobs")
                .define("allowPassiveMobs", true);
        ALLOW_PLAYER_KILLS = builder
                .comment("允许 PvP 击杀计入碎片掉落。",
                        "默认关闭，避免刷人头。")
                .translation("nightmare_core.config.allow_player_kills")
                .define("allowPlayerKills", false);
        builder.pop();

        builder.push("nightmare_core");
        builder.comment("梦魇之核自身设置。");
        builder.translation("nightmare_core.config.nightmare_core");

        KEEP_CORE_ON_DEATH = builder
                .comment("死亡时保留梦魇之核。",
                        "梦魇之核是进度钥匙，默认保留以避免意外死亡导致循环中断。")
                .translation("nightmare_core.config.keep_core_on_death")
                .define("keepCoreOnDeath", true);
        GIVE_CORE_ON_FIRST_SLEEP = builder
                .comment("玩家第一次睡觉时，是否在床边掉落一枚梦魇之核。",
                        "关闭后不会自动给予，梦魇之核也不会再生成合成配方。")
                .translation("nightmare_core.config.give_core_on_first_sleep")
                .define("giveCoreOnFirstSleep", true);
        builder.pop();

        builder.push("nightmares");
        builder.comment("梦魇生物相关设置。");
        builder.translation("nightmare_core.config.nightmares");

        NIGHTMARE_SPAWN_CHANCE_PERCENT = builder
                .comment("佩戴梦魇之核入睡时，生成梦魇的概率（百分比）。",
                        "只在非和平难度下生效，每次上床判定一次。")
                .translation("nightmare_core.config.sleep_spawn_chance")
                .defineInRange("sleepSpawnChancePercent", 10.0D, 0.0D, 100.0D);
        BLESSING_BAG_CHANCE_PERCENT = builder
                .comment("击败梦魇后掉落祝福袋的概率（百分比）。")
                .translation("nightmare_core.config.blessing_bag_chance")
                .defineInRange("blessingBagChancePercent", 10.0D, 0.0D, 100.0D);

        EYE = defineVariant(builder, "eye", "梦魇之眸",
                20.0D, 3.0D, 3.0D, 3.0D, 0.23D, 35.0D, 2.0D, 30, 30.0D);
        SKULL = defineVariant(builder, "skull", "梦魇之颅",
                32.0D, 2.1D, 2.1D, 2.1D, 0.23D, 35.0D, 2.0D, 25, 30.0D);
        SHADOW = defineVariant(builder, "shadow", "梦魇之影",
                20.0D, 3.0D, 3.0D, 3.0D, 0.345D, 35.0D, 2.0D, 20, 30.0D);
        HEART = defineVariant(builder, "heart", "梦魇之心",
                20.0D, 3.0D, 3.0D, 3.0D, 0.0D, 35.0D, 2.0D, 5, 30.0D);
        HAND = defineVariant(builder, "hand", "梦魇之手",
                20.0D, 3.0D, 3.0D, 3.0D, 0.23D, 35.0D, 2.0D, 20, 30.0D);

        EYE_RANGED_DISTANCE = builder
                .comment("梦魇之眸开始远程射击的最小距离（格）。")
                .translation("nightmare_core.config.nightmares.eye.ranged_distance")
                .defineInRange("eyeRangedDistance", 4.0D, 1.0D, 64.0D);
        EYE_RANGED_COOLDOWN_TICKS = builder
                .comment("梦魇之眸两次远程射击的间隔（tick）。")
                .translation("nightmare_core.config.nightmares.eye.ranged_cooldown")
                .defineInRange("eyeRangedCooldownTicks", 30, 5, 1200);
        EYE_PROJECTILE_DAMAGE = builder
                .comment("梦魇之眸弹射物的伤害。")
                .translation("nightmare_core.config.nightmares.eye.projectile_damage")
                .defineInRange("eyeProjectileDamage", 3.0D, 0.0D, 2048.0D);
        EYE_PROJECTILE_SPEED = builder
                .comment("梦魇之眸弹射物的初速度（格/tick）。")
                .translation("nightmare_core.config.nightmares.eye.projectile_speed")
                .defineInRange("eyeProjectileSpeed", 0.22D, 0.01D, 4.0D);
        SHADOW_DAMAGE_REDUCTION_PERCENT = builder
                .comment("梦魇之影受到无附魔武器攻击时的减伤比例（百分比）。")
                .translation("nightmare_core.config.nightmares.shadow.damage_reduction")
                .defineInRange("shadowDamageReductionPercent", 40.0D, 0.0D, 100.0D);
        HEART_AURA_RADIUS = builder
                .comment("梦魇之心保护周围梦魇的半径（格）。")
                .translation("nightmare_core.config.nightmares.heart.aura_radius")
                .defineInRange("heartAuraRadius", 16.0D, 0.0D, 64.0D);
        HAND_EFFECT_DURATION_TICKS = builder
                .comment("梦魇之手附加中毒/凋灵的持续时间（tick）。")
                .translation("nightmare_core.config.nightmares.hand.effect_duration")
                .defineInRange("handEffectDurationTicks", 100, 1, 1200);
        builder.pop();

        builder.push("curses");
        builder.comment("七重诅咒相关设置。此处只包含需要平衡调整的诅咒。");
        builder.translation("nightmare_core.config.curses");

        builder.push("health_cap");
        builder.comment("第 2 诅咒：生命回复上限。");
        builder.translation("nightmare_core.config.curse_health_cap");
        CURSE_HEALTH_CAP_DEFAULT_PERCENT = builder
                .comment("0 级时的生命回复上限比例（百分比）。默认 10%。")
                .translation("nightmare_core.config.curse_health_cap_default")
                .defineInRange("defaultPercent", 10.0D, 0.0D, 100.0D);
        CURSE_HEALTH_CAP_LEVELS_PER_STEP = builder
                .comment("每多少经验等级提升一次上限。默认 20 级。")
                .translation("nightmare_core.config.curse_health_cap_levels")
                .defineInRange("levelsPerIncrement", 20, 1, 10000);
        CURSE_HEALTH_CAP_STEP_PERCENT = builder
                .comment("每次提升增加的上限比例（百分比）。默认 10%。")
                .translation("nightmare_core.config.curse_health_cap_step")
                .defineInRange("incrementPercent", 10.0D, 0.0D, 100.0D);
        CURSE_HEALTH_CAP_MAX_PERCENT = builder
                .comment("生命回复上限的最高比例（百分比）。默认 90%。")
                .translation("nightmare_core.config.curse_health_cap_max")
                .defineInRange("maxPercent", 90.0D, 0.0D, 100.0D);
        builder.pop();

        builder.push("damage");
        builder.comment("第 3 诅咒：伤害衰减。");
        builder.translation("nightmare_core.config.curse_damage");
        CURSE_DAMAGE_THRESHOLD = builder
                .comment("伤害阈值。玩家对他人造成的伤害不高于此值时完全无效。默认 5 点。")
                .translation("nightmare_core.config.curse_damage_threshold")
                .defineInRange("threshold", 5.0D, 0.0D, 2048.0D);
        CURSE_DAMAGE_REDUCTION_PERCENT = builder
                .comment("超过阈值后的伤害削减比例（百分比）。默认 20%。")
                .translation("nightmare_core.config.curse_damage_reduction")
                .defineInRange("reductionPercent", 20.0D, 0.0D, 100.0D);
        builder.pop();

        builder.push("spawn_point");
        builder.comment("第 4 诅咒：出生点限制。");
        builder.translation("nightmare_core.config.curse_spawn_point");
        CURSE_SPAWN_HOSTILE_RADIUS = builder
                .comment("保存出生点时，检测敌对生物的水平范围（格）。默认 16。")
                .translation("nightmare_core.config.curse_spawn_radius")
                .defineInRange("hostileRadius", 16.0D, 0.0D, 128.0D);
        builder.pop();

        builder.push("darkness");
        builder.comment("第 6 诅咒：黑暗侵蚀。");
        builder.translation("nightmare_core.config.curse_darkness");
        CURSE_DARKNESS_TRIGGER_SECONDS = builder
                .comment("处于亮度低于 3 的环境多少秒后开始受伤。默认 5 秒。")
                .translation("nightmare_core.config.curse_darkness_trigger")
                .defineInRange("triggerSeconds", 5.0D, 0.0D, 600.0D);
        CURSE_DARKNESS_DAMAGE_PERCENT = builder
                .comment("触发后每秒损失的最大生命值比例（百分比）。默认 20%。",
                        "该伤害不致死，最多保留 1 点生命。")
                .translation("nightmare_core.config.curse_darkness_damage")
                .defineInRange("damagePercentPerSecond", 20.0D, 0.0D, 100.0D);
        builder.pop();

        builder.push("insanity");
        builder.comment("第 7 诅咒：精神失常。");
        builder.translation("nightmare_core.config.curse_insanity");
        CURSE_INSANITY_DAYS = builder
                .comment("连续多少游戏日不入睡后触发精神失常。默认 7 天。")
                .translation("nightmare_core.config.curse_insanity_days")
                .defineInRange("days", 7.0D, 0.1D, 3650.0D);
        builder.pop();

        builder.pop();

        SPEC = builder.build();
    }

    private static VariantStats defineVariant(
            ForgeConfigSpec.Builder builder, String id, String name,
            double health, double easy, double normal, double hard,
            double speed, double followRange, double armor, int spawnWeight,
            double sunlightPercent) {
        builder.push(id);
        builder.comment(name + "的专属属性与生成权重。");
        builder.translation("nightmare_core.config.nightmares." + id);

        ForgeConfigSpec.DoubleValue healthValue = builder
                .comment("最大生命值。")
                .translation("nightmare_core.config.nightmares." + id + ".health")
                .defineInRange("health", health, 1.0D, 1024.0D);
        ForgeConfigSpec.DoubleValue easyValue = builder
                .comment("简单难度攻击伤害。")
                .translation("nightmare_core.config.nightmares." + id + ".attack_easy")
                .defineInRange("attackDamageEasy", easy, 0.0D, 2048.0D);
        ForgeConfigSpec.DoubleValue normalValue = builder
                .comment("普通难度攻击伤害。")
                .translation("nightmare_core.config.nightmares." + id + ".attack_normal")
                .defineInRange("attackDamageNormal", normal, 0.0D, 2048.0D);
        ForgeConfigSpec.DoubleValue hardValue = builder
                .comment("困难难度攻击伤害。")
                .translation("nightmare_core.config.nightmares." + id + ".attack_hard")
                .defineInRange("attackDamageHard", hard, 0.0D, 2048.0D);
        ForgeConfigSpec.DoubleValue speedValue = builder
                .comment("移动速度属性。")
                .translation("nightmare_core.config.nightmares." + id + ".speed")
                .defineInRange("movementSpeed", speed, 0.0D, 10.0D);
        ForgeConfigSpec.DoubleValue followValue = builder
                .comment("跟随范围。")
                .translation("nightmare_core.config.nightmares." + id + ".follow_range")
                .defineInRange("followRange", followRange, 1.0D, 128.0D);
        ForgeConfigSpec.DoubleValue armorValue = builder
                .comment("护甲值。")
                .translation("nightmare_core.config.nightmares." + id + ".armor")
                .defineInRange("armor", armor, 0.0D, 30.0D);
        ForgeConfigSpec.IntValue weightValue = builder
                .comment("生成权重；0 表示不会自然生成。")
                .translation("nightmare_core.config.nightmares." + id + ".spawn_weight")
                .defineInRange("spawnWeight", spawnWeight, 0, 1000);
        ForgeConfigSpec.DoubleValue sunlightValue = builder
                .comment("阳光每秒损失的最大生命值百分比。")
                .translation("nightmare_core.config.nightmares." + id + ".sunlight")
                .defineInRange("sunlightDamagePercent", sunlightPercent, 0.0D, 100.0D);
        builder.pop();

        return new VariantStats(healthValue, easyValue, normalValue, hardValue,
                speedValue, followValue, armorValue, weightValue, sunlightValue);
    }

    public record VariantStats(
            ForgeConfigSpec.DoubleValue health,
            ForgeConfigSpec.DoubleValue attackEasy,
            ForgeConfigSpec.DoubleValue attackNormal,
            ForgeConfigSpec.DoubleValue attackHard,
            ForgeConfigSpec.DoubleValue movementSpeed,
            ForgeConfigSpec.DoubleValue followRange,
            ForgeConfigSpec.DoubleValue armor,
            ForgeConfigSpec.IntValue spawnWeight,
            ForgeConfigSpec.DoubleValue sunlightPercent) {
    }

    private Config() {
    }
}
