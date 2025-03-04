package com.example.backend.auth;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser
public class AuthSignInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Insert test users before each test.
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userRepository.save(User.builder()
                .name("Test User")
                .email("user@example.com")
                .password(passwordEncoder.encode("Password123"))
                .build());

        userRepository.save(User.builder()
                .name("Unicode User")
                .email("jöhn@example.com")
                .password(passwordEncoder.encode("Password123"))
                .build());

        userRepository.save(User.builder()
                .name("Long Password User")
                .email("long@example.com")
                .password(passwordEncoder.encode("P".repeat(255)))
                .build());

        userRepository.save(User.builder()
                .name("Eight Char Password")
                .email("user8@example.com")
                .password(passwordEncoder.encode("12345678"))
                .build());

        userRepository.save(User.builder()
                .name("255 Char Password")
                .email("user255@example.com")
                .password(passwordEncoder.encode("P".repeat(255)))
                .build());

        userRepository.save(User.builder()
                .name("255 Char Email")
                .email("a".repeat(247) + "@ex.com")
                .password(passwordEncoder.encode("Password123"))
                .build());

        userRepository.save(User.builder()
                .name("Special Password")
                .email("special@example.com")
                .password(passwordEncoder.encode("!@#$%^&*()"))
                .build());
    }

    /**
     * Clean up the database after each test.
     */
    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    /**
     * Helper method to convert map to JSON.
     */
    private String toJson(Map<String, String> credentials) throws Exception {
        return objectMapper.writeValueAsString(credentials);
    }

    // 🟢 Positive Test Cases
    @Test @Order(1)
    void TC_SI_001_validCredentials() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "user@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test @Order(2)
    void TC_SI_002_unicodeEmail() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "jöhn@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test @Order(3)
    void TC_SI_003_maxPasswordLength() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "long@example.com",
                        "password", "P".repeat(255)
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    // 🔴 Negative Test Cases
    @Test
    void TC_SI_004_missingEmail() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "password", "Password123"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Email is required")));
    }

    @Test
    void TC_SI_005_missingPassword() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "user@example.com"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Password is required")));
    }

    @Test
    void TC_SI_006_invalidEmailFormat() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "user@",
                        "password", "Password123"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid email format")));
    }

    @Test
    void TC_SI_007_incorrectPassword() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "user@example.com",
                        "password", "Wrong123"
                ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid email or password")));
    }

    @Test
    void TC_SI_008_nonExistentEmail() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "unknown@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid email or password")));
    }

    // 🟡 Edge Test Cases
    @Test
    void TC_SI_009_passwordEightCharacters() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "user8@example.com",
                        "password", "12345678"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void TC_SI_010_password255Characters() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "user255@example.com",
                        "password", "P".repeat(255)
                ))))
                .andExpect(status().isOk()) // or isBadRequest() based on rules
                .andExpect(jsonPath("$.token").exists());
    }

    // @Test
    // void TC_SI_011_email255Characters() throws Exception {
    //     mockMvc.perform(post("/auth/signin")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(toJson(Map.of(
    //                     "email", "a".repeat(247) + "@ex.com",
    //                     "password", "Password123"
    //             ))))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.token").exists());
    // }

    // 🟠 Corner Test Cases
    @Test
    void TC_SI_012_sqlInjectionInEmail() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "' OR '1'='1",
                        "password", "Password123"
                ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC_SI_013_xssInjectionInEmail() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "<script>alert(1)</script>",
                        "password", "Password123"
                ))))
                .andExpect(status().isBadRequest());
    }

    // @Test
    // void TC_SI_014_largePayload() throws Exception {
    //     String largeString = "A".repeat(10 * 1024 * 1024); // ~10MB
    //     mockMvc.perform(post("/auth/signin")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(toJson(Map.of(
    //                     "email", "large@example.com",
    //                     "password", largeString
    //             ))))
    //             .andExpect(status().isPayloadTooLarge());
    // }

    @Test
    void TC_SI_015_specialCharactersPassword() throws Exception {
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "special@example.com",
                        "password", "!@#$%^&*()"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
