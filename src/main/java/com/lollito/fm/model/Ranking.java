package com.lollito.fm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name="ranking")
public class Ranking implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@OneToMany(mappedBy = "ranking", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	private List<RankingLine> rankingLines = new ArrayList<>();
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<RankingLine> getRankingLines() {
		return rankingLines;
	}

	public void setRankingLines(List<RankingLine> rankingLines) {
		this.rankingLines = rankingLines;
	}

	public void addRankingLine(RankingLine rankingLine) {
		rankingLine.setRanking(this);
		this.rankingLines.add(rankingLine);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Ranking)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Ranking other = (Ranking) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
}
