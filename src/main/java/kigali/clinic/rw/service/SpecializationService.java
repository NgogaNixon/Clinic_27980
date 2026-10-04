package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    public String saveSpecialization(Specialization specialization) {
        specializationRepo.save(specialization);
        return "saved successfully";
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepo.findAll();
    }

    public Optional<Specialization> getSpecializationById(UUID id) {
        return specializationRepo.findById(id);
    }

    public String updateSpecialization(UUID id, Specialization specialization) {
        Optional<Specialization> existingSpecialization = specializationRepo.findById(id);

        if (existingSpecialization.isEmpty()) {
            return "we don't have specialization with this id " + id;
        }

        Specialization specializationToUpdate = existingSpecialization.get();
        specializationToUpdate.setName(specialization.getName());

        specializationRepo.save(specializationToUpdate);
        return "updated successfully";
    }

    public String deleteSpecializationById(UUID id) {
        Optional<Specialization> existingSpecialization = specializationRepo.findById(id);

        if (existingSpecialization.isEmpty()) {
            return "we don't have specialization with this id " + id;
        }

        specializationRepo.deleteById(id);
        return "specialization deleted successfully";
    }

    public String assignDoctorToSpecialization(UUID specializationId, UUID doctorId) {
        Optional<Specialization> existingSpecialization = specializationRepo.findById(specializationId);
        Optional<Doctor> existingDoctor = doctorRepo.findById(doctorId);

        if (existingSpecialization.isEmpty()) {
            return "we don't have specialization with this id " + specializationId;
        }
        if (existingDoctor.isEmpty()) {
            return "we don't have doctor with this id " + doctorId;
        }

        Specialization specialization = existingSpecialization.get();
        Doctor doctor = existingDoctor.get();

        if (specialization.getDoctors().contains(doctor)) {
            return "this doctor already has this specialization";
        }

        specialization.getDoctors().add(doctor);
        specializationRepo.save(specialization);
        return "doctor assigned to specialization successfully";
    }

    public String removeDoctorFromSpecialization(UUID specializationId, UUID doctorId) {
        Optional<Specialization> existingSpecialization = specializationRepo.findById(specializationId);
        Optional<Doctor> existingDoctor = doctorRepo.findById(doctorId);

        if (existingSpecialization.isEmpty()) {
            return "we don't have specialization with this id " + specializationId;
        }
        if (existingDoctor.isEmpty()) {
            return "we don't have doctor with this id " + doctorId;
        }

        Specialization specialization = existingSpecialization.get();
        Doctor doctor = existingDoctor.get();

        if (!specialization.getDoctors().contains(doctor)) {
            return "this doctor does not have this specialization";
        }

        specialization.getDoctors().remove(doctor);
        specializationRepo.save(specialization);
        return "doctor removed from specialization successfully";
    }

    // B3
    public List<Specialization> getUnusedSpecializations() {
        return specializationRepo.findUnusedSpecializations();
    }
}