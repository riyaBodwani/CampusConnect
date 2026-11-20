package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.dto.ApplicationRequestDto;
import com.riyabodwani.campusconnect.dto.ApplicationResponseDto;
import com.riyabodwani.campusconnect.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponseDto apply(
            @RequestBody ApplicationRequestDto requestDto,
            Authentication authentication) {
        String studentEmail = authentication.getName();
        return ApplicationResponseDto.fromEntity(applicationService.apply(studentEmail, requestDto));
    }

    @GetMapping("/my")
    public List<ApplicationResponseDto> getMyApplications(Authentication authentication) {
        String studentEmail = authentication.getName();
        return applicationService.getMyApplications(studentEmail);
    }

    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawApplication(
            @PathVariable Long applicationId,
            Authentication authentication) {
        String studentEmail = authentication.getName();
        applicationService.withdrawApplication(studentEmail, applicationId);
    }
}
