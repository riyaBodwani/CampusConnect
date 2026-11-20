package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.dto.PlacementDriveResponseDto;
import com.riyabodwani.campusconnect.dto.RecruiterPlacementDriveRequestDto;
import com.riyabodwani.campusconnect.service.PlacementDriveService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecruiterPlacementDriveController {

    @Autowired
    private PlacementDriveService placementDriveService;

    @GetMapping({"/recruiter/placement-drives", "/recruiters/placement-drives"})
    public List<PlacementDriveResponseDto> getPlacementDrives(Authentication authentication) {
        String recruiterEmail = authentication.getName();
        return placementDriveService.getPlacementDrivesForRecruiter(recruiterEmail);
    }

    @PostMapping({"/recruiter/placement-drives", "/recruiters/placement-drives"})
    @ResponseStatus(HttpStatus.CREATED)
    public PlacementDriveResponseDto createPlacementDrive(
            @Valid @RequestBody RecruiterPlacementDriveRequestDto requestDto,
            Authentication authentication) {
        String recruiterEmail = authentication.getName();
        return placementDriveService.createPlacementDriveForRecruiter(recruiterEmail, requestDto);
    }

    @PutMapping({"/recruiter/placement-drives/{driveId}", "/recruiters/placement-drives/{driveId}"})
    public PlacementDriveResponseDto updatePlacementDrive(
            @PathVariable Long driveId,
            @Valid @RequestBody RecruiterPlacementDriveRequestDto requestDto,
            Authentication authentication) {
        String recruiterEmail = authentication.getName();
        return placementDriveService.updatePlacementDriveForRecruiter(recruiterEmail, driveId, requestDto);
    }

    @DeleteMapping({"/recruiter/placement-drives/{driveId}", "/recruiters/placement-drives/{driveId}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlacementDrive(
            @PathVariable Long driveId,
            Authentication authentication) {
        String recruiterEmail = authentication.getName();
        placementDriveService.deletePlacementDriveForRecruiter(recruiterEmail, driveId);
    }
}
