package com.riyabodwani.campusconnect;

import com.riyabodwani.campusconnect.dto.ApplicationRequestDto;
import com.riyabodwani.campusconnect.dto.ApplicationResponseDto;
import com.riyabodwani.campusconnect.entity.Application;
import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.entity.Student;
import com.riyabodwani.campusconnect.exception.ApplicationCannotBeWithdrawnException;
import com.riyabodwani.campusconnect.exception.ApplicationNotFoundException;
import com.riyabodwani.campusconnect.exception.DuplicateApplicationException;
import com.riyabodwani.campusconnect.exception.PlacementDriveNotFoundException;
import com.riyabodwani.campusconnect.exception.StudentNotFoundException;
import com.riyabodwani.campusconnect.exception.UnauthorizedApplicationAccessException;
import com.riyabodwani.campusconnect.repository.ApplicationRepository;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.repository.StudentRepository;
import com.riyabodwani.campusconnect.service.ApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private com.riyabodwani.campusconnect.repository.RecruiterRepository recruiterRepository;

    private Student savedStudent;
    private PlacementDrive savedDrive;
    private Company savedCompany;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        Student student = new Student();
        student.setFullName("Alice Smith");
        student.setEmail("alice@example.com");
        student.setPassword("secret123");
        savedStudent = studentRepository.save(student);

        Company company = new Company("Google", "Tech", "https://google.com", "jobs@google.com", "1234567890", "Bangalore", "Tech giant");
        savedCompany = companyRepository.save(company);

        PlacementDrive drive = new PlacementDrive("SWE", "Backend SWE", "Bangalore", 8.0, "CS only", 1500000.0, LocalDate.of(2026, 12, 31), "OPEN", savedCompany);
        savedDrive = placementDriveRepository.save(drive);
    }

    @AfterEach
    void cleanUp() {
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
    void testApply_Success() {
        ApplicationRequestDto requestDto = new ApplicationRequestDto(savedDrive.getId());

        Application application = applicationService.apply(savedStudent.getEmail(), requestDto);

        assertNotNull(application.getId());
        assertEquals("APPLIED", application.getStatus());
        assertEquals(LocalDate.now(), application.getAppliedDate());
        assertEquals(savedStudent.getId(), application.getStudent().getId());
        assertEquals(savedDrive.getId(), application.getPlacementDrive().getId());
    }

    @Test
    void testApply_MissingPlacementDrive_ThrowsPlacementDriveNotFoundException() {
        ApplicationRequestDto requestDto = new ApplicationRequestDto(999999L);

        assertThrows(PlacementDriveNotFoundException.class, () ->
                applicationService.apply(savedStudent.getEmail(), requestDto));
    }

    @Test
    void testApply_NullPlacementDriveId_ThrowsPlacementDriveNotFoundException() {
        ApplicationRequestDto requestDto = new ApplicationRequestDto(null);

        assertThrows(PlacementDriveNotFoundException.class, () ->
                applicationService.apply(savedStudent.getEmail(), requestDto));
    }

    @Test
    void testApply_DuplicateApplication_ThrowsDuplicateApplicationException() {
        ApplicationRequestDto requestDto = new ApplicationRequestDto(savedDrive.getId());

        applicationService.apply(savedStudent.getEmail(), requestDto);

        DuplicateApplicationException ex = assertThrows(DuplicateApplicationException.class, () ->
                applicationService.apply(savedStudent.getEmail(), requestDto));
        assertEquals("Student has already applied to this placement drive", ex.getMessage());
    }

    @Test
    void testApply_NonExistentStudent_ThrowsStudentNotFoundException() {
        ApplicationRequestDto requestDto = new ApplicationRequestDto(savedDrive.getId());

        assertThrows(StudentNotFoundException.class, () ->
                applicationService.apply("nonexistent@example.com", requestDto));
    }

    @Test
    void testGetMyApplications_ReturnsOnlyStudentApplications() {
        Student otherStudent = new Student();
        otherStudent.setFullName("Bob Jones");
        otherStudent.setEmail("bob@example.com");
        otherStudent.setPassword("secret123");
        Student savedOther = studentRepository.save(otherStudent);

        // Apply for savedStudent
        applicationService.apply(savedStudent.getEmail(), new ApplicationRequestDto(savedDrive.getId()));

        java.util.List<ApplicationResponseDto> aliceApps = applicationService.getMyApplications(savedStudent.getEmail());
        assertEquals(1, aliceApps.size());
        assertEquals(savedStudent.getId(), aliceApps.get(0).getStudent().getId());
        assertEquals(savedDrive.getId(), aliceApps.get(0).getPlacementDrive().getId());
        assertEquals(savedCompany.getId(), aliceApps.get(0).getPlacementDrive().getCompanyId());
        assertEquals(savedCompany.getName(), aliceApps.get(0).getPlacementDrive().getCompanyName());

        // Other student has no applications
        java.util.List<ApplicationResponseDto> bobApps = applicationService.getMyApplications(savedOther.getEmail());
        assertTrue(bobApps.isEmpty());
    }

    @Test
    void testGetApplicationsForRecruiter_Success() {
        com.riyabodwani.campusconnect.entity.Recruiter recruiter = recruiterRepository.save(
                new com.riyabodwani.campusconnect.entity.Recruiter("Recruiter One", "rec.one@google.com", "pass123", "9998887770", savedCompany)
        );

        // Apply student
        applicationService.apply(savedStudent.getEmail(), new ApplicationRequestDto(savedDrive.getId()));

        java.util.List<com.riyabodwani.campusconnect.dto.RecruiterApplicationResponseDto> apps =
                applicationService.getApplicationsForRecruiter(recruiter.getEmail());

        assertEquals(1, apps.size());
        assertEquals("APPLIED", apps.get(0).getStatus());
        assertEquals(savedDrive.getId(), apps.get(0).getPlacementDrive().getId());
        assertEquals(savedCompany.getId(), apps.get(0).getPlacementDrive().getCompanyId());
        assertEquals(savedCompany.getName(), apps.get(0).getPlacementDrive().getCompanyName());
        assertEquals(savedStudent.getId(), apps.get(0).getStudent().getId());
        assertEquals(savedStudent.getEmail(), apps.get(0).getStudent().getEmail());
    }

    @Test
    void testGetApplicationsForRecruiter_NonExistentRecruiter_ThrowsException() {
        assertThrows(com.riyabodwani.campusconnect.exception.RecruiterNotFoundException.class, () ->
                applicationService.getApplicationsForRecruiter("notfound@recruiter.com"));
    }

    @Test
    void testGetApplicationsForRecruiter_EmptyApplications_ReturnsEmptyList() {
        com.riyabodwani.campusconnect.entity.Recruiter recruiter = recruiterRepository.save(
                new com.riyabodwani.campusconnect.entity.Recruiter("Recruiter One", "rec.empty@google.com", "pass123", "9998887770", savedCompany)
        );

        java.util.List<com.riyabodwani.campusconnect.dto.RecruiterApplicationResponseDto> apps =
                applicationService.getApplicationsForRecruiter(recruiter.getEmail());

        assertTrue(apps.isEmpty());
    }

    @Test
    void testUpdateApplicationStatus_Success() {
        com.riyabodwani.campusconnect.entity.Recruiter recruiter = recruiterRepository.save(
                new com.riyabodwani.campusconnect.entity.Recruiter("Recruiter One", "rec.status@google.com", "pass123", "9998887770", savedCompany)
        );

        Application application = applicationService.apply(savedStudent.getEmail(), new ApplicationRequestDto(savedDrive.getId()));

        com.riyabodwani.campusconnect.dto.RecruiterApplicationResponseDto result =
                applicationService.updateApplicationStatus(recruiter.getEmail(), application.getId(),
                        new com.riyabodwani.campusconnect.dto.ApplicationStatusUpdateRequestDto("SHORTLISTED"));

        assertEquals("SHORTLISTED", result.getStatus());
        assertEquals(application.getId(), result.getId());
        assertEquals(savedDrive.getId(), result.getPlacementDrive().getId());
        assertEquals(savedCompany.getId(), result.getPlacementDrive().getCompanyId());
        assertEquals(savedCompany.getName(), result.getPlacementDrive().getCompanyName());
    }

    @Test
    void testUpdateApplicationStatus_InvalidStatus_ThrowsException() {
        com.riyabodwani.campusconnect.entity.Recruiter recruiter = recruiterRepository.save(
                new com.riyabodwani.campusconnect.entity.Recruiter("Recruiter One", "rec.invalid@google.com", "pass123", "9998887770", savedCompany)
        );

        Application application = applicationService.apply(savedStudent.getEmail(), new ApplicationRequestDto(savedDrive.getId()));

        assertThrows(com.riyabodwani.campusconnect.exception.InvalidApplicationStatusException.class, () ->
                applicationService.updateApplicationStatus(recruiter.getEmail(), application.getId(),
                        new com.riyabodwani.campusconnect.dto.ApplicationStatusUpdateRequestDto("INVALID_STATUS")));
    }

    @Test
    void testUpdateApplicationStatus_ApplicationNotFound_ThrowsException() {
        com.riyabodwani.campusconnect.entity.Recruiter recruiter = recruiterRepository.save(
                new com.riyabodwani.campusconnect.entity.Recruiter("Recruiter One", "rec.notfound@google.com", "pass123", "9998887770", savedCompany)
        );

        assertThrows(com.riyabodwani.campusconnect.exception.ApplicationNotFoundException.class, () ->
                applicationService.updateApplicationStatus(recruiter.getEmail(), 999999L,
                        new com.riyabodwani.campusconnect.dto.ApplicationStatusUpdateRequestDto("SHORTLISTED")));
    }

    @Test
    void testUpdateApplicationStatus_AnotherCompany_ThrowsException() {
        Company otherCompany = companyRepository.save(new Company("Other Corp", "Tech", "https://other.com", "hr@other.com", "1231231234", "Delhi", "Other"));
        com.riyabodwani.campusconnect.entity.Recruiter otherRecruiter = recruiterRepository.save(
                new com.riyabodwani.campusconnect.entity.Recruiter("Other Recruiter", "other.rec@other.com", "pass123", "9998887771", otherCompany)
        );

        // Application belongs to savedCompany (Google)
        Application application = applicationService.apply(savedStudent.getEmail(), new ApplicationRequestDto(savedDrive.getId()));

        // Other recruiter tries to update Google's application
        assertThrows(UnauthorizedApplicationAccessException.class, () ->
                applicationService.updateApplicationStatus(otherRecruiter.getEmail(), application.getId(),
                        new com.riyabodwani.campusconnect.dto.ApplicationStatusUpdateRequestDto("SHORTLISTED")));
    }

    @Test
    void testWithdrawApplication_Success() {
        Application application = applicationService.apply(savedStudent.getEmail(), new ApplicationRequestDto(savedDrive.getId()));
        Long appId = application.getId();

        applicationService.withdrawApplication(savedStudent.getEmail(), appId);

        assertTrue(applicationRepository.findById(appId).isEmpty());
        assertTrue(studentRepository.findById(savedStudent.getId()).isPresent());
        assertTrue(placementDriveRepository.findById(savedDrive.getId()).isPresent());
    }

    @Test
    void testWithdrawApplication_StudentNotFound_ThrowsException() {
        Application application = applicationService.apply(savedStudent.getEmail(), new ApplicationRequestDto(savedDrive.getId()));
        Long appId = application.getId();

        assertThrows(StudentNotFoundException.class, () ->
                applicationService.withdrawApplication("nonexistent@example.com", appId));
    }

    @Test
    void testWithdrawApplication_ApplicationNotFound_ThrowsException() {
        assertThrows(ApplicationNotFoundException.class, () ->
                applicationService.withdrawApplication(savedStudent.getEmail(), 999999L));
    }

    @Test
    void testWithdrawApplication_UnauthorizedStudent_ThrowsException() {
        Student otherStudent = new Student();
        otherStudent.setFullName("Bob Jones");
        otherStudent.setEmail("bob@example.com");
        otherStudent.setPassword("secret123");
        Student savedOther = studentRepository.save(otherStudent);

        Application application = applicationService.apply(savedStudent.getEmail(), new ApplicationRequestDto(savedDrive.getId()));
        Long appId = application.getId();

        assertThrows(UnauthorizedApplicationAccessException.class, () ->
                applicationService.withdrawApplication(savedOther.getEmail(), appId));

        assertTrue(applicationRepository.findById(appId).isPresent());
    }

    @Test
    void testWithdrawApplication_StatusNotApplied_ThrowsException() {
        com.riyabodwani.campusconnect.entity.Recruiter recruiter = recruiterRepository.save(
                new com.riyabodwani.campusconnect.entity.Recruiter("Recruiter One", "rec.status@google.com", "pass123", "9998887770", savedCompany)
        );

        Application application = applicationService.apply(savedStudent.getEmail(), new ApplicationRequestDto(savedDrive.getId()));
        Long appId = application.getId();

        // Update to SHORTLISTED
        applicationService.updateApplicationStatus(recruiter.getEmail(), appId,
                new com.riyabodwani.campusconnect.dto.ApplicationStatusUpdateRequestDto("SHORTLISTED"));

        assertThrows(ApplicationCannotBeWithdrawnException.class, () ->
                applicationService.withdrawApplication(savedStudent.getEmail(), appId));

        assertTrue(applicationRepository.findById(appId).isPresent());
    }
}
