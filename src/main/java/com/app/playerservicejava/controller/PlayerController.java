package com.app.playerservicejava.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.service.PlayerService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = "v1/players", produces = { MediaType.APPLICATION_JSON_VALUE })
public class PlayerController {
    @Resource
    private PlayerService playerService;

    @Resource
    private ObjectMapper objectMapper;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<ObjectNode> getPlayers() {
        Players players = playerService.getPlayers();
        ObjectNode response = objectMapper.createObjectNode();
        ArrayNode playerResponses = response.putArray("players");

        // Preserve the collection response shape while enriching each player.
        players.getPlayers().stream()
                .map(this::createPlayerResponse)
                .forEach(playerResponses::add);

        return ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObjectNode> getPlayerById(@PathVariable("id") String id) {
        Optional<Player> player = playerService.getPlayerById(id);

        if (player.isPresent()) {
            return new ResponseEntity<>(createPlayerResponse(player.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Converts a player to JSON and adds the calculated age property.
     */
    private ObjectNode createPlayerResponse(Player player) {
        ObjectNode playerResponse = objectMapper.valueToTree(player);
        Integer age = playerService.calculateAge(player);

        if (age == null) {
            playerResponse.putNull("age");
        } else {
            playerResponse.put("age", age);
        }

        return playerResponse;
    }
}
