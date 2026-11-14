package hariom.linkedin.apigateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

/**
 * JWT Authentication Filter.
 * Applied to all routes except /api/v1/auth/**
 * Flow:
 * 1. Extract JWT from Authorization header.
 * 2. Validate JWT Signature and expiry.
 * 3. Extract userId from token claims.
 * 4. Add userId to request header for downstream services.
 * 5. Forward request to correct service.
 *
 * If JWT is invalid or missing: 401 unauthorized
 */
@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final SecretKey signingKey;

    public JwtAuthFilter(@Value("${jwt.secret-key}") String secretKey) {
        super(Config.class);
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretKey)
        );
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            // Skip authentication endpoints
            if (path.equals("/api/v1/auth") || path.startsWith("/api/v1/auth/")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            // No token: Reject
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7).trim();

            try {
                Claims claims = extractClaims(token);
                Object userIdClaim = claims.get("userId");

                if (!(userIdClaim instanceof Number userIdValue)) {
                    return unauthorized(exchange);
                }

                Long userId = userIdValue.longValue();
                String email = claims.getSubject();

                if (email == null || email.isBlank()) {
                    return unauthorized(exchange);
                }

                log.info("JWT validated for user: {}", userId);

                // Add userId to request header for downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(req -> req.headers(headers -> {
                            headers.remove("X-User-Id");
                            headers.remove("X-User-Email");

                            headers.set("X-User-Id", userId.toString());
                            headers.set("X-User-Email", email);
                        }))
                        .build();

                return chain.filter(modifiedExchange);

            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                return unauthorized(exchange);
            }
        };
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Set unauthorized status code and return response
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // for filter factory

    }
}