package com.Tropfen_33.aerowheelfrictionfix.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class FrictionWrench extends Item {

    public FrictionWrench(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        /*
         * The client GUI is opened from AeroTweaksClient.
         *
         * Keeping this item class completely client-independent is important:
         * FrictionWrench is registered on both the physical client and
         * dedicated server.
         */
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(
                Component.translatable("tooltip.aerotweaks.friction_wrench")
        );

        super.appendHoverText(
                stack,
                context,
                tooltipComponents,
                tooltipFlag
        );
    }
}