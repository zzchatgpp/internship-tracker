package com.naharpurawala.internshiptracker.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
@Component @RequiredArgsConstructor @Slf4j
public class FollowUpScheduler{
 private final InternshipApplicationService applicationService;
 @Value("${app.follow-up.days:7}") private long inactivityDays;
 @Scheduled(cron="${app.follow-up.cron:0 0 * * * *}") public void flagInactiveApplications(){
  int flagged=applicationService.flagApplicationsNeedingFollowUp(LocalDateTime.now().minusDays(inactivityDays));
  if(flagged>0)log.info("Flagged {} internship application(s) for follow-up",flagged);
 }
}
