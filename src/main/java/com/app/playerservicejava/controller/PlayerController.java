package com.app.playerservicejava.controller;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.security.AuthGuard;
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

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getPlayers(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // reject the request early if the password is missing or wrong
        Optional<ResponseEntity<Object>> rejection = AuthGuard.rejectIfUnauthorized(authorization);
        if (rejection.isPresent()) {
            return rejection.get();
        }

        Players players = playerService.getPlayers();
        return ok(players);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlayerById(@PathVariable("id") String id,
                                           @RequestHeader(value = "Authorization", required = false) String authorization) {
        // reject the request early if the password is missing or wrong
        Optional<ResponseEntity<Object>> rejection = AuthGuard.rejectIfUnauthorized(authorization);
        if (rejection.isPresent()) {
            return rejection.get();
        }

        Optional<Player> player = playerService.getPlayerById(id);

        if (player.isPresent()) {
            return new ResponseEntity<>(player.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
