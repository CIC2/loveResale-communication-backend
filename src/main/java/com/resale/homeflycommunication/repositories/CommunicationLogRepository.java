package com.resale.homeflycommunication.repositories;

import com.resale.homeflycommunication.models.CommunicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunicationLogRepository  extends JpaRepository<CommunicationLog, Integer> {

}


