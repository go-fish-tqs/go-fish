package gofish.pt.repository;

import gofish.pt.entity.Booking;
import gofish.pt.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

        // Query para ver se já existe alguma reserva "ATIVA" ou "CONFIRMADA" que se
        // sobreponha às datas
        // A lógica é: (StartA < EndB) e (EndA > StartB)
        @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
                        "FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.status NOT IN (gofish.pt.entity.BookingStatus.CANCELLED, gofish.pt.entity.BookingStatus.PENDING, gofish.pt.entity.BookingStatus.COMPLETED) "
                        +
                        "AND b.startDate < :endDate " +
                        "AND b.endDate > :startDate")
        boolean existsOverlappingBooking(@Param("itemId") Long itemId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.status IN (gofish.pt.entity.BookingStatus.CONFIRMED, gofish.pt.entity.BookingStatus.ACTIVE) "
                        +
                        "AND b.endDate >= :start " +
                        "AND b.startDate <= :end")
        List<Booking> findBookingsInRange(@Param("itemId") Long itemId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

        List<Booking> findAllByUserId(Long userId);

        List<Booking> findAllByItemId(Long itemId);

        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.id = :itemId " +
                        "AND b.status NOT IN (gofish.pt.entity.BookingStatus.CANCELLED) " +
                        "AND b.endDate >= :start " +
                        "AND b.startDate <= :end " +
                        "ORDER BY b.startDate ASC")
        List<Booking> findByItemIdAndDateRange(@Param("itemId") Long itemId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

        // Find all bookings on items owned by a specific user
        @Query("SELECT b FROM Booking b " +
                        "WHERE b.item.owner.id = :ownerId " +
                        "ORDER BY b.startDate DESC")
        List<Booking> findAllByItemOwnerId(@Param("ownerId") Long ownerId);

}