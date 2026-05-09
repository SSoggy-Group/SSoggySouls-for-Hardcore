package org.ssoggy.ssoggysouls.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.Entity;
import org.ssoggy.ssoggysouls.listener.LimboServerListener;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/entity/Entity;", at = @At("HEAD"), cancellable = true)
    private void onTeleportTo(TeleportTarget target, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (LimboServerListener.shouldBlockPortal(player)) {
            cir.setReturnValue(player);
        }
    }
}
