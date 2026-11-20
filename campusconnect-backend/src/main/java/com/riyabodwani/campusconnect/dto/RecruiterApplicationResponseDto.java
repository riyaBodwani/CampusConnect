package com.riyabodwani.campusconnect.dto;

import com.riyabodwani.campusconnect.entity.Application;
import com.riyabodwani.campusconnect.entity.Student;

import java.time.LocalDate;

public class RecruiterApplicationResponseDto {

    private Long id;
    private StudentResponseDto student;
    private PlacementDriveResponseDto placementDrive;
    private LocalDate appliedDate;
    private String status;

    public RecruiterApplicationResponseDto() {
    }

    public RecruiterApplicationResponseDto(Long id, StudentResponseDto student, PlacementDriveResponseDto placementDrive, LocalDate appliedDate, String status) {
        this.id = id;
        this.student = student;
        this.placementDrive = placementDrive;
        this.appliedDate = appliedDate;
        this.status = status;
    }

    public static RecruiterApplicationResponseDto fromEntity(Application application) {
        if (application == null) {
            return null;
        }

        Student s = application.getStudent();
        StudentResponseDto studentDto = null;
        if (s != null) {
            studentDto = new StudentResponseDto(
                    s.getId(),
                    s.getFullName(),
                    s.getDateOfBirth(),
                    s.getEmail(),
                    s.getPhoneNumber(),
                    s.getAddress(),
                    s.getCourse(),
                    s.getBranch(),
                    s.getCgpa(),
                    s.getPlacementStatus()
            );
        }

        PlacementDriveResponseDto driveDto = PlacementDriveResponseDto.fromEntity(application.getPlacementDrive());

        return new RecruiterApplicationResponseDto(
                application.getId(),
                studentDto,
                driveDto,
                application.getAppliedDate(),
                application.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentResponseDto getStudent() {
        return student;
    }

    public void setStudent(StudentResponseDto student) {
        this.student = student;
    }

    public PlacementDriveResponseDto getPlacementDrive() {
        return placementDrive;
    }

    public void setPlacementDrive(PlacementDriveResponseDto placementDrive) {
        this.placementDrive = placementDrive;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDate appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
