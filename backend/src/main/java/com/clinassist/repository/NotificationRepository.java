package com.clinassist.repository;

import com.clinassist.entity.Notification;
import com.clinassist.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUser(User user);
    
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId " +
           "AND n.status = 'UNREAD' ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotifications(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadNotifications(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.scheduledFor <= :now " +
           "AND n.isSent = false")
    List<Notification> findScheduledNotificationsToSend(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, " +
           "n.status = 'READ' WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId, 
                    @Param("readAt") LocalDateTime readAt);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, " +
           "n.status = 'READ' WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
    
    @Query("SELECT n FROM Notification n WHERE n.referenceId = :refId " +
           "AND n.referenceType = :refType")
    List<Notification> findByReference(
            @Param("refId") Long referenceId,
            @Param("refType") String referenceType);
}

