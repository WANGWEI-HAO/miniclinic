package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;
import tw.edu.fju.miniclinic.model.ChangePasswordRequest;
import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.DoctorRepository;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCrypt;
@RestController
public class DoctorApiController {

    @Autowired
    private DoctorRepository doctorRepo;

    @GetMapping("/api/doctors")
    public List<Doctor> getDoctors(
            @RequestParam(required = false) String department) {
        if (department == null || department.isBlank()) {
            return doctorRepo.findAll();
        }
        return doctorRepo.findByDepartment(department);
    }

    @GetMapping("/api/doctors/{doctorId}")
    public ResponseEntity<Doctor> getDoctor(@PathVariable String doctorId) {
        Optional<Doctor> doctor = doctorRepo.findById(doctorId);
        return doctor
            .map(d -> ResponseEntity.ok(d))       // 有 → 200 OK + 資料
            .orElse(ResponseEntity.notFound().build());  // 沒有 → 404
    }

    @GetMapping("/api/departments")
    public List<String> getDepartments() {
        return doctorRepo.findAllDepartments();
    }

    @PostMapping("/api/doctors")
public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
    Doctor saved = doctorRepo.save(doctor);
    return ResponseEntity.status(201).body(saved);
    }
@PutMapping("/api/doctors/{doctorId}")
public ResponseEntity<Doctor> updateDoctor(
        @PathVariable String doctorId,
        @RequestBody Doctor updated) {

    return doctorRepo.findById(doctorId)
        .map(existing -> {
            existing.setName(updated.getName());
            existing.setDepartment(updated.getDepartment());
            existing.setSpecialty(updated.getSpecialty());
            return ResponseEntity.ok(doctorRepo.save(existing));
        })
        .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/doctors/{doctorId}")
public ResponseEntity<Void> deleteDoctor(@PathVariable String doctorId) {
    if (!doctorRepo.existsById(doctorId)) {
        return ResponseEntity.notFound().build();
    }
    doctorRepo.deleteById(doctorId);
    return ResponseEntity.noContent().build();  // 204 No Content
    }

    /**
     * 修改醫師密碼。
     * PUT /api/doctors/{doctorId}/password
     * @param doctorId 醫師 ID
     * @param request 包含 currentPassword, newPassword, confirmNewPassword 的 JSON 物件
     * @param session 用於驗證登入醫師 ID
     * @return 成功則回傳 200 OK，否則回傳錯誤狀態碼
     */
    @PutMapping("/api/doctors/{doctorId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String doctorId,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpSession session) {

        String loggedInDoctorId = (String) session.getAttribute("loggedInDoctorId");

        // 1. 驗證是否為本人操作
        if (loggedInDoctorId == null || !loggedInDoctorId.equals(doctorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to change this doctor's password.");
        }

        // 2. 查詢醫師
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found."));

        // 3. 驗證舊密碼
        if (!BCrypt.checkpw(request.getCurrentPassword(), doctor.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect current password.");
        }

        // 4. 驗證新密碼與確認密碼是否一致
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirmation do not match.");
        }

        // 5. 更新密碼並儲存
        doctor.setPasswordHash(BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt()));
        doctorRepo.save(doctor);
        return ResponseEntity.ok().build();
    }
}
