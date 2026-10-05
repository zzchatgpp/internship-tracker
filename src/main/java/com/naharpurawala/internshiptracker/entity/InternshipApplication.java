package com.naharpurawala.internshiptracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="internship_applications",indexes={@Index(name="idx_application_owner",columnList="owner_id"),@Index(name="idx_application_stage",columnList="stage"),@Index(name="idx_application_updated",columnList="updated_at"),@Index(name="idx_application_follow_up",columnList="follow_up_needed,updated_at")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InternshipApplication {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=160) private String company;
    @Column(nullable=false,length=160) private String role;
    @Column(length=160) private String location;
    @Column(length=500) private String jobUrl;
    private LocalDate appliedDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) @Builder.Default private ApplicationStage stage=ApplicationStage.APPLIED;
    @Column(length=2000) private String notes;
    @Column(nullable=false) @Builder.Default private boolean followUpNeeded=false;
    @Column(nullable=false,updatable=false) @Builder.Default private LocalDateTime createdAt=LocalDateTime.now();
    @Column(nullable=false) @Builder.Default private LocalDateTime updatedAt=LocalDateTime.now();
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="owner_id",nullable=false) private User owner;
    @OneToMany(mappedBy="application",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("changedAt ASC") @Builder.Default private List<StageHistory> stageHistory=new ArrayList<>();
    public void touch(){ this.updatedAt=LocalDateTime.now(); }
    public void addHistory(StageHistory history){ stageHistory.add(history); history.setApplication(this); }
}
