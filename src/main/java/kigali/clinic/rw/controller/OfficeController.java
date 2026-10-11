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
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.service.AppointmentService;
import kigali.clinic.rw.service.OfficeService;

// the old path /api/office and the quiz path /api/offices both work
@RestController
@RequestMapping(value = { "/api/office", "/api/offices" })
public class OfficeController {

    @Autowired
    private OfficeService officeService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveOffice(@RequestBody Office office) {
        String returnedMessage = officeService.saveOffice(office);
        return new ResponseEntity<>(returnedMessage, HttpStatus.CREATED);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Office>> getAllOffices() {
        List<Office> offices = officeService.getAllOffices();
        return new ResponseEntity<>(offices, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOfficeById(@PathVariable UUID id) {
        Optional<Office> office = officeService.getOfficeById(id);

        if (office.isPresent()) {
            return new ResponseEntity<>(office.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("we don't have office with this id " + id, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateOffice(@PathVariable UUID id, @RequestBody Office office) {
        String returnedMessage = officeService.updateOffice(id, office);

        if (returnedMessage.startsWith("we don't have")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{officeNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteOneOffice(@PathVariable int officeNumber) {
        String returnedMessage = officeService.deleteOneOffice(officeNumber);

        if (returnedMessage.startsWith("we don't have")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    // C3: /api/offices/busiest
    @GetMapping(value = "/busiest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBusiestOffice() {
        List<Object[]> rows = appointmentService.getBusiestOffice();

        if (rows.isEmpty()) {
            return new ResponseEntity<>("No appointments yet", HttpStatus.OK);
        }
        return new ResponseEntity<>(rows.get(0), HttpStatus.OK);
    }

    // Quiz 2 A3: /api/offices/free
    @GetMapping(value = "/free", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Office>> getFreeOffices() {
        List<Office> offices = officeService.getFreeOffices();
        return new ResponseEntity<>(offices, HttpStatus.OK);
    }
}