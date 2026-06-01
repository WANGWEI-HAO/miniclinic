package tw.edu.fju.miniclinic.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordForm {

    @NotBlank(message = "舊密碼不能為空")
    private String oldPassword;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 8, message = "密碼至少需要 8 個字元")
    private String newPassword;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmNewPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}