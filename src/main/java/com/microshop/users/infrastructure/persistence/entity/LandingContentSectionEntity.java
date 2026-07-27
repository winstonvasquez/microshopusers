package com.microshop.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "landing_content_section")
@Comment("Contenido editable de /portal/landing (Problema, Como funciona, Confianza, FAQ)")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LandingContentSectionEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_key", nullable = false, unique = true, length = 50)
    private String sectionKey;

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;
}
