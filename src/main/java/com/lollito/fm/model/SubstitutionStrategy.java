package com.lollito.fm.model;

import java.util.HashMap;
import java.util.Map;

public enum SubstitutionStrategy {
	AGGRESSIVE(0),
	DEFENSIVE(1),
	BALANCED(2),
	AUTO(3);

	private int value;
	private static Map<Integer, SubstitutionStrategy> map = new HashMap<>();

	private SubstitutionStrategy( int value ) {
		  this.value = value;
	}

	public int getvalue() {
		return value;
	}

	public void setvalue(int value) {
		this.value = value;
	}

	static {
        for (SubstitutionStrategy source : SubstitutionStrategy.values()) {
            map.put(source.value, source);
        }
    }

    public static SubstitutionStrategy valueOf(int source) {
        return (SubstitutionStrategy)map.get(source);
    }
}
