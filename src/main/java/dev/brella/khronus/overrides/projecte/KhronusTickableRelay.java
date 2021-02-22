package dev.brella.khronus.overrides.projecte;

import dev.brella.khronus.api.IKhronusTickable;
import dev.brella.khronus.watchdogs.KhronusWatchdog;
import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.api.tile.TileEmcBase;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.tiles.*;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class KhronusTickableRelay implements IKhronusTickable<RelayMK1Tile> {
    public static Field CHARGE_RATE_FIELD;

    public static Method SEND_TO_ALL_ACCEPTORS_METHOD;

    public static Method ADD_EMC_METHOD;
    public static Method REMOVE_EMC_METHOD;

    public static MethodHandle SEND_TO_ALL_ACCEPTORS_METHOD_HANDLE;
    public static MethodHandle ADD_EMC_METHOD_HANDLE;
    public static MethodHandle REMOVE_EMC_METHOD_HANDLE;

    public static MethodHandle CHARGE_RATE_GETTER;

    static {
        try {
//            Class<CollectorMK1Tile> collector = CollectorMK1Tile.class;

            CHARGE_RATE_FIELD = RelayMK1Tile.class.getDeclaredField("chargeRate");

            CHARGE_RATE_FIELD.setAccessible(true);

            Class<TileEmcBase> emc = TileEmcBase.class;

            ADD_EMC_METHOD = emc.getDeclaredMethod("addEMC", long.class);
            REMOVE_EMC_METHOD = emc.getDeclaredMethod("removeEMC", long.class);
            SEND_TO_ALL_ACCEPTORS_METHOD = TileEmc.class.getDeclaredMethod("sendToAllAcceptors", long.class);

            ADD_EMC_METHOD.setAccessible(true);
            REMOVE_EMC_METHOD.setAccessible(true);
            SEND_TO_ALL_ACCEPTORS_METHOD.setAccessible(true);

            MethodHandles.Lookup lookup = MethodHandles.lookup();

            ADD_EMC_METHOD_HANDLE = lookup.unreflect(ADD_EMC_METHOD);
            REMOVE_EMC_METHOD_HANDLE = lookup.unreflect(REMOVE_EMC_METHOD);
            SEND_TO_ALL_ACCEPTORS_METHOD_HANDLE = lookup.unreflect(SEND_TO_ALL_ACCEPTORS_METHOD);

            CHARGE_RATE_GETTER = lookup.unreflectGetter(CHARGE_RATE_FIELD);
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public RelayMK1Tile base;
    public long chargeRate;

    public KhronusTickableRelay(RelayMK1Tile base) {
        this.base = base;
        this.chargeRate = base instanceof RelayMK3Tile ? Constants.RELAY_MK3_OUTPUT : base instanceof RelayMK2Tile ? Constants.RELAY_MK2_OUTPUT : Constants.RELAY_MK1_OUTPUT;
    }

    @Override
    public void onAddedTo(World world) {
        try {
//            base.emcGen = (long) EMC_GEN_GETTER.invokeExact(base);
            this.chargeRate = (long) CHARGE_RATE_GETTER.invokeExact(base);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onWatchdogChanged(World world, KhronusWatchdog oldWatchdog, KhronusWatchdog newWatchdog) {
        try {
//            base.emcGen = (long) EMC_GEN_GETTER.invokeExact(base);
            this.chargeRate = (long) CHARGE_RATE_GETTER.invokeExact(base);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void update(int ticks, int bonusTicks) {
        if (base.getWorld().isRemote)
            return;

        try {
            if (base.getStoredEmc() == 0) return;

            long chargeRate = this.chargeRate * (ticks + bonusTicks);

            SEND_TO_ALL_ACCEPTORS_METHOD_HANDLE.invokeExact((TileEmc) base, Math.min(base.getStoredEmc(), chargeRate));

            ItemStackHandler input = (ItemStackHandler) base.getInput();

            ItemHelper.compactInventory(input);

            ItemStack stack = input.getStackInSlot(0);

            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof IItemEmc) {
                    IItemEmc itemEmc = ((IItemEmc) stack.getItem());
                    long emcVal = itemEmc.getStoredEmc(stack);

                    if (emcVal > chargeRate) {
                        emcVal = chargeRate;
                    }

                    if (emcVal > 0 && base.getStoredEmc() + emcVal <= base.getMaximumEmc()) {
                        ADD_EMC_METHOD_HANDLE.invokeExact((TileEmcBase) base, emcVal);
                        itemEmc.extractEmc(stack, emcVal);
                    }
                } else {
                    long emcVal = EMCHelper.getEmcSellValue(stack);

                    if (emcVal > 0 && (base.getStoredEmc() + emcVal) <= base.getMaximumEmc()) {
                        ADD_EMC_METHOD_HANDLE.invokeExact((TileEmcBase) base, emcVal);
                        input.getStackInSlot(0).shrink(1);
                    }
                }
            }

            ItemStack chargeable = base.getOutput().getStackInSlot(0);

            if (!chargeable.isEmpty() && base.getStoredEmc() > 0 && chargeable.getItem() instanceof IItemEmc) {
                IItemEmc itemEmc = ((IItemEmc) chargeable.getItem());
                long starEmc = itemEmc.getStoredEmc(chargeable);
                long maxStarEmc = itemEmc.getMaximumEmc(chargeable);
                long toSend = Math.min(base.getStoredEmc(), chargeRate);

                if ((starEmc + toSend) > maxStarEmc) {
                    toSend = maxStarEmc - starEmc;
                }

                itemEmc.addEmc(chargeable, toSend);
                REMOVE_EMC_METHOD_HANDLE.invokeExact((TileEmcBase) base, toSend);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    @Override
    public RelayMK1Tile getSource() {
        return base;
    }
}
