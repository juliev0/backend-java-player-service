package com.app.playerservicejava.service;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerService.class);

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * Returns complete player records and removes last names for non-admin users.
     */
    public Players getPlayers(boolean isAdmin) {
        Players visiblePlayers = new Players();

        // Copy each entity so response filtering does not modify persisted players.
        playerRepository.findAll()
                .forEach(player -> visiblePlayers.getPlayers().add(playerVisibleToUser(player, isAdmin)));

        return visiblePlayers;
    }

    /**
     * Copies a complete player record and removes the last name for non-admin users.
     */
    private Player playerVisibleToUser(Player player, boolean isAdmin) {
        Player visiblePlayer = new Player();
        BeanUtils.copyProperties(player, visiblePlayer);

        if (!isAdmin) {
            visiblePlayer.setLastName(null);
        }

        return visiblePlayer;
    }

    /**
     * Returns a complete player record and removes the last name for non-admin users.
     */
    public Optional<Player> getPlayerById(String playerId, boolean isAdmin) {
        Optional<Player> player = null;

        /* simulated network delay */
        try {
            player = playerRepository.findById(playerId);
            Thread.sleep((long)(Math.random() * 2000));
        } catch (Exception e) {
            LOGGER.error("message=Exception in getPlayerById; exception={}", e.toString());
            return Optional.empty();
        }

        // Apply the same field visibility rule used by the player collection.
        return player.map(matchingPlayer -> playerVisibleToUser(matchingPlayer, isAdmin));
    }

}
