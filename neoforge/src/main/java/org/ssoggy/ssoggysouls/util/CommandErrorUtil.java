package org.ssoggy.ssoggysouls.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;

public final class CommandErrorUtil {
    private CommandErrorUtil() {
        // SonarCloud: hide implicit public constructor
    }

    public static Component buildErrorComponent(Component baseMessage, String suggestCommand) {
        return baseMessage.copy().withStyle(s -> s.withColor(ChatFormatting.RED)
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, MessageUtil.get("click-to-autofill").copy().withStyle(ChatFormatting.GRAY))));
    }
}
