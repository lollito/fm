package com.lollito.fm.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "manager_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ManagerProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;

    @Column(name = "current_xp")
    @Builder.Default
    private Long currentXp = 0L;

    @Column(name = "talent_points")
    @Builder.Default
    private Integer talentPoints = 0;

    @ElementCollection(targetClass = ManagerPerk.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "manager_unlocked_perks", joinColumns = @JoinColumn(name = "manager_profile_id"))
    @Column(name = "perk")
    @Builder.Default
    private Set<ManagerPerk> unlockedPerks = new HashSet<>();

}
