package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.dto.CompanyResponseDto;
import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<CompanyResponseDto> getAllCompanies() {
        return companyService.getAllCompanies().stream()
                .map(CompanyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CompanyResponseDto getCompanyById(@PathVariable Long id) {
        return CompanyResponseDto.fromEntity(companyService.getCompanyById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponseDto createCompany(@RequestBody Company company) {
        return CompanyResponseDto.fromEntity(companyService.createCompany(company));
    }

    @PutMapping("/{id}")
    public CompanyResponseDto updateCompany(@PathVariable Long id, @RequestBody Company company) {
        return CompanyResponseDto.fromEntity(companyService.updateCompany(id, company));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
    }
}
