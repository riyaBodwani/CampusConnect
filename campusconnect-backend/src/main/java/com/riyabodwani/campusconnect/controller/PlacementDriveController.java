package com.riyabodwani.campusconnect.controller;

import com.riyabodwani.campusconnect.dto.PlacementDriveResponseDto;
import com.riyabodwani.campusconnect.entity.PlacementDrive;
import com.riyabodwani.campusconnect.service.PlacementDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/placement-drives")
public class PlacementDriveController {

    @Autowired
    private PlacementDriveService placementDriveService;

    @GetMapping
    public List<PlacementDriveResponseDto> getAllPlacementDrives() {
        return placementDriveService.getAllPlacementDrives().stream()
                .map(PlacementDriveResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PlacementDriveResponseDto getPlacementDriveById(@PathVariable Long id) {
        return PlacementDriveResponseDto.fromEntity(placementDriveService.getPlacementDriveById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlacementDriveResponseDto createPlacementDrive(@RequestBody PlacementDrive placementDrive) {
        return PlacementDriveResponseDto.fromEntity(placementDriveService.createPlacementDrive(placementDrive));
    }

    @PutMapping("/{id}")
    public PlacementDriveResponseDto updatePlacementDrive(@PathVariable Long id, @RequestBody PlacementDrive placementDrive) {
        return PlacementDriveResponseDto.fromEntity(placementDriveService.updatePlacementDrive(id, placementDrive));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlacementDrive(@PathVariable Long id) {
        placementDriveService.deletePlacementDrive(id);
    }
}
