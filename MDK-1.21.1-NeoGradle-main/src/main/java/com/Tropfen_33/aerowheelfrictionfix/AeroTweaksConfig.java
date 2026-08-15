package com.Tropfen_33.aerowheelfrictionfix;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AeroTweaksConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue DEFAULT_FRICTION = BUILDER
            .comment("Standard starting rolling friction multiplier for wheels (default: 1.0)")
            .defineInRange("defaultFriction", 1.0, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}