package com.app.playerservicejava.controller.chat;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "v1/chat", produces = { MediaType.APPLICATION_JSON_VALUE })
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private static final int MAX_COUNTRY_LENGTH = 56;

    @Autowired
    private ChatClientService chatClientService;

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

    // Returns a generated nickname for a player from the requested country.
    @GetMapping("/nickname")
    public ResponseEntity<?> nickname(@RequestParam("country") String country) {
        // reject missing or overly long input, since it is placed directly into the prompt
        if (country == null || country.isBlank() || country.length() > MAX_COUNTRY_LENGTH) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "country must be non-blank and at most " + MAX_COUNTRY_LENGTH + " characters."));
        }

        try {
            String nickname = chatClientService.generateNickname(country.trim());
            return ResponseEntity.ok(Map.of("country", country.trim(), "nickname", nickname));
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
