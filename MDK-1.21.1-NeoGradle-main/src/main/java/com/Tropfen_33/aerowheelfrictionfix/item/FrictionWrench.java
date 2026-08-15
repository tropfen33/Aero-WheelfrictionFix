package com.Tropfen_33.aerowheelfrictionfix.item;

import com.Tropfen_33.aerowheelfrictionfix.client.WheelFrictionAdjustmentHUD;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class FrictionWrench extends Item {
    public FrictionWrench(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            BlockPos pos = context.getClickedPos();
            BlockEntity be = context.getLevel().getBlockEntity(pos);
            
            if (be != null && be.getClass().getName().contains("WheelMountBlockEntity")) {
                double currentFriction = be.getPersistentData().contains("CustomFriction") 
                        ? be.getPersistentData().getDouble("CustomFriction") 
                        : 0.01;
                
                Minecraft.getInstance().setScreen(new WheelFrictionAdjustmentHUD(pos, currentFriction));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.aerotweaks.friction_wrench"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}