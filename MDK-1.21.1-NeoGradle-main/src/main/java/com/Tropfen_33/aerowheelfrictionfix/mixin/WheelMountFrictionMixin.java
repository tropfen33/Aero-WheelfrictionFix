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
                CompoundTag nbt = be.saveWithoutMetadata(be.getLevel().registryAccess());
                String nbtStr = nbt.toString();

                double multiplier = AeroTweaksConfig.ROLLING_FRICTION_MULTIPLIER.get(); // Fallback

                if (nbtStr.contains("monstrous_tire")) {
                    multiplier = AeroTweaksConfig.MONSTROUS_TIRE_MULTIPLIER.get();
                } else if (nbtStr.contains("large_tire")) {
                    multiplier = AeroTweaksConfig.LARGE_TIRE_MULTIPLIER.get();
                } else if (nbtStr.contains("small_tire")) {
                    multiplier = AeroTweaksConfig.SMALL_TIRE_MULTIPLIER.get();
                } else if (nbtStr.contains("tire")) {
                    multiplier = AeroTweaksConfig.TIRE_MULTIPLIER.get();
                } else {
                    multiplier = AeroTweaksConfig.WHEEL_MOUNT_MULTIPLIER.get();
                }

                if (be.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                    
                    // 1. Corrected 90-degree vector orientation
                    double rawX = facing.getStepX();
                    double rawZ = facing.getStepZ();
                    double correctedX = -rawZ;
                    double correctedZ = rawX;

                    Vector3d forwardDir = new Vector3d(correctedX, facing.getStepY(), correctedZ);

                    // 2. Rotate into world space using the vehicle's rigid body rotation
                    Quaterniond rotation = getBodyRotation(rigidBody);
                    if (rotation != null) {
                        forwardDir.rotate(rotation);
                    }
                    forwardDir.normalize();

                    // 3. Isolate forward force via dot product projection
                    double forwardMagnitude = this.queuedForce.dot(forwardDir);
                    Vector3d forwardForce = new Vector3d(forwardDir).mul(forwardMagnitude);

                    // 4. Keep sideways grip and vertical suspension completely untouched
                    Vector3d otherForces = new Vector3d(this.queuedForce).sub(forwardForce);

                    // 5. Apply the config multiplier ONLY to forward rolling resistance
                    forwardForce.mul(multiplier);

                    // 6. Recombine forces back into queuedForce
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