package com.riyabodwani.campusconnect.service;

import com.riyabodwani.campusconnect.dto.RecruiterLoginRequestDto;
import com.riyabodwani.campusconnect.dto.RecruiterLoginResponseDto;
import com.riyabodwani.campusconnect.dto.RecruiterRequestDto;
import com.riyabodwani.campusconnect.dto.RecruiterResponseDto;
import com.riyabodwani.campusconnect.entity.Company;
import com.riyabodwani.campusconnect.entity.Recruiter;
import com.riyabodwani.campusconnect.exception.CompanyNotFoundException;
import com.riyabodwani.campusconnect.exception.InvalidRecruiterCredentialsException;
import com.riyabodwani.campusconnect.exception.RecruiterEmailAlreadyExistsException;
import com.riyabodwani.campusconnect.repository.CompanyRepository;
import com.riyabodwani.campusconnect.repository.RecruiterRepository;
import com.riyabodwani.campusconnect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RecruiterService {

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public RecruiterResponseDto registerRecruiter(RecruiterRequestDto requestDto) {
        if (recruiterRepository.existsByEmail(requestDto.getEmail())) {
            throw new RecruiterEmailAlreadyExistsException("Email '" + requestDto.getEmail() + "' is already registered");
        }

        Company company = companyRepository.findById(requestDto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + requestDto.getCompanyId() + " not found"));

        Recruiter recruiter = new Recruiter();
        recruiter.setFullName(requestDto.getFullName());
        recruiter.setEmail(requestDto.getEmail());
        recruiter.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        recruiter.setPhoneNumber(requestDto.getPhoneNumber());
        recruiter.setCompany(company);

        Recruiter saved = recruiterRepository.save(recruiter);

        return new RecruiterResponseDto(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getPhoneNumber(),
                company.getId(),
                company.getName()
        );
    }

    public RecruiterLoginResponseDto login(RecruiterLoginRequestDto requestDto) {
        Recruiter recruiter = recruiterRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new InvalidRecruiterCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(requestDto.getPassword(), recruiter.getPassword())) {
            throw new InvalidRecruiterCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateRecruiterToken(recruiter.getEmail(), recruiter.getId());

        Long companyId = recruiter.getCompany() != null ? recruiter.getCompany().getId() : null;
        String companyName = recruiter.getCompany() != null ? recruiter.getCompany().getName() : null;

        return new RecruiterLoginResponseDto(
                token,
                recruiter.getId(),
                recruiter.getFullName(),
                recruiter.getEmail(),
                recruiter.getPhoneNumber(),
                companyId,
                companyName
        );
    }
}
