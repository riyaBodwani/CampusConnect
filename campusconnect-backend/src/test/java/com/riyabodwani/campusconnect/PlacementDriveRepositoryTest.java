package com.riyabodwani.campusconnect;

import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PlacementDriveRepositoryTest {

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @AfterEach
    void cleanUp() {
        placementDriveRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void testSaveAndFindPlacementDrive_VerifiesTableCreationAndCompanyForeignKey() {
        Company company = new Company();
        company.setName("Initech Global");
        company.setIndustry("Technology");
        company.setWebsite("https://initech.example.com");
        company.setContactEmail("jobs@initech.example.com");
        company.setContactPhone("9876543210");
        company.setLocation("Bangalore");
        company.setDescription("Enterprise software solutions provider");
        Company savedCompany = companyRepository.save(company);

        PlacementDrive drive = new PlacementDrive();
        drive.setJobTitle("Software Engineer");
        drive.setJobDescription("Full-stack Java and React development");
        drive.setLocation("Bangalore / Hybrid");
        drive.setMinimumCgpa(7.5);
        drive.setEligibilityCriteria("B.Tech / B.E in CS / IT with no active backlogs");
        drive.setSalary(850000.0);
        drive.setApplicationDeadline(LocalDate.of(2026, 12, 31));
        drive.setStatus("OPEN");
        drive.setCompany(savedCompany);

        PlacementDrive savedDrive = placementDriveRepository.save(drive);

        assertNotNull(savedDrive.getId(), "Generated placement drive ID should not be null");
        assertEquals("Software Engineer", savedDrive.getJobTitle());
        assertEquals("Full-stack Java and React development", savedDrive.getJobDescription());
        assertEquals("Bangalore / Hybrid", savedDrive.getLocation());
        assertEquals(7.5, savedDrive.getMinimumCgpa());
        assertEquals("B.Tech / B.E in CS / IT with no active backlogs", savedDrive.getEligibilityCriteria());
        assertEquals(850000.0, savedDrive.getSalary());
        assertEquals(LocalDate.of(2026, 12, 31), savedDrive.getApplicationDeadline());
        assertEquals("OPEN", savedDrive.getStatus());
        assertNotNull(savedDrive.getCompany());
        assertEquals(savedCompany.getId(), savedDrive.getCompany().getId());

        Optional<PlacementDrive> found = placementDriveRepository.findById(savedDrive.getId());
        assertTrue(found.isPresent());
        assertEquals("Software Engineer", found.get().getJobTitle());
        assertEquals(savedCompany.getId(), found.get().getCompany().getId());
    }

    @Test
    void testFindByCompanyId_ReturnsOnlyPlacementDrivesForCompany() {
        Company companyA = companyRepository.save(new Company("Comp A", "Tech", "https://a.com", "a@a.com", "111", "Pune", "Desc A"));
        Company companyB = companyRepository.save(new Company("Comp B", "Tech", "https://b.com", "b@b.com", "222", "Pune", "Desc B"));

        PlacementDrive driveA1 = new PlacementDrive("Dev 1", "Desc", "Pune", 7.0, "CS", 500000.0, LocalDate.now().plusMonths(1), "OPEN", companyA);
        PlacementDrive driveA2 = new PlacementDrive("Dev 2", "Desc", "Pune", 7.0, "CS", 600000.0, LocalDate.now().plusMonths(1), "OPEN", companyA);
        PlacementDrive driveB1 = new PlacementDrive("Dev 3", "Desc", "Pune", 7.0, "CS", 700000.0, LocalDate.now().plusMonths(1), "OPEN", companyB);

        placementDriveRepository.save(driveA1);
        placementDriveRepository.save(driveA2);
        placementDriveRepository.save(driveB1);

        List<PlacementDrive> companyADrives = placementDriveRepository.findByCompanyId(companyA.getId());
        assertEquals(2, companyADrives.size());

        List<PlacementDrive> companyBDrives = placementDriveRepository.findByCompanyId(companyB.getId());
        assertEquals(1, companyBDrives.size());
    }
}
