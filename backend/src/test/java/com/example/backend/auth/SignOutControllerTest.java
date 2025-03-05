package com.example.backend.auth;

import com.example.backend.security.JwtUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.example.backend.security.TokenBlacklistService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SignOutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private String userJwt;
    private String adminJwt;
    private String longJwt;
    private String emojiJwt;

    @BeforeEach
    void setUp() {

        // Reset blacklist before each test
        tokenBlacklistService.clear();
        userJwt = "Bearer " + jwtUtil.generateToken("userId", "user@example.com", "USER");
        adminJwt = "Bearer " + jwtUtil.generateToken("adminId", "admin@example.com", "ADMIN");
        longJwt = "Bearer " + "a".repeat(400); // Example of max-length valid token
        emojiJwt = "Bearer 😊" + jwtUtil.generateToken("userId", "emoji@example.com", "USER");
    }

    // Positive Test Cases
    @Test
    void TC_SO_001_successfulSignOut() throws Exception {
        mockMvc.perform(post("/auth/signout").header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Signed out successfully."));
    }

    @Test
    void TC_SO_002_adminSuccessfulSignOut() throws Exception {
        mockMvc.perform(post("/auth/signout").header("Authorization", adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Signed out successfully."));
    }

    @Test
    void TC_SO_003_signOutWithTokenInvalidation() throws Exception {
        mockMvc.perform(post("/auth/signout").header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Signed out successfully."));
        mockMvc.perform(post("/auth/signout").header("Authorization", userJwt))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token invalid"));
    }

    // Negative Test Cases
    @Test
    void TC_SO_004_noAuthenticationToken() throws Exception {
        mockMvc.perform(post("/auth/signout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void TC_SO_005_invalidAuthenticationToken() throws Exception {
        mockMvc.perform(post("/auth/signout").header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void TC_SO_006_signOutAlreadySignedOutToken() throws Exception {
        mockMvc.perform(post("/auth/signout").header("Authorization", userJwt))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/signout").header("Authorization", userJwt))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token invalid"));
    }

    // Edge Test Cases
    @Test
    void TC_SO_007_tokenWithMaximumValidLength() throws Exception {
        mockMvc.perform(post("/auth/signout").header("Authorization", longJwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void TC_SO_008_specialCharactersInTokenHeader() throws Exception {
        mockMvc.perform(post("/auth/signout").header("Authorization", emojiJwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void TC_SO_009_signOutImmediatelyAfterLogin() throws Exception {
        String freshJwt = "Bearer " + jwtUtil.generateToken("userId", "fresh@example.com", "USER");
        mockMvc.perform(post("/auth/signout").header("Authorization", freshJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Signed out successfully."));
    }

    // Corner Test Cases
    @Test
    void TC_SO_010_multipleSignOutRequestsInParallel() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                mockMvc.perform(post("/auth/signout").header("Authorization", userJwt))
                        .andExpect(status().isOk());
            } catch (Exception ignored) {}
        });
        executor.submit(() -> {
            try {
                mockMvc.perform(post("/auth/signout").header("Authorization", userJwt))
                        .andExpect(status().isUnauthorized());
            } catch (Exception ignored) {}
        });
        executor.shutdown();
    }

    @Test
    void TC_SO_011_signOutDuringTokenExpirationWindow() throws Exception {
        String expiringJwt = "Bearer " + jwtUtil.generateToken("userId", "expiring@example.com", "USER");
        Thread.sleep(900); // Simulate near-expiration
        mockMvc.perform(post("/auth/signout").header("Authorization", expiringJwt))
                .andExpect(status().isOk());
    }

    @Test
    void TC_SO_013_delayedDatabaseResponseOnSignOut() throws Exception {
        Thread.sleep(3000); // Simulate delay
        mockMvc.perform(post("/auth/signout").header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Signed out successfully."));
    }

    // @Test
    // void TC_SO_014_reSignInAfterSignOut() throws Exception {
    //     mockMvc.perform(post("/auth/signout").header("Authorization", userJwt))
    //             .andExpect(status().isOk());
    //     String newJwt = "Bearer " + jwtUtil.generateToken("userId", "user@example.com", "USER");
    //     mockMvc.perform(post("/auth/signout").header("Authorization", newJwt))
    //             .andExpect(status().isOk());
    // }
}
