package com.scheduler.distributed_job_scheduler_backend.services;

import com.scheduler.distributed_job_scheduler_backend.dto.auth.AuthRequest;
import com.scheduler.distributed_job_scheduler_backend.dto.auth.AuthResponse;
import com.scheduler.distributed_job_scheduler_backend.entities.User;
import com.scheduler.distributed_job_scheduler_backend.repository.UserRepository;
import com.scheduler.distributed_job_scheduler_backend.security.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public void signup(AuthRequest request) {
        String email = request.getEmail().trim();
        if(userRepository.findByEmailIgnoreCase(email).isPresent()){
            throw new IllegalArgumentException("Email already exists.");
        }
        String password = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        userRepository.save(user);
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.getEmail().trim(), request.getPassword()));

        if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new IllegalStateException("Authentication provider returned an unexpected principal type.");
        }

        User user = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        AuthResponse response = new AuthResponse();
        response.setToken(jwtUtil.generateToken(userDetails));
        response.setTokenType("Bearer");
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        return response;
    }
}
