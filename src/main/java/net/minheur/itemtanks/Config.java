package net.minheur.itemtanks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = ItemTanks.MODID)
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // a list of strings that are treated as resource locations for items
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ILLEGAL_TANKS_ITEMS = BUILDER.comment("Items that cannot be put into tanks").defineListAllowEmpty("items",
            List.of("minecraft:bedrock", "minecraft:barrier", "minecraft:command_block", "minecraft:structure_block",
                    "minecraft:chain_command_block", "minecraft:repeat_command_block"),
            Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static Set<Item> items;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // convert the list of strings into a set of items
        items = ILLEGAL_TANKS_ITEMS.get().stream().map(itemName -> BuiltInRegistries.ITEM.getValue(Identifier.parse(itemName))).collect(Collectors.toSet());
    }
}
