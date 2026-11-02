package com.app.playerservicejava.dto;

import com.app.playerservicejava.model.Players;

import java.util.List;

/**
 * Defines the collection returned by the players API.
 */
public record PlayersResponse(List<PlayerResponse> players) {

    /**
     * Maps persisted players to their API representations.
     */
    public static PlayersResponse from(Players players) {
        List<PlayerResponse> playerResponses = players.getPlayers().stream()
                .map(PlayerResponse::from)
                .toList();

        return new PlayersResponse(playerResponses);
    }
}
