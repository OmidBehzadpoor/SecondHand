package com.example.secondhand.controller;

import com.example.secondhand.dto.response.AdvertisementImageResponse;
import com.example.secondhand.exception.AdvertisementImageNotFoundException;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.exception.InvalidImageException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.service.AdvertisementImageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdvertisementImageControllerTest {

    @Mock
    private AdvertisementImageService advertisementImageService;

    private MockMvc mockMvc;

    private static final User CURRENT_USER = User.builder().id(1L).username("seller").role(Role.USER).build();
    private static final byte[] JPEG_BYTES = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01, 0x02};

    @BeforeEach
    void setUp() {
        AdvertisementImageController controller = new AdvertisementImageController(advertisementImageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        CURRENT_USER, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== upload ====================

    @Test
    void upload_shouldReturn201_whenFileIsValid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);
        AdvertisementImageResponse response = AdvertisementImageResponse.builder()
                .id(1L).imageUrl("/uploads/advertisements/10/abc.jpg").build();

        when(advertisementImageService.uploadImage(eq(10L), any(), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(multipart("/api/advertisements/10/images").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imageUrl").value("/uploads/advertisements/10/abc.jpg"));
    }

    @Test
    void upload_shouldReturn400_whenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/advertisements/10/images").file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_shouldReturn400_whenServiceRejectsInvalidImageFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "fake.jpg", "image/jpeg", JPEG_BYTES);

        when(advertisementImageService.uploadImage(eq(10L), any(), eq(CURRENT_USER)))
                .thenThrow(new InvalidImageException("محتوای فایل با فرمت اعلام‌شده مطابقت ندارد"));

        mockMvc.perform(multipart("/api/advertisements/10/images").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_shouldReturn403_whenUserIsNotOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

        when(advertisementImageService.uploadImage(eq(10L), any(), eq(CURRENT_USER)))
                .thenThrow(new UnauthorizedActionException("شما اجازه‌ی افزودن تصویر به این آگهی را ندارید"));

        mockMvc.perform(multipart("/api/advertisements/10/images").file(file))
                .andExpect(status().isForbidden());
    }

    // ==================== delete ====================

    @Test
    void delete_shouldReturn200_whenSuccessful() throws Exception {
        mockMvc.perform(delete("/api/advertisements/10/images/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("IMAGE_DELETED"));
    }

    @Test
    void delete_shouldReturn404_whenImageDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new AdvertisementImageNotFoundException("تصویر مورد نظر یافت نشد"))
                .when(advertisementImageService).deleteImage(eq(10L), eq(99L), eq(CURRENT_USER));

        mockMvc.perform(delete("/api/advertisements/10/images/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn403_whenUserIsNotOwner() throws Exception {
        org.mockito.Mockito.doThrow(new UnauthorizedActionException("شما اجازه‌ی حذف تصویر این آگهی را ندارید"))
                .when(advertisementImageService).deleteImage(eq(10L), eq(5L), eq(CURRENT_USER));

        mockMvc.perform(delete("/api/advertisements/10/images/5"))
                .andExpect(status().isForbidden());
    }

    // ==================== list ====================

    @Test
    void list_shouldReturn200_withImageList() throws Exception {
        AdvertisementImageResponse response = AdvertisementImageResponse.builder()
                .id(1L).imageUrl("/uploads/advertisements/10/abc.jpg").build();

        when(advertisementImageService.getImages(eq(10L))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/advertisements/10/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].imageUrl").value("/uploads/advertisements/10/abc.jpg"));
    }

    @Test
    void list_shouldReturn404_whenAdvertisementDoesNotExist() throws Exception {
        when(advertisementImageService.getImages(eq(99L)))
                .thenThrow(new com.example.secondhand.exception.AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        mockMvc.perform(get("/api/advertisements/99/images"))
                .andExpect(status().isNotFound());
    }
}
