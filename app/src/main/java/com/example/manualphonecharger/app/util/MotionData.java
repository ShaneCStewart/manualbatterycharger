package com.example.manualphonecharger.app.util;

public class MotionData {

    private int totalMotion;
    private int rateOfMotion;

    private MotionData(){
        totalMotion = 0;
        rateOfMotion = 0;
    }

    private static class MotionDataHolder{
        private static final MotionData INSTANCE = new MotionData();
    }

    public static MotionData getInstance(){
        return MotionDataHolder.INSTANCE;
    }
    public int getTotalMotion() {
        return totalMotion;
    }

    public int getRateOfMotion() {
        return rateOfMotion;
    }

    public void updateTotalMotion(int lastMovement) {
        this.totalMotion += lastMovement;
    }

    public void setRateOfMotion(int rateOfMotion) {
        this.rateOfMotion = rateOfMotion;
    }
}
