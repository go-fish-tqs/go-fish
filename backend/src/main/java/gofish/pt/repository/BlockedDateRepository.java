package gofish.pt.repository;

import gofish.pt.entity.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {

    /**
     * Finds all blocked date periods for a specific item that fall within a given date range.
     * This is used to show all blocked periods in a calendar view, for example.
     * @param itemId The ID of the item.
     * @param startDate The start of the query range.
     * @param endDate The end of the query range.
     * @return A list of BlockedDate entities.
     */
    @Query("SELECT b FROM BlockedDate b WHERE b.item.id = :itemId AND b.startDate <= :endDate AND b.endDate >= :startDate")
    List<BlockedDate> findBlockedDatesInRange(
            @Param("itemId") Long itemId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
