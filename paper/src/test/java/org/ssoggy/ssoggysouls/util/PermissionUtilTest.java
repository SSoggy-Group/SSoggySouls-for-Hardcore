package org.ssoggy.ssoggysouls.util;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.SSoggySouls;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionUtilTest {

    private SSoggySouls plugin;
    private CommandSender sender;
    private Player player;

    @BeforeEach
    void setUp() {
        plugin = mock(SSoggySouls.class);
        sender = mock(CommandSender.class);
        player = mock(Player.class);

        // Default to enabled and on Limbo server for most tests
        when(plugin.isLimboOpSecurityEnabled()).thenReturn(true);
        when(plugin.isLimboServer()).thenReturn(true);
        when(plugin.getLimboTrustedAdmins()).thenReturn(Collections.emptySet());
    }

    @Test
    void testIsBlockedByLimboOpSecurity_Disabled() {
        when(plugin.isLimboOpSecurityEnabled()).thenReturn(false);
        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurity_NonPlayer() {
        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurity_NotLimbo() {
        when(plugin.isLimboServer()).thenReturn(false);
        when(player.isOp()).thenReturn(true);
        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurity_NotOp() {
        when(player.isOp()).thenReturn(false);
        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurity_WhitelistedUuid() {
        UUID uuid = UUID.randomUUID();
        when(player.isOp()).thenReturn(true);
        when(player.getUniqueId()).thenReturn(uuid);
        when(plugin.getLimboTrustedAdmins()).thenReturn(Set.of(uuid.toString()));

        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurity_WhitelistedName() {
        String name = "TestPlayer";
        when(player.isOp()).thenReturn(true);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn(name);
        when(plugin.getLimboTrustedAdmins()).thenReturn(Set.of(name.toLowerCase()));

        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurity_BypassPermission() {
        when(player.isOp()).thenReturn(true);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.hasPermission("ssoggysouls.bypass-limbo-op-security")).thenReturn(true);

        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurity_Blocked() {
        when(player.isOp()).thenReturn(true);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("BlockedPlayer");
        when(player.hasPermission("ssoggysouls.bypass-limbo-op-security")).thenReturn(false);

        assertTrue(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testSendSecurityBlockMessage() {
        PermissionUtil.sendSecurityBlockMessage(sender);

        verify(sender).sendMessage(MessageUtil.colorize("&cSecurity Error: On the Limbo server, OP status cannot be used to execute this command."));
        verify(sender).sendMessage(MessageUtil.colorize("&7Either /deop yourself on Limbo, ask an administrator to add you to the whitelist, or have them grant you the bypass permission &e(ssoggysouls.bypass-limbo-op-security)&7."));
    }
}
