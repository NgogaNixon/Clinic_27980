package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}