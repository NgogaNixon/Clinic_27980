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
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    public String saveAppointment(Appointment appointment) {
        // Quiz 2 B1: no appointment in the past
        if (appointment.getAppointmentDate() != null
                && appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }

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

        // Quiz 2 B1: a new appointment always starts as SCHEDULED, whatever the JSON says
        appointment.setStatus(AppointmentStatus.SCHEDULED);

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
        // Quiz 2 B2: the status is not changed here anymore, only in the status endpoint

        appointmentRepo.save(appointmentToUpdate);
        return "updated successfully";
    }

    public String deleteAppointmentById(UUID id) {
        Optional<Appointment> existingAppointment = appointmentRepo.findById(id);

        if (existingAppointment.isEmpty()) {
            return "we don't have appointment with this id " + id;
        }

        // Quiz 2 C4: an appointment with prescriptions cannot be deleted
        if (prescriptionRepo.existsByAppointment_Id(id)) {
            return "That appointment has prescriptions, delete them first";
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

    // Quiz 2 B2: change the status, only the allowed moves
    public String updateAppointmentStatus(UUID id, AppointmentStatus newStatus) {
        Optional<Appointment> existingAppointment = appointmentRepo.findById(id);

        if (existingAppointment.isEmpty()) {
            return "The appointment with that id does not exist";
        }

        Appointment appointment = existingAppointment.get();
        AppointmentStatus oldStatus = appointment.getStatus();

        boolean allowed = false;

        if (oldStatus == AppointmentStatus.SCHEDULED) {
            if (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED) {
                allowed = true;
            }
        } else if (oldStatus == AppointmentStatus.CONFIRMED) {
            if (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED) {
                allowed = true;
            }
        }
        // COMPLETED and CANCELLED are final, so allowed stays false for them

        if (!allowed) {
            return "Cannot change status from " + oldStatus + " to " + newStatus;
        }

        appointment.setStatus(newStatus);
        appointmentRepo.save(appointment);
        return "Appointment status updated successfully";
    }

    // Quiz 2 B3: move the appointment to a new date
    public String rescheduleAppointment(UUID id, LocalDate newDate) {
        Optional<Appointment> existingAppointment = appointmentRepo.findById(id);

        if (existingAppointment.isEmpty()) {
            return "The appointment with that id does not exist";
        }

        Appointment appointment = existingAppointment.get();

        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return "A " + appointment.getStatus() + " appointment cannot be rescheduled";
        }

        if (newDate.isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }

        if (appointment.getDoctor() != null) {
            boolean alreadyBooked = appointmentRepo.existsByDoctor_IdAndAppointmentDateAndStatusNot(
                    appointment.getDoctor().getId(),
                    newDate,
                    AppointmentStatus.CANCELLED);

            if (alreadyBooked) {
                return "Doctor is already booked on that date";
            }
        }

        appointment.setAppointmentDate(newDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepo.save(appointment);
        return "Appointment rescheduled successfully";
    }

    // Quiz 2 D1: the agenda of a doctor for one day, without the cancelled ones
    public List<Appointment> getDoctorAgenda(UUID doctorId, LocalDate date) {
        return appointmentRepo.findByDoctor_IdAndAppointmentDateAndStatusNot(
                doctorId, date, AppointmentStatus.CANCELLED);
    }

    // Quiz 2 D2: upcoming appointments of a patient (today or later, SCHEDULED or CONFIRMED)
    public List<Appointment> getUpcomingAppointmentsOfPatient(String ssNumber) {
        List<AppointmentStatus> statuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);
        return appointmentRepo.findByPatient_SsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
                ssNumber, LocalDate.now(), statuses);
    }
}