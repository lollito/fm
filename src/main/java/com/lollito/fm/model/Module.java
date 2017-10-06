package com.lollito.fm.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Table(name = "module")
public class Module implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String name;
	
    private Integer cd;
    private Integer wb;
    private Integer mf;
    private Integer wng;
    private Integer fw;
    
    public Module(){
    	
    }
    
	public Module(String name, Integer cd, Integer wb, Integer mf, Integer wng, Integer fw) {
		this.name = name;
		this.cd = cd;
		this.wb = wb;
		this.mf = mf;
		this.wng = wng;
		this.fw = fw;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCd() {
		return cd;
	}

	public void setCd(Integer cd) {
		this.cd = cd;
	}

	public Integer getWb() {
		return wb;
	}

	public void setWb(Integer wb) {
		this.wb = wb;
	}

	public Integer getMf() {
		return mf;
	}

	public void setMf(Integer mf) {
		this.mf = mf;
	}

	public Integer getWng() {
		return wng;
	}

	public void setWng(Integer wng) {
		this.wng = wng;
	}

	public Integer getFw() {
		return fw;
	}

	public void setFw(Integer fw) {
		this.fw = fw;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Module)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Module other = (Module) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("id", id)
				.append("name", name)
				.toString();
	}
}
