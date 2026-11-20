package com.riyabodwani.campusconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "placement_drives")
public class PlacementDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobTitle;

    private String jobDescription;

    private String location;

    private Double minimumCgpa;

    private String eligibilityCriteria;

    private Double salary;

    private LocalDate applicationDeadline;

    private String status;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public PlacementDrive() {
    }

    public PlacementDrive(String jobTitle, String jobDescription, String location, Double minimumCgpa,
                          String eligibilityCriteria, Double salary, LocalDate applicationDeadline,
                          String status, Company company) {
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.location = location;
        this.minimumCgpa = minimumCgpa;
        this.eligibilityCriteria = eligibilityCriteria;
        this.salary = salary;
        this.applicationDeadline = applicationDeadline;
        this.status = status;
        this.company = company;
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
