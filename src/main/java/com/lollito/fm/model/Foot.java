package com.lollito.fm.model;

import java.util.HashMap;
import java.util.Map;

public enum Foot {
	RIGHT(0),
	LEFT(1);
	
	private int value;
	private static Map<Integer, Foot> map = new HashMap<>();
	
	private Foot( int value ) {
		  this.value = value;
	}

	public int getvalue() {
		return value;
	}

	public void setvalue(int value) {
		this.value = value;
	}
	
	static {
        for (Foot source : Foot.values()) {
            map.put(source.value, source);
        }
    }

    public static Foot valueOf(int source) {
        return (Foot)map.get(source);
    }
}
