package com.naharpurawala.internshiptracker.repository;

import com.naharpurawala.internshiptracker.entity.ApplicationStage;
import com.naharpurawala.internshiptracker.entity.InternshipApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InternshipApplicationRepository extends JpaRepository<InternshipApplication, Long> {

    Optional<InternshipApplication> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("""
            select a
            from InternshipApplication a
            where a.owner.id = :ownerId
              and (:stage is null or a.stage = :stage)
              and (
                    :search is null
                    or lower(a.company) like lower(concat('%', :search, '%'))
                    or lower(a.role) like lower(concat('%', :search, '%'))
                  )
            """)
    Page<InternshipApplication> searchForOwner(
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("stage") ApplicationStage stage,
            Pageable pageable
    );

    long countByOwnerId(Long ownerId);
    long countByOwnerIdAndStage(Long ownerId, ApplicationStage stage);
    long countByOwnerIdAndFollowUpNeededTrue(Long ownerId);

    List<InternshipApplication> findAllByUpdatedAtBeforeAndFollowUpNeededFalseAndStageIn(
            LocalDateTime cutoff,
            Collection<ApplicationStage> stages
    );
}
