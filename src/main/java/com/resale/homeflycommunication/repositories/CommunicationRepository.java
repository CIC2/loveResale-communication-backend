package com.resale.homeflycommunication.repositories;

import com.resale.homeflycommunication.models.Communication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunicationRepository extends JpaRepository<Communication, Integer> {
    Page<Communication> findCommunicationByType(int typeId, Pageable pageable);
}


