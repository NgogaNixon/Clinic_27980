package kigali.clinic.rw.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.service.AppointmentService;
import kigali.clinic.rw.service.DoctorService;
import kigali.clinic.rw.service.PatientService;

// the old path /api/patient and the quiz path /api/patients both work
@RestController
@RequestMapping(value = { "/api/patient", "/api/patients" })
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> savePatient(@RequestBody Patient patient) {
        String returnedMessage = patientService.savePatient(patient);
        return new ResponseEntity<>(returnedMessage, HttpStatus.CREATED);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPatientById(@PathVariable UUID id) {
        Optional<Patient> patient = patientService.getPatientById(id);

        if (patient.isPresent()) {
            return new ResponseEntity<>(patient.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("we don't have patient with this id " + id, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePatient(@PathVariable UUID id, @RequestBody Patient patient) {
        String returnedMessage = patientService.updatePatient(id, patient);

        if (returnedMessage.startsWith("we don't have")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deletePatientById(@PathVariable UUID id) {
        String returnedMessage = patientService.deletePatientById(id);

        if (returnedMessage.startsWith("we don't have")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    // A1: /api/patients/by-last-name?lastName=uwase
    @GetMapping(value = "/by-last-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Patient>> getPatientsByLastName(@RequestParam String lastName) {
        List<Patient> patients = patientService.getPatientsByLastName(lastName);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    // B4: /api/patients/of-doctor/{doctorId}
    @GetMapping(value = "/of-doctor/{doctorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPatientsOfDoctor(@PathVariable UUID doctorId) {
        // if the doctor does not exist, answer 404
        if (doctorService.getDoctorById(doctorId).isEmpty()) {
            return new ResponseEntity<>("The doctor with that id does not exist", HttpStatus.NOT_FOUND);
        }

        List<Patient> patients = patientService.getPatientsOfDoctor(doctorId);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    // C2: /api/patients/frequent?min=3
    @GetMapping(value = "/frequent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Patient>> getFrequentPatients(@RequestParam long min) {
        List<Patient> patients = patientService.getFrequentPatients(min);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    // Quiz 2 D2: GET /api/patients/{ssNumber}/upcoming
    @GetMapping(value = "/{ssNumber}/upcoming", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUpcomingAppointments(@PathVariable String ssNumber) {
        // if the patient does not exist, answer 404
        if (!patientService.patientExistsBySsNumber(ssNumber)) {
            return new ResponseEntity<>("That patient with that ssnumber does not exist", HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = appointmentService.getUpcomingAppointmentsOfPatient(ssNumber);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }
}