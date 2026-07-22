package com.example.secondhand.security;

import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.repository.UserRepository;
import com.example.secondhand.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic sometoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_shouldAuthenticate_whenTokenIsValidAndUserExistsAndActive() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = User.builder().id(1L).username("ali123").role(Role.USER).status(UserStatus.ACTIVE).build();

        when(jwtService.extractUsername("valid-token")).thenReturn("ali123");
        when(userRepository.findByUsername("ali123")).thenReturn(Optional.of(user));
        when(jwtService.validateToken("valid-token", "ali123")).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(user, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenUserIsBlocked_evenWithAValidToken() throws ServletException, IOException {
        // This is the key behavior change from before: the filter now rejects
        // authentication globally for BLOCKED users at this layer, not just inside
        // individual services like ChatService. A still-valid, unexpired JWT for a
        // blocked user must not result in an authenticated SecurityContext anywhere.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User blockedUser = User.builder().id(1L).username("ali123").role(Role.USER).status(UserStatus.BLOCKED).build();

        when(jwtService.extractUsername("valid-token")).thenReturn("ali123");
        when(userRepository.findByUsername("ali123")).thenReturn(Optional.of(blockedUser));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).validateToken(any(), any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenTokenIsValidButUserNoLongerExists() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-token")).thenReturn("deleted-user");
        when(userRepository.findByUsername("deleted-user")).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).validateToken(any(), any());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenValidateTokenReturnsFalse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = User.builder().id(1L).username("ali123").role(Role.USER).status(UserStatus.ACTIVE).build();

        when(jwtService.extractUsername("stale-token")).thenReturn("ali123");
        when(userRepository.findByUsername("ali123")).thenReturn(Optional.of(user));
        when(jwtService.validateToken("stale-token", "ali123")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldNotThrow_andShouldContinueChain_whenTokenIsMalformed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer not-a-real-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("not-a-real-jwt"))
                .thenThrow(new io.jsonwebtoken.MalformedJwtException("malformed"));

        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotOverwriteExistingAuthentication_whenAlreadyAuthenticated() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User existingUser = User.builder().id(5L).username("already-auth").role(Role.USER).build();
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(existingUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(jwtService.extractUsername("valid-token")).thenReturn("ali123");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(existingUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(userRepository, never()).findByUsername(any());
    }
}
