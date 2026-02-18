package com.resale.loveresalecommunication.repositories;

import com.resale.loveresalecommunication.models.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Integer> {
    List<UserPermission> findByUserId(Integer userId);

}


