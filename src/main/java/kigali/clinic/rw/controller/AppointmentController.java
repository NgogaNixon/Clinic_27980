package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.service.AppointmentService;

// the old path /api/appointment and the quiz path /api/appointments both work
@RestController
@RequestMapping(value = { "/api/appointment", "/api/appointments" })
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveAppointment(@RequestBody Appointment appointment) {
        String returnedMessage = appointmentService.saveAppointment(appointment);

        // A4: double booking gives 409
        if (returnedMessage.startsWith("Doctor is already booked")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.CREATED);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentById(@PathVariable UUID id) {
        Optional<Appointment> appointment = appointmentService.getAppointmentById(id);

        if (appointment.isPresent()) {
            return new ResponseEntity<>(appointment.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("we don't have appointment with this id " + id, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAppointment(@PathVariable UUID id, @RequestBody Appointment appointment) {
        String returnedMessage = appointmentService.updateAppointment(id, appointment);

        if (returnedMessage.startsWith("we don't have")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAppointmentById(@PathVariable UUID id) {
        String returnedMessage = appointmentService.deleteAppointmentById(id);

        if (returnedMessage.startsWith("we don't have")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @GetMapping(value = "/doctor/{doctorId}/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getPendingAppointmentsByDoctor(@PathVariable UUID doctorId) {
        List<Appointment> appointments = appointmentService.getPendingAppointmentsByDoctor(doctorId);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping(value = "/overdue-pending", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object[]>> getOverduePendingAppointments() {
        List<Object[]> appointments = appointmentService.getOverduePendingAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping(value = "/count-per-doctor", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object[]>> getAppointmentCountPerDoctor() {
        List<Object[]> counts = appointmentService.getAppointmentCountPerDoctor();
        return new ResponseEntity<>(counts, HttpStatus.OK);
    }

    @GetMapping(value = "/specialization/{specializationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorSpecialization(@PathVariable UUID specializationId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorSpecialization(specializationId);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    // A2: /api/appointments/by-status?status=SCHEDULED
    @GetMapping(value = "/by-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@RequestParam AppointmentStatus status) {
        List<Appointment> appointments = appointmentService.getAppointmentsByStatus(status);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    // A3: /api/appointments/between?start=2026-10-01&end=2026-10-31
    @GetMapping(value = "/between", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getAppointmentsBetween(@RequestParam String start,
                                                                    @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        List<Appointment> appointments = appointmentService.getAppointmentsBetween(startDate, endDate);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    // C1: /api/appointments/stats/by-status
    @GetMapping(value = "/stats/by-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object[]>> getStatsByStatus() {
        List<Object[]> stats = appointmentService.getAppointmentCountByStatus();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    // C4: /api/appointments/cancel-day?doctorId=...&date=2026-10-20
    @PatchMapping(value = "/cancel-day", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> cancelDay(@RequestParam UUID doctorId, @RequestParam String date) {
        LocalDate day = LocalDate.parse(date);
        String returnedMessage = appointmentService.cancelDoctorDay(doctorId, day);
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    // Bonus: /api/appointments/page?page=0&size=5&sort=appointmentDate,desc
    @GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Appointment>> getAppointmentsPage(@RequestParam int page,
                                                                 @RequestParam int size,
                                                                 @RequestParam String sort) {
        // "appointmentDate,desc" becomes the field name and the direction
        String[] sortParts = sort.split(",");
        String fieldName = sortParts[0];

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length > 1) {
            direction = Sort.Direction.fromString(sortParts[1]);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, fieldName));
        Page<Appointment> result = appointmentService.getAppointmentsPage(pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // Bonus: /api/appointments/cancelled-before?date=2026-10-15
    @DeleteMapping(value = "/cancelled-before", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteCancelledBefore(@RequestParam String date) {
        LocalDate day = LocalDate.parse(date);
        String returnedMessage = appointmentService.deleteCancelledBefore(day);
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }
}