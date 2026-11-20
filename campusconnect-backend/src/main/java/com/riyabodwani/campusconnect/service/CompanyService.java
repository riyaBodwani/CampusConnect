package com.riyabodwani.campusconnect.service;

import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.exception.CompanyNotFoundException;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));
    }

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public Company updateCompany(Long id, Company updatedCompany) {
        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));

        existingCompany.setName(updatedCompany.getName());
        existingCompany.setIndustry(updatedCompany.getIndustry());
        existingCompany.setWebsite(updatedCompany.getWebsite());
        existingCompany.setContactEmail(updatedCompany.getContactEmail());
        existingCompany.setContactPhone(updatedCompany.getContactPhone());
        existingCompany.setLocation(updatedCompany.getLocation());
        existingCompany.setDescription(updatedCompany.getDescription());

        return companyRepository.save(existingCompany);
    }

    public void deleteCompany(Long id) {
        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));

        companyRepository.delete(existingCompany);
    }
}
