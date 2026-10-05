package com.naharpurawala.internshiptracker.service;

import com.naharpurawala.internshiptracker.dto.ApplicationRequest;
import com.naharpurawala.internshiptracker.dto.DashboardStats;
import com.naharpurawala.internshiptracker.entity.ApplicationStage;
import com.naharpurawala.internshiptracker.entity.InternshipApplication;
import com.naharpurawala.internshiptracker.entity.StageHistory;
import com.naharpurawala.internshiptracker.entity.User;
import com.naharpurawala.internshiptracker.repository.InternshipApplicationRepository;
import com.naharpurawala.internshiptracker.repository.StageHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternshipApplicationServiceTest {
    @Mock private InternshipApplicationRepository applicationRepository;
    @Mock private StageHistoryRepository stageHistoryRepository;
    private InternshipApplicationService service;
    private User owner;

    @BeforeEach
    void setUp() {
        service = new InternshipApplicationService(applicationRepository, stageHistoryRepository);
        owner = User.builder().id(10L).name("Mohammed").email("mohammed@example.com").password("encoded").build();
    }

    @Test
    void requireOwnedApplication_returnsApplicationForCorrectOwner() {
        InternshipApplication application = application(42L, owner, ApplicationStage.APPLIED);
        when(applicationRepository.findByIdAndOwnerId(42L, 10L)).thenReturn(Optional.of(application));
        InternshipApplication result = service.requireOwnedApplication(42L, owner);
        assertThat(result).isSameAs(application);
        verify(applicationRepository).findByIdAndOwnerId(42L, 10L);
    }

    @Test
    void requireOwnedApplication_blocksReadingAnotherUsersApplication() {
        when(applicationRepository.findByIdAndOwnerId(99L, 10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.requireOwnedApplication(99L, owner))
                .isInstanceOf(InternshipApplicationService.ApplicationNotFoundException.class)
                .hasMessageContaining("99");
        verify(applicationRepository).findByIdAndOwnerId(99L, 10L);
        verify(applicationRepository, never()).findById(99L);
    }

    @Test
    void update_blocksChangingAnotherUsersApplication() {
        when(applicationRepository.findByIdAndOwnerId(99L, 10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, request(ApplicationStage.INTERVIEW), owner))
                .isInstanceOf(InternshipApplicationService.ApplicationNotFoundException.class);
        verify(applicationRepository, never()).save(any(InternshipApplication.class));
        verify(stageHistoryRepository, never()).save(any(StageHistory.class));
    }

    @Test
    void delete_blocksDeletingAnotherUsersApplication() {
        when(applicationRepository.findByIdAndOwnerId(99L, 10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L, owner))
                .isInstanceOf(InternshipApplicationService.ApplicationNotFoundException.class);
        verify(applicationRepository, never()).delete(any(InternshipApplication.class));
    }

    @Test
    void create_assignsOwnerTrimsFieldsAndRecordsInitialStage() {
        ApplicationRequest request = request(ApplicationStage.APPLIED);
        request.setCompany("  OpenAI  ");
        request.setRole("  Software Engineering Intern ");
        request.setLocation("   ");
        request.setNotes("  Interested in backend work  ");
        when(applicationRepository.save(any(InternshipApplication.class))).thenAnswer(invocation -> {
            InternshipApplication saved = invocation.getArgument(0);
            saved.setId(50L);
            return saved;
        });

        InternshipApplication created = service.create(request, owner);
        assertThat(created.getOwner()).isSameAs(owner);
        assertThat(created.getCompany()).isEqualTo("OpenAI");
        assertThat(created.getRole()).isEqualTo("Software Engineering Intern");
        assertThat(created.getLocation()).isNull();
        assertThat(created.getNotes()).isEqualTo("Interested in backend work");

        ArgumentCaptor<StageHistory> historyCaptor = ArgumentCaptor.forClass(StageHistory.class);
        verify(stageHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getApplication()).isSameAs(created);
        assertThat(historyCaptor.getValue().getFromStage()).isNull();
        assertThat(historyCaptor.getValue().getToStage()).isEqualTo(ApplicationStage.APPLIED);
    }

    @Test
    void update_recordsHistoryOnlyWhenStageActuallyChanges() {
        InternshipApplication existing = application(42L, owner, ApplicationStage.APPLIED);
        existing.setFollowUpNeeded(true);
        when(applicationRepository.findByIdAndOwnerId(42L, 10L)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(existing)).thenReturn(existing);
        service.update(42L, request(ApplicationStage.INTERVIEW), owner);
        assertThat(existing.getStage()).isEqualTo(ApplicationStage.INTERVIEW);
        assertThat(existing.isFollowUpNeeded()).isFalse();
        ArgumentCaptor<StageHistory> historyCaptor = ArgumentCaptor.forClass(StageHistory.class);
        verify(stageHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getFromStage()).isEqualTo(ApplicationStage.APPLIED);
        assertThat(historyCaptor.getValue().getToStage()).isEqualTo(ApplicationStage.INTERVIEW);
    }

    @Test
    void update_doesNotCreateFakeHistoryWhenStageIsUnchanged() {
        InternshipApplication existing = application(42L, owner, ApplicationStage.APPLIED);
        when(applicationRepository.findByIdAndOwnerId(42L, 10L)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(existing)).thenReturn(existing);
        service.update(42L, request(ApplicationStage.APPLIED), owner);
        verify(stageHistoryRepository, never()).save(any(StageHistory.class));
    }

    @Test
    void flagApplicationsNeedingFollowUp_marksEveryStaleApplicationReturnedByRepository() {
        LocalDateTime cutoff = LocalDateTime.of(2026, 10, 1, 12, 0);
        InternshipApplication first = application(1L, owner, ApplicationStage.APPLIED);
        InternshipApplication second = application(2L, owner, ApplicationStage.INTERVIEW);
        when(applicationRepository.findAllByUpdatedAtBeforeAndFollowUpNeededFalseAndStageIn(any(LocalDateTime.class), any()))
                .thenReturn(List.of(first, second));
        int flagged = service.flagApplicationsNeedingFollowUp(cutoff);
        assertThat(flagged).isEqualTo(2);
        assertThat(first.isFollowUpNeeded()).isTrue();
        assertThat(second.isFollowUpNeeded()).isTrue();
        verify(applicationRepository).saveAll(List.of(first, second));
    }

    @Test
    void getDashboardStats_calculatesCountsAndInterviewRateForOwner() {
        when(applicationRepository.countByOwnerId(10L)).thenReturn(10L);
        when(applicationRepository.countByOwnerIdAndStage(10L, ApplicationStage.APPLIED)).thenReturn(4L);
        when(applicationRepository.countByOwnerIdAndStage(10L, ApplicationStage.INTERVIEW)).thenReturn(3L);
        when(applicationRepository.countByOwnerIdAndStage(10L, ApplicationStage.OFFER)).thenReturn(1L);
        when(applicationRepository.countByOwnerIdAndStage(10L, ApplicationStage.REJECTED)).thenReturn(2L);
        when(applicationRepository.countByOwnerIdAndFollowUpNeededTrue(10L)).thenReturn(2L);

        DashboardStats stats = service.getDashboardStats(owner);
        assertThat(stats.totalApplications()).isEqualTo(10L);
        assertThat(stats.appliedCount()).isEqualTo(4L);
        assertThat(stats.interviewCount()).isEqualTo(3L);
        assertThat(stats.offerCount()).isEqualTo(1L);
        assertThat(stats.rejectedCount()).isEqualTo(2L);
        assertThat(stats.followUpCount()).isEqualTo(2L);
        assertThat(stats.interviewRate()).isEqualTo(40.0);
    }

    private InternshipApplication application(Long id, User applicationOwner, ApplicationStage stage) {
        return InternshipApplication.builder().id(id).company("Example Co").role("Java Intern")
                .appliedDate(LocalDate.of(2026, 9, 1)).stage(stage).owner(applicationOwner).build();
    }

    private ApplicationRequest request(ApplicationStage stage) {
        ApplicationRequest request = new ApplicationRequest();
        request.setCompany("Example Co");
        request.setRole("Java Intern");
        request.setAppliedDate(LocalDate.of(2026, 9, 1));
        request.setStage(stage);
        request.setNotes("Updated notes");
        return request;
    }
}
