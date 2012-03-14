package com.camerasudokusolver.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: SmartCat
 * Date: 21.01.12
 * Time: 16:56
 * To change this template use File | Settings | File Templates.
 */
public class GrayscaleImage {
    int[][] finalImage;
    long[][] integralForm;

    public GrayscaleImage(int[] intensity, int width, int height) {
        finalImage = new int[width][height];
        integralForm = new long[width][height];

        processByteArray(intensity, width, height);
    }

    private void processByteArray(int[] intensity, int width, int height) {
        for (int j = 0, yp = 0; j < height; j++) {
            for (int i = 0; i < width; i++, yp++) {
                finalImage[i][j] = intensity[yp];
            }
        }
        postProcessImage(width, height);
    }

    private void postProcessImage(int width, int height) {
        fillIntegralMatrix(width, height);
    }

    private void fillIntegralMatrix(int width, int height) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                /*
				a b
				c y

				i = c+b+y-a
				*/
                long a, b, c;
                a = (j < 1 || i < 1) ? 0 : integralForm[i - 1][j - 1];
                c = (i < 1) ? 0 : integralForm[i - 1][j];
                b = (j < 1) ? 0 : integralForm[i][j - 1];

                integralForm[i][j] = c + b + finalImage[i][j] - a;
            }
        }
    }
    
    public final long getIntegralFormPixel(int x, int y) {
        return integralForm[x][y];
    }
    
    public final int getFinalImagePixel(int x, int y) {
        return finalImage[x][y];
    }
}
