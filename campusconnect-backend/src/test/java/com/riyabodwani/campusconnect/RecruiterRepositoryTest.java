package com.riyabodwani.campusconnect;

import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.Recruiter;
import com.riyabodwani.campusconnect.repository.ApplicationRepository;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.repository.RecruiterRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RecruiterRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company savedCompany;

    @BeforeEach
    void setUp() {
        cleanUp();

        Company company = new Company();
        company.setName("Acme Innovations");
        company.setIndustry("Technology");
        company.setWebsite("https://acme.example.com");
        company.setContactEmail("hr@acme.example.com");
        company.setContactPhone("9876543210");
        company.setLocation("Bangalore");
        company.setDescription("Tech solutions company");
        savedCompany = companyRepository.save(company);
    }

    @AfterEach
    void cleanUp() {
        applicationRepository.deleteAll();
        placementDriveRepository.deleteAll();
        recruiterRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void test1_SaveAndRetrieveRecruiter() {
        Recruiter recruiter = new Recruiter();
        recruiter.setFullName("Jane Doe");
        recruiter.setEmail("jane.doe@acme.example.com");
        recruiter.setPassword("securePassword123");
        recruiter.setPhoneNumber("9876543210");
        recruiter.setCompany(savedCompany);

        Recruiter saved = recruiterRepository.save(recruiter);

        assertNotNull(saved.getId(), "Recruiter ID should be generated");
        assertEquals("Jane Doe", saved.getFullName());
        assertEquals("jane.doe@acme.example.com", saved.getEmail());
        assertEquals("securePassword123", saved.getPassword());
        assertEquals("9876543210", saved.getPhoneNumber());

        Optional<Recruiter> retrieved = recruiterRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent(), "Saved recruiter should be found by ID");
        assertEquals("Jane Doe", retrieved.get().getFullName());
    }

    @Test
    void test2_FindByEmail() {
        Recruiter recruiter = new Recruiter("Bob Smith", "bob.smith@acme.example.com", "password456", "9876543211", savedCompany);
        recruiterRepository.save(recruiter);

        Optional<Recruiter> found = recruiterRepository.findByEmail("bob.smith@acme.example.com");
        assertTrue(found.isPresent(), "Recruiter should be found by email");
        assertEquals("Bob Smith", found.get().getFullName());

        Optional<Recruiter> notFound = recruiterRepository.findByEmail("nonexistent@acme.example.com");
        assertFalse(notFound.isPresent(), "Non-existent recruiter email should not be found");
    }

    @Test
    void test3_ExistsByEmail_ReturnsTrueForExistingEmail() {
        Recruiter recruiter = new Recruiter("Alice Wonder", "alice.wonder@acme.example.com", "password789", "9876543212", savedCompany);
        recruiterRepository.save(recruiter);

        boolean exists = recruiterRepository.existsByEmail("alice.wonder@acme.example.com");
        assertTrue(exists, "existsByEmail should return true for existing email");
    }

    @Test
    void test4_ExistsByEmail_ReturnsFalseForNonExistingEmail() {
        boolean exists = recruiterRepository.existsByEmail("nobody@acme.example.com");
        assertFalse(exists, "existsByEmail should return false for non-existing email");
    }

    @Test
    void test5_RecruiterCorrectlyAssociatedWithCompany() {
        Recruiter recruiter = new Recruiter("Charlie Brown", "charlie@acme.example.com", "password321", "9876543213", savedCompany);
        Recruiter saved = recruiterRepository.save(recruiter);

        Optional<Recruiter> retrieved = recruiterRepository.findById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertNotNull(retrieved.get().getCompany(), "Associated company should not be null");
        assertEquals(savedCompany.getId(), retrieved.get().getCompany().getId());
        assertEquals("Acme Innovations", retrieved.get().getCompany().getName());
    }
}
