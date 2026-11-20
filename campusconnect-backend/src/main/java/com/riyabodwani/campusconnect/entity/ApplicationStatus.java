package com.riyabodwani.campusconnect.entity;

public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    REJECTED,
    SELECTED;

    public static boolean isValid(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        for (ApplicationStatus s : values()) {
            if (s.name().equalsIgnoreCase(status.trim())) {
                return true;
            }
        }
        return false;
    }
}
