package com.lunacycle.service;

import com.lunacycle.dto.AuthDto;
import com.lunacycle.exception.EmailAlreadyExistsException;
import com.lunacycle.model.User;
import com.lunacycle.repository.UserRepository;
import com.lunacycle.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .averageCycleLength(
                        request.getAverageCycleLength() != null
                                ? request.getAverageCycleLength() : 28)
                .onboardingDone(false)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .onboardingDone(false)
                .build();
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .onboardingDone(user.isOnboardingDone())
                .build();
    }
}