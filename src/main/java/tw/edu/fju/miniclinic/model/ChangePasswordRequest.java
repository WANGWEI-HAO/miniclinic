package tw.edu.fju.miniclinic.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "Current password cannot be empty")
    private String currentPassword;

    @NotBlank(message = "New password cannot be empty")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm new password cannot be empty")
    private String confirmNewPassword;

    // Getters and Setters
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}