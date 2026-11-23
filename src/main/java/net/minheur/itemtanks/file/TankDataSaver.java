package net.minheur.itemtanks.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minheur.itemtanks.TankContent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TankDataSaver extends SavedData {
    private static final String KEY = "osc_tanks";

    private final Map<UUID, TankContent> tanks;

    public TankDataSaver() {
        this(new ConcurrentHashMap<>());
    }
    public TankDataSaver(Map<UUID, TankContent> tanks) {
        this.tanks = new ConcurrentHashMap<>(tanks);
    }

    public static final Codec<TankDataSaver> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(
                    UUIDUtil.CODEC, TankContent.CODEC
            ).fieldOf("tanks").forGetter(state -> state.tanks)
    ).apply(instance, TankDataSaver::new));

    public static final SavedDataType<TankDataSaver> TYPE = new SavedDataType<>(
            KEY, TankDataSaver::new, CODEC
    );

    @Contract(pure = true)
    public @NonNull @UnmodifiableView Map<UUID, TankContent> getTanks() {
        return Collections.unmodifiableMap(tanks);
    }

    public static @NonNull TankDataSaver get(@NonNull ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public void setTank(UUID uuid, TankContent content) {
        tanks.put(uuid, content);
        setDirty();
    }
    public void removeTank(UUID uuid) {
        if (tanks.remove(uuid) != null) setDirty();
    }

}
