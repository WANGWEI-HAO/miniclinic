package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import tw.edu.fju.miniclinic.model.AppointmentRepository;
import tw.edu.fju.miniclinic.model.DoctorRepository;
import tw.edu.fju.miniclinic.model.PatientRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StatsController {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @GetMapping("/stats")
    public String showStats(Model model) {
        // Fetch total counts
        model.addAttribute("doctorCount", doctorRepo.count());
        model.addAttribute("patientCount", patientRepo.count());
        model.addAttribute("appointmentCount", appointmentRepo.count());

        // Fetch appointment counts grouped by department
        List<Object[]> apptCountsByDepartmentRaw = appointmentRepo.countAppointmentsByDepartment();
        Map<String, Long> apptCountsByDepartment = new LinkedHashMap<>();
        for (Object[] row : apptCountsByDepartmentRaw) {
            apptCountsByDepartment.put((String) row[0], (Long) row[1]);
        }
        model.addAttribute("apptCountsByDepartment", apptCountsByDepartment);

        return "stats"; // Corresponds to templates/stats.html
    }
}