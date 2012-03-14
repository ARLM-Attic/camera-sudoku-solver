package com.camerasudokusolver.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: SmartCat
 * Date: 21.01.12
 */
public final class Constants {
    private static Constants instance;

    public int screenWidth = 480;
    public int screenHeight = 320;

    public float localThresholdFactor = 0.97f;  //can variates 0.8 ... 1

    private Constants() {
    }

    public static Constants getInstance() {
        if (instance == null) {
            instance = new Constants();
        }

        return instance;
    }
}
