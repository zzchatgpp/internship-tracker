package com.naharpurawala.internshiptracker.repository;
import com.naharpurawala.internshiptracker.entity.StageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StageHistoryRepository extends JpaRepository<StageHistory,Long>{ List<StageHistory> findAllByApplicationIdOrderByChangedAtAsc(Long applicationId); }
