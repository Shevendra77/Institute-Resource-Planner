package com.example.irp.repository;

import com.example.irp.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Integer> {

    // --- UPDATED METHOD FOR SCHEDULER ---
    // Ab ye 'approvalTime' ko track karega
    @Query("SELECT a FROM Allocation a WHERE a.status = 'APPROVED_PENDING_DELIVERY' AND a.approvalTime < :limit")
    List<Allocation> findExpiredByApprovalTime(@Param("limit") LocalDateTime limit);

    // Standard Spring Data JPA Finder
    List<Allocation> findByStatus(String status);

    @Query("SELECT a FROM Allocation a WHERE a.userId = :userId")
    List<Allocation> findByUserId(@Param("userId") int userId);

    // Dynamic locked quantity tracker
    @Query(value = "SELECT COALESCE(SUM(quantity), 0) FROM allocation " +
            "WHERE resource_id = :resourceId " +
            "AND status IN ('ISSUED', 'RETURN_PENDING_ADMIN', 'APPROVED_PENDING_DELIVERY', 'OVERDUE')", nativeQuery = true)
    int getBookedQuantityByResourceId(@Param("resourceId") int resourceId);

    // Overlapping schedule block validator
    @Query(value = "SELECT COUNT(*) FROM allocation WHERE resource_id = :resourceId " +
            "AND status NOT IN ('REJECTED', 'REJECTED_BY_USER', 'TIME_EXPIRED') " +
            "AND (:startTime < end_time AND :endTime > start_time)", nativeQuery = true)
    long countOverlappingBookings(@Param("resourceId") int resourceId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    // Active allocations concurrent counter
    @Query(value = "SELECT COUNT(*) FROM allocation WHERE resource_id = :resourceId " +
            "AND status IN ('ISSUED', 'RETURN_PENDING_ADMIN', 'OVERDUE') " +
            "AND (:now BETWEEN start_time AND end_time)", nativeQuery = true)
    long countActiveBookingsNow(@Param("resourceId") int resourceId, @Param("now") LocalDateTime now);

    // State management database query modifier
    @Transactional
    @Modifying
    @Query("UPDATE Allocation a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("id") int id, @Param("status") String status);
}