package com.riyabodwani.campusconnect;

import com.riyabodwani.campusconnect.dto.RecruiterRequestDto;
import com.riyabodwani.campusconnect.dto.RecruiterResponseDto;
import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.Recruiter;
import com.riyabodwani.campusconnect.exception.CompanyNotFoundException;
import com.riyabodwani.campusconnect.exception.RecruiterEmailAlreadyExistsException;
import com.riyabodwani.campusconnect.repository.ApplicationRepository;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.repository.RecruiterRepository;
import com.riyabodwani.campusconnect.service.RecruiterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RecruiterServiceTest {

    @Autowired
    private RecruiterService recruiterService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private Company savedCompany;

    @BeforeEach
    void setUp() {
        cleanUp();

        Company company = new Company("Apex Systems", "Technology", "https://apex.com", "info@apex.com", "9876543210", "Pune", "IT Company");
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
    void testRegisterRecruiter_Success() {
        RecruiterRequestDto requestDto = new RecruiterRequestDto(
                "Sarah Connor",
                "sarah@apex.com",
                "Password123!",
                "9876543210",
                savedCompany.getId()
        );

        RecruiterResponseDto responseDto = recruiterService.registerRecruiter(requestDto);

        assertNotNull(responseDto.getId());
        assertEquals("Sarah Connor", responseDto.getFullName());
        assertEquals("sarah@apex.com", responseDto.getEmail());
        assertEquals("9876543210", responseDto.getPhoneNumber());
        assertEquals(savedCompany.getId(), responseDto.getCompanyId());
        assertEquals("Apex Systems", responseDto.getCompanyName());

        // Verify password in DB is BCrypt-hashed
        Recruiter savedEntity = recruiterRepository.findById(responseDto.getId()).orElseThrow();
        assertNotEquals("Password123!", savedEntity.getPassword());
        assertTrue(passwordEncoder.matches("Password123!", savedEntity.getPassword()));
    }

    @Test
    void testRegisterRecruiter_DuplicateEmail_ThrowsRecruiterEmailAlreadyExistsException() {
        RecruiterRequestDto requestDto1 = new RecruiterRequestDto(
                "Sarah Connor",
                "sarah@apex.com",
                "Password123!",
                "9876543210",
                savedCompany.getId()
        );
        recruiterService.registerRecruiter(requestDto1);

        RecruiterRequestDto requestDto2 = new RecruiterRequestDto(
                "Sarah Clone",
                "sarah@apex.com",
                "OtherPass123!",
                "9876543211",
                savedCompany.getId()
        );

        assertThrows(RecruiterEmailAlreadyExistsException.class, () ->
                recruiterService.registerRecruiter(requestDto2));
    }

    @Test
    void testRegisterRecruiter_NonExistentCompany_ThrowsCompanyNotFoundException() {
        RecruiterRequestDto requestDto = new RecruiterRequestDto(
                "John Doe",
                "john@nowhere.com",
                "Password123!",
                "9876543210",
                999999L
        );

        assertThrows(CompanyNotFoundException.class, () ->
                recruiterService.registerRecruiter(requestDto));
    }

    @Test
    void testLogin_Success() {
        RecruiterRequestDto registerDto = new RecruiterRequestDto(
                "Bruce Wayne",
                "bruce@apex.com",
                "BatPass123!",
                "9876543210",
                savedCompany.getId()
        );
        recruiterService.registerRecruiter(registerDto);

        com.riyabodwani.campusconnect.dto.RecruiterLoginRequestDto loginDto =
                new com.riyabodwani.campusconnect.dto.RecruiterLoginRequestDto("bruce@apex.com", "BatPass123!");

        com.riyabodwani.campusconnect.dto.RecruiterLoginResponseDto response = recruiterService.login(loginDto);

        assertNotNull(response.getToken());
        assertEquals("Bruce Wayne", response.getFullName());
        assertEquals("bruce@apex.com", response.getEmail());
        assertEquals("9876543210", response.getPhoneNumber());
        assertEquals(savedCompany.getId(), response.getCompanyId());
        assertEquals("Apex Systems", response.getCompanyName());
    }

    @Test
    void testLogin_WrongPassword_ThrowsInvalidRecruiterCredentialsException() {
        RecruiterRequestDto registerDto = new RecruiterRequestDto(
                "Clark Kent",
                "clark@apex.com",
                "SuperPass123!",
                "9876543210",
                savedCompany.getId()
        );
        recruiterService.registerRecruiter(registerDto);

        com.riyabodwani.campusconnect.dto.RecruiterLoginRequestDto loginDto =
                new com.riyabodwani.campusconnect.dto.RecruiterLoginRequestDto("clark@apex.com", "WrongPassword!");

        com.riyabodwani.campusconnect.exception.InvalidRecruiterCredentialsException ex =
                assertThrows(com.riyabodwani.campusconnect.exception.InvalidRecruiterCredentialsException.class, () ->
                        recruiterService.login(loginDto));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void testLogin_NonExistentEmail_ThrowsInvalidRecruiterCredentialsException() {
        com.riyabodwani.campusconnect.dto.RecruiterLoginRequestDto loginDto =
                new com.riyabodwani.campusconnect.dto.RecruiterLoginRequestDto("nonexistent@apex.com", "SomePass123!");

        com.riyabodwani.campusconnect.exception.InvalidRecruiterCredentialsException ex =
                assertThrows(com.riyabodwani.campusconnect.exception.InvalidRecruiterCredentialsException.class, () ->
                        recruiterService.login(loginDto));
        assertEquals("Invalid email or password", ex.getMessage());
    }
}
