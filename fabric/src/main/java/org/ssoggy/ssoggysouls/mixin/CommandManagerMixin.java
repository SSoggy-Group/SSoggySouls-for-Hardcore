package org.ssoggy.ssoggysouls.mixin;

import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.ssoggy.ssoggysouls.listener.LimboServerListener;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    /**
     * Intercepts command execution before {@link CommandManager#executeWithPrefix} runs.
     *
     * <p>When the command source is a {@link ServerPlayerEntity} and
     * {@link LimboServerListener#shouldBlockCommand(ServerPlayerEntity, String)} returns {@code true},
     * this injection prevents further command processing.</p>
     *
     * <p>{@code cir.setReturnValue(0)} marks the command as handled with a neutral result, which
     * effectively cancels normal execution for blocked commands.</p>
     */
    @Inject(method = "executeWithPrefix", at = @At("HEAD"), cancellable = true)
    private void onExecuteWithPrefix(ServerCommandSource source, String command, CallbackInfoReturnable<Integer> cir) {
        if (source.getEntity() instanceof ServerPlayerEntity player && LimboServerListener.shouldBlockCommand(player, command)) {
            player.sendMessage(Text.literal("You cannot use commands in your current state."), false);
            cir.setReturnValue(0);
        }
    }
}
