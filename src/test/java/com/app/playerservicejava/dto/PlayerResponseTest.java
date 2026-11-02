package com.app.playerservicejava.dto;

import com.app.playerservicejava.model.Player;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerResponseTest {

    /**
     * Verifies that entity fields are copied into the API response.
     */
    @Test
    void mapsPlayerFieldsToResponse() {
        Player player = createPlayerBornOn("2000", "10", "18");
        player.setPlayerId("sample01");
        player.setFirstName("Sample");

        PlayerResponse response = PlayerResponse.from(player, LocalDate.of(2026, 10, 18));

        assertEquals("sample01", response.playerId());
        assertEquals("Sample", response.firstName());
        assertEquals(26, response.age());
    }

    /**
     * Verifies that age counts only completed years for a living player.
     */
    @Test
    void calculatesLivingPlayerAgeBeforeBirthday() {
        Player player = createPlayerBornOn("2000", "10", "19");

        PlayerResponse response = PlayerResponse.from(player, LocalDate.of(2026, 10, 18));

        assertEquals(25, response.age());
    }

    /**
     * Verifies that a deceased player's age is calculated on the death date.
     */
    @Test
    void calculatesDeceasedPlayerAgeAtDeath() {
        Player player = createPlayerBornOn("1930", "5", "20");
        player.setDeathYear("2000");
        player.setDeathMonth("5");
        player.setDeathDay("19");

        PlayerResponse response = PlayerResponse.from(player, LocalDate.of(2026, 10, 18));

        assertEquals(69, response.age());
    }

    /**
     * Verifies that incomplete dates do not produce an inaccurate age.
     */
    @Test
    void returnsNullAgeWhenBirthDateIsIncomplete() {
        Player player = createPlayerBornOn("2000", "10", null);

        PlayerResponse response = PlayerResponse.from(player, LocalDate.of(2026, 10, 18));

        assertNull(response.age());
    }

    /**
     * Creates a player with the supplied birth date fields.
     */
    private Player createPlayerBornOn(String year, String month, String day) {
        Player player = new Player();
        player.setBirthYear(year);
        player.setBirthMonth(month);
        player.setBirthDay(day);
        return player;
    }
}
