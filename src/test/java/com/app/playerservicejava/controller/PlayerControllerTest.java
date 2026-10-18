package com.app.playerservicejava.controller;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.service.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlayerControllerTest {
    private PlayerController playerController;

    /**
     * Configures the controller with deterministic local dependencies.
     */
    @BeforeEach
    void setUp() {
        playerController = new PlayerController();
        ReflectionTestUtils.setField(playerController, "playerService", new StubPlayerService());
        ReflectionTestUtils.setField(playerController, "objectMapper", new ObjectMapper());
    }

    /**
     * Verifies that the collection response preserves player fields and adds age.
     */
    @Test
    void addsAgeToEveryPlayerInCollectionResponse() {
        ResponseEntity<ObjectNode> response = playerController.getPlayers();

        ObjectNode responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("sample01", responseBody.at("/players/0/playerId").asText());
        assertEquals(42, responseBody.at("/players/0/age").asInt());
    }

    /**
     * Verifies that the individual response preserves player fields and adds age.
     */
    @Test
    void addsAgeToIndividualPlayerResponse() {
        ResponseEntity<ObjectNode> response = playerController.getPlayerById("sample01");

        ObjectNode responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("sample01", responseBody.get("playerId").asText());
        assertEquals(42, responseBody.get("age").asInt());
    }

    /**
     * Supplies deterministic player data without accessing the repository.
     */
    private static class StubPlayerService extends PlayerService {

        @Override
        public Players getPlayers() {
            Players players = new Players();
            players.getPlayers().add(createPlayer());
            return players;
        }

        @Override
        public Optional<Player> getPlayerById(String playerId) {
            return Optional.of(createPlayer());
        }

        @Override
        public Integer calculateAge(Player player) {
            return 42;
        }

        /**
         * Creates the player returned by the stub service.
         */
        private Player createPlayer() {
            Player player = new Player();
            player.setPlayerId("sample01");
            return player;
        }
    }
}
