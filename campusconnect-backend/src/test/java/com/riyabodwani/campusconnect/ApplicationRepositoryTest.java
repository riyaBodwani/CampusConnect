package com.riyabodwani.campusconnect;

import com.riyabodwani.campusconnect.entity.Application;
import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.entity.Student;
import com.riyabodwani.campusconnect.repository.ApplicationRepository;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.repository.RecruiterRepository;
import com.riyabodwani.campusconnect.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @AfterEach
    void cleanUp() {
        applicationRepository.deleteAll();
        placementDriveRepository.deleteAll();
        recruiterRepository.deleteAll();
        companyRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    void testSaveAndFindApplication_VerifiesRelationshipsAndPersistence() {
        Student student = new Student();
        student.setFullName("John Doe");
        student.setEmail("john.doe@example.com");
        student.setPassword("password123");
        student.setCgpa(8.5);
        Student savedStudent = studentRepository.save(student);

        Company company = new Company();
        company.setName("Tech Corp");
        company.setIndustry("Software");
        Company savedCompany = companyRepository.save(company);

        PlacementDrive drive = new PlacementDrive();
        drive.setJobTitle("Software Engineer");
        drive.setCompany(savedCompany);
        drive.setLocation("Pune");
        drive.setSalary(900000.0);
        drive.setMinimumCgpa(7.5);
        drive.setStatus("OPEN");
        drive.setApplicationDeadline(LocalDate.of(2026, 12, 31));
        PlacementDrive savedDrive = placementDriveRepository.save(drive);

        Application application = new Application();
        application.setStudent(savedStudent);
        application.setPlacementDrive(savedDrive);
        application.setAppliedDate(LocalDate.of(2026, 9, 24));
        application.setStatus("APPLIED");

        Application savedApplication = applicationRepository.save(application);

        assertNotNull(savedApplication.getId(), "Application ID should be generated");
        assertEquals(LocalDate.of(2026, 9, 24), savedApplication.getAppliedDate());
        assertEquals("APPLIED", savedApplication.getStatus());
        assertNotNull(savedApplication.getStudent());
        assertEquals(savedStudent.getId(), savedApplication.getStudent().getId());
        assertNotNull(savedApplication.getPlacementDrive());
        assertEquals(savedDrive.getId(), savedApplication.getPlacementDrive().getId());

        Optional<Application> found = applicationRepository.findById(savedApplication.getId());
        assertTrue(found.isPresent(), "Saved application should be found by ID");
        assertEquals(savedStudent.getId(), found.get().getStudent().getId());
        assertEquals(savedDrive.getId(), found.get().getPlacementDrive().getId());
        assertEquals("APPLIED", found.get().getStatus());
    }
}
