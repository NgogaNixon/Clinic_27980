package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    // Quiz 2 C2 (DERIVED): prescriptions of one appointment
    List<Prescription> findByAppointment(Appointment appointment);

    // Quiz 2 C3 (JPQL): every prescription of a patient, newest first
    @Query("SELECT p FROM Prescription p "
         + "WHERE p.appointment.patient.ssNumber = :ss "
         + "ORDER BY p.issuedDate DESC")
    List<Prescription> findPrescriptionsByPatientSsNumber(@Param("ss") String ssNumber);

    // Quiz 2 C4 (DERIVED): does this appointment have at least one prescription?
    boolean existsByAppointment_Id(UUID appointmentId);

    // Quiz 2 Bonus (JPQL): one row per medicine with how many times it was prescribed, most prescribed first
    @Query("SELECT p.medicineName, COUNT(p) FROM Prescription p "
         + "GROUP BY p.medicineName "
         + "ORDER BY COUNT(p) DESC")
    List<Object[]> findTopMedicines();
}