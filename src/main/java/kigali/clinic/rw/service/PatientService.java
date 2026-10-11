package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepo;

    public String savePatient(Patient patient) {
        patientRepo.save(patient);
        return "saved successfully";
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public Optional<Patient> getPatientById(UUID id) {
        return patientRepo.findById(id);
    }

    public String updatePatient(UUID id, Patient patient) {
        Optional<Patient> existingPatient = patientRepo.findById(id);

        if (existingPatient.isEmpty()) {
            return "we don't have patient with this id " + id;
        }

        Patient patientToUpdate = existingPatient.get();
        patientToUpdate.setFirstName(patient.getFirstName());
        patientToUpdate.setLastName(patient.getLastName());
        patientToUpdate.setDateOfBirth(patient.getDateOfBirth());
        patientToUpdate.setSsNumber(patient.getSsNumber());

        patientRepo.save(patientToUpdate);
        return "updated successfully";
    }

    public String deletePatientById(UUID id) {
        Optional<Patient> existingPatient = patientRepo.findById(id);

        if (existingPatient.isEmpty()) {
            return "we don't have patient with this id " + id;
        }

        patientRepo.deleteById(id);
        return "patient deleted successfully";
    }

    // A1
    public List<Patient> getPatientsByLastName(String lastName) {
        return patientRepo.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    // B4
    public List<Patient> getPatientsOfDoctor(UUID doctorId) {
        return patientRepo.findPatientsByDoctorId(doctorId);
    }

    // C2
    public List<Patient> getFrequentPatients(long min) {
        return patientRepo.findFrequentPatients(min);
    }

    // Quiz 2 D2: does a patient with this ssNumber exist?
    public boolean patientExistsBySsNumber(String ssNumber) {
        return patientRepo.existsBySsNumber(ssNumber);
    }
}