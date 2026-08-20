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

public record UpdateWheelFrictionPayload(
        BlockPos pos,
        double friction
) implements CustomPacketPayload {

    public static final Type<UpdateWheelFrictionPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            AeroTweaks.MODID,
                            "update_wheel_friction"
                    )
            );

    public static final StreamCodec<
            FriendlyByteBuf,
            UpdateWheelFrictionPayload
            > STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateWheelFrictionPayload::pos,

            StreamCodec.of(
                    FriendlyByteBuf::writeDouble,
                    FriendlyByteBuf::readDouble
            ),
            UpdateWheelFrictionPayload::friction,

            UpdateWheelFrictionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(
            final UpdateWheelFrictionPayload data,
            final IPayloadContext context
    ) {

        context.enqueueWork(() -> {

            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            /*
             * Prevent invalid NaN / infinite values.
             */
            if (!Double.isFinite(data.friction())) {
                return;
            }

            /*
             * Keep friction inside the range used by the GUI.
             */
            double friction = Math.clamp(
                    data.friction(),
                    0.0,
                    1.0
            );

            BlockPos pos = data.pos();

            /*
             * Prevent a client from modifying an arbitrary wheel
             * thousands of blocks away.
             */
            if (!player.level().hasChunkAt(pos)) {
                return;
            }

            if (player.distanceToSqr(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5
            ) > 64.0) {
                return;
            }

            BlockEntity blockEntity =
                    player.level().getBlockEntity(pos);

            if (blockEntity == null) {
                return;
            }

            if (!blockEntity.getClass().getName()
                    .contains("WheelMountBlockEntity")) {
                return;
            }

            blockEntity
                    .getPersistentData()
                    .putDouble("CustomFriction", friction);

            blockEntity.setChanged();

            player.level().sendBlockUpdated(
                    pos,
                    blockEntity.getBlockState(),
                    blockEntity.getBlockState(),
                    3
            );
        });
    }
}