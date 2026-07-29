package com.Tropfen_33.aerowheelfrictionfix;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(AeroTweaks.MODID)
public class AeroTweaks {
    public static final String MODID = "aerotweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    // NeoForge 1.21.1 injects the ModContainer automatically into the constructor if we ask for it!
    public AeroTweaks(IEventBus modEventBus, ModContainer modContainer) {
        // This is the correct method for NeoForge 1.21.1+ config registration
        modContainer.registerConfig(ModConfig.Type.COMMON, AeroTweaksConfig.SPEC);

        LOGGER.info("AeroWheelfrictionFix initialized successfully with configurable options!");
    }
}