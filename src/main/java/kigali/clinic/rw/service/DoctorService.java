package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private SpecializationRepository specializationRepo;

    @Autowired
    private OfficeRepository officeRepo;

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

    // Quiz 2 A1: add one specialization to a doctor, keep the ones he already has
    @Transactional
    public String addSpecializationToDoctor(UUID doctorId, UUID specializationId) {
        Optional<Doctor> existingDoctor = doctorRepo.findById(doctorId);
        if (existingDoctor.isEmpty()) {
            return "The doctor with that id does not exist";
        }

        Optional<Specialization> existingSpecialization = specializationRepo.findById(specializationId);
        if (existingSpecialization.isEmpty()) {
            return "That specialization does not exist";
        }

        Doctor doctor = existingDoctor.get();
        Specialization specialization = existingSpecialization.get();

        if (doctor.getSpecializations().contains(specialization)) {
            return "The doctor already has that specialization";
        }

        doctor.getSpecializations().add(specialization);
        doctorRepo.save(doctor);
        return "Specialization added to the doctor";
    }

    // Quiz 2 A2: remove only the link, the specialization itself stays
    @Transactional
    public String removeSpecializationFromDoctor(UUID doctorId, UUID specializationId) {
        Optional<Doctor> existingDoctor = doctorRepo.findById(doctorId);
        if (existingDoctor.isEmpty()) {
            return "The doctor with that id does not exist";
        }

        Optional<Specialization> existingSpecialization = specializationRepo.findById(specializationId);
        if (existingSpecialization.isEmpty()) {
            return "That specialization does not exist";
        }

        Doctor doctor = existingDoctor.get();
        Specialization specialization = existingSpecialization.get();

        if (!doctor.getSpecializations().contains(specialization)) {
            return "The doctor does not have that specialization";
        }

        doctor.getSpecializations().remove(specialization);
        doctorRepo.save(doctor);
        return "Specialization removed from the doctor";
    }

    // Quiz 2 A4: give the doctor another office, the old office becomes free
    public String changeDoctorOffice(UUID doctorId, int officeNumber) {
        Optional<Doctor> existingDoctor = doctorRepo.findById(doctorId);
        if (existingDoctor.isEmpty()) {
            return "The doctor with that id does not exist";
        }

        Optional<Office> existingOffice = officeRepo.findByOfficeNumber(officeNumber);
        if (existingOffice.isEmpty()) {
            return "The office with that office number does not exist";
        }

        // the office must not belong to another doctor
        boolean officeIsTaken = doctorRepo.existsByOffice_OfficeNumberAndIdNot(officeNumber, doctorId);
        if (officeIsTaken) {
            return "That office is taken by other doctor";
        }

        Doctor doctor = existingDoctor.get();
        doctor.setOffice(existingOffice.get());
        doctorRepo.save(doctor);
        return "Office changed successfully";
    }

    // Quiz 2 D3: search doctors by first name or last name
    public List<Doctor> searchDoctorsByName(String name) {
        return doctorRepo.searchDoctorsByName(name);
    }

    // Quiz 2 Bonus: how many COMPLETED appointments each doctor had between two dates
    public List<Object[]> getDoctorWorkload(LocalDate start, LocalDate end) {
        return doctorRepo.findDoctorWorkload(AppointmentStatus.COMPLETED, start, end);
    }
}