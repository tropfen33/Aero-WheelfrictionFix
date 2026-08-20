package com.Tropfen_33.aerowheelfrictionfix.client;

import com.Tropfen_33.aerowheelfrictionfix.AeroTweaks;
import com.Tropfen_33.aerowheelfrictionfix.item.ModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@net.neoforged.fml.common.Mod(
        value = AeroTweaks.MODID,
        dist = Dist.CLIENT
)
@EventBusSubscriber(
        modid = AeroTweaks.MODID,
        value = Dist.CLIENT
)
public class AeroTweaksClient {

    public AeroTweaksClient(ModContainer container) {

        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                ConfigurationScreen::new
        );
    }

    @SubscribeEvent
    static void onClientSetup(
            FMLClientSetupEvent event
    ) {
        AeroTweaks.LOGGER.info(
                "Aero Wheels Friction Fix client initialized."
        );
    }

    /**
     * Opens the friction HUD when the player right-clicks
     * a wheel with the Friction Wrench.
     *
     * The HUD starts in dragging mode, so the player does NOT
     * need to right-click a second time.
     */
    @SubscribeEvent
    static void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event
    ) {

        // Only handle this on the physical client.
        if (!event.getLevel().isClientSide()) {
            return;
        }

        // Only the main hand should activate the wrench.
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        // Make sure this is the local client player.
        if (!(event.getEntity()
                instanceof net.minecraft.client.player.LocalPlayer)) {
            return;
        }

        // Make sure the player is holding the friction wrench.
        if (!event.getItemStack().is(
                ModItems.FRICTION_WRENCH.get()
        )) {
            return;
        }

        BlockPos pos = event.getPos();

        BlockEntity blockEntity =
                event.getLevel().getBlockEntity(pos);

        if (blockEntity == null) {
            return;
        }

        /*
         * Only open the HUD for an Aeronautics wheel mount.
         */
        if (!blockEntity.getClass()
                .getName()
                .contains("WheelMountBlockEntity")) {
            return;
        }

        /*
         * Read the wheel's previously saved friction.
         *
         * If this wheel has never been changed before,
         * use 0.01 as the starting value.
         */
        double currentFriction = 0.01;

        if (blockEntity.getPersistentData()
                .contains("CustomFriction")) {

            currentFriction =
                    blockEntity.getPersistentData()
                            .getDouble("CustomFriction");
        }

        /*
         * Make sure the value is valid.
         */
        currentFriction = Math.clamp(
                currentFriction,
                0.0,
                1.0
        );

        /*
         * Open the HUD.
         *
         * WheelFrictionAdjustmentHUD starts with
         * isDragging = true, so the current right-click
         * acts as the beginning of the adjustment.
         */
        Minecraft.getInstance().setScreen(
                new WheelFrictionAdjustmentHUD(
                        pos,
                        currentFriction
                )
        );

        /*
         * Stop the normal interaction so the wrench does
         * not perform another action on the wheel.
         */
        event.setCanceled(true);
        event.setCancellationResult(
                InteractionResult.SUCCESS
        );
    }

    @SubscribeEvent
    static void onClientLogout(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {
        // Nothing currently needs to be cleaned up here.
    }
}