package com.lollito.fm.model.rest;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lollito.fm.model.Mentality;

public class FormationRequest {

	private Long moduleId;
	private Mentality mentality;
	private List<Long> playersId;
	
	public Long getModuleId() {
		return moduleId;
	}
	
	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}
	
	

	public Mentality getMentality() {
		return mentality;
	}

	public void setMentality(Mentality mentality) {
		this.mentality = mentality;
	}

	public List<Long> getPlayersId() {
		return playersId;
	}
	
	public void setPlayersId(List<Long> playersId) {
		this.playersId = playersId;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("moduleId", moduleId)
				.append("playersId", playersId)
				.toString();
	}
}
