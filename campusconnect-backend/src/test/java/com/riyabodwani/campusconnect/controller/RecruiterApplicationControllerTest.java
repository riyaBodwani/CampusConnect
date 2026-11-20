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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import org.springframework.http.MediaType;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class RecruiterApplicationControllerTest {

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
    private Company companyC;

    private Recruiter recruiterA;
    private Recruiter recruiterB;
    private Recruiter recruiterC;

    private Student student1;
    private Student student2;

    private PlacementDrive driveA;
    private PlacementDrive driveB;

    private Application appA;
    private Application appB;

    private String recruiterAToken;
    private String recruiterBToken;
    private String recruiterCToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        // Create companies
        companyA = companyRepository.save(new Company("Company A", "Tech", "https://companya.com", "contact@companya.com", "1111111111", "Pune", "Tech Corp A"));
        companyB = companyRepository.save(new Company("Company B", "Finance", "https://companyb.com", "contact@companyb.com", "2222222222", "Mumbai", "Finance Corp B"));
        companyC = companyRepository.save(new Company("Company C", "Healthcare", "https://companyc.com", "contact@companyc.com", "3333333333", "Bangalore", "Health Corp C"));

        // Create recruiters
        recruiterA = recruiterRepository.save(new Recruiter("Alice Recruiter", "alice.rec@companya.com", passwordEncoder.encode("Secret123!"), "9999990001", companyA));
        recruiterB = recruiterRepository.save(new Recruiter("Bob Recruiter", "bob.rec@companyb.com", passwordEncoder.encode("Secret123!"), "9999990002", companyB));
        recruiterC = recruiterRepository.save(new Recruiter("Charlie Recruiter", "charlie.rec@companyc.com", passwordEncoder.encode("Secret123!"), "9999990003", companyC));

        // Create students
        student1 = new Student();
        student1.setFullName("John Doe");
        student1.setEmail("john.doe@university.com");
        student1.setPassword(passwordEncoder.encode("StudentPass123!"));
        student1.setPhoneNumber("9876543210");
        student1.setCourse("B.Tech");
        student1.setBranch("CSE");
        student1.setCgpa(8.9);
        student1.setDateOfBirth(LocalDate.of(2002, 5, 20));
        student1.setAddress("Pune");
        student1.setPlacementStatus("UNPLACED");
        student1 = studentRepository.save(student1);

        student2 = new Student();
        student2.setFullName("Jane Smith");
        student2.setEmail("jane.smith@university.com");
        student2.setPassword(passwordEncoder.encode("StudentPass456!"));
        student2.setPhoneNumber("9876543211");
        student2.setCourse("B.Tech");
        student2.setBranch("IT");
        student2.setCgpa(9.2);
        student2.setDateOfBirth(LocalDate.of(2002, 8, 15));
        student2.setAddress("Mumbai");
        student2.setPlacementStatus("UNPLACED");
        student2 = studentRepository.save(student2);

        // Create placement drives
        driveA = placementDriveRepository.save(new PlacementDrive(
                "Software Engineer", "Backend Java Developer", "Pune", 7.5,
                "B.Tech CSE/IT", 1200000.0, LocalDate.now().plusMonths(1), "OPEN", companyA
        ));

        driveB = placementDriveRepository.save(new PlacementDrive(
                "Data Analyst", "Analytics Role", "Mumbai", 7.0,
                "All Engineering", 900000.0, LocalDate.now().plusMonths(2), "OPEN", companyB
        ));

        // Create applications
        appA = applicationRepository.save(new Application(student1, driveA, LocalDate.now(), "APPLIED"));
        appB = applicationRepository.save(new Application(student2, driveB, LocalDate.now(), "APPLIED"));

        // Generate tokens
        recruiterAToken = jwtUtil.generateRecruiterToken(recruiterA.getEmail(), recruiterA.getId());
        recruiterBToken = jwtUtil.generateRecruiterToken(recruiterB.getEmail(), recruiterB.getId());
        recruiterCToken = jwtUtil.generateRecruiterToken(recruiterC.getEmail(), recruiterC.getId());
        studentToken = jwtUtil.generateToken(student1.getEmail(), student1.getId(), "STUDENT");
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
    void test1_RecruiterWithApplications_ReturnsOnlyTheirCompanyApplications() throws Exception {
        mockMvc.perform(get("/recruiter/applications")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appA.getId()))
                .andExpect(jsonPath("$[0].status").value("APPLIED"))
                .andExpect(jsonPath("$[0].appliedDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$[0].student.id").value(student1.getId()))
                .andExpect(jsonPath("$[0].student.fullName").value(student1.getFullName()))
                .andExpect(jsonPath("$[0].student.email").value(student1.getEmail()))
                .andExpect(jsonPath("$[0].student.phoneNumber").value(student1.getPhoneNumber()))
                .andExpect(jsonPath("$[0].student.course").value("B.Tech"))
                .andExpect(jsonPath("$[0].student.branch").value("CSE"))
                .andExpect(jsonPath("$[0].student.cgpa").value(8.9))
                .andExpect(jsonPath("$[0].placementDrive.id").value(driveA.getId()))
                .andExpect(jsonPath("$[0].placementDrive.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$[0].placementDrive.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$[0].placementDrive.companyName").value("Company A"))
                .andExpect(jsonPath("$[0].placementDrive.company").doesNotExist());
    }

    @Test
    void test2_RecruiterWithNoApplications_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/recruiter/applications")
                        .header("Authorization", "Bearer " + recruiterCToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void test3_CompanyARecruiterCannotSeeCompanyBApplications() throws Exception {
        // Recruiter A should only see Company A's application
        mockMvc.perform(get("/recruiter/applications")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appA.getId()))
                .andExpect(jsonPath("$[0].placementDrive.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$[0].placementDrive.company").doesNotExist());

        // Recruiter B should only see Company B's application
        mockMvc.perform(get("/recruiter/applications")
                        .header("Authorization", "Bearer " + recruiterBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appB.getId()))
                .andExpect(jsonPath("$[0].placementDrive.companyId").value(companyB.getId()))
                .andExpect(jsonPath("$[0].placementDrive.company").doesNotExist());
    }

    @Test
    void test4_StudentJwt_Returns403Forbidden() throws Exception {
        mockMvc.perform(get("/recruiter/applications")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void test5_NoJwt_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/recruiter/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test6_InvalidJwt_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/recruiter/applications")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test7_ResponseDoesNotExposePasswords() throws Exception {
        MvcResult result = mockMvc.perform(get("/recruiter/applications")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].student.password").doesNotExist())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertFalse(content.toLowerCase().contains("password"), "Response should not contain any password field");
        assertFalse(content.contains("StudentPass123!"), "Response should not contain student plain password");
        assertFalse(content.contains("Secret123!"), "Response should not contain recruiter plain password");
    }

    @Test
    void test8_PluralRecruitersApplicationsRoute_AlsoAccessible() throws Exception {
        mockMvc.perform(get("/recruiters/applications")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appA.getId()));
    }

    @Test
    void test9_RecruiterNotFound_Returns404NotFound() throws Exception {
        String nonExistentRecruiterToken = jwtUtil.generateRecruiterToken("ghost.recruiter@unknown.com", 99999L);

        mockMvc.perform(get("/recruiter/applications")
                        .header("Authorization", "Bearer " + nonExistentRecruiterToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test10_UpdateStatus_ToShortlisted_Success() throws Exception {
        String payload = "{\"status\":\"SHORTLISTED\"}";

        mockMvc.perform(patch("/recruiter/applications/" + appA.getId() + "/status")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appA.getId()))
                .andExpect(jsonPath("$.status").value("SHORTLISTED"))
                .andExpect(jsonPath("$.student.id").value(student1.getId()))
                .andExpect(jsonPath("$.student.email").value(student1.getEmail()))
                .andExpect(jsonPath("$.placementDrive.id").value(driveA.getId()))
                .andExpect(jsonPath("$.placementDrive.companyName").value("Company A"))
                .andExpect(jsonPath("$.placementDrive.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$.placementDrive.company").doesNotExist());
    }

    @Test
    void test11_UpdateStatus_ToRejected_Success() throws Exception {
        String payload = "{\"status\":\"REJECTED\"}";

        mockMvc.perform(patch("/recruiter/applications/" + appA.getId() + "/status")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appA.getId()))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void test12_UpdateStatus_ToSelected_Success() throws Exception {
        String payload = "{\"status\":\"SELECTED\"}";

        mockMvc.perform(patch("/recruiter/applications/" + appA.getId() + "/status")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appA.getId()))
                .andExpect(jsonPath("$.status").value("SELECTED"));
    }

    @Test
    void test13_UpdateStatus_InvalidStatus_Returns400() throws Exception {
        String payload = "{\"status\":\"HIRED\"}";

        mockMvc.perform(patch("/recruiter/applications/" + appA.getId() + "/status")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void test14_UpdateStatus_BlankStatus_Returns400() throws Exception {
        String payload = "{\"status\":\"   \"}";

        mockMvc.perform(patch("/recruiter/applications/" + appA.getId() + "/status")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void test15_UpdateStatus_AnotherCompanyApplication_Returns403() throws Exception {
        // Recruiter A belongs to Company A, appB belongs to Company B
        String payload = "{\"status\":\"SHORTLISTED\"}";

        mockMvc.perform(patch("/recruiter/applications/" + appB.getId() + "/status")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void test16_UpdateStatus_ApplicationNotFound_Returns404() throws Exception {
        String payload = "{\"status\":\"SHORTLISTED\"}";

        mockMvc.perform(patch("/recruiter/applications/999999/status")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test17_UpdateStatus_StudentJwt_Returns403() throws Exception {
        String payload = "{\"status\":\"SHORTLISTED\"}";

        mockMvc.perform(patch("/recruiter/applications/" + appA.getId() + "/status")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void test18_UpdateStatus_NoJwt_Returns401() throws Exception {
        String payload = "{\"status\":\"SHORTLISTED\"}";

        mockMvc.perform(patch("/recruiter/applications/" + appA.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test19_UpdateStatus_InvalidJwt_Returns401() throws Exception {
        String payload = "{\"status\":\"SHORTLISTED\"}";

        mockMvc.perform(patch("/recruiter/applications/" + appA.getId() + "/status")
                        .header("Authorization", "Bearer invalid.token.value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test20_UpdateStatus_ResponseDoesNotExposePasswords() throws Exception {
        String payload = "{\"status\":\"SHORTLISTED\"}";

        MvcResult result = mockMvc.perform(patch("/recruiter/applications/" + appA.getId() + "/status")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student.password").doesNotExist())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertFalse(content.toLowerCase().contains("password"), "Response should not contain any password field");
        assertFalse(content.contains("StudentPass123!"), "Response should not contain student plain password");
        assertFalse(content.contains("Secret123!"), "Response should not contain recruiter plain password");
    }

    @Test
    void test21_UpdateStatus_PluralRoute_AlsoAccessible() throws Exception {
        String payload = "{\"status\":\"SHORTLISTED\"}";

        mockMvc.perform(patch("/recruiters/applications/" + appA.getId() + "/status")
                        .header("Authorization", "Bearer " + recruiterAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appA.getId()))
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    void test22_PlacementDriveResponseDto_ContractVerification() throws Exception {
        mockMvc.perform(get("/recruiter/applications")
                        .header("Authorization", "Bearer " + recruiterAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].placementDrive.id").value(driveA.getId()))
                .andExpect(jsonPath("$[0].placementDrive.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$[0].placementDrive.jobDescription").value("Backend Java Developer"))
                .andExpect(jsonPath("$[0].placementDrive.location").value("Pune"))
                .andExpect(jsonPath("$[0].placementDrive.minimumCgpa").value(7.5))
                .andExpect(jsonPath("$[0].placementDrive.eligibilityCriteria").value("B.Tech CSE/IT"))
                .andExpect(jsonPath("$[0].placementDrive.salary").value(1200000.0))
                .andExpect(jsonPath("$[0].placementDrive.status").value("OPEN"))
                .andExpect(jsonPath("$[0].placementDrive.companyId").value(companyA.getId()))
                .andExpect(jsonPath("$[0].placementDrive.companyName").value("Company A"))
                .andExpect(jsonPath("$[0].placementDrive.company").doesNotExist());
    }
}
