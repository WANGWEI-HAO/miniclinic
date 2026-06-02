package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.edu.fju.miniclinic.model.AppointmentRepository;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.PatientRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for providing system statistics.
 * This endpoint does not require authentication and is intended for external tools.
 */
@RestController
public class StatsApiController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @GetMapping("/api/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalDoctors", doctorRepo.count());
        stats.put("totalPatients", patientRepo.count());
        stats.put("totalAppointments", appointmentRepo.count());
        Map<String, Long> byStatus = appointmentRepo.countAppointmentsByStatus().stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1], (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        stats.put("byStatus", byStatus);
        return stats;
    }
}