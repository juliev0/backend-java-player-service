package com.app.playerservicejava.dto;

import com.app.playerservicejava.model.Player;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;

/**
 * Defines the player data returned by the player API.
 */
public record PlayerResponse(
        String playerId,
        String birthYear,
        String birthMonth,
        String birthDay,
        String birthCountry,
        String birthState,
        String birthCity,
        String deathYear,
        String deathMonth,
        String deathDay,
        String deathCountry,
        String deathState,
        String deathCity,
        String firstName,
        String lastName,
        String givenName,
        String weight,
        String height,
        String bats,
        String throwStats,
        String debut,
        String finalGame,
        String retroId,
        String bbrefId,
        Integer age) {

    /**
     * Maps a persisted player to the representation returned by the API.
     */
    public static PlayerResponse from(Player player) {
        return from(player, LocalDate.now());
    }

    /**
     * Maps a persisted player using the supplied date for age calculation.
     */
    static PlayerResponse from(Player player, LocalDate currentDate) {
        return new PlayerResponse(
                player.getPlayerId(),
                player.getBirthYear(),
                player.getBirthMonth(),
                player.getBirthDay(),
                player.getBirthCountry(),
                player.getBirthState(),
                player.getBirthCity(),
                player.getDeathYear(),
                player.getDeathMonth(),
                player.getDeathDay(),
                player.getDeathCountry(),
                player.getDeathState(),
                player.getDeathCity(),
                player.getFirstName(),
                player.getLastName(),
                player.getGivenName(),
                player.getWeight(),
                player.getHeight(),
                player.getBats(),
                player.getThrowStats(),
                player.getDebut(),
                player.getFinalGame(),
                player.getRetroId(),
                player.getBbrefId(),
                calculateAge(player, currentDate));
    }

    /**
     * Calculates age in completed years for a living or deceased player.
     */
    private static Integer calculateAge(Player player, LocalDate currentDate) {
        LocalDate birthDate = parseDate(
                player.getBirthYear(),
                player.getBirthMonth(),
                player.getBirthDay());
        if (birthDate == null) {
            return null;
        }

        // A deceased player's age stops increasing on their date of death.
        LocalDate ageCalculationDate;
        if (player.getDeathYear() == null || player.getDeathYear().isBlank()) {
            ageCalculationDate = currentDate;
        } else {
            ageCalculationDate = parseDate(
                    player.getDeathYear(),
                    player.getDeathMonth(),
                    player.getDeathDay());
            if (ageCalculationDate == null) {
                return null;
            }
        }

        // Reject inconsistent records instead of returning a negative age.
        if (ageCalculationDate.isBefore(birthDate)) {
            return null;
        }

        return Period.between(birthDate, ageCalculationDate).getYears();
    }

    /**
     * Builds a date when all three source values are present and valid.
     */
    private static LocalDate parseDate(String year, String month, String day) {
        if (year == null || year.isBlank()
                || month == null || month.isBlank()
                || day == null || day.isBlank()) {
            return null;
        }

        try {
            return LocalDate.of(
                    Integer.parseInt(year),
                    Integer.parseInt(month),
                    Integer.parseInt(day));
        } catch (DateTimeException | NumberFormatException invalidDate) {
            return null;
        }
    }
}
