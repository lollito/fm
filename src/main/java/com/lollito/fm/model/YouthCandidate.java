package com.lollito.fm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "youth_candidate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class YouthCandidate implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    private String name;
    private String surname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate birth;

    @Enumerated(EnumType.ORDINAL)
    private PlayerRole role;

    @Enumerated(EnumType.ORDINAL)
    private Foot preferredFoot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_id")
    @ToString.Exclude
    private Country nationality;

    // Attributes (Current ability of the candidate)
    private Double stamina;
    private Double playmaking;
    private Double scoring;
    private Double winger;
    private Double goalkeeping;
    private Double passing;
    private Double defending;
    private Double setPieces;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "youth_academy_id")
    @JsonIgnore
    @ToString.Exclude
    private YouthAcademy youthAcademy;

    @Transient
    public Integer getAge() {
        return Period.between(birth, LocalDate.now()).getYears();
    }

    @Transient
    public Integer getAverage(){
        return ((this.stamina == null ? 0 : this.stamina.intValue()) +
                (this.playmaking == null ? 0 : this.playmaking.intValue()) +
                (this.scoring == null ? 0 : this.scoring.intValue()) +
                (this.winger == null ? 0 : this.winger.intValue()) +
                (this.goalkeeping == null ? 0 : this.goalkeeping.intValue()) +
                (this.passing == null ? 0 : this.passing.intValue()) +
                (this.defending == null ? 0 : this.defending.intValue()) +
                (this.setPieces == null ? 0 : this.setPieces.intValue())) / 8;
    }
}
