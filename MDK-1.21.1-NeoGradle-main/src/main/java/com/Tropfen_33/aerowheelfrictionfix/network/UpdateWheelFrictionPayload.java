package com.Tropfen_33.aerowheelfrictionfix.network;

import com.Tropfen_33.aerowheelfrictionfix.AeroTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateWheelFrictionPayload(BlockPos pos, double friction) implements CustomPacketPayload {
    public static final Type<UpdateWheelFrictionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AeroTweaks.MODID, "update_wheel_friction"));

    public static final StreamCodec<FriendlyByteBuf, UpdateWheelFrictionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateWheelFrictionPayload::pos,
            StreamCodec.of(FriendlyByteBuf::writeDouble, FriendlyByteBuf::readDouble), UpdateWheelFrictionPayload::friction,
            UpdateWheelFrictionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final UpdateWheelFrictionPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player != null && player.level() != null) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be != null && be.getClass().getName().contains("WheelMountBlockEntity")) {
                    var persistentData = be.getPersistentData();
                    persistentData.putDouble("CustomFriction", data.friction());
                    be.setChanged();
                    
                    player.level().sendBlockUpdated(data.pos(), be.getBlockState(), be.getBlockState(), 3);
                }
            }
        });
    }
}