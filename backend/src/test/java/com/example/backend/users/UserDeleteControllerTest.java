package com.example.backend.users;

import com.example.backend.model.User;
import com.example.backend.security.JwtUtil;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDeleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String userId;
    private String userJwt;
    private String adminJwt;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = userRepository.save(new User(null, "Delete Me", "deleteme@example.com", "Password123", "USER", new Date(), new Date()));
        userId = user.getId();
        userJwt = "Bearer " + jwtUtil.generateToken(userId, user.getEmail(), user.getRole());
        adminJwt = "Bearer " + jwtUtil.generateToken("adminId", "admin@example.com", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    // Positive Test Cases

    @Test
    void TC_DU_001_deleteExistingUserByValidId() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", userJwt))
                .andExpect(status().isNoContent());
    }

    @Test
    void TC_DU_002_adminDeletesAnyUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", adminJwt))
                .andExpect(status().isNoContent());
    }

    @Test
    void TC_DU_003_userDeletesOwnAccount() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", userJwt))
                .andExpect(status().isNoContent());
    }

    // Negative Test Cases

    @Test
    void TC_DU_004_noAuthenticationToken() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void TC_DU_005_invalidAuthenticationToken() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void TC_DU_006_forbiddenRole() throws Exception {
        String guestJwt = "Bearer " + jwtUtil.generateToken("guestId", "guest@example.com", "GUEST");

        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", guestJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void TC_DU_007_userNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", "nonexistentId")
                .header("Authorization", userJwt))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC_DU_008_invalidIdFormat() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", "@@@@")
                .header("Authorization", userJwt))
                .andExpect(status().isBadRequest());
    }

    // Edge Test Cases

    @Test
    void TC_DU_009_deleteUserWithMaximumFieldLengths() throws Exception {
        User longUser = userRepository.save(new User(null, "N".repeat(255), "e".repeat(245) + "@mail.com", "Password123", "USER", new Date(), new Date()));
        mockMvc.perform(delete("/api/users/{id}", longUser.getId())
                .header("Authorization", userJwt))
                .andExpect(status().isNoContent());
    }

    @Test
    void TC_DU_010_specialCharactersInId() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", "%%%%")
                .header("Authorization", userJwt))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC_DU_011_boundaryUUID() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", "00000000-0000-0000-0000-000000000000")
                .header("Authorization", userJwt))
                .andExpect(status().isBadRequest());
    }

    // Corner Test Cases

    @Test
    void TC_DU_012_simultaneousDeletionsOfSameUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", userJwt))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", userJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    void TC_DU_013_userDeletedMidOperation() throws Exception {
        userRepository.deleteById(userId);
        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", userJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    void TC_DU_014_deleteUserWithEmojisInName() throws Exception {
        User emojiUser = userRepository.save(new User(null, "😊 User", "emoji@example.com", "Password123", "USER", new Date(), new Date()));

        mockMvc.perform(delete("/api/users/{id}", emojiUser.getId())
                .header("Authorization", userJwt))
                .andExpect(status().isNoContent());
    }

    @Test
    void TC_DU_015_slowDatabaseResponse() throws Exception {
        Thread.sleep(3000); // Simulate delay
        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", userJwt))
                .andExpect(status().isNoContent());
    }

    @Test
    void TC_DU_016_expiredJwtToken() throws Exception {
        String expiredJwt = "Bearer expired.jwt.token";
        mockMvc.perform(delete("/api/users/{id}", userId)
                .header("Authorization", expiredJwt))
                .andExpect(status().isUnauthorized());
    }
}
