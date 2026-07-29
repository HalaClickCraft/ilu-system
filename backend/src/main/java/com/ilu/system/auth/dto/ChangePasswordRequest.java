package com.ilu.system.auth.dto;
public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
    public String getCurrentPassword() { return currentPassword; } public void setCurrentPassword(String v) { this.currentPassword = v; }
    public String getNewPassword() { return newPassword; } public void setNewPassword(String v) { this.newPassword = v; }
    public String getConfirmPassword() { return confirmPassword; } public void setConfirmPassword(String v) { this.confirmPassword = v; }
}
