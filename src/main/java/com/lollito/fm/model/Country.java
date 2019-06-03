package com.lollito.fm.model;

import java.util.HashMap;
import java.util.Map;

public enum Country {
	ITALY(0),
	ENGLAND(1),
	FRANCE(2),
	GERMANY(3),
	SPAIN(4),
	;

	private int value;
	private static Map<Integer, Country> map = new HashMap<>();
	
	private Country( int value ) {
		  this.value = value;
	}

	public int getvalue() {
		return value;
	}

	public void setvalue(int value) {
		this.value = value;
	}
	
	static {
        for (Country source : Country.values()) {
            map.put(source.value, source);
        }
    }

    public static Country valueOf(int source) {
        return (Country)map.get(source);
    }
}
