package com.lollito.fm.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
	public static Integer randomValue(int from, int to){
		return ThreadLocalRandom.current().nextInt(from, to + 1);
	}
	
	public static <T> T randomValueFromList(List<T> list){
        return list.get(randomValue(0, list.size() - 1));
	}
}
