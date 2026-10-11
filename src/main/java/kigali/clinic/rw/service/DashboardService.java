package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class DashboardService {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private OfficeService officeService;

    // Quiz 2 D4: one Map with the main numbers of the clinic
    public Map<String, Long> getSummary() {
        // LinkedHashMap keeps the order in which we put the values
        Map<String, Long> summary = new LinkedHashMap<>();

        summary.put("patients", patientRepo.count());
        summary.put("doctors", doctorRepo.count());

        // reuse A3: the free offices are the size of that list
        long freeOffices = officeService.getFreeOffices().size();
        summary.put("freeOffices", freeOffices);

        summary.put("appointmentsToday", appointmentRepo.countByAppointmentDate(LocalDate.now()));
        summary.put("scheduled", appointmentRepo.countByStatus(AppointmentStatus.SCHEDULED));
        summary.put("prescriptions", prescriptionRepo.count());

        return summary;
    }
}