package net.minheur.itemtanks.operations;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minheur.itemtanks.TankHandler;
import net.minheur.itemtanks.file.Config;
import org.jspecify.annotations.NonNull;

public final class IllegalTankItems {

    public static boolean isItemForbidden(@NonNull ItemStack stack) {
        if (stack.isEmpty()) return true;
        if (TankHandler.isTank(stack)) return true;
        if (Config.items.contains(stack.getItem())) return true;

        CustomData nbtComponent = stack.get(DataComponents.CUSTOM_DATA);
        if (nbtComponent == null) return false;
        CompoundTag nbt = nbtComponent.copyTag();

        Boolean flag = nbt.getBoolean("no_tank_flag").orElse(null);
        return flag != null && flag;
    }

}
