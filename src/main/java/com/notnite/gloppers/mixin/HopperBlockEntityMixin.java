package com.notnite.gloppers.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    @Unique
    private static boolean canTransfer(Inventory to, ItemStack stack) {
        try {
            if (to instanceof HopperBlockEntity) {
                var hopperName = ((HopperBlockEntity) to).getName().copyContentOnly().getString();
                var itemRegistryEntry = stack.getRegistryEntry().getKey();
                if (itemRegistryEntry.isEmpty()) return false;
                var itemName = itemRegistryEntry.get().getValue().getPath();

                if (hopperName.startsWith("!")) {
                    var globs = hopperName.substring(1).split(",");
                    for (var glob : globs) {
                        var strippedGlob = glob.replaceAll("[^a-zA-Z0-9_*?]", "");
                        var regex = strippedGlob.replace(".", "\\.").replace("*", ".*").replace("?", ".");
                        if (itemName.matches(regex)) return true;
                    }

                    // No globs matched, so don't transfer
                    return false;
                }
            }
        } catch (Exception e) {
            // ignored
        }

        // Doesn't have a glob (or exception), so transfer
        return true;
    }

    /**
     * This method replaces the {@link HopperBlockEntity#getStack} call in {@link HopperBlockEntity#insert}, in order to filter out all {@link ItemStack item stacks} that do not match the pattern in the inventory. It does so by returning an {@link ItemStack#EMPTY empty item stack}, since the hopper skips empty slots.
     */
    @WrapOperation(method = "insert",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getStack(I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack hideIncompatibleStacks(
        HopperBlockEntity instance, int i, Operation<ItemStack> original,
        @Local Inventory inventory) {
        var stack = original.call(instance, i);
        if (!canTransfer(inventory, stack))
            return ItemStack.EMPTY;
        return stack;
    }

    // This handles the case where a hopper extracts from a hopper above it (such as two hoppers facing forward, stacked
    // on top of one another).
    @Inject(
        method = "extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void extract(Hopper hopper, Inventory inventory, int slot, Direction
        side, CallbackInfoReturnable<Boolean> cir) {
        var item = inventory.getStack(slot);
        if (!canTransfer(hopper, item)) cir.setReturnValue(false);
    }

    // This handles the case where an item entity is dropped onto the hopper from above.
    @Inject(
        method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void extract(Inventory inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!canTransfer(inventory, itemEntity.getStack())) cir.setReturnValue(false);
    }
}
