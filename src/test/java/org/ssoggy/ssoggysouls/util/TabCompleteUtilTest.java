package org.ssoggy.ssoggysouls.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class TabCompleteUtilTest {

    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    private Player createMockPlayer(String name) {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn(name);
        return player;
    }

    @Test
    void testGetOnlinePlayerNames_EmptyPrefix() {
        // Setup
        List<Player> players = List.of(
                createMockPlayer("Alice"),
                createMockPlayer("Bob")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(players);

        // Execute
        List<String> result = TabCompleteUtil.getOnlinePlayerNames("");

        // Verify
        assertEquals(List.of("Alice", "Bob"), result);
    }

    @Test
    void testGetOnlinePlayerNames_WithPrefix() {
        // Setup
        Collection<? extends Player> players = List.of(
                createMockPlayer("Alice"),
                createMockPlayer("Alex"),
                createMockPlayer("Bob")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn((Collection) players);

        // Execute
        List<String> result = TabCompleteUtil.getOnlinePlayerNames("al");

        // Verify
        assertEquals(List.of("Alice", "Alex"), result);
    }

    @Test
    void testGetOnlinePlayerNames_CaseInsensitive() {
        // Setup
        Collection<? extends Player> players = List.of(
                createMockPlayer("Charlie"),
                createMockPlayer("david")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn((Collection) players);

        // Execute
        List<String> result1 = TabCompleteUtil.getOnlinePlayerNames("CH");
        List<String> result2 = TabCompleteUtil.getOnlinePlayerNames("Da");

        // Verify
        assertEquals(List.of("Charlie"), result1);
        assertEquals(List.of("david"), result2);
    }

    @Test
    void testGetOnlinePlayerNames_NoMatches() {
        // Setup
        Collection<? extends Player> players = List.of(
                createMockPlayer("Eve"),
                createMockPlayer("Frank")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn((Collection) players);

        // Execute
        List<String> result = TabCompleteUtil.getOnlinePlayerNames("z");

        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOnlinePlayerNames_NoPlayersOnline() {
        // Setup
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of());

        // Execute
        List<String> result = TabCompleteUtil.getOnlinePlayerNames("a");

        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_EmptyPrefix() {
        List<String> options = List.of("apple", "banana", "cherry");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "");
        assertEquals(List.of("apple", "banana", "cherry"), result);
    }

    @Test
    void testFilterStartsWith_WithPrefix() {
        List<String> options = List.of("apple", "apricot", "banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "ap");
        assertEquals(List.of("apple", "apricot"), result);
    }

    @Test
    void testFilterStartsWith_CaseInsensitive() {
        List<String> options = List.of("Apple", "apricot", "BANANA");
        List<String> result1 = TabCompleteUtil.filterStartsWith(options, "AP");
        List<String> result2 = TabCompleteUtil.filterStartsWith(options, "ba");

        assertEquals(List.of("Apple", "apricot"), result1);
        assertEquals(List.of("BANANA"), result2);
    }

    @Test
    void testFilterStartsWith_NoMatches() {
        List<String> options = List.of("apple", "banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "z");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_EmptyOptions() {
        List<String> options = List.of();
        List<String> result = TabCompleteUtil.filterStartsWith(options, "a");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_SpecialCharacters() {
        List<String> options = List.of("apple pie", "banana-split", "cherry, sour", "apple-cider");
        List<String> result1 = TabCompleteUtil.filterStartsWith(options, "apple ");
        List<String> result2 = TabCompleteUtil.filterStartsWith(options, "banana-");

        assertEquals(List.of("apple pie"), result1);
        assertEquals(List.of("banana-split"), result2);
    }
}
