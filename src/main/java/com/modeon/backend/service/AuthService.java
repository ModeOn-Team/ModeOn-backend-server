package com.modeon.backend.service;

import com.modeon.backend.dto.*;
import com.modeon.backend.entity.AuthProvider;
import com.modeon.backend.entity.User;
import com.modeon.backend.exception.AuthenticationException;
import com.modeon.backend.exception.UserAlreadyExistsException;
import com.modeon.backend.exception.UserNotActivatedException;
import com.modeon.backend.repository.UserRepository;
import com.modeon.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public boolean register(RegisterRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .address(request.getAddress())
                .provider(AuthProvider.LOCAL)
                .build();

        userRepository.save(user);

        return true;
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            String loginId = request.getEmail() != null ? request.getEmail() : request.getUsername();

            User user = userRepository.findByEmail(loginId)
                    .or(() -> userRepository.findByUsername(loginId))
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

            if (!user.isEnabled()) {
                throw new DisabledException("User is not activated");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginId, request.getPassword())
            );

            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return AuthResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .user(UserDto.fromEntity(user))
                    .build();

        } catch (DisabledException | AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Invalid email or password");
        }
    }


    public AuthResponse refreshToken(RefreshTokenRequest request){
        String refreshToken = request.getRefreshToken();

        String username = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        String jwtToken = jwtService.generateToken(user);
        String NewRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(NewRefreshToken)
                .user(UserDto.fromEntity(user))
                .build();
    }

    public String getCode(String email){
        return redisTemplate.opsForValue().get("PwChangeCodeCache::" + email);
    }

    public void changePassword(ChangePasswordRequest request){
        String email = request.getEmail();

        if (!request.getAuthCode().equals(getCode(email))){
            throw new IllegalArgumentException("wrong Code");
        }

        if (!request.getPassword().equals(request.getPassword2())){
            throw new IllegalArgumentException("Passwords do not match.");
        }


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("user not found: " + email));

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
    }
}