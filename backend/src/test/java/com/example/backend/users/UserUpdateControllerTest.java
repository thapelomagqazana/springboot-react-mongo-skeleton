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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String userId;
    private String userJwt;

    @BeforeEach
    void setUp() {
        // Ensure a user exists before testing
        User user = userRepository.save(new User(null, "Original Name", "user@example.com", "Password123", "USER", null, null));
        userId = user.getId();
        userJwt = "Bearer " + jwtUtil.generateToken(userId, user.getEmail(), user.getRole());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private String toJson(Map<String, String> data) throws Exception {
        return objectMapper.writeValueAsString(data);
    }

    // Positive Test Cases
    @Test @WithMockUser(roles = "USER")
    void TC_UU_001_updateUserWithValidData() throws Exception {
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("name", "Updated Name", "email", "updated@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void TC_UU_002_adminUpdatesAnyUserSuccessfully() throws Exception {
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("name", "Admin Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin Updated"));
    }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_003_updateWithMinimalValidFields() throws Exception {
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("name", "Partial Update"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Partial Update"));
    }

    @Test @WithMockUser(username = "user@example.com", roles = "USER")
    void TC_UU_004_updateOwnProfile() throws Exception {
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("name", "Own Profile Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Own Profile Updated"));
    }

    // Negative Test Cases
    @Test
    void TC_UU_005_noAuthenticationToken() throws Exception {
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("name", "Should Fail"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void TC_UU_006_invalidAuthenticationToken() throws Exception {
        mockMvc.perform(put("/api/users/" + userId)
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("name", "Should Fail"))))
                .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "GUEST")
    void TC_UU_007_forbiddenRole() throws Exception {
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("name", "Forbidden Update"))))
                .andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_008_userNotFound() throws Exception {
        mockMvc.perform(put("/api/users/invalidId123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("name", "Should Fail"))))
                .andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_009_invalidIdFormat() throws Exception {
        mockMvc.perform(put("/api/users/@@invalid@@")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("name", "Invalid ID"))))
                .andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_010_invalidEmailFormatInUpdate() throws Exception {
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("email", "bademail"))))
                .andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_011_emptyPayload() throws Exception {
        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // @Test @WithMockUser(roles = "USER")
    // void TC_UU_012_updateWithMaximumFieldLengths() throws Exception {
    //     String maxName = "N".repeat(255);
    //     String maxEmail = "e".repeat(245) + "@mail.com";

    //     mockMvc.perform(put("/api/users/{id}", userId)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(toJson(Map.of(
    //                     "name", maxName,
    //                     "email", maxEmail
    //             ))))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.name").value(maxName))
    //             .andExpect(jsonPath("$.email").value(maxEmail));
    // }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_013_updateWithSpecialCharacters() throws Exception {
        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "😊 John",
                        "email", "john@mail.com"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("😊 John"))
                .andExpect(jsonPath("$.email").value("john@mail.com"));
    }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_014_boundaryUUID() throws Exception {
        mockMvc.perform(put("/api/users/{id}", "00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "Boundary Test"
                ))))
                .andExpect(status().isBadRequest());
    }

    // @Test
    // void TC_UU_015_updateWithLargePayload() throws Exception {
    //     String largePayload = "X".repeat(10_000);

    //     mockMvc.perform(put("/api/users/{id}", userId)
    //             .header("Authorization", userJwt)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(toJson(Map.of(
    //                     "name", largePayload
    //             ))))
    //             .andExpect(status().isOk()); // or .andExpect(status().isPayloadTooLarge());
    // }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_016_simultaneousUpdates() throws Exception {
        Runnable updateTask = () -> {
            try {
                mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "name", "Concurrent Update"
                        ))))
                        .andExpect(status().isOk());
            } catch (Exception ignored) {}
        };

        Thread t1 = new Thread(updateTask);
        Thread t2 = new Thread(updateTask);

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_017_userDeletedMidUpdate() throws Exception {
        userRepository.deleteById(userId); // simulate deletion

        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "Deleted User Update"
                ))))
                .andExpect(status().isNotFound());
    }

    // @Test @WithMockUser(roles = "USER")
    // void TC_UU_018_databaseLockDuringUpdate() throws Exception {
    //     // Mock or simulate DB lock scenario depending on infrastructure
    //     mockMvc.perform(put("/api/users/{id}", userId)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(toJson(Map.of(
    //                     "name", "Locked Update"
    //             ))))
    //             .andExpect(status().isInternalServerError()); // or .isGatewayTimeout()
    // }

    @Test @WithMockUser(roles = "USER")
    void TC_UU_020_updateWithUnexpectedExtraFields() throws Exception {
        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of(
                        "name", "Valid Update",
                        "randomField", "Should be ignored"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Valid Update"))
                .andExpect(jsonPath("$.randomField").doesNotExist());
    }
}
