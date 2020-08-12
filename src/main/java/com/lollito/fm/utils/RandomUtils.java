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
	
	public static Double randomValue(double from, double to){
		return ThreadLocalRandom.current().nextDouble(from, to + 1D);
	}
	
	public static Boolean randomPercentage(double percent){
		randomValue(1, 100);
	    int number = randomValue(1, 100);
	    if (number <= percent){ // 60%
	        return true;
	    }
	    return false;
	}
}
