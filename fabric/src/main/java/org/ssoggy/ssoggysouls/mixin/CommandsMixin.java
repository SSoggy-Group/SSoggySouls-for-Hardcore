package org.ssoggy.ssoggysouls.mixin;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.ssoggy.ssoggysouls.listener.LimboServerListener;

@Mixin(Commands.class)
public class CommandsMixin {
    // performCommand is the common sink for player chat commands and performPrefixedCommand
    @Inject(method = "performCommand", at = @At("HEAD"), cancellable = true)
    private void onPerformCommand(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci) {
        CommandSourceStack source = parseResults.getContext().getSource();
        if (source.getEntity() instanceof ServerPlayer player && LimboServerListener.shouldBlockCommand(player, command)) {
            player.sendSystemMessage(Component.literal("You cannot use commands in your current state."));
            ci.cancel();
        }
    }
}
