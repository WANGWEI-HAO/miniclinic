package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import tw.edu.fju.miniclinic.model.PatientRepository;

@Controller
public class PatientPageController {

    @Autowired
    private PatientRepository patientRepo;

    @GetMapping("/patients")
    public String listPatients(Model model) {
        model.addAttribute("patients", patientRepo.findAll());
        return "patients"; // 對應 templates/patients.html
    }
}