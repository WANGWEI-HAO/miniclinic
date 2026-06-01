package tw.edu.fju.miniclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.edu.fju.miniclinic.model.Patient;
import tw.edu.fju.miniclinic.model.PatientRepository;

import java.util.List;

@RestController
public class PatientApiController {

    @Autowired
    private PatientRepository patientRepo;

    @GetMapping("/api/patients")
    public List<Patient> getPatients() {
        return patientRepo.findAll();
    }
}