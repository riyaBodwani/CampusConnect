package com.riyabodwani.campusconnect.dto;

public class ApplicationStatusUpdateRequestDto {

    private String status;

    public ApplicationStatusUpdateRequestDto() {
    }

    public ApplicationStatusUpdateRequestDto(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
