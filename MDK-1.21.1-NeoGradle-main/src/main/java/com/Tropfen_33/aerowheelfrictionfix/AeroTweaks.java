package com.Tropfen_33.aerowheelfrictionfix;

import com.Tropfen_33.aerowheelfrictionfix.item.ModItems;
import com.Tropfen_33.aerowheelfrictionfix.network.UpdateWheelFrictionPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(AeroTweaks.MODID)
public class AeroTweaks {
    public static final String MODID = "aerotweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AeroTweaks(IEventBus modEventBus, ModContainer modContainer) {
        // Register the simplified config spec
        modContainer.registerConfig(ModConfig.Type.COMMON, AeroTweaksConfig.SPEC);
        
        ModItems.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerPayloads);

        LOGGER.info("AeroWheelfrictionFix initialized successfully!");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.FRICTION_WRENCH.get());
        }
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0.0");
        registrar.playToServer(
                UpdateWheelFrictionPayload.TYPE,
                UpdateWheelFrictionPayload.STREAM_CODEC,
                UpdateWheelFrictionPayload::handleData
        );
    }
}