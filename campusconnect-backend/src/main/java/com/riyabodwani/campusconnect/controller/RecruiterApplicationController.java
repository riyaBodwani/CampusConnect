package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.dto.ApplicationStatusUpdateRequestDto;
import com.riyabodwani.campusconnect.dto.RecruiterApplicationResponseDto;
import com.riyabodwani.campusconnect.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RecruiterApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping({"/recruiter/applications", "/recruiters/applications"})
    public List<RecruiterApplicationResponseDto> getApplications(Authentication authentication) {
        String recruiterEmail = authentication.getName();
        return applicationService.getApplicationsForRecruiter(recruiterEmail);
    }

    @PatchMapping({"/recruiter/applications/{applicationId}/status", "/recruiters/applications/{applicationId}/status"})
    public RecruiterApplicationResponseDto updateStatus(
            @PathVariable Long applicationId,
            @RequestBody ApplicationStatusUpdateRequestDto requestDto,
            Authentication authentication) {
        String recruiterEmail = authentication.getName();
        return applicationService.updateApplicationStatus(recruiterEmail, applicationId, requestDto);
    }
}
