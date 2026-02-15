package com.resale.homeflycommunication.repositories;

import com.resale.homeflycommunication.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Page<Notification> findByCustomerIdAndSourceOrderBySentAtDesc(
            Integer customerId,
            Integer source,
            Pageable pageable
    );

    Optional<Notification> findByIdAndCustomerIdAndSource(Integer id, Integer customerId, Integer source);
    Page<Notification> findByUserIdAndSourceOrderBySentAtDesc(Integer userId, Integer source, Pageable pageable);
    Page<Notification> findBySourceOrderBySentAtDesc(Integer source, Pageable pageable);

}


