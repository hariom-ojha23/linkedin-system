package hariom.linkedin.userservice.service;

import hariom.linkedin.userservice.dto.AuthResponse;
import hariom.linkedin.userservice.dto.LoginRequest;
import hariom.linkedin.userservice.dto.RegisterRequest;
import hariom.linkedin.userservice.entity.User;
import hariom.linkedin.userservice.enums.UserRole;
import hariom.linkedin.userservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final KafkaTemplate<Long, Object> kafkaTemplate;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private long accessJwtExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshJwtExpiration;

    private static final String USER_CREATED_TOPIC = "user.created";

    /**
     * Register a new user.
     * Publish event to kafka to be consumed by search service
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getEmail());

        boolean emailExist = userRepository.existsByEmail(request.getEmail());
        if (emailExist) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .headLine(request.getHeadLine())
                .about(request.getAbout())
                .location(request.getLocation())
                .build();

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.NORMAL_USER);

        User savedUser = userRepository.save(user);
        log.info("User registered: {}", savedUser.getId());

        // Public user.created event to kaka
        // Search service consume this and indexes user

        Map<String, Object> userCreatedEvent = new HashMap<>();

        userCreatedEvent.put("userId", savedUser.getId());
        userCreatedEvent.put("email", savedUser.getEmail());
        userCreatedEvent.put("firstName", savedUser.getFirstName());
        userCreatedEvent.put("lastName", savedUser.getLastName());
        userCreatedEvent.put("headline", savedUser.getHeadLine());
        userCreatedEvent.put("location", savedUser.getLocation());

        kafkaTemplate.send(USER_CREATED_TOPIC, savedUser.getId(), userCreatedEvent);

        log.info("user.created event published: {}", savedUser.getId());

        String accessToken = generateAccessToken(savedUser.getId(), savedUser.getEmail());
        String refreshToken = generateRefreshToken(savedUser.getId());

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    /**
     * Login existing user
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository
                .getUserByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getEmail()));

        // Bcrypt verify compare raw password with saved password hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        log.info("Login successful: {}", user.getId());

        String accessToken = generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = generateRefreshToken(user.getId());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    /**
     * Generate access token
     */
    private String generateAccessToken(Long userId, String email) {
        return Jwts.builder()
                .claim("userId", userId)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessJwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Generate refresh token.
     * Used to get a new access token when it expires.
     * Client sends refresh token to /auth/refresh endpoint
     */
    private String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshJwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Generate auth response
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Get Sign in key
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
