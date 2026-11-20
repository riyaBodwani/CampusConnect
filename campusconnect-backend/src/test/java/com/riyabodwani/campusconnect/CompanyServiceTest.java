package com.riyabodwani.campusconnect;

import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.exception.CompanyNotFoundException;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CompanyServiceTest {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll();
    }

    private Company buildCompany(String name) {
        Company company = new Company();
        company.setName(name);
        company.setIndustry("Technology");
        company.setWebsite("https://" + name.toLowerCase().replaceAll("\\s+", "") + ".com");
        company.setContactEmail("hr@" + name.toLowerCase().replaceAll("\\s+", "") + ".com");
        company.setContactPhone("9876543210");
        company.setLocation("Bangalore");
        company.setDescription("Top tech company");
        return company;
    }

    @Test
    void testCreateCompany_Success() {
        Company company = buildCompany("Google");
        Company created = companyService.createCompany(company);

        assertNotNull(created.getId());
        assertEquals("Google", created.getName());
        assertEquals("Technology", created.getIndustry());
        assertEquals("https://google.com", created.getWebsite());
        assertEquals("hr@google.com", created.getContactEmail());
    }

    @Test
    void testGetAllCompanies_ReturnsList() {
        companyService.createCompany(buildCompany("Company One"));
        companyService.createCompany(buildCompany("Company Two"));

        List<Company> companies = companyService.getAllCompanies();
        assertEquals(2, companies.size());
    }

    @Test
    void testGetCompanyById_Success() {
        Company saved = companyService.createCompany(buildCompany("Amazon"));

        Company found = companyService.getCompanyById(saved.getId());
        assertNotNull(found);
        assertEquals("Amazon", found.getName());
    }

    @Test
    void testGetCompanyById_NotFound_ThrowsException() {
        assertThrows(CompanyNotFoundException.class, () -> companyService.getCompanyById(999999L));
    }

    @Test
    void testUpdateCompany_Success() {
        Company saved = companyService.createCompany(buildCompany("Microsoft"));

        Company updateInfo = new Company();
        updateInfo.setName("Microsoft Corporation");
        updateInfo.setIndustry("Cloud & Software");
        updateInfo.setWebsite("https://microsoft.com");
        updateInfo.setContactEmail("recruitment@microsoft.com");
        updateInfo.setContactPhone("1234567890");
        updateInfo.setLocation("Hyderabad");
        updateInfo.setDescription("Global tech enterprise");

        Company updated = companyService.updateCompany(saved.getId(), updateInfo);

        assertEquals(saved.getId(), updated.getId());
        assertEquals("Microsoft Corporation", updated.getName());
        assertEquals("Cloud & Software", updated.getIndustry());
        assertEquals("recruitment@microsoft.com", updated.getContactEmail());
        assertEquals("Hyderabad", updated.getLocation());
    }

    @Test
    void testUpdateCompany_NotFound_ThrowsException() {
        Company updateInfo = buildCompany("Nonexistent");
        assertThrows(CompanyNotFoundException.class, () -> companyService.updateCompany(999999L, updateInfo));
    }

    @Test
    void testDeleteCompany_Success() {
        Company saved = companyService.createCompany(buildCompany("Netflix"));

        companyService.deleteCompany(saved.getId());

        assertFalse(companyRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testDeleteCompany_NotFound_ThrowsException() {
        assertThrows(CompanyNotFoundException.class, () -> companyService.deleteCompany(999999L));
    }
}
