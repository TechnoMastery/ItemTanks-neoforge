package net.minheur.itemtanks.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minheur.itemtanks.TankHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin {

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void betterosc$rejectTanks(CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

        if (!TankHandler.isTank(menu.getSlot(0).getItem())
                && !TankHandler.isTank(menu.getSlot(1).getItem())) return;

        menu.getSlot(2).set(ItemStack.EMPTY);
        ci.cancel();
    }
}
