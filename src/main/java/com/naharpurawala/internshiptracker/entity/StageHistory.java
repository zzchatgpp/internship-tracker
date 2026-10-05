package com.naharpurawala.internshiptracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="stage_history", indexes={@Index(name="idx_history_application",columnList="application_id"),@Index(name="idx_history_changed_at",columnList="changed_at")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StageHistory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(name="from_stage",length=30) private ApplicationStage fromStage;
    @Enumerated(EnumType.STRING) @Column(name="to_stage",nullable=false,length=30) private ApplicationStage toStage;
    @Column(name="changed_at",nullable=false,updatable=false) @Builder.Default private LocalDateTime changedAt=LocalDateTime.now();
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="application_id",nullable=false) private InternshipApplication application;
}
