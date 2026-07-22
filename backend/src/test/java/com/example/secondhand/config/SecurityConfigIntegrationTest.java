package com.example.secondhand.config;

import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.repository.UserRepository;
import com.example.secondhand.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User createUser(String username, String phone, Role role, UserStatus status) {
        return userRepository.save(User.builder()
                .name("Test User").username(username).password(passwordEncoder.encode("123456"))
                .phone(phone).email(username + "@example.com").role(role).status(status).build());
    }

    // ==================== public endpoints (permitAll) ====================

    @Test
    void register_shouldBeAccessible_withoutAuthentication() throws Exception {
        String body = """
                {"name":"Ali","username":"newuser1","password":"123456","phone":"09121110000","email":"a@a.com"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void login_shouldBeAccessible_withoutAuthentication() throws Exception {
        createUser("loginuser1", "09121110001", Role.USER, UserStatus.ACTIVE);

        String body = """
                {"username":"loginuser1","password":"123456"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    void getAllAdvertisements_shouldBeAccessible_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/advertisements"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllCategories_shouldBeAccessible_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllCities_shouldBeAccessible_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk());
    }

    @Test
    void healthActuator_shouldBeAccessible_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void uploadsPath_shouldNotRequireAuthentication_evenForMissingFile() throws Exception {
        // GET /uploads/** is explicitly permitAll; a missing file should 404, not 401 —
        // the important thing is that Spring Security itself never blocks the request.
        int statusCode = mockMvc.perform(get("/uploads/advertisements/999/does-not-exist.jpg"))
                .andReturn().getResponse().getStatus();

        org.junit.jupiter.api.Assertions.assertNotEquals(401, statusCode);
    }

    // ==================== protected endpoints (must reject anonymous access) ====================

    @Test
    void getMyAdvertisements_shouldReturn401_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/advertisements/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("ابتدا وارد حساب کاربری شوید"));
    }

    @Test
    void createAdvertisement_shouldReturn401_withoutAuthentication() throws Exception {
        String body = """
                {"title":"Laptop","description":"desc","price":1000,"categoryId":1,"cityId":1,"imageUrls":[]}
                """;

        mockMvc.perform(post("/api/advertisements")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAdvertisement_shouldReturn401_whenTokenIsInvalid() throws Exception {
        String body = """
                {"title":"Laptop","description":"desc","price":1000,"categoryId":1,"cityId":1,"imageUrls":[]}
                """;

        mockMvc.perform(post("/api/advertisements")
                        .header("Authorization", "Bearer not-a-real-token")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ==================== authenticated access with a real token ====================

    @Test
    void createAdvertisement_shouldBeAccepted_withValidToken() throws Exception {
        User user = createUser("realuser1", "09121110002", Role.USER, UserStatus.ACTIVE);
        String token = jwtService.generateToken(user);

        String body = """
                {"title":"Laptop","description":"desc","price":1000,"categoryId":999,"cityId":999,"imageUrls":[]}
                """;

        mockMvc.perform(post("/api/advertisements")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status != 401 && status != 403,
                            "expected authentication to succeed, got status " + status);
                });
    }

    // ==================== global blocked-user rejection ====================

    @Test
    void createAdvertisement_shouldReturn401_whenTokenBelongsToBlockedUser() throws Exception {
        // This is the key new behavior: JwtAuthenticationFilter now rejects a BLOCKED
        // user's token globally, not just inside individual services. A still-valid,
        // unexpired JWT for a blocked user must not authenticate anywhere in the app.
        User blockedUser = createUser("blockeduser1", "09121110005", Role.USER, UserStatus.BLOCKED);
        String token = jwtService.generateToken(blockedUser);

        String body = """
                {"title":"Laptop","description":"desc","price":1000,"categoryId":1,"cityId":1,"imageUrls":[]}
                """;

        mockMvc.perform(post("/api/advertisements")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ==================== role-based access (admin-only endpoints) ====================

    @Test
    void getPendingAdvertisements_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        User user = createUser("regularuser1", "09121110003", Role.USER, UserStatus.ACTIVE);
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/admin/advertisements/pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPendingAdvertisements_shouldReturn200_whenUserIsAdmin() throws Exception {
        User admin = createUser("adminuser1", "09121110004", Role.ADMIN, UserStatus.ACTIVE);
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/admin/advertisements/pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getPendingAdvertisements_shouldReturn401_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/advertisements/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminDashboard_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        User user = createUser("regularuser2", "09121110006", Role.USER, UserStatus.ACTIVE);
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDashboard_shouldReturn200_whenUserIsAdmin() throws Exception {
        User admin = createUser("adminuser2", "09121110007", Role.ADMIN, UserStatus.ACTIVE);
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void adminUserList_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        User user = createUser("regularuser3", "09121110008", Role.USER, UserStatus.ACTIVE);
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUserList_shouldReturn200_whenUserIsAdmin() throws Exception {
        User admin = createUser("adminuser3", "09121110009", Role.ADMIN, UserStatus.ACTIVE);
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
