package com.riyabodwani.campusconnect;

import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.exception.CompanyNotFoundException;
import com.riyabodwani.campusconnect.exception.PlacementDriveNotFoundException;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.service.PlacementDriveService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PlacementDriveServiceTest {

    @Autowired
    private PlacementDriveService placementDriveService;

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company savedCompany;

    @BeforeEach
    void setUp() {
        placementDriveRepository.deleteAll();
        companyRepository.deleteAll();

        Company company = new Company();
        company.setName("Acme Corporation");
        company.setIndustry("Software");
        company.setWebsite("https://acme.org");
        company.setContactEmail("contact@acme.org");
        company.setContactPhone("1234567890");
        company.setLocation("Pune");
        company.setDescription("Tech company");
        savedCompany = companyRepository.save(company);
    }

    @AfterEach
    void cleanUp() {
        placementDriveRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void testCreatePlacementDrive_SuccessWithExistingCompany() {
        PlacementDrive drive = new PlacementDrive();
        drive.setJobTitle("Software Developer");
        drive.setJobDescription("Java & Spring Boot role");
        drive.setLocation("Pune");
        drive.setMinimumCgpa(7.0);
        drive.setEligibilityCriteria("Computer Science or IT graduates");
        drive.setSalary(700000.0);
        drive.setApplicationDeadline(LocalDate.of(2026, 11, 30));
        drive.setStatus("OPEN");

        Company companyRef = new Company();
        companyRef.setId(savedCompany.getId());
        drive.setCompany(companyRef);

        PlacementDrive created = placementDriveService.createPlacementDrive(drive);

        assertNotNull(created.getId());
        assertEquals("Software Developer", created.getJobTitle());
        assertNotNull(created.getCompany());
        assertEquals(savedCompany.getId(), created.getCompany().getId());
    }

    @Test
    void testCreatePlacementDrive_MissingCompany_ThrowsCompanyNotFoundException() {
        PlacementDrive drive = new PlacementDrive();
        drive.setJobTitle("QA Engineer");

        Company nonExistentCompany = new Company();
        nonExistentCompany.setId(999999L);
        drive.setCompany(nonExistentCompany);

        assertThrows(CompanyNotFoundException.class, () -> placementDriveService.createPlacementDrive(drive));
    }

    @Test
    void testCreatePlacementDrive_NullCompany_ThrowsCompanyNotFoundException() {
        PlacementDrive drive = new PlacementDrive();
        drive.setJobTitle("QA Engineer");
        drive.setCompany(null);

        assertThrows(CompanyNotFoundException.class, () -> placementDriveService.createPlacementDrive(drive));
    }

    @Test
    void testGetAllAndGetById_Success() {
        PlacementDrive drive = new PlacementDrive("DevOps", "CI/CD role", "Remote", 6.5, "Any Engineer", 800000.0, LocalDate.of(2026, 12, 1), "OPEN", savedCompany);
        PlacementDrive saved = placementDriveRepository.save(drive);

        List<PlacementDrive> all = placementDriveService.getAllPlacementDrives();
        assertFalse(all.isEmpty());

        PlacementDrive found = placementDriveService.getPlacementDriveById(saved.getId());
        assertEquals("DevOps", found.getJobTitle());
        assertEquals(savedCompany.getId(), found.getCompany().getId());
    }

    @Test
    void testGetPlacementDriveById_NotFound_ThrowsPlacementDriveNotFoundException() {
        assertThrows(PlacementDriveNotFoundException.class, () -> placementDriveService.getPlacementDriveById(999999L));
    }

    @Test
    void testUpdatePlacementDrive_SuccessWithExistingCompany() {
        PlacementDrive drive = new PlacementDrive("Frontend Dev", "React role", "Pune", 7.0, "All branches", 600000.0, LocalDate.of(2026, 10, 15), "OPEN", savedCompany);
        PlacementDrive saved = placementDriveRepository.save(drive);

        Company updatedCompany = new Company();
        updatedCompany.setName("Beta Corp");
        Company newSavedCompany = companyRepository.save(updatedCompany);

        PlacementDrive updateDetails = new PlacementDrive();
        updateDetails.setJobTitle("Senior Frontend Engineer");
        updateDetails.setJobDescription("React & TypeScript lead");
        updateDetails.setLocation("Mumbai");
        updateDetails.setMinimumCgpa(7.5);
        updateDetails.setEligibilityCriteria("CS graduates with 8+ CGPA preferred");
        updateDetails.setSalary(950000.0);
        updateDetails.setApplicationDeadline(LocalDate.of(2026, 11, 15));
        updateDetails.setStatus("EXTENDED");

        Company companyRef = new Company();
        companyRef.setId(newSavedCompany.getId());
        updateDetails.setCompany(companyRef);

        PlacementDrive updated = placementDriveService.updatePlacementDrive(saved.getId(), updateDetails);

        assertEquals("Senior Frontend Engineer", updated.getJobTitle());
        assertEquals("Mumbai", updated.getLocation());
        assertEquals(newSavedCompany.getId(), updated.getCompany().getId());
    }

    @Test
    void testUpdatePlacementDrive_MissingPlacementDrive_ThrowsPlacementDriveNotFoundException() {
        PlacementDrive updateDetails = new PlacementDrive();
        updateDetails.setCompany(savedCompany);

        assertThrows(PlacementDriveNotFoundException.class, () -> placementDriveService.updatePlacementDrive(999999L, updateDetails));
    }

    @Test
    void testUpdatePlacementDrive_MissingCompany_ThrowsCompanyNotFoundException() {
        PlacementDrive drive = new PlacementDrive("Frontend Dev", "React role", "Pune", 7.0, "All branches", 600000.0, LocalDate.of(2026, 10, 15), "OPEN", savedCompany);
        PlacementDrive saved = placementDriveRepository.save(drive);

        PlacementDrive updateDetails = new PlacementDrive();
        Company nonExistentCompany = new Company();
        nonExistentCompany.setId(888888L);
        updateDetails.setCompany(nonExistentCompany);

        assertThrows(CompanyNotFoundException.class, () -> placementDriveService.updatePlacementDrive(saved.getId(), updateDetails));
    }

    @Test
    void testDeletePlacementDrive_Success() {
        PlacementDrive drive = new PlacementDrive("Data Analyst", "SQL & Python", "Pune", 6.8, "Math/CS/IT", 650000.0, LocalDate.of(2026, 10, 20), "OPEN", savedCompany);
        PlacementDrive saved = placementDriveRepository.save(drive);

        placementDriveService.deletePlacementDrive(saved.getId());

        assertFalse(placementDriveRepository.existsById(saved.getId()));
    }

    @Test
    void testDeletePlacementDrive_NotFound_ThrowsPlacementDriveNotFoundException() {
        assertThrows(PlacementDriveNotFoundException.class, () -> placementDriveService.deletePlacementDrive(999999L));
    }
}
