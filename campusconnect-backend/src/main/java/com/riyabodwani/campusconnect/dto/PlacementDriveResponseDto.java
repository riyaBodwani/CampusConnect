package com.riyabodwani.campusconnect.dto;

import com.riyabodwani.campusconnect.entity.PlacementDrive;

import java.time.LocalDate;

public class PlacementDriveResponseDto {

    private Long id;
    private String jobTitle;
    private String jobDescription;
    private String location;
    private Double minimumCgpa;
    private String eligibilityCriteria;
    private Double salary;
    private LocalDate applicationDeadline;
    private String status;
    private Long companyId;
    private String companyName;

    public PlacementDriveResponseDto() {
    }

    public PlacementDriveResponseDto(Long id, String jobTitle, String jobDescription, String location,
                                     Double minimumCgpa, String eligibilityCriteria, Double salary,
                                     LocalDate applicationDeadline, String status,
                                     Long companyId, String companyName) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.location = location;
        this.minimumCgpa = minimumCgpa;
        this.eligibilityCriteria = eligibilityCriteria;
        this.salary = salary;
        this.applicationDeadline = applicationDeadline;
        this.status = status;
        this.companyId = companyId;
        this.companyName = companyName;
    }

    public static PlacementDriveResponseDto fromEntity(PlacementDrive drive) {
        if (drive == null) {
            return null;
        }

        Long companyId = (drive.getCompany() != null) ? drive.getCompany().getId() : null;
        String companyName = (drive.getCompany() != null) ? drive.getCompany().getName() : null;

        return new PlacementDriveResponseDto(
                drive.getId(),
                drive.getJobTitle(),
                drive.getJobDescription(),
                drive.getLocation(),
                drive.getMinimumCgpa(),
                drive.getEligibilityCriteria(),
                drive.getSalary(),
                drive.getApplicationDeadline(),
                drive.getStatus(),
                companyId,
                companyName
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getMinimumCgpa() {
        return minimumCgpa;
    }

    public void setMinimumCgpa(Double minimumCgpa) {
        this.minimumCgpa = minimumCgpa;
    }

    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public void setEligibilityCriteria(String eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
