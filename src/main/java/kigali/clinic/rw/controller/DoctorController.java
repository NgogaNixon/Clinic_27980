package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.service.AppointmentService;
import kigali.clinic.rw.service.DoctorService;

// the old path /api/doctor and the quiz path /api/doctors both work
@RestController
@RequestMapping(value = { "/api/doctor", "/api/doctors" })
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor) {
        String returnedMessage = doctorService.saveDoctor(doctor);
        return new ResponseEntity<>(returnedMessage, HttpStatus.CREATED);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDoctorById(@PathVariable UUID id) {
        Optional<Doctor> doctor = doctorService.getDoctorById(id);

        if (doctor.isPresent()) {
            return new ResponseEntity<>(doctor.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("we don't have doctor with this id " + id, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateDoctor(@PathVariable UUID id, @RequestBody Doctor doctor) {
        String returnedMessage = doctorService.updateDoctor(id, doctor);

        if (returnedMessage.startsWith("we don't have")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteDoctorById(@PathVariable UUID id) {
        String returnedMessage = doctorService.deleteDoctorById(id);

        if (returnedMessage.startsWith("we don't have")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    // B1: /api/doctors/by-specialization?name=cardiology
    @GetMapping(value = "/by-specialization", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@RequestParam String name) {
        List<Doctor> doctors = doctorService.getDoctorsBySpecialization(name);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    // B2: /api/doctors/without-office
    @GetMapping(value = "/without-office", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Doctor>> getDoctorsWithoutOffice() {
        List<Doctor> doctors = doctorService.getDoctorsWithoutOffice();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    // Quiz 2 A1: PUT /api/doctors/{doctorId}/specializations/{specializationId}
    @PutMapping(value = "/{doctorId}/specializations/{specializationId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addSpecializationToDoctor(@PathVariable UUID doctorId,
                                                       @PathVariable UUID specializationId) {
        String returnedMessage = doctorService.addSpecializationToDoctor(doctorId, specializationId);

        if (returnedMessage.equals("The doctor with that id does not exist")
                || returnedMessage.equals("That specialization does not exist")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        } else if (returnedMessage.equals("The doctor already has that specialization")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }
    }

    // Quiz 2 A2: DELETE /api/doctors/{doctorId}/specializations/{specializationId}
    @DeleteMapping(value = "/{doctorId}/specializations/{specializationId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> removeSpecializationFromDoctor(@PathVariable UUID doctorId,
                                                            @PathVariable UUID specializationId) {
        String returnedMessage = doctorService.removeSpecializationFromDoctor(doctorId, specializationId);

        if (returnedMessage.equals("The doctor with that id does not exist")
                || returnedMessage.equals("That specialization does not exist")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        } else if (returnedMessage.equals("The doctor does not have that specialization")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }
    }

    // Quiz 2 A4: PATCH /api/doctors/{doctorId}/office?officeNumber=104
    @PatchMapping(value = "/{doctorId}/office", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeDoctorOffice(@PathVariable UUID doctorId,
                                                @RequestParam int officeNumber) {
        String returnedMessage = doctorService.changeDoctorOffice(doctorId, officeNumber);

        if (returnedMessage.equals("The doctor with that id does not exist")
                || returnedMessage.equals("The office with that office number does not exist")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        } else if (returnedMessage.equals("That office is taken by other doctor")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }
    }

    // Quiz 2 D1: GET /api/doctors/{doctorId}/agenda?date=2026-10-20
    @GetMapping(value = "/{doctorId}/agenda", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDoctorAgenda(@PathVariable UUID doctorId, @RequestParam String date) {
        // if the doctor does not exist, answer 404
        if (doctorService.getDoctorById(doctorId).isEmpty()) {
            return new ResponseEntity<>("The doctor with that id does not exist", HttpStatus.NOT_FOUND);
        }

        LocalDate day = LocalDate.parse(date);
        List<Appointment> appointments = appointmentService.getDoctorAgenda(doctorId, day);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    // Quiz 2 D3: GET /api/doctors/search?name=gis
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Doctor>> searchDoctorsByName(@RequestParam String name) {
        List<Doctor> doctors = doctorService.searchDoctorsByName(name);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    // Quiz 2 Bonus: GET /api/doctors/workload?start=2026-10-01&end=2026-10-31
    @GetMapping(value = "/workload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object[]>> getDoctorWorkload(@RequestParam String start,
                                                            @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        List<Object[]> workload = doctorService.getDoctorWorkload(startDate, endDate);
        return new ResponseEntity<>(workload, HttpStatus.OK);
    }
}