package net.minheur.itemtanks;

import net.minecraft.server.MinecraftServer;
import net.minheur.itemtanks.file.TankDataSaver;
import org.jspecify.annotations.NonNull;

public final class TankHandler {

    public static @NonNull TankDataSaver getTankState(@NonNull MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TankDataSaver.TYPE);
    }

}
