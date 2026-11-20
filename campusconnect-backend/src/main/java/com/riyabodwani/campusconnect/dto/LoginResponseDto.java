package com.riyabodwani.campusconnect.dto;

public class LoginResponseDto {

    private String message;
    private String token;
    private StudentResponseDto student;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String message, String token, StudentResponseDto student) {
        this.message = message;
        this.token = token;
        this.student = student;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public StudentResponseDto getStudent() {
        return student;
    }

    public void setStudent(StudentResponseDto student) {
        this.student = student;
    }
}
