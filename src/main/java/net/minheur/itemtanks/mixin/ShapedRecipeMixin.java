package net.minheur.itemtanks.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minheur.itemtanks.TankHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Shadow @Final
    ItemStack result;

    @Inject(
            method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void betterosc$rejectTankForBeacon(
            CraftingInput input,
            Level level,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!result.is(Items.BEACON)) return; // todo: remove

        for (ItemStack stack : input.items()) {
            if (TankHandler.isTank(stack)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
