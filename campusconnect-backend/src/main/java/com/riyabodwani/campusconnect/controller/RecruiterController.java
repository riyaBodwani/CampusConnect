package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.dto.RecruiterLoginRequestDto;
import com.riyabodwani.campusconnect.dto.RecruiterLoginResponseDto;
import com.riyabodwani.campusconnect.dto.RecruiterRequestDto;
import com.riyabodwani.campusconnect.dto.RecruiterResponseDto;
import com.riyabodwani.campusconnect.service.RecruiterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recruiters")
public class RecruiterController {

    @Autowired
    private RecruiterService recruiterService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RecruiterResponseDto register(@Valid @RequestBody RecruiterRequestDto requestDto) {
        return recruiterService.registerRecruiter(requestDto);
    }

    @PostMapping("/login")
    public RecruiterLoginResponseDto login(@Valid @RequestBody RecruiterLoginRequestDto requestDto) {
        return recruiterService.login(requestDto);
    }
}
