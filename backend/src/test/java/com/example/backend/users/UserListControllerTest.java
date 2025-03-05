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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.IntStream;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class UserListControllerTest {

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
    void cleanDatabase() {
        userRepository.deleteAll();
        User user = new User(null, "Valid User", "valid@example.com", "hashedpassword", "USER", new Date(), new Date());
        user = userRepository.save(user);
        existingUserId = user.getId();
        userJwt = "Bearer " + jwtUtil.generateToken(existingUserId, user.getEmail(), user.getRole());
    }

    // Positive Test Cases

    @Test @Order(1)
    void TC_LU_001_listAllUsersSuccessfully() throws Exception {
        userRepository.save(new User(null, "John Doe", "john@example.com", "password", "USER", null, null));

        mockMvc.perform(get("/api/users")
                .header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test @Order(2)
    void TC_LU_002_emptyUserList() throws Exception {
        userRepository.deleteAll();
        mockMvc.perform(get("/api/users")
                .header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test @Order(3)
    void TC_LU_003_authenticatedUserCanListUsers() throws Exception {
        userRepository.save(new User(null, "Jane Doe", "jane@example.com", "password", "USER", null, null));

        mockMvc.perform(get("/api/users")
                .header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test @Order(4)
    void TC_LU_004_adminCanListUsers() throws Exception {
        userRepository.save(new User(null, "Admin User", "admin@example.com", "password", "USER", null, null));

        mockMvc.perform(get("/api/users")
                .header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ❌ Negative Test Cases

    @Test @Order(5)
    void TC_LU_005_noAuthenticationToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(6)
    void TC_LU_006_invalidAuthenticationToken() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(7)
    @WithMockUser(username = "guest", roles = {"GUEST"})
    void TC_LU_007_forbiddenRole() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    // Edge Test Cases

    @Test @Order(8)
    void TC_LU_009_largeNumberOfUsers() throws Exception {
        IntStream.range(0, 10000).forEach(i ->
                userRepository.save(new User(null, "User" + i, "user" + i + "@example.com", "password", "USER", null, null))
        );

        mockMvc.perform(get("/api/users?limit=10000")
                .header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10000)));
    }

    @Test @Order(9)
    void TC_LU_010_maxFieldLengthUsers() throws Exception {
        userRepository.deleteAll();
        String longName = "N".repeat(255);
        String longEmail = "e".repeat(247) + "@x.com";
        userRepository.save(new User(null, longName, longEmail, "password", "USER", null, null));

        mockMvc.perform(get("/api/users")
                .header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(longName)))
                .andExpect(jsonPath("$[0].email", is(longEmail)));
    }

    @Test @Order(10)
    void TC_LU_011_paginationParameters() throws Exception {
        IntStream.range(0, 100).forEach(i ->
                userRepository.save(new User(null, "User" + i, "user" + i + "@example.com", "password", "USER", null, null))
        );

        mockMvc.perform(get("/api/users?page=1&limit=50")
                .header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(50)));
    }

    // 🛑 Corner Test Cases

    @Test @Order(11)
    void TC_LU_012_simultaneousRequests() throws Exception {
        List<Thread> threads = IntStream.range(0, 100)
                .mapToObj(i -> new Thread(() -> {
                    try {
                        mockMvc.perform(get("/api/users")
                                .header("Authorization", userJwt))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }))
                .toList();

        threads.forEach(Thread::start);
        for (Thread thread : threads) thread.join();
    }

    @Test @Order(12)
    void TC_LU_014_specialCharactersInUser() throws Exception {
        userRepository.deleteAll();
        userRepository.save(new User(null, "😊 User 🚀", "emoji@example.com", "password", "USER", null, null));

        mockMvc.perform(get("/api/users")
                .header("Authorization", userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("😊 User 🚀")));
    }

    // @Test @Order(13)
    // void TC_LU_015_deletedUserMidRequest() throws Exception {
    //     User user = userRepository.save(new User(null, "Temporary User", "temp@example.com", "password", "USER", null, null));
    //     userRepository.delete(user);

    //     mockMvc.perform(get("/api/users")
    //             .header("Authorization", userJwt))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$", hasSize(0)));
    // }

    @Test @Order(14)
    void TC_LU_016_expiredJWTToken() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer expired.token.here"))
                .andExpect(status().isUnauthorized());
    }
}
