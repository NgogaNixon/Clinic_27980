package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    // A1 (DERIVED): patients with this last name, any letter case, sorted by first name A to Z
    List<Patient> findByLastNameIgnoreCaseOrderByFirstNameAsc(String lastName);

    // B4 (JPQL): patients who have at least one appointment with this doctor, each listed once
    @Query("SELECT DISTINCT p FROM Patient p "
         + "JOIN p.appointments a "
         + "WHERE a.doctor.id = :doctorId")
    List<Patient> findPatientsByDoctorId(@Param("doctorId") UUID doctorId);

    // C2 (JPQL): patients with at least :min appointments, the one with the most first
    @Query("SELECT p FROM Patient p "
         + "JOIN p.appointments a "
         + "GROUP BY p.id "
         + "HAVING COUNT(a) >= :min "
         + "ORDER BY COUNT(a) DESC")
    List<Patient> findFrequentPatients(@Param("min") long min);

    // Quiz 2 D2 (DERIVED): does a patient with this ssNumber exist?
    boolean existsBySsNumber(String ssNumber);
}