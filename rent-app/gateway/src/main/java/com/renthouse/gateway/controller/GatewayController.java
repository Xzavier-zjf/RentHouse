package com.renthouse.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {
    
    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("租房网站API网关");
    }
}