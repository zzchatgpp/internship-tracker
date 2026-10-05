package com.naharpurawala.internshiptracker.service;

import com.naharpurawala.internshiptracker.dto.ApplicationRequest;
import com.naharpurawala.internshiptracker.dto.DashboardStats;
import com.naharpurawala.internshiptracker.entity.*;
import com.naharpurawala.internshiptracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InternshipApplicationService {
    private static final int PAGE_SIZE=8;
    private static final Set<ApplicationStage> FOLLOW_UP_STAGES=Set.of(ApplicationStage.APPLIED,ApplicationStage.INTERVIEW);
    private final InternshipApplicationRepository applicationRepository;
    private final StageHistoryRepository stageHistoryRepository;

    @Transactional(readOnly=true)
    public Page<InternshipApplication> searchForOwner(User owner,String search,ApplicationStage stage,int page){
        String normalizedSearch=normalizeSearch(search);
        int safePage=Math.max(page,0);
        return applicationRepository.searchForOwner(owner.getId(),normalizedSearch,stage,PageRequest.of(safePage,PAGE_SIZE,Sort.by(Sort.Direction.DESC,"updatedAt").and(Sort.by(Sort.Direction.DESC,"id"))));
    }

    @Transactional(readOnly=true)
    public DashboardStats getDashboardStats(User owner){
        Long ownerId=owner.getId();
        long total=applicationRepository.countByOwnerId(ownerId);
        long applied=applicationRepository.countByOwnerIdAndStage(ownerId,ApplicationStage.APPLIED);
        long interview=applicationRepository.countByOwnerIdAndStage(ownerId,ApplicationStage.INTERVIEW);
        long offer=applicationRepository.countByOwnerIdAndStage(ownerId,ApplicationStage.OFFER);
        long rejected=applicationRepository.countByOwnerIdAndStage(ownerId,ApplicationStage.REJECTED);
        long followUps=applicationRepository.countByOwnerIdAndFollowUpNeededTrue(ownerId);
        double interviewRate=total==0?0.0:Math.round(((interview+offer)*1000.0)/total)/10.0;
        return new DashboardStats(total,applied,interview,offer,rejected,followUps,interviewRate);
    }

    @Transactional(readOnly=true)
    public InternshipApplication requireOwnedApplication(Long id,User owner){
        return applicationRepository.findByIdAndOwnerId(id,owner.getId()).orElseThrow(()->new ApplicationNotFoundException(id));
    }

    @Transactional(readOnly=true)
    public List<StageHistory> getStageHistory(Long applicationId,User owner){
        requireOwnedApplication(applicationId,owner);
        return stageHistoryRepository.findAllByApplicationIdOrderByChangedAtAsc(applicationId);
    }

    @Transactional
    public InternshipApplication create(ApplicationRequest request,User owner){
        InternshipApplication application=InternshipApplication.builder()
            .company(clean(request.getCompany())).role(clean(request.getRole()))
            .location(cleanNullable(request.getLocation())).jobUrl(cleanNullable(request.getJobUrl()))
            .appliedDate(request.getAppliedDate()).stage(request.getStage())
            .notes(cleanNullable(request.getNotes())).owner(owner).build();
        InternshipApplication saved=applicationRepository.save(application);
        recordStageChange(saved,null,saved.getStage());
        return saved;
    }

    @Transactional
    public InternshipApplication update(Long id,ApplicationRequest request,User owner){
        InternshipApplication application=requireOwnedApplication(id,owner);
        ApplicationStage previousStage=application.getStage();
        ApplicationStage nextStage=request.getStage();
        application.setCompany(clean(request.getCompany())); application.setRole(clean(request.getRole()));
        application.setLocation(cleanNullable(request.getLocation())); application.setJobUrl(cleanNullable(request.getJobUrl()));
        application.setAppliedDate(request.getAppliedDate()); application.setStage(nextStage);
        application.setNotes(cleanNullable(request.getNotes())); application.setFollowUpNeeded(false); application.touch();
        InternshipApplication saved=applicationRepository.save(application);
        if(!Objects.equals(previousStage,nextStage))recordStageChange(saved,previousStage,nextStage);
        return saved;
    }

    @Transactional public void delete(Long id,User owner){ applicationRepository.delete(requireOwnedApplication(id,owner)); }

    @Transactional
    public int flagApplicationsNeedingFollowUp(LocalDateTime cutoff){
        List<InternshipApplication> stale=applicationRepository.findAllByUpdatedAtBeforeAndFollowUpNeededFalseAndStageIn(cutoff,FOLLOW_UP_STAGES);
        stale.forEach(a->a.setFollowUpNeeded(true)); applicationRepository.saveAll(stale); return stale.size();
    }

    public ApplicationRequest toRequest(InternshipApplication application){
        ApplicationRequest request=new ApplicationRequest();
        request.setCompany(application.getCompany()); request.setRole(application.getRole()); request.setLocation(application.getLocation());
        request.setJobUrl(application.getJobUrl()); request.setAppliedDate(application.getAppliedDate()); request.setStage(application.getStage()); request.setNotes(application.getNotes());
        return request;
    }

    private void recordStageChange(InternshipApplication application,ApplicationStage fromStage,ApplicationStage toStage){
        stageHistoryRepository.save(StageHistory.builder().application(application).fromStage(fromStage).toStage(toStage).build());
    }
    private String normalizeSearch(String search){if(search==null)return null;String n=search.trim();return n.isEmpty()?null:n;}
    private String clean(String value){return value==null?null:value.trim();}
    private String cleanNullable(String value){if(value==null)return null;String c=value.trim();return c.isEmpty()?null:c;}
    public static class ApplicationNotFoundException extends RuntimeException{public ApplicationNotFoundException(Long id){super("Application "+id+" was not found");}}
}
