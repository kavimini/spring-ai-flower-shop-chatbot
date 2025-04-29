package com.shop.flower.controller;

import com.shop.flower.service.FlowerShopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FlowerShopController {

    private final FlowerShopService flowerShopService;

    public FlowerShopController(FlowerShopService flowerShopService) {
        this.flowerShopService = flowerShopService;
    }

    @GetMapping("/ask-chatbot")
    public String askChatBot(@RequestParam String prompt) {
        return flowerShopService.askGemini(prompt);
    }
}
