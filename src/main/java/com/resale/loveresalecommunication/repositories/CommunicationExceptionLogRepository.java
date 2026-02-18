package com.resale.loveresalecommunication.repositories;

import com.resale.loveresalecommunication.models.CommunicationExceptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CommunicationExceptionLogRepository extends JpaRepository<CommunicationExceptionLog, Integer> {
}

