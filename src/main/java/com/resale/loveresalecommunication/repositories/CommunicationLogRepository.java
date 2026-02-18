package com.resale.loveresalecommunication.repositories;

import com.resale.loveresalecommunication.models.CommunicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunicationLogRepository  extends JpaRepository<CommunicationLog, Integer> {

}


