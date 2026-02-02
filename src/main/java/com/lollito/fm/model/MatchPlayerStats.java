package com.lollito.fm.model;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "match_player_stats")
public class MatchPlayerStats implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    @JsonIgnore
    private Match match;

    @ManyToOne
    private Player player;

    private Integer goals = 0;
    private Integer assists = 0;
    private Integer yellowCards = 0;
    private Integer redCards = 0;
    private Integer shots = 0;
    private Integer shotsOnTarget = 0;
    private Integer passes = 0;
    private Integer completedPasses = 0;
    private Integer tackles = 0;
    private Double rating = 6.0;
    private Boolean mvp = false;
    private String position;

    public MatchPlayerStats() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Integer getGoals() { return goals; }
    public void setGoals(Integer goals) { this.goals = goals; }

    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }

    public Integer getYellowCards() { return yellowCards; }
    public void setYellowCards(Integer yellowCards) { this.yellowCards = yellowCards; }

    public Integer getRedCards() { return redCards; }
    public void setRedCards(Integer redCards) { this.redCards = redCards; }

    public Integer getShots() { return shots; }
    public void setShots(Integer shots) { this.shots = shots; }

    public Integer getShotsOnTarget() { return shotsOnTarget; }
    public void setShotsOnTarget(Integer shotsOnTarget) { this.shotsOnTarget = shotsOnTarget; }

    public Integer getPasses() { return passes; }
    public void setPasses(Integer passes) { this.passes = passes; }

    public Integer getCompletedPasses() { return completedPasses; }
    public void setCompletedPasses(Integer completedPasses) { this.completedPasses = completedPasses; }

    public Integer getTackles() { return tackles; }
    public void setTackles(Integer tackles) { this.tackles = tackles; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Boolean getMvp() { return mvp; }
    public void setMvp(Boolean mvp) { this.mvp = mvp; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 121).append(id).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MatchPlayerStats)) return false;
        if (this == obj) return true;
        MatchPlayerStats other = (MatchPlayerStats) obj;
        return new EqualsBuilder().append(id, other.id).isEquals();
    }
}
