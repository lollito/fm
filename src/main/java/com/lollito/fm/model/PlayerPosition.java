package com.lollito.fm.model;

import java.util.HashMap;
import java.util.Map;

public enum PlayerPosition {
	DEFENCE(0),
	MIDFIELD(1),
	OFFENCE(2);

	private int value;
	private static Map<Integer, PlayerPosition> map = new HashMap<>();
	
	private PlayerPosition( int value ) {
		  this.value = value;
	}

	public int getvalue() {
		return value;
	}

	public void setvalue(int value) {
		this.value = value;
	}
	
	static {
        for (PlayerPosition source : PlayerPosition.values()) {
            map.put(source.value, source);
        }
    }

    public static PlayerPosition valueOf(int source) {
        return (PlayerPosition)map.get(source);
    }
}
