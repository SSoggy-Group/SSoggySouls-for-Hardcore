package org.ssoggy.ssoggysouls.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;

public class CommandErrorUtil {
    public static Component buildErrorComponent(String messageKey, String suggestCommand) {
        return MessageUtil.get(messageKey).copy().withStyle(s -> s.withColor(ChatFormatting.RED)
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, MessageUtil.get("click-to-autofill").copy().withStyle(ChatFormatting.GRAY))));
    }
}
