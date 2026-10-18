package com.app.playerservicejava.service;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setPlayerId("player-1");
        player.setBirthYear("1919");
        player.setFirstName("Jackie");
        player.setLastName("Robinson");
    }

    @Test
    void getPlayersIncludesCompletePlayersForAdminUsers() {
        when(playerRepository.findAll()).thenReturn(List.of(player));

        Players players = playerService.getPlayers(true);

        assertThat(players.getPlayers())
                .extracting(Player::getPlayerId, Player::getBirthYear, Player::getFirstName, Player::getLastName)
                .containsExactly(tuple("player-1", "1919", "Jackie", "Robinson"));
    }

    @Test
    void getPlayersIncludesCompletePlayersWithoutLastNamesForRegularUsers() {
        when(playerRepository.findAll()).thenReturn(List.of(player));

        Players players = playerService.getPlayers(false);

        assertThat(players.getPlayers())
                .extracting(Player::getPlayerId, Player::getBirthYear, Player::getFirstName, Player::getLastName)
                .containsExactly(tuple("player-1", "1919", "Jackie", null));

        // Confirm response filtering does not erase the last name from the repository entity.
        assertThat(player.getLastName()).isEqualTo("Robinson");
    }

    @Test
    void getPlayerByIdIncludesCompletePlayerForAdminUser() {
        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));

        Optional<Player> visiblePlayer = playerService.getPlayerById("player-1", true);

        assertThat(visiblePlayer)
                .get()
                .extracting(Player::getPlayerId, Player::getBirthYear, Player::getFirstName, Player::getLastName)
                .containsExactly("player-1", "1919", "Jackie", "Robinson");
    }

    @Test
    void getPlayerByIdIncludesCompletePlayerWithoutLastNameForRegularUser() {
        when(playerRepository.findById("player-1")).thenReturn(Optional.of(player));

        Optional<Player> visiblePlayer = playerService.getPlayerById("player-1", false);

        assertThat(visiblePlayer)
                .get()
                .extracting(Player::getPlayerId, Player::getBirthYear, Player::getFirstName, Player::getLastName)
                .containsExactly("player-1", "1919", "Jackie", null);

        // Confirm response filtering does not erase the last name from the repository entity.
        assertThat(player.getLastName()).isEqualTo("Robinson");
    }
}
