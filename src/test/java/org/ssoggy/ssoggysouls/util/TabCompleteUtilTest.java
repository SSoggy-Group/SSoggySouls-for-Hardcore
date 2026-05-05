package org.ssoggy.ssoggysouls.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
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
        Collection<? extends Player> players = Arrays.asList(
                createMockPlayer("Alice"),
                createMockPlayer("Alex"),
                createMockPlayer("Bob")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn((Collection) players);

        // Execute
        List<String> result = TabCompleteUtil.getOnlinePlayerNames("al");

        // Verify
        assertEquals(2, result.size());
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Alex"));
    }

    @Test
    void testGetOnlinePlayerNames_CaseInsensitive() {
        // Setup
        Collection<? extends Player> players = Arrays.asList(
                createMockPlayer("Charlie"),
                createMockPlayer("david")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn((Collection) players);

        // Execute
        List<String> result1 = TabCompleteUtil.getOnlinePlayerNames("CH");
        List<String> result2 = TabCompleteUtil.getOnlinePlayerNames("Da");

        // Verify
        assertEquals(1, result1.size());
        assertEquals("Charlie", result1.get(0));

        assertEquals(1, result2.size());
        assertEquals("david", result2.get(0));
    }

    @Test
    void testGetOnlinePlayerNames_NoMatches() {
        // Setup
        Collection<? extends Player> players = Arrays.asList(
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
        List<String> options = Arrays.asList("apple", "apricot", "banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "ap");
        assertEquals(2, result.size());
        assertTrue(result.contains("apple"));
        assertTrue(result.contains("apricot"));
    }

    @Test
    void testFilterStartsWith_CaseInsensitive() {
        List<String> options = Arrays.asList("Apple", "apricot", "BANANA");
        List<String> result1 = TabCompleteUtil.filterStartsWith(options, "AP");
        List<String> result2 = TabCompleteUtil.filterStartsWith(options, "ba");

        assertEquals(2, result1.size());
        assertTrue(result1.contains("Apple"));
        assertTrue(result1.contains("apricot"));

        assertEquals(1, result2.size());
        assertTrue(result2.contains("BANANA"));
    }

    @Test
    void testFilterStartsWith_NoMatches() {
        List<String> options = Arrays.asList("apple", "banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "z");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_EmptyOptions() {
        List<String> options = Arrays.asList();
        List<String> result = TabCompleteUtil.filterStartsWith(options, "a");
        assertTrue(result.isEmpty());
    }
}
