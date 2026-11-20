package com.riyabodwani.campusconnect.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecruiterPlacementDriveRequestDto {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String jobDescription;

    private String location;

    @DecimalMin(value = "0.0", message = "Minimum CGPA cannot be negative")
    @DecimalMax(value = "10.0", message = "Minimum CGPA cannot exceed 10.0")
    private Double minimumCgpa;

    private String eligibilityCriteria;

    @DecimalMin(value = "0.0", message = "Salary cannot be negative")
    private Double salary;

    @NotNull(message = "Application deadline is required")
    private LocalDate applicationDeadline;

    private String status;

    public RecruiterPlacementDriveRequestDto() {
    }

    public RecruiterPlacementDriveRequestDto(String jobTitle, String jobDescription, String location,
                                             Double minimumCgpa, String eligibilityCriteria, Double salary,
                                             LocalDate applicationDeadline, String status) {
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.location = location;
        this.minimumCgpa = minimumCgpa;
        this.eligibilityCriteria = eligibilityCriteria;
        this.salary = salary;
        this.applicationDeadline = applicationDeadline;
        this.status = status;
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
}
