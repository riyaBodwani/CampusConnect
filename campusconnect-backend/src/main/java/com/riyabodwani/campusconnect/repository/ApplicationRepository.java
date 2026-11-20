package com.riyabodwani.campusconnect.repository;

import com.riyabodwani.campusconnect.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByStudentIdAndPlacementDriveId(Long studentId, Long placementDriveId);
    List<Application> findByStudentId(Long studentId);

    @Query("SELECT a FROM Application a WHERE a.placementDrive.company.id = :companyId")
    List<Application> findByPlacementDriveCompanyId(@Param("companyId") Long companyId);
}
