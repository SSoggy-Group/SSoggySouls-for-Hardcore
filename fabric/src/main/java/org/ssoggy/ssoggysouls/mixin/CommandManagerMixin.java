package org.ssoggy.ssoggysouls.mixin;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ssoggy.ssoggysouls.listener.LimboServerListener;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "executeWithPrefix", at = @At("HEAD"), cancellable = true)
    private void onExecuteWithPrefix(ServerCommandSource source, String command, CallbackInfoReturnable<Integer> cir) {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            if (LimboServerListener.shouldBlockCommand(player, command)) {
                cir.setReturnValue(0);
            }
        }
    }
}
