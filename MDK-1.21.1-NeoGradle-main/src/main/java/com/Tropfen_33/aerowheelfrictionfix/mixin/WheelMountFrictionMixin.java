package com.Tropfen_33.aerowheelfrictionfix.mixin;

import com.Tropfen_33.aerowheelfrictionfix.AeroTweaksConfig;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity")
public abstract class WheelMountFrictionMixin {

    @Shadow private Vector3d queuedForce;

    @Inject(
            method = "sable$physicsTick(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Ldev/ryanhcode/sable/api/physics/handle/RigidBodyHandle;D)V",
            at = @At(value = "FIELD", target = "Ldev/ryanhcode/offroad/content/blocks/wheel_mount/WheelMountBlockEntity;forceTotal:Ldev/ryanhcode/sable/api/physics/force/ForceTotal;", shift = At.Shift.BEFORE),
            remap = false
    )
    private void clearForwardRollingResistance(ServerSubLevel sublevel, RigidBodyHandle rigidBody, double delta, CallbackInfo ci) {
        BlockEntity be = (BlockEntity) (Object) this;

        if (be.getLevel() != null) {
            int brakeSignal = be.getLevel().getSignal(be.getBlockPos().above(), Direction.UP);

            if (brakeSignal == 0) {
                // Uses the single config fallback value (default 1.0)
                double multiplier = AeroTweaksConfig.DEFAULT_FRICTION.get();

                CompoundTag persistentData = be.getPersistentData();
                if (persistentData.contains("CustomFriction")) {
                    multiplier = persistentData.getDouble("CustomFriction");
                }

                if (be.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                    
                    double rawX = facing.getStepX();
                    double rawZ = facing.getStepZ();
                    double correctedX = -rawZ;
                    double correctedZ = rawX;

                    Vector3d forwardDir = new Vector3d(correctedX, facing.getStepY(), correctedZ);

                    Quaterniond rotation = getBodyRotation(rigidBody);
                    if (rotation != null) {
                        forwardDir.rotate(rotation);
                    }
                    forwardDir.normalize();

                    double forwardMagnitude = this.queuedForce.dot(forwardDir);
                    Vector3d forwardForce = new Vector3d(forwardDir).mul(forwardMagnitude);

                    Vector3d otherForces = new Vector3d(this.queuedForce).sub(forwardForce);

                    forwardForce.mul(multiplier);

                    this.queuedForce.set(otherForces).add(forwardForce);
                }
            }
        }
    }

    private Quaterniond getBodyRotation(RigidBodyHandle rigidBody) {
        if (rigidBody == null) return null;
        try {
            String[] possibleMethods = {"getRotation", "rotation", "getPose", "pose", "getTransform", "transform"};
            for (String methodName : possibleMethods) {
                try {
                    java.lang.reflect.Method m = rigidBody.getClass().getMethod(methodName);
                    Object res = m.invoke(rigidBody);
                    if (res instanceof Quaterniond) {
                        return (Quaterniond) res;
                    } else if (res != null) {
                        try {
                            java.lang.reflect.Method rotMethod = res.getClass().getMethod("getRotation");
                            Object rotRes = rotMethod.invoke(res);
                            if (rotRes instanceof Quaterniond) {
                                return (Quaterniond) rotRes;
                            }
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }
}