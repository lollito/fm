package com.lollito.fm.utils;
public class Level{
    public static final double base_exp = 100;
    public static final double exp_increase_per_level = 0.2;
    public static final int max_level = 100;
 
    public static int level (double exp) {
    	//level = exp_increase_per_level * Math.sqrt(exp)
    	return  (int) (exp_increase_per_level * Math.sqrt(exp));
 
        
    }
 
    public static double levelToExp (int level) {
    	//exp = (level / exp_increase_per_level) ^ 2
    	return  (int) Math.pow(level / exp_increase_per_level, 2);
    }
 
    public static double getLevelProgress(double exp) {
        int level = level(exp);
		double currentLevelXP = levelToExp(level);
        double nextLevelXP = levelToExp(level + 1);
        double neededXP = nextLevelXP - currentLevelXP;
        double earnedXP = nextLevelXP - exp;
        return 100 - (int) Math.ceil((earnedXP / neededXP) * 100);
    }
    
    public static void main(String []args){
        
        for (int i = 0; i < 10; i++) {
        	System.out.println("Tol "+ i + ", "+levelToExp(i)+" exp");
        	System.out.println(levelToExp(i) + " is level "+ level(levelToExp(i)));
		}
        
        
        
 
 
 
    }
}