package com.shop.flower.service;

import com.shop.flower.repository.FlowerShopRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlowerShopService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;
    private final FlowerShopRepository flowerRepository;

    // Consider making this session/user-specific if needed
    private final List<String> chatHistory = new ArrayList<>();

    public FlowerShopService(RestTemplate restTemplate, FlowerShopRepository flowerRepository) {
        this.restTemplate = restTemplate;
        this.flowerRepository = flowerRepository;
    }

    public String askGemini(String userInput) {
        chatHistory.add("User: " + userInput);

        String response = generateGeminiPrompt();

        chatHistory.add("Gemini: " + response);

        return response;
    }

    private String generateGeminiPrompt() {
        String flowerList = flowerRepository.findAll().stream()
                .map(f -> String.format("{\"type\": \"%s\", \"price\": %s}", f.getType(), f.getPrice()))
                .collect(Collectors.joining(", ", "[", "]"));

        String chatContext = String.join("\n", chatHistory);

        String prompt = String.format("""
        You are a helpful assistant in a flower shop.
        Available flowers and prices: %s

        Chat History:
        %s

        You can help customers with:
        - Flower types (e.g., "What are daisies?")
        - Bouquet prices (e.g., "What is the price of Roses?")
        - Orders and availability (e.g., "Are Sunflowers available?")
        
        If the user asks about a flower that is not available, respond with:
        "Sorry, we do not have [flower name] available."
        
        If the user asks anything unrelated (like animals, weather, sports, or news), respond with:
        "I'm here to help with flower orders. Please ask me about flowers."
        """, flowerList, chatContext);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject messageContent = new JSONObject();
        messageContent.put("parts", Collections.singletonList(
                Collections.singletonMap("text", prompt)
        ));

        JSONObject body = new JSONObject();
        body.put("contents", Collections.singletonList(messageContent));

        String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return extractTextFromResponse(response.getBody());
            } else {
                return "Sorry, Gemini didn't respond.";
            }
        } catch (Exception e) {
            return "Something went wrong: " + e.getMessage();
        }
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONObject candidates = jsonObject.getJSONArray("candidates").getJSONObject(0);
            JSONObject content = candidates.getJSONObject("content");
            JSONObject part = content.getJSONArray("parts").getJSONObject(0);
            return part.getString("text");
        } catch (Exception e) {
            return "Could not parse Gemini's response.";
        }
    }
}
