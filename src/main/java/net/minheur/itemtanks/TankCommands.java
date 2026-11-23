package net.minheur.itemtanks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minheur.itemtanks.operations.OperationResults;
import net.minheur.itemtanks.operations.TankOperationHandler;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class TankCommands {

    private static final Supplier<SuggestionProvider<CommandSourceStack>> TANKS_SUGGESTIONS = () ->
            ((context, builder) -> {
                TankHandler.getTankState().getTanks().keySet().stream()
                        .map(UUID::toString)
                        .filter(id -> id.startsWith(builder.getRemaining()))
                        .forEach(builder::suggest);
                return builder.buildFuture();
            });

    public static void register(@NonNull RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                literal("tank")

                        .then(literal("create").executes(TankCommands::handleSimpleCreate)
                                .then(argument("maxAmount", IntegerArgumentType.integer()).executes(TankCommands::handleMaxedCreate)))

                        .then(literal("add").then(literal("all").executes(source -> {
                                    if (!(source.getSource().getEntity() instanceof ServerPlayer user)) return 0;

                                    ItemStack tank = user.getMainHandItem();
                                    if (!TankHandler.isTank(tank)) {
                                        source.getSource().sendFailure(Component.literal("Please hold a tank in your main hand!"));
                                        return 0;
                                    }

                                    for (int i = 0; i < user.getInventory().getContainerSize(); i++) {
                                        ItemStack target = user.getInventory().getItem(i);
                                        final int index = i;
                                        if (!TankOperationHandler.safeIncrement(tank, target, (stack -> user.getInventory().setItem(index, stack))))
                                            break;
                                    }

                                    ItemStack offHand = user.getOffhandItem();
                                    TankOperationHandler.safeIncrement(tank, offHand, (stack) -> user.setItemInHand(InteractionHand.OFF_HAND, stack));

                                    return 1;
                                }))
                                .then(argument("amount", IntegerArgumentType.integer())
                                        .executes(source -> {
                                            if (!(source.getSource().getEntity() instanceof ServerPlayer user))
                                                return 0;

                                            ItemStack tank = user.getMainHandItem();
                                            if (!TankHandler.isTank(tank)) {
                                                source.getSource().sendFailure(Component.literal("Please hold a tank in your main hand!"));
                                                return 0;
                                            }

                                            int[] leftToAdd = {IntegerArgumentType.getInteger(source, "amount")};

                                            for (int i = 0; i < user.getInventory().getContainerSize(); i++) {
                                                ItemStack target = user.getInventory().getItem(i);
                                                final int index = i;
                                                if (!TankOperationHandler.safeIncrement(tank, target, (stack -> user.getInventory().setItem(index, stack)), leftToAdd))
                                                    break;
                                                if (leftToAdd[0] <= 0) break;
                                            }

                                            if (leftToAdd[0] <= 0) return 1;

                                            ItemStack offHand = user.getOffhandItem();
                                            TankOperationHandler.safeIncrement(tank, offHand, (stack -> user.setItemInHand(InteractionHand.OFF_HAND, stack)), leftToAdd);
                                            return 1;
                                        })))

                        .then(literal("withdraw")
                                .then(literal("all").executes(source -> {
                                    if (!(source.getSource().getEntity() instanceof ServerPlayer user))
                                        return 0;

                                    ItemStack tank = user.getMainHandItem();
                                    if (!TankHandler.isTank(tank)) {
                                        source.getSource().sendFailure(Component.literal("Please hold a tank in your main hand!"));
                                        return 0;
                                    }

                                    ItemStack contentRef = TankOperationHandler.getTankContent(tank);
                                    long amountStoredLeft = TankOperationHandler.getAmountStored(tank);
                                    int maxStackSize = contentRef.getMaxStackSize();

                                    while (amountStoredLeft > 0) {
                                        ItemStack giving = contentRef.copy();

                                        if (amountStoredLeft <= maxStackSize) {
                                            long withdrawn = amountStoredLeft;
                                            giving.setCount((int) withdrawn);

                                            if (!user.addItem(giving) && !giving.isEmpty()) user.drop(giving, true);
                                            amountStoredLeft = 0;
                                            OperationResults result = TankOperationHandler.decrementTank(tank, withdrawn);
                                            if (result == OperationResults.EMPTIED) {
                                                user.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                                break;
                                            }
                                        } else {

                                            giving.setCount(maxStackSize);

                                            if (!user.giveItemStack(giving)) user.dropItem(giving, true);
                                            amountStoredLeft -= maxStackSize;
                                            OperationResults result = TankOperationHandler.decrementTank(tank, maxStackSize);
                                            if (result == OperationResults.EMPTIED)
                                                user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

                                        }
                                    }

                                    user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

                                    return 1;
                                }))
                                .then(argument("amount", LongArgumentType.longArg()).executes(source -> {
                                    if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user))
                                        return 0;

                                    ItemStack tank = user.getMainHandStack();
                                    if (!TankHandler.isTank(tank)) {
                                        source.getSource().sendError(Text.literal("Please hold a tank in your main hand!"));
                                        return 0;
                                    }

                                    long amountToWithdraw = LongArgumentType.getLong(source, "amount");
                                    long amountStored = TankOperationHandler.getAmountStored(tank);

                                    if (amountStored < amountToWithdraw) amountToWithdraw = amountStored;

                                    ItemStack contentRef = TankOperationHandler.getTankContent(tank);
                                    int maxStackSize = contentRef.getMaxCount();

                                    while (amountToWithdraw > 0) {
                                        ItemStack giving = contentRef.copy();
                                        long withdrawn = Math.min(amountToWithdraw, maxStackSize);
                                        giving.setCount((int) withdrawn);

                                        if (!user.giveItemStack(giving)) user.dropItem(giving, true);
                                        amountToWithdraw -= withdrawn;

                                        OperationResults result = TankOperationHandler.decrementTank(tank, withdrawn);
                                        if (result == OperationResults.EMPTIED) {
                                            user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                            return 1;
                                        }
                                    }

                                    if (TankHandler.isTank(tank) && TankOperationHandler.getAmountStored(tank) <= 0)
                                        user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                    return 1;
                                }))
                        )

                        .then(literal("details").executes(source -> {
                            if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user))
                                return 0;

                            ItemStack tank = user.getMainHandStack();
                            if (!TankHandler.isTank(tank)) {
                                source.getSource().sendError(Text.literal("Please hold a tank in your main hand!"));
                                return 0;
                            }

                            source.getSource().sendMessage(Text.literal("Stored item: ")
                                    .append(TankOperationHandler.getTankContent(tank).toHoverableText()));
                            return 1;
                        }))

                        .then(literal("updateDisplay").executes(source -> {
                            if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user))
                                return 0;

                            ItemStack tank = user.getMainHandStack();
                            if (!TankHandler.isTank(tank)) {
                                source.getSource().sendError(Text.literal("Please hold a tank in your main hand!"));
                                return 0;
                            }

                            TankHandler.updateTankDisplay(tank);
                            return 1;
                        }))

                        .then(literal("admin").requires(TankCommands::isOpped)
                                .then(literal("adminGiveTank").then(argument("uuid", UuidArgumentType.uuid())
                                        .suggests(TANKS_SUGGESTIONS.get()).executes(source -> {
                                            if (!(source.getSource().getEntity() instanceof ServerPlayerEntity user))
                                                return 0;
                                            UUID targetTank = UuidArgumentType.getUuid(source, "uuid");

                                            if (!TankHandler.getTankState().getTanks().containsKey(targetTank)) {
                                                source.getSource().sendError(Text.literal("This tank doesn't exists!"));
                                                return 0;
                                            }

                                            ItemStack tank = TankHandler.buildTankFromUUID(targetTank);
                                            if (!user.giveItemStack(tank)) user.dropItem(tank, true);
                                            return 1;
                                        })
                                ))
                        )
        );

        dispatcher.register(literal("t").redirect(dispatcher.getRoot().getChild("tank")));
        dispatcher.register(literal("tc")
                .executes(TankCommands::handleSimpleCreate)
                .redirect(dispatcher.getRoot().getChild("tank").getChild("create")));
        dispatcher.register(literal("ta").redirect(dispatcher.getRoot().getChild("tank").getChild("add")));
        dispatcher.register(literal("tw").redirect(dispatcher.getRoot().getChild("tank").getChild("withdraw")));
    }
}
