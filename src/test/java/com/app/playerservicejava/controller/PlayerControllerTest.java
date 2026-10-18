package com.app.playerservicejava.controller;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerController playerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(playerController).build();
    }

    @Test
    void getPlayersReturnsEntirePlayersWhenIsAdminIsTrue() throws Exception {
        Players players = playersWithDetails("Jackie", "Robinson");
        when(playerService.getPlayers(true)).thenReturn(players);

        mockMvc.perform(get("/v1/players").param("isAdmin", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].playerId").value("player-1"))
                .andExpect(jsonPath("$.players[0].birthYear").value("1919"))
                .andExpect(jsonPath("$.players[0].firstName").value("Jackie"))
                .andExpect(jsonPath("$.players[0].lastName").value("Robinson"));

        verify(playerService).getPlayers(true);
    }

    @Test
    void getPlayersReturnsEntirePlayersWithoutLastNamesWhenIsAdminIsFalse() throws Exception {
        Players players = playersWithDetails("Jackie", null);
        when(playerService.getPlayers(false)).thenReturn(players);

        mockMvc.perform(get("/v1/players").param("isAdmin", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].playerId").value("player-1"))
                .andExpect(jsonPath("$.players[0].birthYear").value("1919"))
                .andExpect(jsonPath("$.players[0].firstName").value("Jackie"))
                .andExpect(jsonPath("$.players[0].lastName").doesNotExist());

        verify(playerService).getPlayers(false);
    }

    @Test
    void getPlayerByIdReturnsEntirePlayerWhenIsAdminIsTrue() throws Exception {
        Player player = playerWithDetails("Jackie", "Robinson");
        when(playerService.getPlayerById("player-1", true)).thenReturn(Optional.of(player));

        mockMvc.perform(get("/v1/players/player-1").param("isAdmin", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value("player-1"))
                .andExpect(jsonPath("$.birthYear").value("1919"))
                .andExpect(jsonPath("$.firstName").value("Jackie"))
                .andExpect(jsonPath("$.lastName").value("Robinson"));

        verify(playerService).getPlayerById("player-1", true);
    }

    @Test
    void getPlayerByIdReturnsEntirePlayerWithoutLastNameWhenIsAdminIsFalse() throws Exception {
        Player player = playerWithDetails("Jackie", null);
        when(playerService.getPlayerById("player-1", false)).thenReturn(Optional.of(player));

        mockMvc.perform(get("/v1/players/player-1").param("isAdmin", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value("player-1"))
                .andExpect(jsonPath("$.birthYear").value("1919"))
                .andExpect(jsonPath("$.firstName").value("Jackie"))
                .andExpect(jsonPath("$.lastName").doesNotExist());

        verify(playerService).getPlayerById("player-1", false);
    }

    /**
     * Creates a response containing one complete player for controller serialization tests.
     */
    private Players playersWithDetails(String firstName, String lastName) {
        Players players = new Players();
        players.getPlayers().add(playerWithDetails(firstName, lastName));
        return players;
    }

    /**
     * Creates a stored player containing identifying, biographical, and name fields.
     */
    private Player playerWithDetails(String firstName, String lastName) {
        Player player = new Player();
        player.setPlayerId("player-1");
        player.setBirthYear("1919");
        player.setFirstName(firstName);
        player.setLastName(lastName);
        return player;
    }
}
