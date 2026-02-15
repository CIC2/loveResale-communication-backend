package com.resale.homeflycommunication.repositories;

import com.resale.homeflycommunication.models.CommunicationExceptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CommunicationExceptionLogRepository extends JpaRepository<CommunicationExceptionLog, Integer> {
}

