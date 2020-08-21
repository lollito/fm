package com.lollito.fm.utils;
public class Level{
    public static final double base_exp = 100;
    public static final double exp_increase_per_level = 0.5;
    public static final int max_level = 100;
 
    public static int level (double exp) {
 
        int i = 1;//Starting from lvl 0, if you want to start from lvl 1, just initialize to 1
        double test_exp = base_exp;
 
            while ( i < max_level ) { //Test for max_level, replace with while(true) for no max
                if (test_exp > exp) { //Check if enough exp for lvl i
                    return i; //if so, return the level
                }
 
                //The test failed, so the exp is higher than the necessary for level i.
                test_exp = test_exp + test_exp * exp_increase_per_level; //Increase it by 8% to test the next level
                i++; //Since the test failed, the level is higher
            }
 
        return max_level;//Return when it reach max_level
    }
 
    public static double levelToExp (int level) {
        if(level == 1)
            return base_exp; //return the base_exp when reach the first achievable level
        else {
            double prev = levelToExp(level-1); //Computes the previous level exp to calculate this one
            return prev + prev * exp_increase_per_level;
        }
    }
 
    public static double levelToTotalExp (int level) {
        if(level == 1)
            return base_exp; //return the base_exp when reach the first achievable  level
        else {
            return levelToTotalExp(level-1) + levelToExp(level-1) * (1 + exp_increase_per_level);
        }
    }
 
    public static void main(String []args){
    	System.out.println("0 is level "+level(0));
        System.out.println("100 is level "+level(100));
        System.out.println("900 is level "+level(900));
        System.out.println("1000 is level "+level(1000));
        System.out.println("2000 is level "+level(2000));
        System.out.println("3000 is level "+level(3000));
        System.out.println("6000 is level "+level(6000));
        System.out.println("9'000 is level "+level(9000));
        System.out.println("90'000 is level "+level(90000));
        System.out.println("188'594 is level "+level(188594));
        System.out.println("188'595 is level "+level(188595));
        System.out.println("900'000 is level "+level(900000));
 
        System.out.println("Tol 1, "+levelToExp(1)+" exp");
        System.out.println("Tol 2, "+levelToExp(2)+" exp");
        System.out.println("Tol 3, "+levelToExp(3)+" exp");
        System.out.println("Tol 4, "+levelToExp(4)+" exp");
        System.out.println("Tol 5, "+levelToExp(5)+" exp");
        System.out.println("Tol 6, "+levelToExp(6)+" exp");
        System.out.println("Tol 7, "+levelToExp(7)+" exp");
        System.out.println("Tol 8, "+levelToExp(8)+" exp");
        System.out.println("Tol 9, "+levelToExp(9)+" exp");
        System.out.println("Tol 10, "+levelToExp(10)+" exp");
        System.out.println("Tol 30, "+levelToExp(30)+" exp");
        System.out.println("Tol 99, "+levelToExp(99)+" exp");
        System.out.println("Tol 100, "+levelToExp(100)+" exp");
        System.out.println("Tol 200, "+levelToExp(200)+" exp");
 
        System.out.println("Totalto level 2, "+levelToTotalExp(2)+" accumulated");
        System.out.println("Totalto level 4, "+levelToTotalExp(4)+" accumulated");
        System.out.println("Totalto level 10, "+levelToTotalExp(10)+" accumulated");
 
 
    }
}