package com.app.playerservicejava.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerTest {

    /**
     * Verifies that age counts only completed years for a living player.
     */
    @Test
    void calculatesLivingPlayerAgeBeforeBirthday() {
        Player player = createPlayerBornOn("2000", "10", "19");

        Integer age = player.calculateAge(LocalDate.of(2026, 10, 18));

        assertEquals(25, age);
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

        Integer age = player.calculateAge(LocalDate.of(2026, 10, 18));

        assertEquals(69, age);
    }

    /**
     * Verifies that incomplete dates do not produce an inaccurate age.
     */
    @Test
    void returnsNullWhenBirthDateIsIncomplete() {
        Player player = createPlayerBornOn("2000", "10", null);

        Integer age = player.calculateAge(LocalDate.of(2026, 10, 18));

        assertNull(age);
    }

    /**
     * Verifies that the derived age is included in player JSON responses.
     */
    @Test
    void serializesAgeInPlayerJson() throws Exception {
        Player player = createPlayerBornOn("2000", "10", "18");
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode playerJson = objectMapper.readTree(objectMapper.writeValueAsString(player));

        assertEquals(player.getAge(), playerJson.get("age").intValue());
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
