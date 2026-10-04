package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.repository.DoctorRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepo;

    public String saveDoctor(Doctor doctor) {
        doctorRepo.save(doctor);
        return "saved successfully";
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    public Optional<Doctor> getDoctorById(UUID id) {
        return doctorRepo.findById(id);
    }

    public String updateDoctor(UUID id, Doctor doctor) {
        Optional<Doctor> existingDoctor = doctorRepo.findById(id);

        if (existingDoctor.isEmpty()) {
            return "we don't have doctor with this id " + id;
        }

        Doctor doctorToUpdate = existingDoctor.get();
        doctorToUpdate.setFirstName(doctor.getFirstName());
        doctorToUpdate.setLastName(doctor.getLastName());
        doctorToUpdate.setDateOfBirth(doctor.getDateOfBirth());

        doctorRepo.save(doctorToUpdate);
        return "updated successfully";
    }

    public String deleteDoctorById(UUID id) {
        Optional<Doctor> existingDoctor = doctorRepo.findById(id);

        if (existingDoctor.isEmpty()) {
            return "we don't have doctor with this id " + id;
        }

        doctorRepo.deleteById(id);
        return "doctor deleted successfully";
    }

    // B1
    public List<Doctor> getDoctorsBySpecialization(String name) {
        return doctorRepo.findDoctorsBySpecializationName(name);
    }

    // B2
    public List<Doctor> getDoctorsWithoutOffice() {
        return doctorRepo.findDoctorsWithoutOffice();
    }
}