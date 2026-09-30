package org.ssoggy.ssoggysouls.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommandUtilTest {

    @Test
    void testCheckPermission_hasPermission() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("some.permission")).thenReturn(true);

        boolean result = CommandUtil.checkPermission(sender, "some.permission");

        assertTrue(result);
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    void testCheckPermission_noPermission() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("some.permission")).thenReturn(false);

        boolean result = CommandUtil.checkPermission(sender, "some.permission");

        assertFalse(result);
        verify(sender).sendMessage(MessageUtil.colorize("&cYou don't have permission to use this command."));
    }

    @Test
    void testCheckPermissionCustomMessage_hasPermission() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("some.permission")).thenReturn(true);

        boolean result = CommandUtil.checkPermission(sender, "some.permission", "&cCustom error!");

        assertTrue(result);
        verify(sender, never()).sendMessage(anyString());
    }

    @Test
    void testCheckPermissionCustomMessage_noPermission() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("some.permission")).thenReturn(false);

        boolean result = CommandUtil.checkPermission(sender, "some.permission", "&cCustom error!");

        assertFalse(result);
        verify(sender).sendMessage(MessageUtil.colorize("&cCustom error!"));
    }

    @Test
    void testCheckPermissionCustomMessage_nullMessage() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("some.permission")).thenReturn(false);

        boolean result = CommandUtil.checkPermission(sender, "some.permission", null);

        assertFalse(result);
        verify(sender).sendMessage("");
    }

    @Test
    void testSendInteractiveUsage_playerSender() {
        Player player = mock(Player.class);
        String usageText = "&cUsage: /test <arg>";
        String suggestCmd = "/test ";

        CommandUtil.sendInteractiveUsage(player, usageText, suggestCmd);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(player).sendMessage(captor.capture());

        Component sentComponent = captor.getValue();
        assertNotNull(sentComponent);

        ClickEvent clickEvent = sentComponent.clickEvent();
        assertNotNull(clickEvent);
        assertEquals(ClickEvent.Action.SUGGEST_COMMAND, clickEvent.action());
        assertEquals(suggestCmd, clickEvent.value());

        HoverEvent<?> hoverEvent = sentComponent.hoverEvent();
        assertNotNull(hoverEvent);
        assertEquals(HoverEvent.Action.SHOW_TEXT, hoverEvent.action());
    }

    @Test
    void testSendInteractiveUsage_nonPlayerSender() {
        CommandSender sender = mock(CommandSender.class);
        String usageText = "&cUsage: /test <arg>";
        String suggestCmd = "/test ";

        CommandUtil.sendInteractiveUsage(sender, usageText, suggestCmd);

        verify(sender).sendMessage(MessageUtil.colorize(usageText));
    }
}
