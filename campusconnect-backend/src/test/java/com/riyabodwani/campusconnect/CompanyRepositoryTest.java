package com.riyabodwani.campusconnect;

import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @AfterEach
    void cleanUp() {
        companyRepository.deleteAll();
    }

    @Test
    void testSaveAndFindCompany_VerifiesTableCreationAndFieldPersistence() {
        Company company = new Company();
        company.setName("Acme Tech Solutions");
        company.setIndustry("Information Technology");
        company.setWebsite("https://acmetech.example.com");
        company.setContactEmail("contact@acmetech.example.com");
        company.setContactPhone("9876543210");
        company.setLocation("Pune, Maharashtra");
        company.setDescription("A leading software engineering firm specializing in cloud solutions.");

        Company saved = companyRepository.save(company);

        assertNotNull(saved.getId(), "Generated ID should not be null");
        assertEquals("Acme Tech Solutions", saved.getName());
        assertEquals("Information Technology", saved.getIndustry());
        assertEquals("https://acmetech.example.com", saved.getWebsite());
        assertEquals("contact@acmetech.example.com", saved.getContactEmail());
        assertEquals("9876543210", saved.getContactPhone());
        assertEquals("Pune, Maharashtra", saved.getLocation());
        assertEquals("A leading software engineering firm specializing in cloud solutions.", saved.getDescription());

        Optional<Company> found = companyRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Acme Tech Solutions", found.get().getName());
        assertTrue(companyRepository.existsByName("Acme Tech Solutions"));
    }
}
