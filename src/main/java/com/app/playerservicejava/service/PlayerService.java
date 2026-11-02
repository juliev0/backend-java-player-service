package com.app.playerservicejava.service;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
public class PlayerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerService.class);

    @Autowired
    private PlayerRepository playerRepository;

    public Players getPlayers() {
        Players players = new Players();
        playerRepository.findAll().forEach(player -> {
            populateAge(player);
            players.getPlayers().add(player);
        });
        return players;
    }

    public Optional<Player> getPlayerById(String playerId) {
        Optional<Player> player = null;

        /* simulated network delay */
        try {
            player = playerRepository.findById(playerId);
            player.ifPresent(this::populateAge);
            Thread.sleep((long)(Math.random() * 2000));
        } catch (Exception e) {
            LOGGER.error("message=Exception in getPlayerById; exception={}", e.toString());
            return Optional.empty();
        }
        return player;
    }

    /**
     * Adds the calculated age to a player before it leaves the service layer.
     */
    private void populateAge(Player player) {
        player.setAge(calculateAge(player, LocalDate.now()));
    }

    /**
     * Calculates age in completed years for a living or deceased player.
     */
    static Integer calculateAge(Player player, LocalDate currentDate) {
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
