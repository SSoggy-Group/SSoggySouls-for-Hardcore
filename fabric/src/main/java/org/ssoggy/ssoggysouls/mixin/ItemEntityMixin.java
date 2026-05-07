package org.ssoggy.ssoggysouls.mixin;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ssoggy.ssoggysouls.hrm.HeadDespawnListener;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        // Check if the item is about to despawn
        if (entity.getItemAge() >= 6000 && HeadDespawnListener.shouldCancelDespawn(entity)) {
            // Cancel despawning by resetting age or calling setNeverDespawn() again just in case
            entity.setNeverDespawn();
        }
    }
}
