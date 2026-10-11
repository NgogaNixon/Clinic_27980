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
import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.service.AppointmentService;
import kigali.clinic.rw.service.PrescriptionService;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private AppointmentService appointmentService;

    // Quiz 2 C1: POST /api/prescriptions/save?appointmentId=...
    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> savePrescription(@RequestParam UUID appointmentId,
                                              @RequestBody Prescription prescription) {
        String returnedMessage = prescriptionService.savePrescription(appointmentId, prescription);

        if (returnedMessage.startsWith("The appointment with that id")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        } else if (returnedMessage.startsWith("Prescriptions are only for")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(returnedMessage, HttpStatus.CREATED);
        }
    }

    // Quiz 2 C2: GET /api/prescriptions/appointment/{appointmentId}
    @GetMapping(value = "/appointment/{appointmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPrescriptionsOfAppointment(@PathVariable UUID appointmentId) {
        Optional<Appointment> appointment = appointmentService.getAppointmentById(appointmentId);

        // if the appointment does not exist, answer 404
        if (appointment.isEmpty()) {
            return new ResponseEntity<>("The appointment with that id does not exist", HttpStatus.NOT_FOUND);
        }

        List<Prescription> prescriptions = prescriptionService.getPrescriptionsOfAppointment(appointment.get());
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    // Quiz 2 C3: GET /api/prescriptions/patient/{ssNumber}
    @GetMapping(value = "/patient/{ssNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Prescription>> getPrescriptionsOfPatient(@PathVariable String ssNumber) {
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsOfPatient(ssNumber);
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    // Quiz 2 C4: PUT /api/prescriptions/update/{id}
    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePrescription(@PathVariable UUID id, @RequestBody Prescription prescription) {
        String returnedMessage = prescriptionService.updatePrescription(id, prescription);

        if (returnedMessage.startsWith("The prescription with that id")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    // Quiz 2 C4: DELETE /api/prescriptions/delete/{id}
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deletePrescriptionById(@PathVariable UUID id) {
        String returnedMessage = prescriptionService.deletePrescriptionById(id);

        if (returnedMessage.startsWith("The prescription with that id")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    // Quiz 2 Bonus: GET /api/prescriptions/stats/top-medicines
    @GetMapping(value = "/stats/top-medicines", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Object[]>> getTopMedicines() {
        List<Object[]> medicines = prescriptionService.getTopMedicines();
        return new ResponseEntity<>(medicines, HttpStatus.OK);
    }
}