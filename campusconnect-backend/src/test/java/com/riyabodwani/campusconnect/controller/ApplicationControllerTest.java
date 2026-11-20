package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.entity.Recruiter;
import com.riyabodwani.campusconnect.entity.Student;
import com.riyabodwani.campusconnect.repository.*;
import com.riyabodwani.campusconnect.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ApplicationControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    private Student studentA;
    private Student studentB;
    private PlacementDrive savedDrive;
    private Company savedCompany;
    private Recruiter savedRecruiter;
    private String studentAToken;
    private String studentBToken;
    private String recruiterToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        studentA = new Student();
        studentA.setFullName("Student A");
        studentA.setEmail("studentA@example.com");
        studentA.setPassword("secret123");
        studentA = studentRepository.save(studentA);

        studentB = new Student();
        studentB.setFullName("Student B");
        studentB.setEmail("studentB@example.com");
        studentB.setPassword("secret123");
        studentB = studentRepository.save(studentB);

        Company company = new Company("Microsoft", "Tech", "https://microsoft.com", "jobs@ms.com", "9876543210", "Hyderabad", "Software Company");
        savedCompany = companyRepository.save(company);

        savedRecruiter = recruiterRepository.save(
                new Recruiter("Recruiter MS", "recruiter.ms@microsoft.com", "secret123", "9876543219", savedCompany)
        );

        PlacementDrive drive = new PlacementDrive("Software Engineer", "Backend Engineering", "Hyderabad", 7.5, "CS/IT only", 1200000.0, LocalDate.of(2026, 12, 31), "OPEN", savedCompany);
        savedDrive = placementDriveRepository.save(drive);

        studentAToken = jwtUtil.generateToken(studentA.getEmail(), studentA.getId(), "STUDENT");
        studentBToken = jwtUtil.generateToken(studentB.getEmail(), studentB.getId(), "STUDENT");
        recruiterToken = jwtUtil.generateRecruiterToken(savedRecruiter.getEmail(), savedRecruiter.getId());
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

    private long extractApplicationId(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    @Test
    void test1_SuccessfulApplication_Returns201() throws Exception {
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";

        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.student.id").value(studentA.getId()))
                .andExpect(jsonPath("$.student.email").value(studentA.getEmail()))
                .andExpect(jsonPath("$.student.password").doesNotExist())
                .andExpect(jsonPath("$.placementDrive.id").value(savedDrive.getId()))
                .andExpect(jsonPath("$.placementDrive.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$.placementDrive.companyName").value("Microsoft"))
                .andExpect(jsonPath("$.placementDrive.company").doesNotExist());
    }

    @Test
    void test2_MissingPlacementDrive_Returns404() throws Exception {
        String payload = "{\"placementDriveId\":999999}";

        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test3_DuplicateApplication_Returns400() throws Exception {
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";

        // First application succeeds
        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        // Duplicate application fails with 400
        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Student has already applied to this placement drive"));
    }

    @Test
    void test4_UnauthenticatedRequest_Returns401() throws Exception {
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test5_NonStudentRole_Returns403() throws Exception {
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";

        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void test6_StudentIdentityComesFromAuthenticationRatherThanRequestBody() throws Exception {
        // studentA is logged in, but payload attempts to forge studentId to studentB's id
        String payloadWithForgedStudentId = "{" +
                "\"placementDriveId\":" + savedDrive.getId() + "," +
                "\"studentId\":" + studentB.getId() +
                "}";

        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithForgedStudentId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.student.id").value(studentA.getId()))
                .andExpect(jsonPath("$.student.email").value(studentA.getEmail()));
    }

    @Test
    void test7_GetMyApplications_Success_ReturnsOnlyStudentApplications() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].student.id").value(studentA.getId()))
                .andExpect(jsonPath("$[0].student.email").value(studentA.getEmail()))
                .andExpect(jsonPath("$[0].student.password").doesNotExist())
                .andExpect(jsonPath("$[0].placementDrive.id").value(savedDrive.getId()))
                .andExpect(jsonPath("$[0].placementDrive.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$[0].placementDrive.companyName").value("Microsoft"))
                .andExpect(jsonPath("$[0].placementDrive.company").doesNotExist());
    }

    @Test
    void test8_GetMyApplications_NoApplications_Returns200WithEmptyArray() throws Exception {
        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void test9_GetMyApplications_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/applications/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test10_GetMyApplications_NonStudentRole_Returns403() throws Exception {
        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void test11_GetMyApplications_AnotherStudentApplicationsNotReturned() throws Exception {
        // Both studentA and studentB apply to savedDrive
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        // studentA calls /applications/my
        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].student.id").value(studentA.getId()))
                .andExpect(jsonPath("$[0].student.email").value(studentA.getEmail()));

        // studentB calls /applications/my
        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].student.id").value(studentB.getId()))
                .andExpect(jsonPath("$[0].student.email").value(studentB.getEmail()));
    }

    @Test
    void test12_GetMyApplications_SeesOwnApplicationWithCurrentAppliedStatus() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        // studentA gets applications
        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].status").value("APPLIED"))
                .andExpect(jsonPath("$[0].appliedDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$[0].placementDrive.id").value(savedDrive.getId()))
                .andExpect(jsonPath("$[0].placementDrive.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$[0].placementDrive.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$[0].placementDrive.companyName").value("Microsoft"))
                .andExpect(jsonPath("$[0].placementDrive.company").doesNotExist());
    }

    @Test
    void test13_GetMyApplications_ReflectsUpdatedShortlistedStatus() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Recruiter updates status to SHORTLISTED
        String updatePayload = "{\"status\":\"SHORTLISTED\"}";
        mockMvc.perform(patch("/recruiter/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk());

        // studentA calls /applications/my and sees updated SHORTLISTED status
        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appId))
                .andExpect(jsonPath("$[0].status").value("SHORTLISTED"));
    }

    @Test
    void test14_GetMyApplications_ReflectsUpdatedRejectedStatus() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Recruiter updates status to REJECTED
        String updatePayload = "{\"status\":\"REJECTED\"}";
        mockMvc.perform(patch("/recruiter/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk());

        // studentA calls /applications/my and sees updated REJECTED status
        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appId))
                .andExpect(jsonPath("$[0].status").value("REJECTED"));
    }

    @Test
    void test15_GetMyApplications_ReflectsUpdatedSelectedStatus() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Recruiter updates status to SELECTED
        String updatePayload = "{\"status\":\"SELECTED\"}";
        mockMvc.perform(patch("/recruiter/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk());

        // studentA calls /applications/my and sees updated SELECTED status
        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appId))
                .andExpect(jsonPath("$[0].status").value("SELECTED"));
    }

    @Test
    void test16_GetMyApplications_InvalidJwt_Returns401() throws Exception {
        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test17_GetMyApplications_ResponseDoesNotExposePasswords() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].student.password").doesNotExist())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertFalse(content.toLowerCase().contains("password"), "Response should not contain any password field");
        assertFalse(content.contains("secret123"), "Response should not contain raw student password");
    }

    @Test
    void test18_WithdrawApplication_Success_Returns204AndDeletesApplication() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);
        assertTrue(applicationRepository.findById(appId).isPresent(), "Application must exist in DB before withdrawal");

        // studentA withdraws application
        mockMvc.perform(delete("/applications/" + appId)
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isNoContent());

        // Verify application is removed from database
        assertTrue(applicationRepository.findById(appId).isEmpty(), "Application must be deleted from DB");

        // Verify student, drive, and company remain intact
        assertTrue(studentRepository.findById(studentA.getId()).isPresent(), "Student record must remain intact");
        assertTrue(placementDriveRepository.findById(savedDrive.getId()).isPresent(), "Placement drive record must remain intact");
        assertTrue(companyRepository.findById(savedCompany.getId()).isPresent(), "Company record must remain intact");
    }

    @Test
    void test19_WithdrawApplication_ApplicationNotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/applications/999999")
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test20_WithdrawApplication_OtherStudentApplication_Returns403() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // studentB attempts to withdraw studentA's application
        mockMvc.perform(delete("/applications/" + appId)
                        .header("Authorization", "Bearer " + studentBToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // Application must still exist in DB
        assertTrue(applicationRepository.findById(appId).isPresent(), "Application must not be deleted by unauthorized student");
    }

    @Test
    void test21_WithdrawApplication_StatusShortlisted_Returns409() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Recruiter shortlists application
        mockMvc.perform(patch("/recruiter/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHORTLISTED\"}"))
                .andExpect(status().isOk());

        // studentA attempts to withdraw shortlisted application
        mockMvc.perform(delete("/applications/" + appId)
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // Application must still exist in DB with SHORTLISTED status
        assertTrue(applicationRepository.findById(appId).isPresent());
        assertEquals("SHORTLISTED", applicationRepository.findById(appId).get().getStatus());
    }

    @Test
    void test22_WithdrawApplication_StatusRejected_Returns409() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Recruiter rejects application
        mockMvc.perform(patch("/recruiter/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isOk());

        // studentA attempts to withdraw rejected application
        mockMvc.perform(delete("/applications/" + appId)
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // Application must still exist in DB with REJECTED status
        assertTrue(applicationRepository.findById(appId).isPresent());
        assertEquals("REJECTED", applicationRepository.findById(appId).get().getStatus());
    }

    @Test
    void test23_WithdrawApplication_StatusSelected_Returns409() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Recruiter selects application
        mockMvc.perform(patch("/recruiter/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SELECTED\"}"))
                .andExpect(status().isOk());

        // studentA attempts to withdraw selected application
        mockMvc.perform(delete("/applications/" + appId)
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // Application must still exist in DB with SELECTED status
        assertTrue(applicationRepository.findById(appId).isPresent());
        assertEquals("SELECTED", applicationRepository.findById(appId).get().getStatus());
    }

    @Test
    void test24_WithdrawApplication_Unauthenticated_Returns401() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Unauthenticated call
        mockMvc.perform(delete("/applications/" + appId))
                .andExpect(status().isUnauthorized());

        // Application must still exist
        assertTrue(applicationRepository.findById(appId).isPresent());
    }

    @Test
    void test25_WithdrawApplication_InvalidJwt_Returns401() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Call with invalid JWT
        mockMvc.perform(delete("/applications/" + appId)
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());

        // Application must still exist
        assertTrue(applicationRepository.findById(appId).isPresent());
    }

    @Test
    void test26_WithdrawApplication_RecruiterRole_Returns403() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Recruiter attempts to call DELETE /applications/{id}
        mockMvc.perform(delete("/applications/" + appId)
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());

        // Application must still exist
        assertTrue(applicationRepository.findById(appId).isPresent());
    }

    @Test
    void test27_WithdrawApplication_StudentNotFoundInDb_Returns404() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // Token with non-existent student email
        String nonExistentToken = jwtUtil.generateToken("ghost@example.com", 99999L, "STUDENT");

        mockMvc.perform(delete("/applications/" + appId)
                        .header("Authorization", "Bearer " + nonExistentToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test28_WithdrawApplication_CannotSpoofStudentIdViaQueryOrBody() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // studentB attempts to withdraw studentA's app by passing studentA's studentId in query & body
        mockMvc.perform(delete("/applications/" + appId + "?studentId=" + studentA.getId())
                        .header("Authorization", "Bearer " + studentBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":" + studentA.getId() + "}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // Application must still exist
        assertTrue(applicationRepository.findById(appId).isPresent());
    }

    @Test
    void test29_WithdrawApplication_ReapplyAfterWithdrawal_Returns201() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        MvcResult applyResult = mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String applyResponse = applyResult.getResponse().getContentAsString();
        long appId = extractApplicationId(applyResponse);

        // studentA withdraws
        mockMvc.perform(delete("/applications/" + appId)
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isNoContent());

        // studentA re-applies to same drive - succeeds because old application was deleted
        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void test30_GetMyApplications_PlacementDriveResponseDto_ContractVerification() throws Exception {
        // studentA applies
        String payload = "{\"placementDriveId\":" + savedDrive.getId() + "}";
        mockMvc.perform(post("/applications")
                        .header("Authorization", "Bearer " + studentAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/applications/my")
                        .header("Authorization", "Bearer " + studentAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].placementDrive.id").value(savedDrive.getId()))
                .andExpect(jsonPath("$[0].placementDrive.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$[0].placementDrive.jobDescription").value("Backend Engineering"))
                .andExpect(jsonPath("$[0].placementDrive.location").value("Hyderabad"))
                .andExpect(jsonPath("$[0].placementDrive.minimumCgpa").value(7.5))
                .andExpect(jsonPath("$[0].placementDrive.eligibilityCriteria").value("CS/IT only"))
                .andExpect(jsonPath("$[0].placementDrive.salary").value(1200000.0))
                .andExpect(jsonPath("$[0].placementDrive.applicationDeadline").value("2026-12-31"))
                .andExpect(jsonPath("$[0].placementDrive.status").value("OPEN"))
                .andExpect(jsonPath("$[0].placementDrive.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$[0].placementDrive.companyName").value("Microsoft"))
                .andExpect(jsonPath("$[0].placementDrive.company").doesNotExist())
                .andExpect(jsonPath("$[0].student.password").doesNotExist());
    }
}
