package com.scheduler.distributed_job_scheduler_backend.services;

import com.scheduler.distributed_job_scheduler_backend.dto.auth.AuthRequest;
import com.scheduler.distributed_job_scheduler_backend.dto.auth.AuthResponse;
import com.scheduler.distributed_job_scheduler_backend.entities.User;
import com.scheduler.distributed_job_scheduler_backend.repository.UserRepository;
import com.scheduler.distributed_job_scheduler_backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    private BCryptPasswordEncoder bcryptEncoder;

    @BeforeEach
    void setUp() {
        bcryptEncoder = new BCryptPasswordEncoder();
        passwordEncoder = bcryptEncoder;
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtUtil);
    }

    @Test
    void signupHashesPasswordWithBcryptBeforeSavingUser() {
        AuthRequest request = request("user@example.com", "my-password");
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());

        authService.signup(request);

        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        String storedPassword = userCaptor.getValue().getPassword();
        assertNotEquals("my-password", storedPassword);
        assertTrue(storedPassword.startsWith("$2"));
        assertTrue(bcryptEncoder.matches("my-password", storedPassword));
        assertEquals("user@example.com", userCaptor.getValue().getEmail());
    }

    @Test
    void signupRejectsDuplicateEmailWithoutSavingUser() {
        AuthRequest request = request("user@example.com", "my-password");
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginWithValidCredentialsReturnsGeneratedJwt() {
        AuthRequest request = request("user@example.com", "my-password");
        UserDetails principal = org.springframework.security.core.userdetails.User.withUsername("user@example.com").password("encoded-password").authorities("USER").build();
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        User user = new User();
        user.setUserId(42L);
        user.setEmail("user@example.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(principal)).thenReturn("signed.jwt.token");

        AuthResponse response = authService.login(request);

        assertEquals("signed.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(Long.valueOf(42L), response.getUserId());
        assertEquals("user@example.com", response.getEmail());
        verify(jwtUtil).generateToken(principal);
    }

    @Test
    void loginRejectsInvalidCredentials() {
        AuthRequest request = request("user@example.com", "wrong-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(userRepository, never()).findByEmailIgnoreCase(any(String.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    private AuthRequest request(String email, String password) {
        AuthRequest request = new AuthRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
