package com.example.backend.users;

import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser
public class UserCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    
    @Autowired
    private UserRepository userRepository;

    /**
     * Clean up the user collection before each test to ensure a fresh state.
     */
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    /**
     * Clean up after each test to ensure no leftover data.
     */
    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    private String toJson(Map<String, String> user) throws Exception {
        return objectMapper.writeValueAsString(user);
    }

    static final String ENDPOINT = "/auth/signup";

    // Positive Test Cases
    @Test @Order(1)
    void TC001_createValidUser() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John Doe",
                        "email", "john@example.com",
                        "password", "StrongPass123"
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.updated").exists());
    }

    @Test @Order(2)
    void TC002_createMinimalValidUser() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "A",
                        "email", "a@example.com",
                        "password", "12345678"
                ))))
                .andExpect(status().isCreated());
    }

    @Test @Order(3)
    void TC003_createSecondUniqueUser() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "Jane",
                        "email", "jane@example.com",
                        "password", "Password123!"
                ))))
                .andExpect(status().isCreated());
    }

    // ❌ Negative Test Cases
    @Test
    void TC004_missingName() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "email", "john@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Name is required")));
    }

    @Test
    void TC005_missingEmail() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John",
                        "password", "Password123"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Email is required")));
    }

    @Test
    void TC006_missingPassword() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John",
                        "email", "john@example.com"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Password is required")));
    }

    @Test
    void TC007_invalidEmailFormat() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John",
                        "email", "john@",
                        "password", "Password123"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid email format")));
    }

    @Test @Order(4)
    void TC008_duplicateEmail() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John Duplicate",
                        "email", "john@example.com",
                        "password", "Password123"
                ))));
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John Duplicate",
                        "email", "john@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Email already exists")));
    }

    @Test
    void TC009_weakPassword() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John",
                        "email", "johnweak@example.com",
                        "password", "123"
                ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Password must be between 8 and 255 characters")));
    }

    // 🟡 Edge Test Cases
    @Test
    void TC010_nameOneCharacter() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "J",
                        "email", "j1@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isCreated());
    }

    @Test
    void TC011_name255Characters() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "J".repeat(255),
                        "email", "j2@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isCreated());
    }

    @Test
    void TC012_passwordMinLength() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John",
                        "email", "john8@example.com",
                        "password", "12345678"
                ))))
                .andExpect(status().isCreated());
    }

    @Test
    void TC013_password255Characters() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John",
                        "email", "john255@example.com",
                        "password", "P".repeat(255)
                ))))
                .andExpect(status().isCreated()); // or isBadRequest() based on rules
    }

    // Corner Test Cases

        @Test
        @Order(14)
        void TC014_sqlInjectionInName() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "'; DROP TABLE users; --",
                        "email", "sql@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isCreated()) // Or check if sanitized based on business rules
                .andExpect(jsonPath("$.name").value("'; DROP TABLE users; --"));
        }

        @Test
        @Order(15)
        void TC015_xssInjectionInName() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "<script>alert(1)</script>",
                        "email", "xss@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isCreated()) // Or sanitized if implemented
                .andExpect(jsonPath("$.name").value("<script>alert(1)</script>"));
        }

        @Test
        @Order(16)
        void TC016_unicodeEmail() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "John Unicode",
                        "email", "jöhn@example.com",
                        "password", "Password123"
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jöhn@example.com"));
        }

        // @Test
        // @Order(17)
        // void TC017_largePayload() throws Exception {
        // String largeString = "A".repeat(10 * 1024 * 1024); // ~10MB string
        // mockMvc.perform(post(ENDPOINT)
        //         .contentType(MediaType.APPLICATION_JSON)
        //         .content(toJson(Map.of(
        //                 "name", largeString,
        //                 "email", "large@example.com",
        //                 "password", "Password123"
        //         ))))
        //         .andExpect(status().isPayloadTooLarge()); // Ensure backend handles large payloads safely
        // }

        @Test
        @Order(18)
        void TC018_raceConditionDuplicateSubmission() throws Exception {
        var user = Map.of(
                "name", "Race Condition",
                "email", "race@example.com",
                "password", "Password123"
        );

        // First submission
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(user)))
                .andExpect(status().isCreated());

        // Immediate duplicate
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(user)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Email already exists")));
        }


}
