package tw.edu.fju.miniclinic.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tw.edu.fju.miniclinic.model.*;
import tw.edu.fju.miniclinic.controller.PasswordForm;

@Controller
public class LoginController {

    @Autowired
    private DoctorRepository doctorRepo;

    // GET：顯示登入頁
    @GetMapping("/login")
    public String loginForm(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    // POST：處理登入
    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult result,
            HttpSession session,
            Model model) {

        // 步驟 1：檢查表單驗證
        if (result.hasErrors()) {
            return "login";  // 顯示錯誤訊息
        }

        // 步驟 2：查詢醫師
        Doctor doctor = doctorRepo.findById(form.getDoctorId()).orElse(null);

        // 步驟 3：檢查密碼（醫師不存在或密碼錯都給同樣的錯誤訊息，避免洩漏帳號是否存在）
        if (doctor == null || !BCrypt.checkpw(form.getPassword(), doctor.getPasswordHash())) {
            model.addAttribute("errorMessage", "醫師編號或密碼錯誤");
            return "login";
        }

        // 步驟 4：登入成功，存入 Session
        session.setAttribute("loggedInDoctorId", doctor.getDoctorId());
        session.setAttribute("loggedInDoctorName", doctor.getName());

        return "redirect:/dashboard";
    }

    // 登出
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 清除 Session
        return "redirect:/login";
    }

    // GET：顯示修改密碼頁
    @GetMapping("/password")
    public String passwordForm(Model model, HttpSession session) {
        String loggedInDoctorName = (String) session.getAttribute("loggedInDoctorName");
        if (loggedInDoctorName == null) {
            // 如果 Session 中沒有醫師姓名，表示未登入或 Session 已失效，重導向登入頁
            // 理想情況下，這應該由 Interceptor 處理，但這裡提供一個備用防護
            return "redirect:/login";
        }
        model.addAttribute("loggedInDoctorName", loggedInDoctorName);
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new PasswordForm());
        }
        return "password";
    }

    // POST：處理修改密碼
    @PostMapping("/password")
    public String changePassword(
            @Valid @ModelAttribute("passwordForm") PasswordForm form,
            BindingResult result,
            HttpSession session,
            Model model) {

        String loggedInDoctorId = (String) session.getAttribute("loggedInDoctorId");
        String loggedInDoctorName = (String) session.getAttribute("loggedInDoctorName");

        // 確保醫師已登入
        if (loggedInDoctorId == null || loggedInDoctorName == null) {
            return "redirect:/login";
        }
        model.addAttribute("loggedInDoctorName", loggedInDoctorName); // 確保模板能顯示醫師姓名

        // 步驟 1：檢查表單驗證 (包括新密碼長度)
        if (result.hasErrors()) {
            return "password";
        }

        // 步驟 2：查詢醫師
        Doctor doctor = doctorRepo.findById(loggedInDoctorId).orElse(null);

        // 如果根據 Session 中的 ID 找不到醫師，表示資料異常，強制重新登入
        if (doctor == null) {
            session.invalidate();
            model.addAttribute("errorMessage", "用戶資料異常，請重新登入。");
            return "login";
        }

        // 步驟 3：驗證舊密碼
        if (!BCrypt.checkpw(form.getOldPassword(), doctor.getPasswordHash())) {
            model.addAttribute("errorMessage", "舊密碼錯誤");
            return "password";
        }

        // 步驟 4：驗證新密碼與確認密碼是否一致
        if (!form.getNewPassword().equals(form.getConfirmNewPassword())) {
            model.addAttribute("errorMessage", "兩次密碼不相符");
            return "password";
        }

        // 步驟 5：更新密碼並儲存
        doctor.setPasswordHash(BCrypt.hashpw(form.getNewPassword(), BCrypt.gensalt()));
        doctorRepo.save(doctor);
        return "redirect:/dashboard"; // 成功後重導向儀表板
    }
}