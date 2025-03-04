package com.example.backend.controller;

import com.example.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Authentication
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<String> signIn(@RequestParam String email, @RequestParam String password) {
        String token = authService.authenticate(email, password);
        return ResponseEntity.ok(token);
    }
}
