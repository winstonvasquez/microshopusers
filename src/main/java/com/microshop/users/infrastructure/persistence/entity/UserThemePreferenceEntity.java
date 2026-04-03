package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_theme_preferences",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_theme", columnNames = {"user_id", "company_id", "module"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserThemePreferenceEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "module", nullable = false, length = 20)
    private String module;

    @Column(name = "theme_key", nullable = false, length = 50)
    private String themeKey;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
