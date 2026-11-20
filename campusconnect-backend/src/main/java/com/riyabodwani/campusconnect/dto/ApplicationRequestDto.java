package com.riyabodwani.campusconnect.dto;

public class ApplicationRequestDto {

    private Long placementDriveId;

    public ApplicationRequestDto() {
    }

    public ApplicationRequestDto(Long placementDriveId) {
        this.placementDriveId = placementDriveId;
    }

    public Long getPlacementDriveId() {
        return placementDriveId;
    }

    public void setPlacementDriveId(Long placementDriveId) {
        this.placementDriveId = placementDriveId;
    }
}
