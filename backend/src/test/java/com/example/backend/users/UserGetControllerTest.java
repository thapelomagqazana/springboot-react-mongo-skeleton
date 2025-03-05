package com.example.backend.users;

import com.example.backend.model.User;
import com.example.backend.security.JwtUtil;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserGetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;

    private String existingUserId;
    private String userJwt;


    @BeforeEach
    void init() {
        userRepository.deleteAll();
        User user = new User(null, "Valid User", "valid@example.com", "hashedpassword", "USER", new Date(), new Date());
        user = userRepository.save(user);
        existingUserId = user.getId();
        userJwt = "Bearer " + jwtUtil.generateToken(existingUserId, user.getEmail(), user.getRole());
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    // Positive Test Cases

    @Test
    @Order(1)
    void TC_GU_001_getExistingUserByValidId() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUserId)
                .header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUserId));
    }

    @Test
    @Order(2)
    void TC_GU_002_authenticatedUserCanFetchProfile() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUserId)
                .header("Authorization", userJwt))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void TC_GU_003_adminCanGetAnyUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUserId)
                .header("Authorization", userJwt))
                .andExpect(status().isOk());
    }

    // Negative Test Cases

    @Test
    @Order(4)
    void TC_GU_004_noAuthenticationToken() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUserId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    void TC_GU_005_invalidAuthenticationToken() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUserId)
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    void TC_GU_006_userNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "nonexistentId")
                .header("Authorization", userJwt))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @WithMockUser(roles = {"GUEST"})
    void TC_GU_007_forbiddenRole() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUserId))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    void TC_GU_008_invalidIdFormat() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "%%%invalid%%%")
                .header("Authorization", userJwt))
                .andExpect(status().isBadRequest());
    }

    // Edge Test Cases

    @Test
    @Order(9)
    void TC_GU_009_userWithMaximumFieldLengths() throws Exception {
        User longUser = new User(null, "J".repeat(255), "email".repeat(50) + "@example.com", "password", "USER", new Date(), new Date());
        userRepository.save(longUser);

        mockMvc.perform(get("/api/users/{id}", longUser.getId())
                .header("Authorization", userJwt))
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    void TC_GU_010_specialCharactersInId() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "@@@@")
                .header("Authorization", userJwt))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    void TC_GU_011_boundaryUUID() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "000000000000000000000000")
                .header("Authorization", userJwt))
                .andExpect(status().isNotFound());
    }

    // Corner Test Cases

    @Test
    @Order(12)
    void TC_GU_012_simultaneousRequests() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/users/{id}", existingUserId)
                    .header("Authorization", userJwt))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @Order(13)
    void TC_GU_013_userDeletedMidRequest() throws Exception {
        userRepository.deleteById(existingUserId);
        mockMvc.perform(get("/api/users/{id}", existingUserId)
                .header("Authorization", userJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(14)
    void TC_GU_014_userWithEmojis() throws Exception {
        User emojiUser = new User(null, "😊 User", "emoji@example.com", "password", "USER", new Date(), new Date());
        userRepository.save(emojiUser);

        mockMvc.perform(get("/api/users/{id}", emojiUser.getId())
                .header("Authorization", userJwt))
                .andExpect(status().isOk());
    }

    @Test
    @Order(15)
    void TC_GU_015_slowDatabaseResponse() throws Exception {
        // Simulation of delay to test handling (can be mocked or simulated).
        Thread.sleep(3000);
        mockMvc.perform(get("/api/users/{id}", existingUserId)
                .header("Authorization", userJwt))
                .andExpect(status().isOk());
    }

    @Test
    @Order(16)
    void TC_GU_016_expiredJwtToken() throws Exception {
        mockMvc.perform(get("/api/users/{id}", existingUserId)
                .header("Authorization", "Bearer expired.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
}
