package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.repository.AppointmentRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepo;

    public String saveAppointment(Appointment appointment) {
        // A4: refuse if the doctor already has a non-cancelled appointment that day
        if (appointment.getDoctor() != null) {
            boolean alreadyBooked = appointmentRepo.existsByDoctor_IdAndAppointmentDateAndStatusNot(
                    appointment.getDoctor().getId(),
                    appointment.getAppointmentDate(),
                    AppointmentStatus.CANCELLED);

            if (alreadyBooked) {
                return "Doctor is already booked on that date";
            }
        }

        appointmentRepo.save(appointment);
        return "saved successfully";
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public Optional<Appointment> getAppointmentById(UUID id) {
        return appointmentRepo.findById(id);
    }

    public String updateAppointment(UUID id, Appointment appointment) {
        Optional<Appointment> existingAppointment = appointmentRepo.findById(id);

        if (existingAppointment.isEmpty()) {
            return "we don't have appointment with this id " + id;
        }

        Appointment appointmentToUpdate = existingAppointment.get();
        appointmentToUpdate.setAppointmentDate(appointment.getAppointmentDate());
        appointmentToUpdate.setReason(appointment.getReason());
        appointmentToUpdate.setStatus(appointment.getStatus());

        appointmentRepo.save(appointmentToUpdate);
        return "updated successfully";
    }

    public String deleteAppointmentById(UUID id) {
        Optional<Appointment> existingAppointment = appointmentRepo.findById(id);

        if (existingAppointment.isEmpty()) {
            return "we don't have appointment with this id " + id;
        }

        appointmentRepo.deleteById(id);
        return "appointment deleted successfully";
    }

    public List<Appointment> getPendingAppointmentsByDoctor(UUID doctorId) {
        return appointmentRepo.findByDoctor_IdAndStatusOrderByAppointmentDateAsc(doctorId, AppointmentStatus.PENDING);
    }

    public List<Object[]> getOverduePendingAppointments() {
        return appointmentRepo.findOverduePendingAppointments(AppointmentStatus.PENDING, LocalDate.now());
    }

    public List<Object[]> getAppointmentCountPerDoctor() {
        return appointmentRepo.countAppointmentsPerDoctor();
    }

    public List<Appointment> getAppointmentsByDoctorSpecialization(UUID specializationId) {
        return appointmentRepo.findAppointmentsByDoctorSpecialization(specializationId);
    }

    public Page<Appointment> getDoctorAppointmentsPaged(UUID doctorId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("appointmentDate").ascending());
        return appointmentRepo.findByDoctor_Id(doctorId, pageable);
    }

    // A2
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepo.findByStatusOrderByAppointmentDateAsc(status);
    }

    // A3
    public List<Appointment> getAppointmentsBetween(LocalDate start, LocalDate end) {
        return appointmentRepo.findByAppointmentDateBetweenOrderByAppointmentDateAsc(start, end);
    }

    // C1
    public List<Object[]> getAppointmentCountByStatus() {
        return appointmentRepo.countAppointmentsByStatus();
    }

    // C3: page 0 with size 1 means "only the first row"
    public List<Object[]> getBusiestOffice() {
        Pageable firstRowOnly = PageRequest.of(0, 1);
        return appointmentRepo.findBusiestOffice(firstRowOnly);
    }

    // C4
    @Transactional
    public String cancelDoctorDay(UUID doctorId, LocalDate date) {
        int howMany = appointmentRepo.cancelDoctorDay(
                doctorId, date, AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED);
        return howMany + " appointments cancelled";
    }

    // Bonus: page by page (findAll(Pageable) already comes with JpaRepository)
    public Page<Appointment> getAppointmentsPage(Pageable pageable) {
        return appointmentRepo.findAll(pageable);
    }

    // Bonus: delete old cancellations
    @Transactional
    public String deleteCancelledBefore(LocalDate date) {
        int howMany = appointmentRepo.deleteCancelledBefore(AppointmentStatus.CANCELLED, date);
        return howMany + " appointments deleted";
    }
}