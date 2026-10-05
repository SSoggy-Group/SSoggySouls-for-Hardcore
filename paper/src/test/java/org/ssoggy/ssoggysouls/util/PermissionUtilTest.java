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

        // Default player info to prevent NPEs
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("TestPlayer");

        // Default to enabled and on Limbo server for most tests
        when(plugin.isLimboOpSecurityEnabled()).thenReturn(true);
        when(plugin.isLimboServer()).thenReturn(true);
        when(plugin.getLimboTrustedAdmins()).thenReturn(Collections.emptySet());
    }

    @Test
    void testIsBlockedByLimboOpSecurityDisabled() {
        when(plugin.isLimboOpSecurityEnabled()).thenReturn(false);
        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurityNonPlayer() {
        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(sender, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurityNotLimbo() {
        when(plugin.isLimboServer()).thenReturn(false);
        when(player.isOp()).thenReturn(true);
        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurityNotOp() {
        when(player.isOp()).thenReturn(false);
        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurityTrustedUuid() {
        when(player.isOp()).thenReturn(true);
        UUID uuid = player.getUniqueId();
        when(plugin.getLimboTrustedAdmins()).thenReturn(Set.of(uuid.toString()));

        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurityTrustedName() {
        String name = player.getName();
        when(player.isOp()).thenReturn(true);
        when(plugin.getLimboTrustedAdmins()).thenReturn(Set.of(name.toLowerCase()));

        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurityBypassPermission() {
        when(player.isOp()).thenReturn(true);
        when(player.hasPermission("ssoggysouls.bypass-limbo-op-security")).thenReturn(true);

        assertFalse(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testIsBlockedByLimboOpSecurityBlocked() {
        when(player.isOp()).thenReturn(true);
        when(player.getName()).thenReturn("BlockedPlayer");
        when(player.hasPermission("ssoggysouls.bypass-limbo-op-security")).thenReturn(false);

        assertTrue(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testSendSecurityBlockMessage() {
        PermissionUtil.sendSecurityBlockMessage(sender);

        verify(sender).sendMessage(MessageUtil.colorize("&cSecurity Error: On the Limbo server, OP status cannot be used to execute this command."));
        verify(sender).sendMessage(MessageUtil.colorize("&7Either /deop yourself on Limbo, ask an administrator to add you to the trusted admins list, or have them grant you the bypass permission &e(ssoggysouls.bypass-limbo-op-security)&7."));
    }

    @Test
    void testIsBlockedByLimboOpSecurityTrustedAdminsNotEmptyNotMatching() {
        when(player.isOp()).thenReturn(true);
        when(plugin.getLimboTrustedAdmins()).thenReturn(Set.of("other-admin-uuid-or-name"));
        when(player.hasPermission("ssoggysouls.bypass-limbo-op-security")).thenReturn(false);

        assertTrue(PermissionUtil.isBlockedByLimboOpSecurity(player, plugin));
    }

    @Test
    void testSendSecurityBlockMessagePlayer() {
        PermissionUtil.sendSecurityBlockMessage(player);

        verify(player).sendMessage(MessageUtil.colorize("&cSecurity Error: On the Limbo server, OP status cannot be used to execute this command."));
        verify(player).sendMessage(org.mockito.ArgumentMatchers.any(net.kyori.adventure.text.Component.class));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<PermissionUtil> constructor = PermissionUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
