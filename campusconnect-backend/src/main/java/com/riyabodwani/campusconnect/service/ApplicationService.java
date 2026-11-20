package com.riyabodwani.campusconnect.service;

import com.riyabodwani.campusconnect.dto.ApplicationRequestDto;
import com.riyabodwani.campusconnect.dto.ApplicationResponseDto;
import com.riyabodwani.campusconnect.entity.Application;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.entity.Student;
import com.riyabodwani.campusconnect.dto.ApplicationStatusUpdateRequestDto;
import com.riyabodwani.campusconnect.dto.RecruiterApplicationResponseDto;
import com.riyabodwani.campusconnect.entity.ApplicationStatus;
import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.Recruiter;
import com.riyabodwani.campusconnect.exception.ApplicationCannotBeWithdrawnException;
import com.riyabodwani.campusconnect.exception.ApplicationNotFoundException;
import com.riyabodwani.campusconnect.exception.DuplicateApplicationException;
import com.riyabodwani.campusconnect.exception.InvalidApplicationStatusException;
import com.riyabodwani.campusconnect.exception.PlacementDriveNotFoundException;
import com.riyabodwani.campusconnect.exception.RecruiterNotFoundException;
import com.riyabodwani.campusconnect.exception.StudentNotFoundException;
import com.riyabodwani.campusconnect.exception.UnauthorizedApplicationAccessException;
import com.riyabodwani.campusconnect.repository.ApplicationRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.repository.RecruiterRepository;
import com.riyabodwani.campusconnect.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    public Application apply(String studentEmail, ApplicationRequestDto requestDto) {
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new StudentNotFoundException("Student not found for email: " + studentEmail));

        Long placementDriveId = requestDto != null ? requestDto.getPlacementDriveId() : null;
        if (placementDriveId == null) {
            throw new PlacementDriveNotFoundException("Placement drive ID must be provided");
        }

        PlacementDrive placementDrive = placementDriveRepository.findById(placementDriveId)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Placement drive with id " + placementDriveId + " not found"));

        if (applicationRepository.existsByStudentIdAndPlacementDriveId(student.getId(), placementDrive.getId())) {
            throw new DuplicateApplicationException("Student has already applied to this placement drive");
        }

        Application application = new Application();
        application.setStudent(student);
        application.setPlacementDrive(placementDrive);
        application.setAppliedDate(LocalDate.now());
        application.setStatus("APPLIED");

        return applicationRepository.save(application);
    }

    public List<ApplicationResponseDto> getMyApplications(String studentEmail) {
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new StudentNotFoundException("Student not found for email: " + studentEmail));
        return applicationRepository.findByStudentId(student.getId()).stream()
                .map(ApplicationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RecruiterApplicationResponseDto> getApplicationsForRecruiter(String recruiterEmail) {
        Recruiter recruiter = recruiterRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RecruiterNotFoundException("Recruiter not found for email: " + recruiterEmail));

        Company company = recruiter.getCompany();
        if (company == null) {
            return Collections.emptyList();
        }

        List<Application> applications = applicationRepository.findByPlacementDriveCompanyId(company.getId());
        return applications.stream()
                .map(RecruiterApplicationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public RecruiterApplicationResponseDto updateApplicationStatus(
            String recruiterEmail,
            Long applicationId,
            ApplicationStatusUpdateRequestDto requestDto) {

        if (requestDto == null || requestDto.getStatus() == null || requestDto.getStatus().isBlank()) {
            throw new InvalidApplicationStatusException("Status is required");
        }

        String normalizedStatus = requestDto.getStatus().trim().toUpperCase();
        if (!ApplicationStatus.isValid(normalizedStatus)) {
            throw new InvalidApplicationStatusException("Invalid application status: " + requestDto.getStatus() +
                    ". Allowed values: APPLIED, SHORTLISTED, REJECTED, SELECTED");
        }

        Recruiter recruiter = recruiterRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RecruiterNotFoundException("Recruiter not found for email: " + recruiterEmail));

        Company recruiterCompany = recruiter.getCompany();
        if (recruiterCompany == null) {
            throw new UnauthorizedApplicationAccessException("Recruiter is not associated with any company");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application with id " + applicationId + " not found"));

        PlacementDrive drive = application.getPlacementDrive();
        if (drive == null || drive.getCompany() == null || !drive.getCompany().getId().equals(recruiterCompany.getId())) {
            throw new UnauthorizedApplicationAccessException("Recruiter does not have permission to update applications for another company");
        }

        application.setStatus(normalizedStatus);
        Application updated = applicationRepository.save(application);

        return RecruiterApplicationResponseDto.fromEntity(updated);
    }

    public void withdrawApplication(String studentEmail, Long applicationId) {
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new StudentNotFoundException("Student not found for email: " + studentEmail));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application with id " + applicationId + " not found"));

        if (application.getStudent() == null || !application.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedApplicationAccessException("You are not authorized to withdraw another student's application");
        }

        if (application.getStatus() == null || !"APPLIED".equalsIgnoreCase(application.getStatus())) {
            throw new ApplicationCannotBeWithdrawnException("Application cannot be withdrawn because its status is " + application.getStatus() + ". Only applications with status 'APPLIED' can be withdrawn.");
        }

        applicationRepository.delete(application);
    }
}
