package net.minheur.itemtanks.mixin;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minheur.itemtanks.TankHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandlerMixin.class)
public abstract class AnvilScreenHandlerMixin {

    @Shadow
    public abstract Slot getSlot(int index);

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void betterosc$rejectTanks(CallbackInfo ci) {
        if (!TankHandler.isTank(getSlot(0).getItem())
                && !TankHandler.isTank(getSlot(1).getItem())) return;

        getSlot(2).set(ItemStack.EMPTY);
        ci.cancel();
    }
}
