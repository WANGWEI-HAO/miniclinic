package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import tw.edu.fju.miniclinic.model.Appointment;
import tw.edu.fju.miniclinic.model.AppointmentRepository;
import tw.edu.fju.miniclinic.model.Doctor;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.Patient;
import tw.edu.fju.miniclinic.model.PatientRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentApiController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    /**
     * 取得總掛號數。
     * GET /api/appointments/count
     * @return 包含總掛號數的 Map (例如: {"count": 3})
     */
    @GetMapping("/count")
    public Map<String, Long> getAppointmentCount() {
        return Map.of("count", appointmentRepo.count());
    }

    /**
     * 取得掛號列表，可依日期和醫師 ID 篩選。
     * GET /api/appointments?date=YYYY-MM-DD&doctorId=D001
     * @param date 可選，掛號日期 (YYYY-MM-DD)
     * @param doctorId 可選，醫師 ID
     * @return 符合條件的掛號列表
     */
    @GetMapping
    public List<Appointment> getAppointments(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String doctorId) {

        LocalDate apptDate = null;
        if (date != null && !date.isBlank()) {
            try {
                apptDate = LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                // 日期格式錯誤，視為無日期篩選或直接返回空列表
                System.err.println("Invalid date format for appointment API: " + date);
                return List.of(); // 返回空列表，表示無符合項或參數錯誤
            }
        }

        Optional<Doctor> doctorOpt = Optional.empty();
        if (doctorId != null && !doctorId.isBlank()) {
            doctorOpt = doctorRepo.findById(doctorId);
            if (doctorOpt.isEmpty()) {
                // 醫師 ID 存在但找不到對應醫師，返回空列表
                return List.of();
            }
        }
        Doctor doctor = doctorOpt.orElse(null);

        if (apptDate != null && doctor != null) {
            return appointmentRepo.findByDoctorAndApptDate(doctor, apptDate);
        } else if (apptDate != null) {
            return appointmentRepo.findByApptDate(apptDate);
        } else if (doctor != null) {
            return appointmentRepo.findByDoctor(doctor);
        } else {
            return appointmentRepo.findAll();
        }
    }

    /**
     * 新增掛號。
     * POST /api/appointments
     * @param request 包含 chartNo, doctorId, apptDate, timeSlot 的 JSON 物件
     * @return 新增成功的 Appointment 物件
     */
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Map<String, String> request) {
        String chartNo = request.get("chartNo");
        String doctorId = request.get("doctorId");
        String apptDateStr = request.get("apptDate");
        String timeSlot = request.get("timeSlot");

        if (chartNo == null || doctorId == null || apptDateStr == null || timeSlot == null) {
            return ResponseEntity.badRequest().body(null); // 缺少必要欄位
        }

        LocalDate apptDate;
        try {
            apptDate = LocalDate.parse(apptDateStr);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(null); // 日期格式錯誤
        }

        Patient patient = patientRepo.findById(chartNo).orElse(null);
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);

        if (patient == null || doctor == null) {
            return ResponseEntity.badRequest().body(null); // 病患或醫師不存在
        }

        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setApptDate(apptDate);
        appt.setTimeSlot(timeSlot);
        appt.setStatus("BOOKED");

        Appointment saved = appointmentRepo.save(appt);
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * 更新指定掛號的狀態。
     * PUT /api/appointments/{apptId}/status
     * @param apptId 掛號 ID
     * @param payload 包含新狀態 (status) 的 JSON 物件
     * @param session 用於驗證登入醫師 ID
     * @return 更新後的 Appointment 物件
     */
    @PutMapping("/{apptId}/status")
    public ResponseEntity<Appointment> updateStatus( // 9.3 更新掛號狀態的 API
            @PathVariable Long apptId,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        String loggedInDoctorId = (String) session.getAttribute("loggedInDoctorId");

        Appointment appt = appointmentRepo.findById(apptId).orElse(null);
        if (appt == null) {
            return ResponseEntity.notFound().build();
        }

        // 只能修改自己的掛號，且必須已登入
        if (loggedInDoctorId == null || !appt.getDoctor().getDoctorId().equals(loggedInDoctorId)) { // 只能修改自己的掛號
            return ResponseEntity.status(403).build();
        }

        String newStatus = payload.get("status");
        if (newStatus == null || !List.of("BOOKED", "COMPLETED", "CANCELLED").contains(newStatus)) { // 無效的狀態值
            return ResponseEntity.badRequest().build();
        }

        appt.setStatus(newStatus);
        return ResponseEntity.ok(appointmentRepo.save(appt));
    }
}