package com.example.secondhand.controller;

import com.example.secondhand.dto.response.AdminUserResponse;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.exception.UserNotFoundException;
import com.example.secondhand.exception.UserStateConflictException;
import com.example.secondhand.model.Role;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminUserController controller = new AdminUserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllUsers_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("getAllUsers");
    }

    @Test
    void blockUser_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("blockUser", Long.class);
    }

    @Test
    void unblockUser_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("unblockUser", Long.class);
    }

    private void assertRequiresAdminRole(String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method = AdminUserController.class.getMethod(methodName, paramTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }

    @Test
    void getAllUsers_shouldReturn200_withList() throws Exception {
        AdminUserResponse response = AdminUserResponse.builder()
                .id(1L).username("ali123").role(Role.USER).userStatus(UserStatus.ACTIVE).build();

        when(userService.getAllUsersForAdmin()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void blockUser_shouldReturn200_whenUserIsActiveAndNotAdmin() throws Exception {
        AdminUserResponse response = AdminUserResponse.builder()
                .id(1L).username("ali123").userStatus(UserStatus.BLOCKED).build();

        when(userService.blockUser(eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userStatus").value("BLOCKED"));
    }

    @Test
    void blockUser_shouldReturn404_whenUserDoesNotExist() throws Exception {
        when(userService.blockUser(eq(99L))).thenThrow(new UserNotFoundException("کاربر یافت نشد"));

        mockMvc.perform(patch("/api/admin/users/99/block"))
                .andExpect(status().isNotFound());
    }

    @Test
    void blockUser_shouldReturn403_whenTargetIsAdmin() throws Exception {
        when(userService.blockUser(eq(2L)))
                .thenThrow(new UnauthorizedActionException("امکان تغییر وضعیت دسترسی سایر مدیران وجود ندارد"));

        mockMvc.perform(patch("/api/admin/users/2/block"))
                .andExpect(status().isForbidden());
    }

    @Test
    void blockUser_shouldReturn409_whenAlreadyBlocked() throws Exception {
        when(userService.blockUser(eq(1L)))
                .thenThrow(new UserStateConflictException("کاربر از قبل مسدود شده است"));

        mockMvc.perform(patch("/api/admin/users/1/block"))
                .andExpect(status().isConflict());
    }

    @Test
    void unblockUser_shouldReturn200_whenCurrentlyBlocked() throws Exception {
        AdminUserResponse response = AdminUserResponse.builder()
                .id(1L).username("ali123").userStatus(UserStatus.ACTIVE).build();

        when(userService.unblockUser(eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/1/unblock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userStatus").value("ACTIVE"));
    }

    @Test
    void unblockUser_shouldReturn409_whenAlreadyActive() throws Exception {
        when(userService.unblockUser(eq(1L)))
                .thenThrow(new UserStateConflictException("کاربر از قبل فعال است"));

        mockMvc.perform(patch("/api/admin/users/1/unblock"))
                .andExpect(status().isConflict());
    }
}
