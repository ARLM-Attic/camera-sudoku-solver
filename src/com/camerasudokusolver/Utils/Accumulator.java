package com.camerasudokusolver.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: SmartCat
 * Date: 21.01.12
 */
public class Accumulator {
    public long[][] votes;

    private final int maxAngle = 180;

    public static final int stripFactor = 3;

    public int rhoSize;
    private int rhoOffset; //??
    public int rhoLow;
    public int rhoHigh;

    public final double[] sinAngle = new double[180];
    public final double[] cosAngle = new double[180];

    public Accumulator(final int width, final int height) {
        rhoSize = (int) ((width + height) * Math.sqrt(2));
        rhoOffset = rhoSize / 2;
        rhoLow = rhoSize / stripFactor;
        rhoHigh = rhoSize - rhoLow;

        votes = new long[rhoSize][maxAngle];

        //generate sin, cos constants
        for (int i = 0; i < maxAngle; i++) {
            double radians = Math.PI * i / 180;
            sinAngle[i] = Math.sin(radians);
            cosAngle[i] = Math.cos(radians);
        }
    }

    public void Add(final int x, final int y, final int startAngle, final int endAngle) {
        if (endAngle >= maxAngle) { return; }
        for (int alpha = startAngle; alpha < endAngle; alpha++) {
            int rho = (int) (cosAngle[alpha] * x + sinAngle[alpha] * y) + rhoOffset;

            votes[rho][alpha]++;
        }
    }
}
