package com.lollito.fm.model;

import java.util.HashMap;
import java.util.Map;

public enum PlayerRole {
	GOALKEEPER(0),
	CENTRALDEFENDER(1),
	WINGBACK(2),
	MIDFIELDER(3),
	WING(4),
	FORWARD(5);

	private int value;
	private static Map<Integer, PlayerRole> map = new HashMap<>();
	
	private PlayerRole( int value ) {
		  this.value = value;
	}

	public int getvalue() {
		return value;
	}

	public void setvalue(int value) {
		this.value = value;
	}
	
	static {
        for (PlayerRole source : PlayerRole.values()) {
            map.put(source.value, source);
        }
    }

    public static PlayerRole valueOf(int source) {
        return (PlayerRole)map.get(source);
    }
}
