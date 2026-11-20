package com.riyabodwani.campusconnect.config;

import com.riyabodwani.campusconnect.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityFilterChainTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void hello_withoutJwt_shouldReturn200() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello! Welcome to Campus Connect!"));
    }

    @Test
    void register_withoutJwt_shouldNotBeBlockedBySecurity() throws Exception {
        String uniqueEmail = "sec_reg_" + System.currentTimeMillis() + "@example.com";
        String payload = "{\"fullName\":\"Security Register Test\",\"email\":\"" + uniqueEmail + "\",\"password\":\"Secret123!\"}";

        mockMvc.perform(post("/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    void login_withoutJwt_shouldNotBeBlockedBySecurity() throws Exception {
        String uniqueEmail = "sec_login_" + System.currentTimeMillis() + "@example.com";
        String regPayload = "{\"fullName\":\"Security Login Test\",\"email\":\"" + uniqueEmail + "\",\"password\":\"Secret123!\"}";

        mockMvc.perform(post("/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regPayload))
                .andExpect(status().isCreated());

        String loginPayload = "{\"email\":\"" + uniqueEmail + "\",\"password\":\"Secret123!\"}";

        MvcResult loginResult = mockMvc.perform(post("/students/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        Matcher tokenMatcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(loginResponse);
        if (!tokenMatcher.find()) {
            throw new IllegalStateException("Token not found in login response: " + loginResponse);
        }
        String token = tokenMatcher.group(1);
        org.junit.jupiter.api.Assertions.assertEquals("STUDENT", jwtUtil.extractRole(token));

        mockMvc.perform(get("/students")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getAllStudents_withoutJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStudentById_withoutJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/students/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putStudent_withoutJwt_shouldReturn401() throws Exception {
        String payload = "{\"fullName\":\"Update Test\",\"email\":\"update@example.com\",\"password\":\"Secret123!\"}";

        mockMvc.perform(put("/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteStudent_withoutJwt_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/students/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllStudents_withValidJwt_shouldBeAccessible() throws Exception {
        String token = jwtUtil.generateToken("student@example.com", 1L);

        mockMvc.perform(get("/students")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getStudentById_withValidJwt_shouldBeAccessible() throws Exception {
        String uniqueEmail = "sec_get_" + System.currentTimeMillis() + "@example.com";
        String regPayload = "{\"fullName\":\"Get Student Test\",\"email\":\"" + uniqueEmail + "\",\"password\":\"Secret123!\"}";

        MvcResult result = mockMvc.perform(post("/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regPayload))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(responseJson);
        if (!matcher.find()) {
            throw new IllegalStateException("ID not found in registration response: " + responseJson);
        }
        long id = Long.parseLong(matcher.group(1));

        String token = jwtUtil.generateToken(uniqueEmail, id);

        mockMvc.perform(get("/students/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getAllStudents_withInvalidJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/students")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStudentById_withInvalidJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/students/1")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withExpiredJwt_shouldReturn401() throws Exception {
        String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date past = new Date(System.currentTimeMillis() - 60000);

        String expiredToken = Jwts.builder()
                .setSubject("expired@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 120000))
                .setExpiration(past)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/students")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withNonStudentRole_shouldReturn403() throws Exception {
        String token = jwtUtil.generateToken("recruiter@example.com", 999L, "RECRUITER");

        mockMvc.perform(get("/students")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStudentById_withNonStudentRole_shouldReturn403() throws Exception {
        String token = jwtUtil.generateToken("recruiter@example.com", 999L, "RECRUITER");

        mockMvc.perform(get("/students/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void putStudent_withValidStudentJwt_shouldBeAccessible() throws Exception {
        String uniqueEmail = "sec_put_" + System.currentTimeMillis() + "@example.com";
        String regPayload = "{\"fullName\":\"Put Student Test\",\"email\":\"" + uniqueEmail + "\",\"password\":\"Secret123!\"}";

        MvcResult result = mockMvc.perform(post("/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regPayload))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(responseJson);
        if (!matcher.find()) {
            throw new IllegalStateException("ID not found in registration response: " + responseJson);
        }
        long id = Long.parseLong(matcher.group(1));
        String token = jwtUtil.generateToken(uniqueEmail, id, "STUDENT");

        String updatePayload = "{\"fullName\":\"Updated Put Name\",\"email\":\"" + uniqueEmail + "\",\"password\":\"Secret123!\"}";

        mockMvc.perform(put("/students/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk());
    }

    @Test
    void deleteStudent_withValidStudentJwt_shouldBeAccessible() throws Exception {
        String uniqueEmail = "sec_del_" + System.currentTimeMillis() + "@example.com";
        String regPayload = "{\"fullName\":\"Delete Student Test\",\"email\":\"" + uniqueEmail + "\",\"password\":\"Secret123!\"}";

        MvcResult result = mockMvc.perform(post("/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regPayload))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(responseJson);
        if (!matcher.find()) {
            throw new IllegalStateException("ID not found in registration response: " + responseJson);
        }
        long id = Long.parseLong(matcher.group(1));
        String token = jwtUtil.generateToken(uniqueEmail, id, "STUDENT");

        mockMvc.perform(delete("/students/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void protectedEndpoint_withValidRecruiterJwt_shouldReturn403ForStudentEndpoint() throws Exception {
        String token = jwtUtil.generateRecruiterToken("recruiter@example.com", 555L);

        mockMvc.perform(get("/students")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withUnrecognizedRoleJwt_shouldReturn401() throws Exception {
        String token = jwtUtil.generateToken("unknown@example.com", 888L, "ANONYMOUS_ROLE");

        mockMvc.perform(get("/students")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withInvalidRecruiterJwt_shouldReturn401() throws Exception {
        String validToken = jwtUtil.generateRecruiterToken("recruiter@example.com", 555L);
        String invalidToken = validToken.substring(0, validToken.length() - 6) + "xxxxxx";

        mockMvc.perform(get("/students")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cors_preflightOptionsRequest_fromAllowedOrigin_shouldReturn200WithCorsHeaders() throws Exception {
        mockMvc.perform(options("/students/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void cors_preflightOptionsRequest_forRecruiterLogin_fromAllowedOrigin_shouldReturn200() throws Exception {
        mockMvc.perform(options("/recruiters/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void cors_actualPostRequest_fromAllowedOrigin_shouldIncludeCorsHeader() throws Exception {
        mockMvc.perform(post("/students/login")
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nonexistent@example.com\",\"password\":\"WrongPassword123\"}"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void cors_requestFromUnauthorizedOrigin_shouldNotIncludeAllowOriginHeader() throws Exception {
        mockMvc.perform(options("/students/login")
                        .header("Origin", "http://malicious-origin.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
