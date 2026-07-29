package com.Tropfen_33.aerowheelfrictionfix;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AeroTweaksConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Global Fallback Multiplier
    public static final ModConfigSpec.DoubleValue ROLLING_FRICTION_MULTIPLIER = BUILDER
            .comment(" How much of the forward rolling friction stays active when NOT braking. (0.0 = frictionless, 1.0 = normal behavior)")
            .defineInRange("rollingFrictionMultiplier", 0.01, 0.0, 1.0);

    // Bare Wheel Mount Default set to 1.0 (Standard behavior)
    public static final ModConfigSpec.DoubleValue WHEEL_MOUNT_MULTIPLIER = BUILDER
            .comment(" Rolling friction multiplier for bare Wheel Mounts (offroad:wheel_mount)")
            .defineInRange("wheelMountMultiplier", 1.0, 0.0, 1.0);

    // Individual Tire Multipliers (Default 0.01)
    public static final ModConfigSpec.DoubleValue SMALL_TIRE_MULTIPLIER = BUILDER
            .comment(" Rolling friction multiplier for Small Tires (offroad:small_tire)")
            .defineInRange("smallTireMultiplier", 0.01, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue TIRE_MULTIPLIER = BUILDER
            .comment(" Rolling friction multiplier for Standard Tires (offroad:tire)")
            .defineInRange("tireMultiplier", 0.01, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue LARGE_TIRE_MULTIPLIER = BUILDER
            .comment(" Rolling friction multiplier for Large Tires (offroad:large_tire)")
            .defineInRange("largeTireMultiplier", 0.01, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue MONSTROUS_TIRE_MULTIPLIER = BUILDER
            .comment(" Rolling friction multiplier for Monstrous Tires (offroad:monstrous_tire)")
            .defineInRange("monstrousTireMultiplier", 0.01, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}