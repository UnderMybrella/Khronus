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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class KhronusTickableCollector implements IKhronusTickable<CollectorMK1Tile> {
    public static Field TO_SORT_FIELD;
    public static Field UNPROCESSED_EMC_FIELD;
    public static Field EMC_GEN_FIELD;
    public static Field HAS_CHARGEABLE_ITEM_FIELD;
    public static Field HAS_FUEL_FIELD;
    public static Field AUX_SLOTS_FIELD;

    public static Method HAS_MAXED_EMC_METHOD;
    public static Method ADD_EMC_METHOD;
    public static Method GET_UPGRADING_METHOD;
    public static Method REMOVE_EMC_METHOD;
    public static Method GET_LOCK_METHOD;
    public static Method SEND_TO_ALL_ACCEPTORS_METHOD;
    public static Method SEND_RELAY_BONUS_METHOD;
    public static Method CHECK_FUEL_OR_KLEIN_METHOD;
    public static Method ROTATE_UPGRADED_METHOD;

    public static MethodHandle HAS_MAXED_EMC_METHOD_HANDLE;
    public static MethodHandle ADD_EMC_METHOD_HANDLE;
    public static MethodHandle GET_UPGRADING_METHOD_HANDLE;
    public static MethodHandle REMOVE_EMC_METHOD_HANDLE;
    public static MethodHandle GET_LOCK_METHOD_HANDLE;
    public static MethodHandle SEND_TO_ALL_ACCEPTORS_METHOD_HANDLE;
    public static MethodHandle SEND_RELAY_BONUS_METHOD_HANDLE;
    public static MethodHandle CHECK_FUEL_OR_KLEIN_METHOD_HANDLE;
    public static MethodHandle ROTATE_UPGRADED_METHOD_HANDLE;

    public static MethodHandle TO_SORT_GETTER;
    public static MethodHandle UNPROCESSED_EMC_GETTER;
    public static MethodHandle EMC_GEN_GETTER;
    public static MethodHandle HAS_CHARGEABLE_ITEM_GETTER;
    public static MethodHandle HAS_FUEL_GETTER;
    public static MethodHandle AUX_SLOTS_GETTER;
    public static MethodHandle TO_SORT_SETTER;
    public static MethodHandle UNPROCESSED_EMC_SETTER;
    public static MethodHandle EMC_GEN_SETTER;
    public static MethodHandle HAS_CHARGEABLE_ITEM_SETTER;
    public static MethodHandle HAS_FUEL_SETTER;
    public static MethodHandle AUX_SLOTS_SETTER;

    static {
        try {
            Class<CollectorMK1Tile> collector = CollectorMK1Tile.class;

            TO_SORT_FIELD = collector.getDeclaredField("toSort");
            UNPROCESSED_EMC_FIELD = collector.getDeclaredField("unprocessedEMC");
            EMC_GEN_FIELD = collector.getDeclaredField("emcGen");
            HAS_CHARGEABLE_ITEM_FIELD = collector.getDeclaredField("hasChargeableItem");
            HAS_FUEL_FIELD = collector.getDeclaredField("hasFuel");
            AUX_SLOTS_FIELD = collector.getDeclaredField("auxSlots");

            TO_SORT_FIELD.setAccessible(true);
            UNPROCESSED_EMC_FIELD.setAccessible(true);
            EMC_GEN_FIELD.setAccessible(true);
            HAS_CHARGEABLE_ITEM_FIELD.setAccessible(true);
            HAS_FUEL_FIELD.setAccessible(true);
            AUX_SLOTS_FIELD.setAccessible(true);

            Class<TileEmcBase> emc = TileEmcBase.class;

            HAS_MAXED_EMC_METHOD = TileEmc.class.getDeclaredMethod("hasMaxedEmc");
            ADD_EMC_METHOD = emc.getDeclaredMethod("addEMC", long.class);
            GET_UPGRADING_METHOD = collector.getDeclaredMethod("getUpgrading");
            REMOVE_EMC_METHOD = emc.getDeclaredMethod("removeEMC", long.class);
            GET_LOCK_METHOD = collector.getDeclaredMethod("getLock");
            SEND_TO_ALL_ACCEPTORS_METHOD = TileEmc.class.getDeclaredMethod("sendToAllAcceptors", long.class);
            SEND_RELAY_BONUS_METHOD = collector.getDeclaredMethod("sendRelayBonus");
            CHECK_FUEL_OR_KLEIN_METHOD = collector.getDeclaredMethod("checkFuelOrKlein");
            ROTATE_UPGRADED_METHOD = collector.getDeclaredMethod("rotateUpgraded");

            HAS_MAXED_EMC_METHOD.setAccessible(true);
            ADD_EMC_METHOD.setAccessible(true);
            GET_UPGRADING_METHOD.setAccessible(true);
            REMOVE_EMC_METHOD.setAccessible(true);
            GET_LOCK_METHOD.setAccessible(true);
            SEND_TO_ALL_ACCEPTORS_METHOD.setAccessible(true);
            SEND_RELAY_BONUS_METHOD.setAccessible(true);
            CHECK_FUEL_OR_KLEIN_METHOD.setAccessible(true);
            ROTATE_UPGRADED_METHOD.setAccessible(true);

            MethodHandles.Lookup lookup = MethodHandles.lookup();

            HAS_MAXED_EMC_METHOD_HANDLE = lookup.unreflect(HAS_MAXED_EMC_METHOD);
            ADD_EMC_METHOD_HANDLE = lookup.unreflect(ADD_EMC_METHOD);
            GET_UPGRADING_METHOD_HANDLE = lookup.unreflect(GET_UPGRADING_METHOD);
            REMOVE_EMC_METHOD_HANDLE = lookup.unreflect(REMOVE_EMC_METHOD);
            GET_LOCK_METHOD_HANDLE = lookup.unreflect(GET_LOCK_METHOD);
            SEND_TO_ALL_ACCEPTORS_METHOD_HANDLE = lookup.unreflect(SEND_TO_ALL_ACCEPTORS_METHOD);
            SEND_RELAY_BONUS_METHOD_HANDLE = lookup.unreflect(SEND_RELAY_BONUS_METHOD);
            CHECK_FUEL_OR_KLEIN_METHOD_HANDLE = lookup.unreflect(CHECK_FUEL_OR_KLEIN_METHOD);
            ROTATE_UPGRADED_METHOD_HANDLE = lookup.unreflect(ROTATE_UPGRADED_METHOD);

            TO_SORT_GETTER = lookup.unreflectGetter(TO_SORT_FIELD);
            UNPROCESSED_EMC_GETTER = lookup.unreflectGetter(UNPROCESSED_EMC_FIELD);
            EMC_GEN_GETTER = lookup.unreflectGetter(EMC_GEN_FIELD);
            HAS_CHARGEABLE_ITEM_GETTER = lookup.unreflectGetter(HAS_CHARGEABLE_ITEM_FIELD);
            HAS_FUEL_GETTER = lookup.unreflectGetter(HAS_FUEL_FIELD);
            AUX_SLOTS_GETTER = lookup.unreflectGetter(AUX_SLOTS_FIELD);

            TO_SORT_SETTER = lookup.unreflectSetter(TO_SORT_FIELD);
            UNPROCESSED_EMC_SETTER = lookup.unreflectSetter(UNPROCESSED_EMC_FIELD);
            EMC_GEN_SETTER = lookup.unreflectSetter(EMC_GEN_FIELD);
            HAS_CHARGEABLE_ITEM_SETTER = lookup.unreflectSetter(HAS_CHARGEABLE_ITEM_FIELD);
            HAS_FUEL_SETTER = lookup.unreflectSetter(HAS_FUEL_FIELD);
            AUX_SLOTS_SETTER = lookup.unreflectSetter(AUX_SLOTS_FIELD);
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public CollectorMK1Tile base;
    public long emcGen;

    public KhronusTickableCollector(CollectorMK1Tile base) {
        this.base = base;
        this.emcGen = base instanceof CollectorMK3Tile ? Constants.COLLECTOR_MK3_GEN : base instanceof CollectorMK2Tile ? Constants.COLLECTOR_MK2_GEN : Constants.COLLECTOR_MK1_GEN;
    }

    @Override
    public void onAddedTo(World world) {
        try {
            this.emcGen = (long) EMC_GEN_GETTER.invokeExact(base);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onWatchdogChanged(World world, KhronusWatchdog oldWatchdog, KhronusWatchdog newWatchdog) {
        try {
            this.emcGen = (long) EMC_GEN_GETTER.invokeExact(base);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void update(int ticks, int bonusTicks) {
        if (base.getWorld().isRemote)
            return;

        try {
            ItemHelper.compactInventory((CombinedInvWrapper) TO_SORT_GETTER.invokeExact(base));
            CHECK_FUEL_OR_KLEIN_METHOD_HANDLE.invokeExact(base);
            updateEmc(ticks, bonusTicks);
            ROTATE_UPGRADED_METHOD_HANDLE.invokeExact(base);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void updateEmc(int ticks, int bonusTicks) throws Throwable {
        long emcGen = this.emcGen * (ticks + bonusTicks);
        if (!(boolean) HAS_MAXED_EMC_METHOD_HANDLE.invokeExact((TileEmc) base)) {
            double unprocessedEMC = ((double) UNPROCESSED_EMC_GETTER.invokeExact(base)) + (emcGen * (base.getSunLevel() / 320.0f));
            UNPROCESSED_EMC_SETTER.invokeExact(base, unprocessedEMC);
//            base.unprocessedEMC += base.emcGen * (base.getSunLevel() / 320.0f);
            if (unprocessedEMC >= 1) {
                long emcToAdd = (long) unprocessedEMC;
                ADD_EMC_METHOD_HANDLE.invokeExact((TileEmcBase) base, emcToAdd);
//                base.addEMC(emcToAdd);
                UNPROCESSED_EMC_SETTER.invokeExact(base, ((double) UNPROCESSED_EMC_GETTER.invokeExact(base)) - emcToAdd);
            }
        }

        ItemStackHandler auxSlots = ((ItemStackHandler) AUX_SLOTS_GETTER.invokeExact(base));
        if (base.getStoredEmc() == 0) {
            return;
        } else if ((boolean) HAS_CHARGEABLE_ITEM_GETTER.invokeExact(base)) {
            long toSend = Math.min(base.getStoredEmc(), emcGen);
            ItemStack upgrading = auxSlots.getStackInSlot(CollectorMK1Tile.UPGRADING_SLOT);
            IItemEmc item = (IItemEmc) upgrading.getItem();

            long itemEmc = item.getStoredEmc(upgrading);
            long maxItemEmc = item.getMaximumEmc(upgrading);

            if ((itemEmc + toSend) > maxItemEmc) {
                toSend = maxItemEmc - itemEmc;
            }

            item.addEmc(upgrading, toSend);
            REMOVE_EMC_METHOD_HANDLE.invokeExact((TileEmcBase) base, toSend);
        } else if ((boolean) HAS_FUEL_GETTER.invokeExact(base)) {
            ItemStack upgrading = auxSlots.getStackInSlot(CollectorMK1Tile.UPGRADING_SLOT);

            if (FuelMapper.getFuelUpgrade(upgrading).isEmpty()) {
                auxSlots.setStackInSlot(CollectorMK1Tile.UPGRADING_SLOT, ItemStack.EMPTY);
            }

            ItemStack lock = auxSlots.getStackInSlot(CollectorMK1Tile.LOCK_SLOT);

            ItemStack result = lock.isEmpty() ? FuelMapper.getFuelUpgrade(upgrading) : lock.copy();

            long upgradeCost = EMCHelper.getEmcValue(result) - EMCHelper.getEmcValue(upgrading);

            if (upgradeCost >= 0 && base.getStoredEmc() >= upgradeCost) {
                ItemStack upgraded = auxSlots.getStackInSlot(CollectorMK1Tile.UPGRADE_SLOT);

                if (upgraded.isEmpty()) {
                    REMOVE_EMC_METHOD_HANDLE.invokeExact(base, upgradeCost);
                    auxSlots.setStackInSlot(CollectorMK1Tile.UPGRADE_SLOT, result);
                    upgrading.shrink(1);
                } else if (ItemHelper.basicAreStacksEqual(result, upgraded) && upgraded.getCount() < upgraded.getMaxStackSize()) {
                    REMOVE_EMC_METHOD_HANDLE.invokeExact(upgradeCost);
                    upgraded.grow(1);
                    upgrading.shrink(1);
                }
            }
        } else {
            //Only send EMC when we are not upgrading fuel or charging an item
            long toSend = Math.min(base.getStoredEmc(), emcGen);
            SEND_TO_ALL_ACCEPTORS_METHOD_HANDLE.invokeExact((TileEmc) base, toSend);
            SEND_RELAY_BONUS_METHOD_HANDLE.invokeExact(base);
        }
    }

    @Override
    public CollectorMK1Tile getSource() {
        return base;
    }
}
