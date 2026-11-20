package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class CompanyControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String validToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        validToken = jwtUtil.generateToken("student@example.com", 1L, "STUDENT");
    }

    @Test
    void testCreateAndGetCompanyEndpoints() throws Exception {
        String payload = "{\"name\":\"Tech Innovators\",\"industry\":\"Software\",\"website\":\"https://techinno.com\",\"contactEmail\":\"info@techinno.com\",\"contactPhone\":\"9876543210\",\"location\":\"Mumbai\",\"description\":\"Innovative software company\"}";

        mockMvc.perform(post("/companies")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tech Innovators"));

        mockMvc.perform(get("/companies")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateAndDeleteCompanyEndpoints() throws Exception {
        String payload = "{\"name\":\"Startup Inc\",\"industry\":\"Fintech\",\"website\":\"https://startup.com\",\"contactEmail\":\"hr@startup.com\",\"contactPhone\":\"9876543210\",\"location\":\"Delhi\",\"description\":\"Fintech startup\"}";

        MvcResult result = mockMvc.perform(post("/companies")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(responseJson);
        if (!matcher.find()) {
            throw new IllegalStateException("ID not found in response: " + responseJson);
        }
        long id = Long.parseLong(matcher.group(1));

        String updatePayload = "{\"name\":\"Startup Corp\",\"industry\":\"Fintech\",\"website\":\"https://startupcorp.com\",\"contactEmail\":\"hr@startupcorp.com\",\"contactPhone\":\"9876543210\",\"location\":\"Noida\",\"description\":\"Fintech enterprise\"}";

        mockMvc.perform(put("/companies/" + id)
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Startup Corp"))
                .andExpect(jsonPath("$.location").value("Noida"));

        mockMvc.perform(get("/companies/" + id)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Startup Corp"));

        mockMvc.perform(delete("/companies/" + id)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/companies/" + id)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCompanyById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/companies/999999")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testCompanyResponseDto_AllFieldsPresent_AndNoEntityCollectionsOrPasswords() throws Exception {
        String payload = "{" +
                "\"name\":\"Acme Global\"," +
                "\"industry\":\"Logistics\"," +
                "\"website\":\"https://acme.example.com\"," +
                "\"contactEmail\":\"contact@acme.example.com\"," +
                "\"contactPhone\":\"9876543210\"," +
                "\"location\":\"Chennai\"," +
                "\"description\":\"Global logistics solutions\"" +
                "}";

        MvcResult result = mockMvc.perform(post("/companies")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Acme Global"))
                .andExpect(jsonPath("$.industry").value("Logistics"))
                .andExpect(jsonPath("$.website").value("https://acme.example.com"))
                .andExpect(jsonPath("$.contactEmail").value("contact@acme.example.com"))
                .andExpect(jsonPath("$.contactPhone").value("9876543210"))
                .andExpect(jsonPath("$.location").value("Chennai"))
                .andExpect(jsonPath("$.description").value("Global logistics solutions"))
                .andExpect(jsonPath("$.recruiters").doesNotExist())
                .andExpect(jsonPath("$.placementDrives").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(responseJson);
        assertTrue(matcher.find());
        long id = Long.parseLong(matcher.group(1));

        mockMvc.perform(get("/companies/" + id)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Acme Global"))
                .andExpect(jsonPath("$.industry").value("Logistics"))
                .andExpect(jsonPath("$.website").value("https://acme.example.com"))
                .andExpect(jsonPath("$.contactEmail").value("contact@acme.example.com"))
                .andExpect(jsonPath("$.contactPhone").value("9876543210"))
                .andExpect(jsonPath("$.location").value("Chennai"))
                .andExpect(jsonPath("$.description").value("Global logistics solutions"))
                .andExpect(jsonPath("$.recruiters").doesNotExist())
                .andExpect(jsonPath("$.placementDrives").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
