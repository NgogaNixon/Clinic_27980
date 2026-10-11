package kigali.clinic.rw.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    // B1 (JPQL): doctors who hold a specialization, letter case does not matter
    @Query("SELECT d FROM Doctor d "
         + "JOIN d.specializations s "
         + "WHERE LOWER(s.name) = LOWER(:name)")
    List<Doctor> findDoctorsBySpecializationName(@Param("name") String name);

    // B2 (JPQL): doctors with no office, sorted by last name
    @Query("SELECT d FROM Doctor d "
         + "WHERE d.office IS NULL "
         + "ORDER BY d.lastName ASC")
    List<Doctor> findDoctorsWithoutOffice();

    // Quiz 2 A4 (DERIVED): is this office number used by a doctor other than this one?
    boolean existsByOffice_OfficeNumberAndIdNot(int officeNumber, UUID doctorId);

    // Quiz 2 D3 (JPQL): first name or last name contains the text, letter case does not matter
    @Query("SELECT d FROM Doctor d "
         + "WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%')) "
         + "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%')) "
         + "ORDER BY d.lastName ASC")
    List<Doctor> searchDoctorsByName(@Param("name") String name);

    // Quiz 2 Bonus (JPQL): first name, last name and number of appointments with the given status
    // between two dates (both included), busiest doctor first, doctors with zero do not appear
    @Query("SELECT d.firstName, d.lastName, COUNT(a) FROM Doctor d "
         + "JOIN d.appointments a "
         + "WHERE a.status = :status "
         + "AND a.appointmentDate BETWEEN :start AND :end "
         + "GROUP BY d.id, d.firstName, d.lastName "
         + "ORDER BY COUNT(a) DESC")
    List<Object[]> findDoctorWorkload(@Param("status") AppointmentStatus status,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);
}