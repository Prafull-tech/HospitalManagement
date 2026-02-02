package com.hospital.hms.system.repository;

import com.hospital.hms.system.entity.ActionType;
import com.hospital.hms.system.entity.RoleModulePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleModulePermissionRepository extends JpaRepository<RoleModulePermission, Long> {

    @Query("SELECT rmp FROM RoleModulePermission rmp JOIN FETCH rmp.module WHERE rmp.role.id = :roleId ORDER BY rmp.module.code")
    List<RoleModulePermission> findByRoleIdWithModule(@Param("roleId") Long roleId);

    List<RoleModulePermission> findByRoleIdOrderByModuleId(Long roleId);

    List<RoleModulePermission> findByRoleIdAndModuleId(Long roleId, Long moduleId);

    Optional<RoleModulePermission> findByRoleIdAndModuleIdAndActionType(Long roleId, Long moduleId, ActionType actionType);

    void deleteByRoleIdAndModuleId(Long roleId, Long moduleId);

    @Query("SELECT rmp FROM RoleModulePermission rmp JOIN FETCH rmp.module WHERE rmp.role.id IN :roleIds")
    List<RoleModulePermission> findByRoleIdInWithModule(@Param("roleIds") List<Long> roleIds);
}
