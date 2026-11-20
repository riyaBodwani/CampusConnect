package com.riyabodwani.campusconnect.dto;

import com.riyabodwani.campusconnect.entity.Company;

public class CompanyResponseDto {

    private Long id;
    private String name;
    private String industry;
    private String website;
    private String contactEmail;
    private String contactPhone;
    private String location;
    private String description;

    public CompanyResponseDto() {
    }

    public CompanyResponseDto(Long id, String name, String industry, String website,
                              String contactEmail, String contactPhone, String location, String description) {
        this.id = id;
        this.name = name;
        this.industry = industry;
        this.website = website;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.location = location;
        this.description = description;
    }

    public static CompanyResponseDto fromEntity(Company company) {
        if (company == null) {
            return null;
        }
        return new CompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getIndustry(),
                company.getWebsite(),
                company.getContactEmail(),
                company.getContactPhone(),
                company.getLocation(),
                company.getDescription()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
