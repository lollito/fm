package com.lollito.fm.model;

import java.util.HashMap;
import java.util.Map;

public enum Mentality {
	DEFENSIVE(0),
	NORMAL(1),
	OFFENSIVE(2);

	private int value;
	private static Map<Integer, Mentality> map = new HashMap<>();
	
	private Mentality( int value ) {
		  this.value = value;
	}

	public int getvalue() {
		return value;
	}

	public void setvalue(int value) {
		this.value = value;
	}
	
	static {
        for (Mentality source : Mentality.values()) {
            map.put(source.value, source);
        }
    }

    public static Mentality valueOf(int source) {
        return (Mentality)map.get(source);
    }
}
