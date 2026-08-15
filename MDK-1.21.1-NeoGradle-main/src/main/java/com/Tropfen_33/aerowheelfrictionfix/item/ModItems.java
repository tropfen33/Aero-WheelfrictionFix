package com.Tropfen_33.aerowheelfrictionfix.item;

import com.Tropfen_33.aerowheelfrictionfix.AeroTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, AeroTweaks.MODID);

    public static final Supplier<Item> FRICTION_WRENCH = ITEMS.register("friction_wrench",
            () -> new FrictionWrench(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}