package dev.brella.khronus.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EnumLagTuning implements ILagTester {
    OFF,
    BASIC,
    XRAY;

    @Override
    public @NotNull EnumLagTuning getLagTuningForStack(@NotNull ItemStack stack, @NotNull EntityPlayer player, @Nullable EntityEquipmentSlot slot) {
        return this;
    }
}
