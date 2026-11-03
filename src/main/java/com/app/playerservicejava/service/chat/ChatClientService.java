package com.app.playerservicejava.service.chat;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.Model;
import io.github.ollama4j.models.OllamaResult;
import io.github.ollama4j.types.OllamaModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.github.ollama4j.utils.OptionsBuilder;
import io.github.ollama4j.utils.PromptBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class ChatClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClientService.class);

    @Autowired
    private OllamaAPI ollamaAPI;

    public List<Model> listModels() throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
        List<Model> models = ollamaAPI.listModels();
        return models;
    }

    public String chat() throws OllamaBaseException, IOException, InterruptedException {
        String model = OllamaModelType.TINYLLAMA;

        // https://ollama4j.github.io/ollama4j/intro
        PromptBuilder promptBuilder =
                new PromptBuilder()
                        .addLine("Recite a haiku about recursion.");

        boolean raw = false;
        OllamaResult response = ollamaAPI.generate(model, promptBuilder.build(), raw, new OptionsBuilder().build());
        return response.getResponse();
    }

    // Asks the language model for a short nickname for a soccer player from the given country,
    // then cleans up the model's reply so only the nickname itself is returned.
    public String generateNickname(String country) throws OllamaBaseException, IOException, InterruptedException {
        String model = OllamaModelType.TINYLLAMA;

        // build a prompt that asks for the nickname only, with no extra commentary
        PromptBuilder promptBuilder =
                new PromptBuilder()
                        .addLine("Generate one short, fun nickname for a soccer player from " + country + ".")
                        .addLine("Reply with only the nickname and nothing else.");

        // use a moderate temperature so nicknames stay varied but reasonably focused
        boolean raw = false;
        OllamaResult response = ollamaAPI.generate(
                model, promptBuilder.build(), raw, new OptionsBuilder().setTemperature(0.7f).build());

        // small models often add extra lines, so keep only the first non-empty line
        String firstLine = response.getResponse().trim().lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .findFirst()
                .orElse("");

        // remove surrounding quotes and asterisks the model may wrap around the nickname
        return firstLine.replaceAll("^[\"'*\\s]+|[\"'*\\s]+$", "");
    }

}
