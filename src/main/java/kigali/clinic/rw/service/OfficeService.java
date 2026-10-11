package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.repository.OfficeRepository;

@Service
public class OfficeService {

    @Autowired
    private OfficeRepository officeRepo;

    public String saveOffice(Office office) {
        if (officeRepo.existsByOfficeNumber(office.getOfficeNumber())) {
            return "office number " + office.getOfficeNumber() + " is already in use";
        }

        officeRepo.save(office);
        return "saved successfully";
    }

    public List<Office> getAllOffices() {
        return officeRepo.findAll();
    }

    public Optional<Office> getOfficeById(UUID id) {
        return officeRepo.findById(id);
    }

    public String updateOffice(UUID id, Office office) {
        Optional<Office> existingOffice = officeRepo.findById(id);

        if (existingOffice.isEmpty()) {
            return "we don't have office with this id " + id;
        }

        Office officeToUpdate = existingOffice.get();

        // only enforce uniqueness if the number is actually changing
        if (office.getOfficeNumber() != officeToUpdate.getOfficeNumber()
                && officeRepo.existsByOfficeNumber(office.getOfficeNumber())) {
            return "office number " + office.getOfficeNumber() + " is already in use";
        }

        officeToUpdate.setName(office.getName());
        officeToUpdate.setOfficeNumber(office.getOfficeNumber());

        officeRepo.save(officeToUpdate);
        return "updated successfully";
    }

    public String deleteOneOffice(int officeNumber) {

        Optional<Office> getOneOffice = officeRepo.findByOfficeNumber(officeNumber);

        if (getOneOffice.isPresent()) {
            officeRepo.deleteById(getOneOffice.get().getId());
            return "Office with this number " + officeNumber + " is deleted successfully";
        } else {
            return "we don't have office with that office Number " + officeNumber;
        }
    }

    // Quiz 2 A3
    public List<Office> getFreeOffices() {
        return officeRepo.findFreeOffices();
    }
}