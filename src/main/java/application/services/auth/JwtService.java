package application.services.auth;

import infrastructure.adapter.out.persistence.auth.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    // ─── Generar tokens ────────────────────────────────────────────

    public String generateAccessToken(CustomUserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getId().toString())
                .claim("email", userDetails.getEmail())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getId().toString())
                .claim("email", userDetails.getEmail())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ─── Extraer datos del token ───────────────────────────────────

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    // ─── Validaciones ──────────────────────────────────────────────

    public boolean isvalid(String token, CustomUserDetails userDetails) {
        Long userId = extractUserId(token);
        return userId.equals(userDetails.getId()) && !isExpired(token);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractTokenType(token));
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // ─── Internos ──────────────────────────────────────────────────

    private <T> T extractClaim(String token, Function<Claims, T> resolv) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolv.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = decodeSecretKey();
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET inválido: se requieren ≥32 bytes "
                    + "(base64 válido o texto crudo). Generá uno con: openssl rand -base64 48");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Base64 si lo es; si no (DecodingException), fallback a bytes crudos UTF-8 —
     * patrón común y soportado por jjwt siempre que cumplan la longitud. El valor
     * del secret JAMÁS se loguea.
     */
    private byte[] decodeSecretKey() {
        try {
            return Decoders.BASE64.decode(secretKey);
        } catch (DecodingException e) {
            log.warn("jwt.secret no es base64 válido — usando bytes crudos (UTF-8). "
                    + "Recomendado: generá uno con openssl rand -base64 48");
            return secretKey.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Fail-fast: valida la clave AL ARRANCAR — un secret inválido aborta el
     * startup en vez de reventar un 500 por cada login.
     */
    @PostConstruct
    void validateSigningKey() {
        getSigningKey();
    }
}
