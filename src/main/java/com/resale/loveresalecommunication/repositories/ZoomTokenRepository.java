package com.resale.loveresalecommunication.repositories;

import com.resale.loveresalecommunication.models.ZoomToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoomTokenRepository extends JpaRepository<ZoomToken, Integer> {
    ZoomToken findTopByOrderByIdDesc();

}


