package net.minheur.itemtanks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minheur.itemtanks.file.TankDataSaver;
import net.minheur.itemtanks.operations.IllegalTankItems;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public final class TankHandler {
    private static final String TANK_TYPE = "tank";
    private static final String TYPE_KEY = "oc_type";
    private static final String UUID_KEY = "tank_uuid";

    private static TankDataSaver tankState;

    public static @NonNull TankDataSaver getTankState() {
        if (tankState == null) ItemTanks.LOGGER.warn("accessing 'null' tankState");
        return tankState;
    }

    public static void loadTanks(@NonNull MinecraftServer server) {
        tankState = server.overworld().getDataStorage().computeIfAbsent(TankDataSaver.TYPE);
    }
    public static void unloadTanks(MinecraftServer server) {
        tankState = null;
    }

    public static boolean isTank(@NonNull ItemStack stack) {
        if (!stack.is(Items.NETHER_STAR)) return false;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;

        CompoundTag nbt = customData.copyTag();
        return TANK_TYPE.equals(nbt.getString(TYPE_KEY).orElse(null))
                && nbt.getString(UUID_KEY)
                .map(TankHandler::isValidUuid)
                .orElse(false);
    }

    public static @Nullable ItemStack createTank(@NonNull ItemStack content) {
        if (content.isEmpty()) return null;
        if (IllegalTankItems.isItemForbidden(content)) return null;

        UUID tankUuid = UUID.randomUUID();
        ItemStack storedStack = content.copyWithCount(1);
        getTankState().setTank(tankUuid, new TankContent(storedStack, content.getCount()));

        return buildTankFromUUID(tankUuid);
    }

    public static @NonNull ItemStack buildTankFromUUID(@NonNull UUID uuid) {
        ItemStack tank = new ItemStack(Items.NETHER_STAR);
        CompoundTag nbt = new CompoundTag();

        nbt.putString(TYPE_KEY, TANK_TYPE);
        nbt.putString(UUID_KEY, uuid.toString());

        tank.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        tank.set(DataComponents.MAX_STACK_SIZE, 1);

        updateTankDisplay(tank);
        return tank;
    }

    public static void updateTankDisplay(ItemStack tank) {
        if (!isTank(tank)) throw new IllegalArgumentException("Can't update non-tank's UI!");

        UUID tankUUID = getTankUUID(tank);

        TankContent content = getTankState().getTanks().get(tankUUID);
        ItemStack stored = content.item();
        long amount = content.amount();

        tank.set(DataComponents.CUSTOM_NAME, Component.empty()
                .append(Component.literal("Tank of ").withStyle(ChatFormatting.AQUA))
                .append(stored.getHoverName()));
        tank.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Amount stored:").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD)
        )));
    }

    public static @NonNull UUID getTankUUID(ItemStack tank) {
        CompoundTag nbt = getTankNbt(tank);

        String tankUUID = nbt.getString(UUID_KEY).orElse(null);
        if (tankUUID == null || !isValidUuid(tankUUID)) throw new IllegalStateException("Tank data doesn't have a UUID!");

        return UUID.fromString(tankUUID);
    }
    public static @NonNull CompoundTag getTankNbt(ItemStack tank) {
        if (!isTank(tank)) throw new IllegalArgumentException("Can't update non-tank's UI!");

        CustomData nbtComponent = tank.get(DataComponents.CUSTOM_DATA);
        if (nbtComponent == null || nbtComponent.isEmpty()) throw new IllegalStateException("No custom data on tank!");
        return nbtComponent.copyTag();
    }

    private static boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

}
