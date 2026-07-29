package com.microshop.users.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final MessageSource messageSource;

    public JwtService(JwtProperties jwtProperties, MessageSource messageSource) {
        this.jwtProperties = jwtProperties;
        this.messageSource = messageSource;
    }

    private String privateKeyPEM;
    private String publicKeyPEM;
    private long jwtExpiration;

    @PostConstruct
    public void init() {
        this.privateKeyPEM = jwtProperties.privateKey();
        this.publicKeyPEM = jwtProperties.publicKey();
        this.jwtExpiration = jwtProperties.expiration();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                // jti único por token. Sin esto, dos logins del MISMO usuario dentro del mismo
                // segundo producían un token BYTE-IDÉNTICO: los claims son los mismos y `iat`/`exp`
                // se serializan en JWT como NumericDate, o sea con precisión de SEGUNDO, no de
                // milisegundo. El segundo token chocaba con el UNIQUE de `sesion.token`
                // (ukp7og8r8eiob14l5r16vixsqew) al guardar la sesión en
                // AuthCommandService.createSession:169, y el login respondía 500.
                //
                // Es un fallo de cara al usuario y con pinta de aleatorio: lo dispara un doble clic
                // en «Entrar», un reintento tras una respuesta lenta o cualquier cliente automático.
                // Lo destapó SuspensionDeTenantTest (login → suspender → reactivar → login), que
                // hace dos logins del mismo usuario en pocos segundos; pasaba o fallaba según lo
                // que tardara la corrida, así que llevaba tiempo siendo intermitente.
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PrivateKey getPrivateKey() {
        try {
            String privateKeyContent = privateKeyPEM
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException(
                    messageSource.getMessage("jwt.key.private.error", null, LocaleContextHolder.getLocale()), e);
        }
    }

    private PublicKey getPublicKey() {
        try {
            String publicKeyContent = publicKeyPEM
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException(
                    messageSource.getMessage("jwt.key.public.error", null, LocaleContextHolder.getLocale()), e);
        }
    }
}
