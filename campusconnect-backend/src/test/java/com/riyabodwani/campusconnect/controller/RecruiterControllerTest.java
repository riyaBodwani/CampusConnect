package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.Recruiter;
import com.riyabodwani.campusconnect.repository.ApplicationRepository;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.repository.RecruiterRepository;
import com.riyabodwani.campusconnect.repository.StudentRepository;
import com.riyabodwani.campusconnect.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class RecruiterControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private Company savedCompany;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        cleanUp();

        Company company = new Company("Stark Enterprises", "Technology", "https://stark.example.com", "hr@stark.example.com", "9876543210", "Mumbai", "Defense and Tech");
        savedCompany = companyRepository.save(company);
    }

    @org.junit.jupiter.api.AfterEach
    void cleanUp() {
        applicationRepository.deleteAll();
        placementDriveRepository.deleteAll();
        recruiterRepository.deleteAll();
        companyRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    void test1_SuccessfulRegistration_Returns201_And_Test3_PasswordNotReturnedInResponse() throws Exception {
        String payload = "{" +
                "\"fullName\":\"Pepper Potts\"," +
                "\"email\":\"pepper@stark.example.com\"," +
                "\"password\":\"StarkSecret123\"," +
                "\"phoneNumber\":\"9876543210\"," +
                "\"companyId\":" + savedCompany.getId() +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("Pepper Potts"))
                .andExpect(jsonPath("$.email").value("pepper@stark.example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$.companyName").value("Stark Enterprises"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void test2_PasswordIsBcryptHashedBeforePersistence() throws Exception {
        String rawPassword = "SecretPlainPassword!";
        String payload = "{" +
                "\"fullName\":\"Tony Stark\"," +
                "\"email\":\"tony@stark.example.com\"," +
                "\"password\":\"" + rawPassword + "\"," +
                "\"phoneNumber\":\"9876543210\"," +
                "\"companyId\":" + savedCompany.getId() +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        Recruiter recruiter = recruiterRepository.findByEmail("tony@stark.example.com").orElseThrow();
        assertNotEquals(rawPassword, recruiter.getPassword(), "Stored password must not be plain text");
        assertTrue(passwordEncoder.matches(rawPassword, recruiter.getPassword()), "Stored password must be a valid BCrypt hash matching the raw password");
    }

    @Test
    void test4_DuplicateRecruiterEmail_Returns400() throws Exception {
        String payload1 = "{" +
                "\"fullName\":\"Happy Hogan\"," +
                "\"email\":\"happy@stark.example.com\"," +
                "\"password\":\"HappyPass123\"," +
                "\"phoneNumber\":\"9876543210\"," +
                "\"companyId\":" + savedCompany.getId() +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload1))
                .andExpect(status().isCreated());

        // Duplicate registration attempt with same email
        String payload2 = "{" +
                "\"fullName\":\"Happy Duplicate\"," +
                "\"email\":\"happy@stark.example.com\"," +
                "\"password\":\"DifferentPass123\"," +
                "\"phoneNumber\":\"9876543211\"," +
                "\"companyId\":" + savedCompany.getId() +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email 'happy@stark.example.com' is already registered"));
    }

    @Test
    void test5_NonExistingCompany_Returns404() throws Exception {
        String payload = "{" +
                "\"fullName\":\"Wong\"," +
                "\"email\":\"wong@nowhere.example.com\"," +
                "\"password\":\"KamarTaj123\"," +
                "\"phoneNumber\":\"9876543210\"," +
                "\"companyId\":999999" +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test6_ValidationFailure_Returns400() throws Exception {
        // Missing required fields and invalid email format
        String invalidPayload = "{" +
                "\"fullName\":\"\"," +
                "\"email\":\"not-an-email\"," +
                "\"password\":\"\"," +
                "\"phoneNumber\":\"123\"," +
                "\"companyId\":null" +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void test7_SuccessfulRecruiterAssociatedWithCorrectCompany() throws Exception {
        Company otherCompany = new Company("Wayne Enterprises", "Finance", "https://wayne.example.com", "hr@wayne.example.com", "9876543211", "Gotham", "Holding company");
        Company savedOtherCompany = companyRepository.save(otherCompany);

        String payload = "{" +
                "\"fullName\":\"Lucius Fox\"," +
                "\"email\":\"lucius@wayne.example.com\"," +
                "\"password\":\"TechMaster123\"," +
                "\"phoneNumber\":\"9876543210\"," +
                "\"companyId\":" + savedOtherCompany.getId() +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(savedOtherCompany.getId()))
                .andExpect(jsonPath("$.companyName").value("Wayne Enterprises"));

        Recruiter recruiter = recruiterRepository.findByEmail("lucius@wayne.example.com").orElseThrow();
        assertEquals(savedOtherCompany.getId(), recruiter.getCompany().getId());
        assertEquals("Wayne Enterprises", recruiter.getCompany().getName());
    }

    @Test
    void test8_RecruiterLogin_Success_Returns200AndValidTokenWithClaimsAndNoPassword() throws Exception {
        String registerPayload = "{" +
                "\"fullName\":\"James Rhodes\"," +
                "\"email\":\"rhodey@stark.example.com\"," +
                "\"password\":\"WarMachine123!\"," +
                "\"phoneNumber\":\"9876543210\"," +
                "\"companyId\":" + savedCompany.getId() +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        String loginPayload = "{" +
                "\"email\":\"rhodey@stark.example.com\"," +
                "\"password\":\"WarMachine123!\"" +
                "}";

        MvcResult result = mockMvc.perform(post("/recruiters/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.fullName").value("James Rhodes"))
                .andExpect(jsonPath("$.email").value("rhodey@stark.example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$.companyName").value("Stark Enterprises"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("rhodey@stark.example.com", jwtUtil.extractEmail(token));
        assertEquals("RECRUITER", jwtUtil.extractRole(token));
        assertNotNull(jwtUtil.extractRecruiterId(token));
        assertNotNull(jwtUtil.extractClaims(token).getIssuedAt());
        assertNotNull(jwtUtil.extractClaims(token).getExpiration());
    }

    @Test
    void test9_RecruiterLogin_WrongPassword_Returns401WithGenericMessage() throws Exception {
        String registerPayload = "{" +
                "\"fullName\":\"Natasha Romanoff\"," +
                "\"email\":\"natasha@stark.example.com\"," +
                "\"password\":\"WidowPass123!\"," +
                "\"phoneNumber\":\"9876543210\"," +
                "\"companyId\":" + savedCompany.getId() +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        String loginPayload = "{" +
                "\"email\":\"natasha@stark.example.com\"," +
                "\"password\":\"WrongPassword!\"" +
                "}";

        mockMvc.perform(post("/recruiters/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void test10_RecruiterLogin_NonExistentEmail_Returns401WithSameGenericMessage() throws Exception {
        String loginPayload = "{" +
                "\"email\":\"nonexistent@stark.example.com\"," +
                "\"password\":\"SomePassword123!\"" +
                "}";

        mockMvc.perform(post("/recruiters/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void test11_RecruiterLogin_WrongEmailAndWrongPasswordProduceSameGenericMessage() throws Exception {
        // Register recruiter
        String registerPayload = "{" +
                "\"fullName\":\"Sam Wilson\"," +
                "\"email\":\"sam@stark.example.com\"," +
                "\"password\":\"FalconPass123!\"," +
                "\"phoneNumber\":\"9876543210\"," +
                "\"companyId\":" + savedCompany.getId() +
                "}";

        mockMvc.perform(post("/recruiters/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        // 1. Existing email, wrong password
        MvcResult wrongPasswordResult = mockMvc.perform(post("/recruiters/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"sam@stark.example.com\",\"password\":\"WrongPassword\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // 2. Non-existent email
        MvcResult wrongEmailResult = mockMvc.perform(post("/recruiters/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nonexistent@stark.example.com\",\"password\":\"AnyPassword\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String wrongPasswordMsg = com.jayway.jsonpath.JsonPath.read(wrongPasswordResult.getResponse().getContentAsString(), "$.message");
        String wrongEmailMsg = com.jayway.jsonpath.JsonPath.read(wrongEmailResult.getResponse().getContentAsString(), "$.message");

        assertEquals("Invalid email or password", wrongPasswordMsg);
        assertEquals(wrongPasswordMsg, wrongEmailMsg, "Both authentication failures must return identical messages");
    }

    @Test
    void test12_StudentLogin_StillWorks_AndContainsStudentRole() throws Exception {
        // Register student directly via repository with encoded password
        com.riyabodwani.campusconnect.entity.Student student = new com.riyabodwani.campusconnect.entity.Student();
        student.setFullName("Peter Parker");
        student.setEmail("peter@student.example.com");
        student.setPassword(passwordEncoder.encode("SpideyPass123!"));
        student = studentRepository.save(student);

        String loginPayload = "{" +
                "\"email\":\"peter@student.example.com\"," +
                "\"password\":\"SpideyPass123!\"" +
                "}";

        MvcResult result = mockMvc.perform(post("/students/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.student.email").value("peter@student.example.com"))
                .andReturn();

        String studentToken = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        assertTrue(jwtUtil.validateToken(studentToken));
        assertEquals("STUDENT", jwtUtil.extractRole(studentToken));
        assertEquals("peter@student.example.com", jwtUtil.extractEmail(studentToken));
        assertEquals(student.getId(), jwtUtil.extractStudentId(studentToken));
        assertNull(jwtUtil.extractRecruiterId(studentToken));
    }
}
