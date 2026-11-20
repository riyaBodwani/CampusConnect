package com.riyabodwani.campusconnect.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtil.generateToken("student@example.com", 101L);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testExtractEmail_ValidToken_ReturnsSubject() {
        String token = jwtUtil.generateToken("student@example.com", 101L);
        assertEquals("student@example.com", jwtUtil.extractEmail(token));
        assertEquals(101L, jwtUtil.extractStudentId(token));
    }

    @Test
    void testGenerateToken_ContainsRoleStudentAndExtractRole() {
        String token = jwtUtil.generateToken("student@example.com", 101L);
        assertEquals("STUDENT", jwtUtil.extractRole(token));
        assertEquals("STUDENT", jwtUtil.extractClaims(token).get("role"));
    }

    @Test
    void testGenerateToken_CustomRole() {
        String token = jwtUtil.generateToken("student@example.com", 101L, "STUDENT");
        assertEquals("STUDENT", jwtUtil.extractRole(token));
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date past = new Date(System.currentTimeMillis() - 60000);

        String expiredToken = Jwts.builder()
                .setSubject("expired@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 120000))
                .setExpiration(past)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void testValidateToken_IncorrectSecret_ReturnsFalse() {
        String wrongSecret = "DifferentSecretKeyForTestingPurposes12345678901234567890!";
        Key wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));

        String invalidSecretToken = Jwts.builder()
                .setSubject("tampered@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(wrongKey, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtUtil.validateToken(invalidSecretToken));
    }

    @Test
    void testValidateToken_MalformedToken_ReturnsFalse() {
        assertFalse(jwtUtil.validateToken("not.a.valid.jwt"));
        assertFalse(jwtUtil.validateToken("completely-invalid-string"));
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken("   "));
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testValidateToken_TamperedPayload_ReturnsFalse() {
        String token = jwtUtil.generateToken("original@example.com", 1L);
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + "X" + "." + parts[2];

        assertFalse(jwtUtil.validateToken(tamperedToken));
    }

    @Test
    void testGenerateRecruiterToken_ValidClaimsAndVerification() {
        String token = jwtUtil.generateRecruiterToken("recruiter@example.com", 202L);

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("recruiter@example.com", jwtUtil.extractEmail(token));
        assertEquals(202L, jwtUtil.extractRecruiterId(token));
        assertEquals("RECRUITER", jwtUtil.extractRole(token));
        assertNotNull(jwtUtil.extractClaims(token).getIssuedAt());
        assertNotNull(jwtUtil.extractClaims(token).getExpiration());
    }

    @Test
    void testStudentToken_UnchangedBehavior() {
        String token = jwtUtil.generateToken("student@example.com", 101L);

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("student@example.com", jwtUtil.extractEmail(token));
        assertEquals(101L, jwtUtil.extractStudentId(token));
        assertEquals("STUDENT", jwtUtil.extractRole(token));
        assertNull(jwtUtil.extractRecruiterId(token));
    }
}
