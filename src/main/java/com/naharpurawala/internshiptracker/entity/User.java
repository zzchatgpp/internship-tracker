package com.naharpurawala.internshiptracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(name = "uk_users_email", columnNames = "email")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, length = 190) private String email;
    @Column(nullable = false, length = 100) private String password;
    @Column(nullable = false, updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true) @OrderBy("updatedAt DESC") @Builder.Default
    private List<InternshipApplication> applications = new ArrayList<>();
    public void addApplication(InternshipApplication application){ applications.add(application); application.setOwner(this); }
    public void removeApplication(InternshipApplication application){ applications.remove(application); application.setOwner(null); }
}
