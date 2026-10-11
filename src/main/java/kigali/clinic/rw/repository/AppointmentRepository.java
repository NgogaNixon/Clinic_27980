package kigali.clinic.rw.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByDoctor_IdAndStatusOrderByAppointmentDateAsc(UUID doctorId, AppointmentStatus status);

    @Query("SELECT a.patient.firstName, a.patient.lastName, a.doctor.firstName, a.doctor.lastName, a.appointmentDate "
         + "FROM Appointment a "
         + "WHERE a.status = :status AND a.appointmentDate < :today")
    List<Object[]> findOverduePendingAppointments(@Param("status") AppointmentStatus status, @Param("today") LocalDate today);

    @Query("SELECT a.doctor.firstName, a.doctor.lastName, COUNT(a) "
         + "FROM Appointment a "
         + "GROUP BY a.doctor.id, a.doctor.firstName, a.doctor.lastName "
         + "ORDER BY COUNT(a) DESC")
    List<Object[]> countAppointmentsPerDoctor();

    @Query("SELECT a FROM Appointment a "
         + "JOIN a.doctor d "
         + "JOIN d.specializations s "
         + "WHERE s.id = :specializationId")
    List<Appointment> findAppointmentsByDoctorSpecialization(@Param("specializationId") UUID specializationId);

    Page<Appointment> findByDoctor_Id(UUID doctorId, Pageable pageable);

    // A2: appointments with a given status, earliest first
    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    // A3: appointments between two dates (both included), ordered by date
    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(LocalDate start, LocalDate end);

    // A4: does this doctor already have an appointment that day which is not cancelled?
    boolean existsByDoctor_IdAndAppointmentDateAndStatusNot(UUID doctorId, LocalDate date, AppointmentStatus status);

    // C1 (JPQL): how many appointments per status
    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatus();

    // C3 (JPQL): office name, office number and number of appointments, biggest first
    // the Pageable is only used to keep the first row
    @Query("SELECT o.name, o.officeNumber, COUNT(a) FROM Appointment a "
         + "JOIN a.doctor d "
         + "JOIN d.office o "
         + "GROUP BY o.id, o.name, o.officeNumber "
         + "ORDER BY COUNT(a) DESC")
    List<Object[]> findBusiestOffice(Pageable pageable);

    // C4 (JPQL UPDATE): cancel all appointments of a doctor on a date, except the COMPLETED ones
    @Modifying
    @Query("UPDATE Appointment a SET a.status = :newStatus "
         + "WHERE a.doctor.id = :doctorId "
         + "AND a.appointmentDate = :date "
         + "AND a.status <> :keepStatus")
    int cancelDoctorDay(@Param("doctorId") UUID doctorId,
                        @Param("date") LocalDate date,
                        @Param("newStatus") AppointmentStatus newStatus,
                        @Param("keepStatus") AppointmentStatus keepStatus);

    // Bonus (JPQL DELETE): remove cancelled appointments older than a date
    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.status = :status AND a.appointmentDate < :date")
    int deleteCancelledBefore(@Param("status") AppointmentStatus status, @Param("date") LocalDate date);

    // Quiz 2 D1 (DERIVED): appointments of a doctor on a day, without the cancelled ones
    List<Appointment> findByDoctor_IdAndAppointmentDateAndStatusNot(UUID doctorId, LocalDate date, AppointmentStatus status);

    // Quiz 2 D2 (DERIVED): appointments of a patient from a date on, with one of the given statuses, soonest first
    List<Appointment> findByPatient_SsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
            String ssNumber, LocalDate date, List<AppointmentStatus> statuses);

    // Quiz 2 D4 (DERIVED countBy): used by the dashboard
    long countByAppointmentDate(LocalDate date);

    long countByStatus(AppointmentStatus status);
}