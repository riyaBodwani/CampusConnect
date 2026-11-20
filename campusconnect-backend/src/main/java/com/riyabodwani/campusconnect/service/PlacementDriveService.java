package com.riyabodwani.campusconnect.service;

import com.riyabodwani.campusconnect.dto.PlacementDriveResponseDto;
import com.riyabodwani.campusconnect.dto.RecruiterPlacementDriveRequestDto;
import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.entity.Recruiter;
import com.riyabodwani.campusconnect.exception.CompanyNotFoundException;
import com.riyabodwani.campusconnect.exception.PlacementDriveNotFoundException;
import com.riyabodwani.campusconnect.exception.RecruiterNotFoundException;
import com.riyabodwani.campusconnect.exception.UnauthorizedPlacementDriveAccessException;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.PlacementDriveRepository;
import com.riyabodwani.campusconnect.repository.RecruiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlacementDriveService {

    @Autowired
    private PlacementDriveRepository placementDriveRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    public List<PlacementDrive> getAllPlacementDrives() {
        return placementDriveRepository.findAll();
    }

    public PlacementDrive getPlacementDriveById(Long id) {
        return placementDriveRepository.findById(id)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Placement drive with id " + id + " not found"));
    }

    public PlacementDrive createPlacementDrive(PlacementDrive placementDrive) {
        if (placementDrive.getCompany() == null || placementDrive.getCompany().getId() == null) {
            throw new CompanyNotFoundException("A valid existing company must be specified for a placement drive");
        }
        Company company = companyRepository.findById(placementDrive.getCompany().getId())
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + placementDrive.getCompany().getId() + " not found"));
        placementDrive.setCompany(company);
        return placementDriveRepository.save(placementDrive);
    }

    public PlacementDrive updatePlacementDrive(Long id, PlacementDrive updated) {
        PlacementDrive existingDrive = placementDriveRepository.findById(id)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Placement drive with id " + id + " not found"));

        if (updated.getCompany() == null || updated.getCompany().getId() == null) {
            throw new CompanyNotFoundException("A valid existing company must be specified for a placement drive");
        }
        Company company = companyRepository.findById(updated.getCompany().getId())
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + updated.getCompany().getId() + " not found"));

        existingDrive.setJobTitle(updated.getJobTitle());
        existingDrive.setJobDescription(updated.getJobDescription());
        existingDrive.setLocation(updated.getLocation());
        existingDrive.setMinimumCgpa(updated.getMinimumCgpa());
        existingDrive.setEligibilityCriteria(updated.getEligibilityCriteria());
        existingDrive.setSalary(updated.getSalary());
        existingDrive.setApplicationDeadline(updated.getApplicationDeadline());
        existingDrive.setStatus(updated.getStatus());
        existingDrive.setCompany(company);

        return placementDriveRepository.save(existingDrive);
    }

    public void deletePlacementDrive(Long id) {
        PlacementDrive existingDrive = placementDriveRepository.findById(id)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Placement drive with id " + id + " not found"));
        placementDriveRepository.delete(existingDrive);
    }

    public PlacementDriveResponseDto createPlacementDriveForRecruiter(String recruiterEmail, RecruiterPlacementDriveRequestDto requestDto) {
        Recruiter recruiter = recruiterRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RecruiterNotFoundException("Recruiter with email " + recruiterEmail + " not found"));

        Company company = recruiter.getCompany();
        if (company == null) {
            throw new CompanyNotFoundException("Recruiter is not associated with any company");
        }

        String status = (requestDto.getStatus() != null && !requestDto.getStatus().isBlank())
                ? requestDto.getStatus().trim()
                : "OPEN";

        PlacementDrive placementDrive = new PlacementDrive();
        placementDrive.setJobTitle(requestDto.getJobTitle());
        placementDrive.setJobDescription(requestDto.getJobDescription());
        placementDrive.setLocation(requestDto.getLocation());
        placementDrive.setMinimumCgpa(requestDto.getMinimumCgpa());
        placementDrive.setEligibilityCriteria(requestDto.getEligibilityCriteria());
        placementDrive.setSalary(requestDto.getSalary());
        placementDrive.setApplicationDeadline(requestDto.getApplicationDeadline());
        placementDrive.setStatus(status);
        placementDrive.setCompany(company);

        PlacementDrive savedDrive = placementDriveRepository.save(placementDrive);
        return PlacementDriveResponseDto.fromEntity(savedDrive);
    }

    public List<PlacementDriveResponseDto> getPlacementDrivesForRecruiter(String recruiterEmail) {
        Recruiter recruiter = recruiterRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RecruiterNotFoundException("Recruiter with email " + recruiterEmail + " not found"));

        Company company = recruiter.getCompany();
        if (company == null) {
            return Collections.emptyList();
        }

        List<PlacementDrive> drives = placementDriveRepository.findByCompanyId(company.getId());
        return drives.stream()
                .map(PlacementDriveResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PlacementDriveResponseDto updatePlacementDriveForRecruiter(
            String recruiterEmail,
            Long driveId,
            RecruiterPlacementDriveRequestDto requestDto) {

        Recruiter recruiter = recruiterRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RecruiterNotFoundException("Recruiter with email " + recruiterEmail + " not found"));

        Company recruiterCompany = recruiter.getCompany();
        if (recruiterCompany == null) {
            throw new UnauthorizedPlacementDriveAccessException("Recruiter is not associated with any company");
        }

        PlacementDrive drive = placementDriveRepository.findById(driveId)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Placement drive with id " + driveId + " not found"));

        if (drive.getCompany() == null || !drive.getCompany().getId().equals(recruiterCompany.getId())) {
            throw new UnauthorizedPlacementDriveAccessException("Recruiter cannot update placement drive belonging to another company");
        }

        drive.setJobTitle(requestDto.getJobTitle());
        drive.setJobDescription(requestDto.getJobDescription());
        drive.setLocation(requestDto.getLocation());
        drive.setMinimumCgpa(requestDto.getMinimumCgpa());
        drive.setEligibilityCriteria(requestDto.getEligibilityCriteria());
        drive.setSalary(requestDto.getSalary());
        drive.setApplicationDeadline(requestDto.getApplicationDeadline());
        if (requestDto.getStatus() != null && !requestDto.getStatus().isBlank()) {
            drive.setStatus(requestDto.getStatus().trim());
        }

        PlacementDrive updatedDrive = placementDriveRepository.save(drive);
        return PlacementDriveResponseDto.fromEntity(updatedDrive);
    }

    public void deletePlacementDriveForRecruiter(String recruiterEmail, Long driveId) {
        Recruiter recruiter = recruiterRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RecruiterNotFoundException("Recruiter with email " + recruiterEmail + " not found"));

        Company recruiterCompany = recruiter.getCompany();
        if (recruiterCompany == null) {
            throw new UnauthorizedPlacementDriveAccessException("Recruiter is not associated with any company");
        }

        PlacementDrive drive = placementDriveRepository.findById(driveId)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Placement drive with id " + driveId + " not found"));

        if (drive.getCompany() == null || !drive.getCompany().getId().equals(recruiterCompany.getId())) {
            throw new UnauthorizedPlacementDriveAccessException("Recruiter cannot delete placement drive belonging to another company");
        }

        placementDriveRepository.delete(drive);
        placementDriveRepository.flush();
    }
}
