package com.app.playerservicejava.controller.chat;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.service.PlayerService;
import com.app.playerservicejava.service.chat.ChatClientService;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(value = "v1/chat", produces = { MediaType.APPLICATION_JSON_VALUE })
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private static final int MAX_COUNTRY_LENGTH = 56;

    @Autowired
    private ChatClientService chatClientService;

    @Autowired
    private PlayerService playerService;

    @PostMapping
    public ResponseEntity<?> chat() {
        try {
            return ResponseEntity.ok(chatClientService.chat());
        } catch (ConnectException exc) {
            return ollamaUnavailable("Ollama chat failed. Ensure tinyllama is pulled and Ollama is running on port 11434.", exc);
        } catch (OllamaBaseException | IOException | InterruptedException exc) {
            return ollamaUnavailable("Ollama chat failed.", exc);
        }
    }

    // Request body for nickname generation: identifies the player to generate a nickname for.
    public record NicknameRequest(String playerId) {}

    // Looks up the player by id, generates a nickname based on the player's birth country,
    // saves it on the player, and returns it.
    @PostMapping("/nickname")
    public ResponseEntity<?> nickname(@RequestBody NicknameRequest request) {
        // reject a missing player id
        if (request == null || request.playerId() == null || request.playerId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "playerId must be provided."));
        }

        // look up the player, responding 404 if no such player exists
        Optional<Player> player = playerService.getPlayerById(request.playerId().trim());
        if (player.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Player not found: " + request.playerId()));
        }

        // the nickname is based on the birth country, so the player must have one on record
        String country = player.get().getBirthCountry();
        if (country == null || country.isBlank() || country.length() > MAX_COUNTRY_LENGTH) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("error", "Player has no usable birth country on record."));
        }

        try {
            String nickname = chatClientService.generateNickname(country.trim());

            // persist the generated nickname on the player record
            playerService.saveNickname(player.get(), nickname);
            return ResponseEntity.ok(Map.of(
                    "playerId", player.get().getPlayerId(),
                    "country", country.trim(),
                    "nickname", nickname));
        } catch (ConnectException exc) {
            return ollamaUnavailable("Ollama nickname generation failed. Ensure tinyllama is pulled and Ollama is running on port 11434.", exc);
        } catch (OllamaBaseException | IOException | InterruptedException exc) {
            return ollamaUnavailable("Ollama nickname generation failed.", exc);
        }
    }

    @GetMapping("/list-models")
    public ResponseEntity<?> listModels() {
        try {
            List<Model> models = chatClientService.listModels();
            return ResponseEntity.ok(models);
        } catch (ConnectException exc) {
            return ollamaUnavailable("Ollama is unavailable. Start the Ollama container and pull tinyllama.", exc);
        } catch (OllamaBaseException | IOException | URISyntaxException | InterruptedException exc) {
            return ollamaUnavailable("Ollama is unavailable. Start the Ollama container and pull tinyllama.", exc);
        }
    }

    private ResponseEntity<Map<String, String>> ollamaUnavailable(String error, Exception exc) {
        LOGGER.error("message={}; exception={}", error, exc.toString());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", error, "detail", String.valueOf(exc.getMessage())));
    }
}
