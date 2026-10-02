package com.example.watergun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WaterGunMod implements ModInitializer {
    public static final String MOD_ID = "watergun";
    public static final Item WATER_GUN = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "water_gun"),
            new WaterGunItem(new Item.Settings().maxCount(1)));

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> e.add(WATER_GUN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(e -> e.add(WATER_GUN));
    }
}
