package com.lollito.fm.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "system_configuration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class SystemConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String configKey;
    private String configValue;
    private String configDescription;

    @Enumerated(EnumType.STRING)
    private ConfigCategory category;

    @Enumerated(EnumType.STRING)
    private ConfigType valueType; // STRING, INTEGER, BOOLEAN, DECIMAL

    private String defaultValue;
    private Boolean isEditable;
    private Boolean requiresRestart;

    private LocalDateTime lastModified;
    private String modifiedBy;

    private String validationRules; // JSON for validation
}
