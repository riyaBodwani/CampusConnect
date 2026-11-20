package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class PlacementDriveControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    private String validToken;
    private Company savedCompany;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        validToken = jwtUtil.generateToken("student@example.com", 1L, "STUDENT");

        placementDriveRepository.deleteAll();
        companyRepository.deleteAll();

        Company c = new Company("Infosys", "IT Services", "https://infosys.com", "contact@infosys.com", "9876543210", "Pune", "IT Company");
        savedCompany = companyRepository.save(c);
    }

    @Test
    void testCreatePlacementDrive_Success_Returns201() throws Exception {
        String payload = "{" +
                "\"jobTitle\":\"Systems Engineer\"," +
                "\"jobDescription\":\"Develop and maintain software\"," +
                "\"location\":\"Pune\"," +
                "\"minimumCgpa\":6.5," +
                "\"eligibilityCriteria\":\"All branches\"," +
                "\"salary\":650000.0," +
                "\"applicationDeadline\":\"2026-11-30\"," +
                "\"status\":\"OPEN\"," +
                "\"company\":{\"id\":" + savedCompany.getId() + "}" +
                "}";

        mockMvc.perform(post("/placement-drives")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobTitle").value("Systems Engineer"))
                .andExpect(jsonPath("$.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$.companyName").value("Infosys"))
                .andExpect(jsonPath("$.company").doesNotExist());
    }

    @Test
    void testCreatePlacementDrive_MissingCompany_Returns404() throws Exception {
        String payload = "{" +
                "\"jobTitle\":\"Systems Engineer\"," +
                "\"location\":\"Pune\"," +
                "\"company\":{\"id\":999999}" +
                "}";

        mockMvc.perform(post("/placement-drives")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testGetAllAndGetById_Success() throws Exception {
        PlacementDrive drive = new PlacementDrive("FullStack Engineer", "Java & React", "Mumbai", 7.0, "CS/IT", 800000.0, LocalDate.of(2026, 12, 1), "OPEN", savedCompany);
        PlacementDrive saved = placementDriveRepository.save(drive);

        mockMvc.perform(get("/placement-drives")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$[0].companyName").value("Infosys"))
                .andExpect(jsonPath("$[0].company").doesNotExist());

        mockMvc.perform(get("/placement-drives/" + saved.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("FullStack Engineer"))
                .andExpect(jsonPath("$.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$.companyName").value("Infosys"))
                .andExpect(jsonPath("$.company").doesNotExist());
    }

    @Test
    void testGetPlacementDriveById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/placement-drives/999999")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testUpdatePlacementDrive_Success_Returns200() throws Exception {
        PlacementDrive drive = new PlacementDrive("FullStack Engineer", "Java & React", "Mumbai", 7.0, "CS/IT", 800000.0, LocalDate.of(2026, 12, 1), "OPEN", savedCompany);
        PlacementDrive saved = placementDriveRepository.save(drive);

        String updatePayload = "{" +
                "\"jobTitle\":\"Lead FullStack Engineer\"," +
                "\"jobDescription\":\"Lead fullstack squad\"," +
                "\"location\":\"Bangalore\"," +
                "\"minimumCgpa\":8.0," +
                "\"eligibilityCriteria\":\"CS/IT only\"," +
                "\"salary\":1200000.0," +
                "\"applicationDeadline\":\"2026-12-15\"," +
                "\"status\":\"OPEN\"," +
                "\"company\":{\"id\":" + savedCompany.getId() + "}" +
                "}";

        mockMvc.perform(put("/placement-drives/" + saved.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Lead FullStack Engineer"))
                .andExpect(jsonPath("$.location").value("Bangalore"))
                .andExpect(jsonPath("$.salary").value(1200000.0))
                .andExpect(jsonPath("$.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$.companyName").value("Infosys"))
                .andExpect(jsonPath("$.company").doesNotExist());
    }

    @Test
    void testUpdatePlacementDrive_MissingCompany_Returns404() throws Exception {
        PlacementDrive drive = new PlacementDrive("FullStack Engineer", "Java & React", "Mumbai", 7.0, "CS/IT", 800000.0, LocalDate.of(2026, 12, 1), "OPEN", savedCompany);
        PlacementDrive saved = placementDriveRepository.save(drive);

        String updatePayload = "{" +
                "\"jobTitle\":\"Lead FullStack Engineer\"," +
                "\"company\":{\"id\":999999}" +
                "}";

        mockMvc.perform(put("/placement-drives/" + saved.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testUpdatePlacementDrive_MissingDrive_Returns404() throws Exception {
        String updatePayload = "{" +
                "\"jobTitle\":\"Lead FullStack Engineer\"," +
                "\"company\":{\"id\":" + savedCompany.getId() + "}" +
                "}";

        mockMvc.perform(put("/placement-drives/999999")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testDeletePlacementDrive_Success_Returns204() throws Exception {
        PlacementDrive drive = new PlacementDrive("Data Engineer", "ETL pipelines", "Pune", 7.0, "CS/IT", 750000.0, LocalDate.of(2026, 12, 1), "OPEN", savedCompany);
        PlacementDrive saved = placementDriveRepository.save(drive);

        mockMvc.perform(delete("/placement-drives/" + saved.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/placement-drives/" + saved.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePlacementDrive_NotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/placement-drives/999999")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testUnauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(get("/placement-drives"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPlacementDrive_DtoAudit_NoPasswordsOrRawCompany() throws Exception {
        PlacementDrive drive = new PlacementDrive("ML Engineer", "Build models", "Hyderabad", 8.0, "CS/IT", 1100000.0, LocalDate.of(2026, 12, 31), "OPEN", savedCompany);
        PlacementDrive saved = placementDriveRepository.save(drive);

        mockMvc.perform(get("/placement-drives/" + saved.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.jobTitle").value("ML Engineer"))
                .andExpect(jsonPath("$.jobDescription").value("Build models"))
                .andExpect(jsonPath("$.location").value("Hyderabad"))
                .andExpect(jsonPath("$.minimumCgpa").value(8.0))
                .andExpect(jsonPath("$.eligibilityCriteria").value("CS/IT"))
                .andExpect(jsonPath("$.salary").value(1100000.0))
                .andExpect(jsonPath("$.applicationDeadline").value("2026-12-31"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.companyId").value(savedCompany.getId()))
                .andExpect(jsonPath("$.companyName").value("Infosys"))
                .andExpect(jsonPath("$.company").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
