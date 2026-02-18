package com.resale.loveresalecommunication.repositories;

import com.resale.loveresalecommunication.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

}

