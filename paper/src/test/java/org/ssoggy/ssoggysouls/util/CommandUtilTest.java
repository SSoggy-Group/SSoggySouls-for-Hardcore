package org.ssoggy.ssoggysouls.util;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
