package com.riyabodwani.campusconnect.controller;


import com.riyabodwani.campusconnect.entity.Application;
import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.entity.Recruiter;
import com.riyabodwani.campusconnect.entity.Student;
import com.riyabodwani.campusconnect.repository.ApplicationRepository;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.repository.RecruiterRepository;
import com.riyabodwani.campusconnect.repository.StudentRepository;
import com.riyabodwani.campusconnect.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class RecruiterPlacementDriveControllerTest {

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
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    private Company companyA;
    private Company companyB;

    private Recruiter recruiterA;
    private Recruiter recruiterB;

    private Student student;

    private String recruiterAToken;
    private String recruiterBToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        // Create companies
        companyA = companyRepository.save(new Company("TechCorp India", "Software", "https://techcorp.in", "hr@techcorp.in", "9876543210", "Pune", "Enterprise Tech Solutions"));
        companyB = companyRepository.save(new Company("FinanceFirst Ltd", "Finance", "https://finfirst.in", "hr@finfirst.in", "9876543211", "Mumbai", "Financial Services"));

        // Create recruiters
        recruiterA = recruiterRepository.save(new Recruiter("Alice Walker", "alice.walker@techcorp.in", passwordEncoder.encode("RecruiterPass123!"), "9988776655", companyA));
        recruiterB = recruiterRepository.save(new Recruiter("Bob Vance", "bob.vance@finfirst.in", passwordEncoder.encode("RecruiterPass456!"), "9988776656", companyB));

        // Create student
        student = new Student();
        student.setFullName("Rahul Sharma");
        student.setEmail("rahul.sharma@college.edu");
        student.setPassword(passwordEncoder.encode("StudentPass789!"));
        student.setPhoneNumber("9123456780");
        student.setCourse("B.Tech");
        student.setBranch("CSE");
        student.setCgpa(8.5);
        student.setDateOfBirth(LocalDate.of(2003, 3, 10));
        student.setAddress("Pune");
        student.setPlacementStatus("UNPLACED");
        student = studentRepository.save(student);

        // Generate tokens
        recruiterAToken = jwtUtil.generateRecruiterToken(recruiterA.getEmail(), recruiterA.getId());
        recruiterBToken = jwtUtil.generateRecruiterToken(recruiterB.getEmail(), recruiterB.getId());
        studentToken = jwtUtil.generateToken(student.getEmail(), student.getId(), "STUDENT");
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        applicationRepository.deleteAll();
        placementDriveRepository.deleteAll();
        recruiterRepository.deleteAll();
        companyRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    void test1_RecruiterCreatesPlacementDrive_Success_Returns201() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Java Backend Developer\"," +
                "\"jobDescription\": \"Backend development role with Spring Boot\"," +
                "\"location\": \"Pune\"," +
                "\"minimumCgpa\": 7.0," +
                "\"eligibilityCriteria\": \"B.Tech / B.E CSE / IT\"," +
                "\"salary\": 600000.0," +
                "\"applicationDeadline\": \"2026-10-15\"," +
                "\"status\": \"OPEN\"" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.jobTitle").value("Java Backend Developer"))
                .andExpect(jsonPath("$.jobDescription").value("Backend development role with Spring Boot"))
                .andExpect(jsonPath("$.location").value("Pune"))
                .andExpect(jsonPath("$.minimumCgpa").value(7.0))
                .andExpect(jsonPath("$.eligibilityCriteria").value("B.Tech / B.E CSE / IT"))
                .andExpect(jsonPath("$.salary").value(600000.0))
                .andExpect(jsonPath("$.applicationDeadline").value("2026-10-15"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.companyName").value("TechCorp India"))
                .andExpect(jsonPath("$.company").doesNotExist());
    }

    @Test
    void test2_CreatedDrive_AutomaticallyAssignedToRecruiterCompany() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Cloud Engineer\"," +
                "\"jobDescription\": \"AWS & DevOps engineer\"," +
                "\"location\": \"Bangalore\"," +
                "\"minimumCgpa\": 6.5," +
                "\"eligibilityCriteria\": \"Engineering graduates\"," +
                "\"salary\": 750000.0," +
                "\"applicationDeadline\": \"2026-11-20\"," +
                "\"status\": \"OPEN\"" +
                "}";

        MvcResult result = mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        Number createdIdNum = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        assertNotNull(createdIdNum);
        Long createdId = createdIdNum.longValue();

        // Verify in database
        PlacementDrive savedInDb = placementDriveRepository.findById(createdId).orElse(null);
        assertNotNull(savedInDb);
        assertEquals(companyA.getId(), savedInDb.getCompany().getId());
        assertEquals("TechCorp India", savedInDb.getCompany().getName());
    }

    @Test
    void test3_RecruiterCannotChooseAnotherCompany_AssignedToRecruiterCompany() throws Exception {
        // Attempt to supply companyB's ID in request
        String payload = "{" +
                "\"jobTitle\": \"Security Specialist\"," +
                "\"jobDescription\": \"Infosec & AppSec\"," +
                "\"location\": \"Pune\"," +
                "\"minimumCgpa\": 7.5," +
                "\"eligibilityCriteria\": \"All branches\"," +
                "\"salary\": 850000.0," +
                "\"applicationDeadline\": \"2026-12-01\"," +
                "\"companyId\": " + companyB.getId() + "," +
                "\"company\": {\"id\": " + companyB.getId() + "}" +
                "}";

        MvcResult result = mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.company").doesNotExist())
                .andReturn();

        Number createdIdNum = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        Long createdId = createdIdNum.longValue();
        PlacementDrive savedInDb = placementDriveRepository.findById(createdId).orElse(null);
        assertNotNull(savedInDb);
        assertEquals(companyA.getId(), savedInDb.getCompany().getId(), "Must be assigned to Recruiter A's company, not company B");
        assertNotEquals(companyB.getId(), savedInDb.getCompany().getId());
    }

    @Test
    void test4_StudentJwt_Returns403Forbidden() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Frontend Engineer\"," +
                "\"applicationDeadline\": \"2026-10-15\"" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void test5_NoJwt_Returns401Unauthorized() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Frontend Engineer\"," +
                "\"applicationDeadline\": \"2026-10-15\"" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test6_InvalidJwt_Returns401Unauthorized() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Frontend Engineer\"," +
                "\"applicationDeadline\": \"2026-10-15\"" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer invalid.token.value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test7a_ValidationFailure_MissingJobTitle_Returns400() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"   \"," +
                "\"applicationDeadline\": \"2026-10-15\"" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.jobTitle").exists());
    }

    @Test
    void test7b_ValidationFailure_MissingApplicationDeadline_Returns400() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Data Scientist\"" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.applicationDeadline").exists());
    }

    @Test
    void test7c_ValidationFailure_NegativeSalary_Returns400() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Data Scientist\"," +
                "\"applicationDeadline\": \"2026-10-15\"," +
                "\"salary\": -1000.0" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.salary").exists());
    }

    @Test
    void test7d_ValidationFailure_NegativeCgpa_Returns400() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Data Scientist\"," +
                "\"applicationDeadline\": \"2026-10-15\"," +
                "\"minimumCgpa\": -0.5" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.minimumCgpa").exists());
    }

    @Test
    void test7e_ValidationFailure_CgpaGreaterThan10_Returns400() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Data Scientist\"," +
                "\"applicationDeadline\": \"2026-10-15\"," +
                "\"minimumCgpa\": 10.5" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.minimumCgpa").exists());
    }

    @Test
    void test8_ResponseContainsExpectedPlacementDriveFields() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Full Stack Developer\"," +
                "\"jobDescription\": \"Full stack application development\"," +
                "\"location\": \"Pune\"," +
                "\"minimumCgpa\": 7.2," +
                "\"eligibilityCriteria\": \"B.Tech / MCA\"," +
                "\"salary\": 900000.0," +
                "\"applicationDeadline\": \"2026-11-01\"," +
                "\"status\": \"OPEN\"" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.jobTitle").value("Full Stack Developer"))
                .andExpect(jsonPath("$.jobDescription").value("Full stack application development"))
                .andExpect(jsonPath("$.location").value("Pune"))
                .andExpect(jsonPath("$.minimumCgpa").value(7.2))
                .andExpect(jsonPath("$.eligibilityCriteria").value("B.Tech / MCA"))
                .andExpect(jsonPath("$.salary").value(900000.0))
                .andExpect(jsonPath("$.applicationDeadline").value("2026-11-01"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.companyName").value("TechCorp India"))
                .andExpect(jsonPath("$.company").doesNotExist());
    }

    @Test
    void test9_ResponseDoesNotExposePasswords() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"DevOps Specialist\"," +
                "\"jobDescription\": \"Kubernetes and Docker\"," +
                "\"location\": \"Pune\"," +
                "\"minimumCgpa\": 7.0," +
                "\"eligibilityCriteria\": \"Engineering\"," +
                "\"salary\": 800000.0," +
                "\"applicationDeadline\": \"2026-10-30\"," +
                "\"status\": \"OPEN\"" +
                "}";

        MvcResult result = mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.company").doesNotExist())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.toLowerCase().contains("password"), "Response should not contain any password field");
        assertFalse(responseContent.contains("RecruiterPass123!"), "Response should not contain recruiter password");
        assertFalse(responseContent.contains("StudentPass789!"), "Response should not contain student password");
    }

    @Test
    void test10_PluralRoute_RecruitersPlacementDrives_AlsoAccessible() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Site Reliability Engineer\"," +
                "\"applicationDeadline\": \"2026-11-15\"" +
                "}";

        mockMvc.perform(post("/recruiters/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobTitle").value("Site Reliability Engineer"))
                .andExpect(jsonPath("$.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.company").doesNotExist());
    }

    @Test
    void test11_RecruiterNotFound_Returns404NotFound() throws Exception {
        String ghostToken = jwtUtil.generateRecruiterToken("nonexistent.recruiter@unknown.com", 999999L);

        String payload = "{" +
                "\"jobTitle\": \"Software Engineer\"," +
                "\"applicationDeadline\": \"2026-11-15\"" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + ghostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test12_RecruiterSeesTheirCompanyPlacementDrives() throws Exception {
        PlacementDrive driveA1 = placementDriveRepository.save(new PlacementDrive(
                "Backend Engineer", "Java & Spring", "Pune", 7.0, "CS/IT", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));
        PlacementDrive driveA2 = placementDriveRepository.save(new PlacementDrive(
                "Frontend Engineer", "React & TypeScript", "Pune", 6.5, "Any", 550000.0, LocalDate.now().plusMonths(2), "OPEN", companyA
        ));
        placementDriveRepository.save(new PlacementDrive(
                "Financial Analyst", "Finance role", "Mumbai", 7.5, "Finance", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyB
        ));

        mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].companyId").value(companyA.getId()))
                .andExpect(jsonPath("$[1].companyId").value(companyA.getId()))
                .andExpect(jsonPath("$[0].company").doesNotExist());
    }

    @Test
    void test13_RecruiterDoesNotSeeAnotherCompanyDrives() throws Exception {
        PlacementDrive driveA = placementDriveRepository.save(new PlacementDrive(
                "Java Dev", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));
        PlacementDrive driveB = placementDriveRepository.save(new PlacementDrive(
                "Finance Dev", "Desc", "Mumbai", 7.5, "Finance", 800000.0, LocalDate.now().plusMonths(1), "OPEN", companyB
        ));

        // Recruiter A only sees Company A's drive
        mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(driveA.getId()))
                .andExpect(jsonPath("$[0].companyId").value(companyA.getId()))
                .andExpect(jsonPath("$[0].companyName").value("TechCorp India"))
                .andExpect(jsonPath("$[0].company").doesNotExist());

        // Recruiter B only sees Company B's drive
        mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(driveB.getId()))
                .andExpect(jsonPath("$[0].companyId").value(companyB.getId()))
                .andExpect(jsonPath("$[0].companyName").value("FinanceFirst Ltd"))
                .andExpect(jsonPath("$[0].company").doesNotExist());
    }

    @Test
    void test14_RecruiterWithNoDrives_ReturnsEmptyList() throws Exception {
        // Company B has drives, but Company A has none
        placementDriveRepository.save(new PlacementDrive(
                "Finance Dev", "Desc", "Mumbai", 7.5, "Finance", 800000.0, LocalDate.now().plusMonths(1), "OPEN", companyB
        ));

        mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void test15_GetPlacementDrives_StudentJwt_Returns403Forbidden() throws Exception {
        mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void test16_GetPlacementDrives_NoJwt_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/recruiter/placement-drives"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test17_GetPlacementDrives_InvalidJwt_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test18_GetPlacementDrives_ResponseContainsExpectedFields() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Platform Architect", "Design cloud platform", "Pune", 8.0,
                "B.Tech / M.Tech", 1500000.0, LocalDate.of(2026, 12, 31), "OPEN", companyA
        ));

        mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(drive.getId()))
                .andExpect(jsonPath("$[0].jobTitle").value("Platform Architect"))
                .andExpect(jsonPath("$[0].jobDescription").value("Design cloud platform"))
                .andExpect(jsonPath("$[0].location").value("Pune"))
                .andExpect(jsonPath("$[0].minimumCgpa").value(8.0))
                .andExpect(jsonPath("$[0].eligibilityCriteria").value("B.Tech / M.Tech"))
                .andExpect(jsonPath("$[0].salary").value(1500000.0))
                .andExpect(jsonPath("$[0].applicationDeadline").value("2026-12-31"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].companyId").value(companyA.getId()))
                .andExpect(jsonPath("$[0].companyName").value("TechCorp India"))
                .andExpect(jsonPath("$[0].company").doesNotExist());
    }

    @Test
    void test19_GetPlacementDrives_ResponseDoesNotExposePasswords() throws Exception {
        placementDriveRepository.save(new PlacementDrive(
                "Platform Architect", "Design cloud platform", "Pune", 8.0,
                "B.Tech / M.Tech", 1500000.0, LocalDate.of(2026, 12, 31), "OPEN", companyA
        ));

        MvcResult result = mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].company").doesNotExist())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.toLowerCase().contains("password"), "Response should not contain password field");
        assertFalse(responseContent.contains("RecruiterPass123!"), "Response should not contain recruiter password");
        assertFalse(responseContent.contains("StudentPass789!"), "Response should not contain student password");
    }

    @Test
    void test20_GetPlacementDrives_PluralRoute_AlsoAccessible() throws Exception {
        placementDriveRepository.save(new PlacementDrive(
                "DevOps Engineer", "CI/CD pipelines", "Pune", 7.0,
                "Any", 800000.0, LocalDate.of(2026, 11, 30), "OPEN", companyA
        ));

        mockMvc.perform(get("/recruiters/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].jobTitle").value("DevOps Engineer"))
                .andExpect(jsonPath("$[0].companyId").value(companyA.getId()))
                .andExpect(jsonPath("$[0].company").doesNotExist());
    }

    @Test
    void test21_GetPlacementDrives_RecruiterNotFound_Returns404NotFound() throws Exception {
        String ghostToken = jwtUtil.generateRecruiterToken("nonexistent.recruiter@unknown.com", 999999L);

        mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + ghostToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test22_RecruiterSuccessfullyUpdatesTheirOwnDrive() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Initial Desc", "Pune", 6.5, "CS", 500000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Senior Java Developer\"," +
                "\"jobDescription\": \"Updated microservices role\"," +
                "\"location\": \"Mumbai\"," +
                "\"minimumCgpa\": 7.5," +
                "\"eligibilityCriteria\": \"CS/IT graduates with 7.5+ CGPA\"," +
                "\"salary\": 950000.0," +
                "\"applicationDeadline\": \"2026-12-15\"," +
                "\"status\": \"OPEN\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(drive.getId()))
                .andExpect(jsonPath("$.jobTitle").value("Senior Java Developer"))
                .andExpect(jsonPath("$.jobDescription").value("Updated microservices role"))
                .andExpect(jsonPath("$.location").value("Mumbai"))
                .andExpect(jsonPath("$.minimumCgpa").value(7.5))
                .andExpect(jsonPath("$.eligibilityCriteria").value("CS/IT graduates with 7.5+ CGPA"))
                .andExpect(jsonPath("$.salary").value(950000.0))
                .andExpect(jsonPath("$.applicationDeadline").value("2026-12-15"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.company").doesNotExist());

        PlacementDrive inDb = placementDriveRepository.findById(drive.getId()).orElse(null);
        assertNotNull(inDb);
        assertEquals("Senior Java Developer", inDb.getJobTitle());
        assertEquals("Mumbai", inDb.getLocation());
        assertEquals(950000.0, inDb.getSalary());
        assertEquals(companyA.getId(), inDb.getCompany().getId());
    }

    @Test
    void test23_RecruiterCannotUpdateAnotherCompanyDrive_Returns403() throws Exception {
        PlacementDrive driveB = placementDriveRepository.save(new PlacementDrive(
                "Company B Job", "Desc", "Mumbai", 7.0, "Finance", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyB
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Malicious Update\"," +
                "\"applicationDeadline\": \"2026-12-15\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + driveB.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        PlacementDrive inDb = placementDriveRepository.findById(driveB.getId()).orElse(null);
        assertNotNull(inDb);
        assertEquals("Company B Job", inDb.getJobTitle());
        assertEquals(companyB.getId(), inDb.getCompany().getId());
    }

    @Test
    void test24_UpdatePlacementDrive_MissingDrive_Returns404() throws Exception {
        String updatePayload = "{" +
                "\"jobTitle\": \"Non-existent Drive\"," +
                "\"applicationDeadline\": \"2026-12-15\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/999999")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test25_UpdatePlacementDrive_MissingJwt_Returns401() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "DevOps", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Updated DevOps\"," +
                "\"applicationDeadline\": \"2026-12-15\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test26_UpdatePlacementDrive_InvalidJwt_Returns401() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "DevOps", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Updated DevOps\"," +
                "\"applicationDeadline\": \"2026-12-15\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer invalid.token.value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test27_UpdatePlacementDrive_StudentJwt_Returns403() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "DevOps", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Updated DevOps\"," +
                "\"applicationDeadline\": \"2026-12-15\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isForbidden());
    }

    @Test
    void test28a_UpdatePlacementDrive_ValidationFailure_BlankJobTitle_Returns400() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"   \"," +
                "\"applicationDeadline\": \"2026-12-15\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.jobTitle").exists());
    }

    @Test
    void test28b_UpdatePlacementDrive_ValidationFailure_NullDeadline_Returns400() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Valid Title\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.applicationDeadline").exists());
    }

    @Test
    void test28c_UpdatePlacementDrive_ValidationFailure_NegativeSalary_Returns400() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Valid Title\"," +
                "\"applicationDeadline\": \"2026-12-15\"," +
                "\"salary\": -500.0" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.salary").exists());
    }

    @Test
    void test28d_UpdatePlacementDrive_ValidationFailure_InvalidCgpa_Returns400() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Valid Title\"," +
                "\"applicationDeadline\": \"2026-12-15\"," +
                "\"minimumCgpa\": 10.5" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.minimumCgpa").exists());
    }

    @Test
    void test29_CompanyCannotBeChangedOrSpoofed() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Updated Title\"," +
                "\"applicationDeadline\": \"2026-12-15\"," +
                "\"companyId\": " + companyB.getId() + "," +
                "\"company\": {\"id\": " + companyB.getId() + "}" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.company").doesNotExist());

        PlacementDrive inDb = placementDriveRepository.findById(drive.getId()).orElse(null);
        assertNotNull(inDb);
        assertEquals(companyA.getId(), inDb.getCompany().getId(), "Company must remain unchanged");
    }

    @Test
    void test30_UpdatedResponseContainsExpectedFields() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Initial Desc", "Pune", 6.0, "CS", 500000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Lead Architect\"," +
                "\"jobDescription\": \"Architecture role\"," +
                "\"location\": \"Pune\"," +
                "\"minimumCgpa\": 8.5," +
                "\"eligibilityCriteria\": \"Senior graduates\"," +
                "\"salary\": 1800000.0," +
                "\"applicationDeadline\": \"2026-12-31\"," +
                "\"status\": \"OPEN\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(drive.getId()))
                .andExpect(jsonPath("$.jobTitle").value("Lead Architect"))
                .andExpect(jsonPath("$.jobDescription").value("Architecture role"))
                .andExpect(jsonPath("$.location").value("Pune"))
                .andExpect(jsonPath("$.minimumCgpa").value(8.5))
                .andExpect(jsonPath("$.eligibilityCriteria").value("Senior graduates"))
                .andExpect(jsonPath("$.salary").value(1800000.0))
                .andExpect(jsonPath("$.applicationDeadline").value("2026-12-31"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.companyName").value("TechCorp India"))
                .andExpect(jsonPath("$.company").doesNotExist());
    }

    @Test
    void test31_UpdatePlacementDrive_ResponseDoesNotExposePasswords() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Desc", "Pune", 6.0, "CS", 500000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Lead Architect\"," +
                "\"applicationDeadline\": \"2026-12-31\"" +
                "}";

        MvcResult result = mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.company").doesNotExist())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.toLowerCase().contains("password"), "Response should not contain password field");
        assertFalse(responseContent.contains("RecruiterPass123!"), "Response should not contain recruiter password");
        assertFalse(responseContent.contains("StudentPass789!"), "Response should not contain student password");
    }

    @Test
    void test32_UpdatePlacementDrive_PluralRoute_AlsoAccessible() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Desc", "Pune", 6.0, "CS", 500000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Updated via Plural Route\"," +
                "\"applicationDeadline\": \"2026-12-31\"" +
                "}";

        mockMvc.perform(put("/recruiters/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Updated via Plural Route"));
    }

    @Test
    void test33_UpdatePlacementDrive_RecruiterNotFound_Returns404() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Initial Title", "Desc", "Pune", 6.0, "CS", 500000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String ghostToken = jwtUtil.generateRecruiterToken("nonexistent.recruiter@unknown.com", 999999L);

        String updatePayload = "{" +
                "\"jobTitle\": \"Ghost Update\"," +
                "\"applicationDeadline\": \"2026-12-31\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + ghostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test34_RecruiterSuccessfullyDeletesTheirOwnDrive_Returns204() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Cloud Engineer", "Desc", "Pune", 7.0, "CS", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        mockMvc.perform(delete("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isNoContent());

        assertFalse(placementDriveRepository.existsById(drive.getId()));
    }

    @Test
    void test35_DeletedDriveNoLongerExists() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Cloud Engineer", "Desc", "Pune", 7.0, "CS", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        mockMvc.perform(delete("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isNoContent());

        assertFalse(placementDriveRepository.findById(drive.getId()).isPresent());
    }

    @Test
    void test36_RecruiterCannotDeleteAnotherCompanyDrive_Returns403() throws Exception {
        PlacementDrive driveB = placementDriveRepository.save(new PlacementDrive(
                "Company B Job", "Desc", "Mumbai", 7.0, "Finance", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyB
        ));

        mockMvc.perform(delete("/recruiter/placement-drives/" + driveB.getId())
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void test37_AnotherCompanyDriveRemainsUnchanged() throws Exception {
        PlacementDrive driveB = placementDriveRepository.save(new PlacementDrive(
                "Company B Job", "Desc", "Mumbai", 7.0, "Finance", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyB
        ));

        mockMvc.perform(delete("/recruiter/placement-drives/" + driveB.getId())
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isForbidden());

        assertTrue(placementDriveRepository.existsById(driveB.getId()));
    }

    @Test
    void test38_DeletePlacementDrive_MissingDrive_Returns404() throws Exception {
        mockMvc.perform(delete("/recruiter/placement-drives/999999")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test39_DeletePlacementDrive_MissingJwt_Returns401() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Cloud Engineer", "Desc", "Pune", 7.0, "CS", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        mockMvc.perform(delete("/recruiter/placement-drives/" + drive.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test40_DeletePlacementDrive_InvalidJwt_Returns401() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Cloud Engineer", "Desc", "Pune", 7.0, "CS", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        mockMvc.perform(delete("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test41_DeletePlacementDrive_StudentJwt_Returns403() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Cloud Engineer", "Desc", "Pune", 7.0, "CS", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        mockMvc.perform(delete("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void test42_DeletePlacementDrive_RecruiterNotFound_Returns404() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Cloud Engineer", "Desc", "Pune", 7.0, "CS", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        String ghostToken = jwtUtil.generateRecruiterToken("nonexistent.recruiter@unknown.com", 999999L);

        mockMvc.perform(delete("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + ghostToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test43_DeletePlacementDrive_PluralRoute_AlsoAccessible() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Cloud Engineer", "Desc", "Pune", 7.0, "CS", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        mockMvc.perform(delete("/recruiters/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isNoContent());

        assertFalse(placementDriveRepository.existsById(drive.getId()));
    }

    @Test
    void test44_ExistingApplicationRelationship_PreventsDeletion_PreservesData() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Software Engineer", "Core Java", "Pune", 7.0, "CS", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        Application application = applicationRepository.save(new Application(
                student, drive, LocalDate.now(), "APPLIED"
        ));

        mockMvc.perform(delete("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // Placement drive is NOT deleted
        assertTrue(placementDriveRepository.existsById(drive.getId()), "Placement drive must not be deleted");

        // Application is NOT deleted
        assertTrue(applicationRepository.existsById(application.getId()), "Application must not be deleted");

        // Application relationship is intact
        Application inDb = applicationRepository.findById(application.getId()).orElse(null);
        assertNotNull(inDb);
        assertEquals(drive.getId(), inDb.getPlacementDrive().getId());
        assertEquals(student.getId(), inDb.getStudent().getId());
    }

    @Test
    void test45_CreateDrive_ReturnsPlacementDriveResponseDto_NoCompanyEntityExposed() throws Exception {
        String payload = "{" +
                "\"jobTitle\": \"Site Reliability Engineer\"," +
                "\"jobDescription\": \"Production infrastructure and monitoring\"," +
                "\"location\": \"Hyderabad\"," +
                "\"minimumCgpa\": 7.5," +
                "\"eligibilityCriteria\": \"B.Tech / M.Tech in CS/IT\"," +
                "\"salary\": 1200000.0," +
                "\"applicationDeadline\": \"2026-11-30\"," +
                "\"status\": \"OPEN\"" +
                "}";

        mockMvc.perform(post("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.jobTitle").value("Site Reliability Engineer"))
                .andExpect(jsonPath("$.jobDescription").value("Production infrastructure and monitoring"))
                .andExpect(jsonPath("$.location").value("Hyderabad"))
                .andExpect(jsonPath("$.minimumCgpa").value(7.5))
                .andExpect(jsonPath("$.eligibilityCriteria").value("B.Tech / M.Tech in CS/IT"))
                .andExpect(jsonPath("$.salary").value(1200000.0))
                .andExpect(jsonPath("$.applicationDeadline").value("2026-11-30"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.companyName").value("TechCorp India"))
                .andExpect(jsonPath("$.company").doesNotExist())
                .andExpect(jsonPath("$.recruiter").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void test46_ListDrives_ReturnsListOfPlacementDriveResponseDto_NoCompanyEntityExposed() throws Exception {
        placementDriveRepository.save(new PlacementDrive(
                "Security Analyst", "AppSec and Penetration Testing", "Pune", 7.0,
                "B.Tech", 850000.0, LocalDate.of(2026, 12, 1), "OPEN", companyA
        ));

        mockMvc.perform(get("/recruiter/placement-drives")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].jobTitle").value("Security Analyst"))
                .andExpect(jsonPath("$[0].jobDescription").value("AppSec and Penetration Testing"))
                .andExpect(jsonPath("$[0].location").value("Pune"))
                .andExpect(jsonPath("$[0].minimumCgpa").value(7.0))
                .andExpect(jsonPath("$[0].eligibilityCriteria").value("B.Tech"))
                .andExpect(jsonPath("$[0].salary").value(850000.0))
                .andExpect(jsonPath("$[0].applicationDeadline").value("2026-12-01"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].companyId").value(companyA.getId()))
                .andExpect(jsonPath("$[0].companyName").value("TechCorp India"))
                .andExpect(jsonPath("$[0].company").doesNotExist())
                .andExpect(jsonPath("$[0].recruiter").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void test47_UpdateDrive_ReturnsPlacementDriveResponseDto_NoCompanyEntityExposed() throws Exception {
        PlacementDrive drive = placementDriveRepository.save(new PlacementDrive(
                "Junior Dev", "Junior Developer", "Pune", 6.0,
                "B.Sc / BCA", 400000.0, LocalDate.of(2026, 10, 1), "OPEN", companyA
        ));

        String updatePayload = "{" +
                "\"jobTitle\": \"Associate Software Engineer\"," +
                "\"jobDescription\": \"Full time software development\"," +
                "\"location\": \"Bangalore\"," +
                "\"minimumCgpa\": 6.5," +
                "\"eligibilityCriteria\": \"B.Tech / MCA\"," +
                "\"salary\": 650000.0," +
                "\"applicationDeadline\": \"2026-11-15\"," +
                "\"status\": \"CLOSED\"" +
                "}";

        mockMvc.perform(put("/recruiter/placement-drives/" + drive.getId())
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(drive.getId()))
                .andExpect(jsonPath("$.jobTitle").value("Associate Software Engineer"))
                .andExpect(jsonPath("$.jobDescription").value("Full time software development"))
                .andExpect(jsonPath("$.location").value("Bangalore"))
                .andExpect(jsonPath("$.minimumCgpa").value(6.5))
                .andExpect(jsonPath("$.eligibilityCriteria").value("B.Tech / MCA"))
                .andExpect(jsonPath("$.salary").value(650000.0))
                .andExpect(jsonPath("$.applicationDeadline").value("2026-11-15"))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.companyName").value("TechCorp India"))
                .andExpect(jsonPath("$.company").doesNotExist())
                .andExpect(jsonPath("$.recruiter").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
