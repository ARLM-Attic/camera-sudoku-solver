package com.camerasudokusolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.View;
import com.camerasudokusolver.Utils.*;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: dmitry.bushtets
 * Date: 1/20/12
 */
public class YUVImageAnalyzer extends View {
    static public boolean readyForNextFrame;

    private GrayscaleImage grayscaleImage;
    private boolean[][] isBlackPixel;
    private boolean[][] isBlackChecked; //TODO: for debug
    final private int canvasWidth = Constants.getInstance().screenWidth;
    final private int canvasHeight = Constants.getInstance().screenHeight;
    final private int rhoCenter = (int) ((canvasHeight + canvasWidth) * Math.sqrt(2) / 2);

    private NormLine normLine;

    private NormLine mLeftLine, mRightLine, mTopLine, mBottomLine; //TODO: for debug
    NormLine horizontalLines[] = new NormLine[10]; //TODO: for debug
    NormLine verticalLines[] = new NormLine[10]; //TODO: for debug

    private Point pRT, pLT, pRB, pLB;
    private Point gridPoints[][] = new Point[10][10];
    private int sudokuNums[][] = new int[9][9];

    private DebugInfo debugInfo;

    public YUVImageAnalyzer(final Context context) {
        super(context);
        debugInfo = new DebugInfo();
        normLine = new NormLine();

        mLeftLine = new NormLine();
        mRightLine = new NormLine();
        mTopLine = new NormLine();
        mBottomLine = new NormLine();

        readyForNextFrame = true;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        if (grayscaleImage == null) {
            readyForNextFrame = true;
            return;
        }

        Paint paintYellow = new Paint();
        paintYellow.setStyle(Paint.Style.FILL);
        paintYellow.setColor(Color.YELLOW);

        Paint paintGreen = new Paint();
        paintGreen.setStyle(Paint.Style.FILL);
        paintGreen.setColor(Color.GREEN);

        Paint paintMagenta = new Paint();
        paintMagenta.setStyle(Paint.Style.FILL);
        paintMagenta.setColor(Color.MAGENTA);
        //paintMagenta.setTextSize(18);

        Paint paintOrange = new Paint();
        paintOrange.setStyle(Paint.Style.FILL);
        paintOrange.setColor(Color.rgb(255, 210, 80));

        ConvertToBlackWhite();

        Date date = new Date();
        long startTime = date.getTime();

        date = new Date();
        debugInfo.drawOnCanvasTime = date.getTime() - startTime;

        if (DetectAngle()) {
            if (DetectWhiteBorder()) {
                DoOCR();

                /*
                for (int i = 0; i < canvasWidth; i++) {
                    for (int j = 0; j < canvasHeight; j++) {
                        //if (isBlackPixel[i][j]) {
                        if (isBlackChecked[i][j]) {
                            canvas.drawPoint(i, j, paintMagenta);
                        }
                    }
                }
                //*/
                /*
                canvas.drawLine(pLB.x, pLB.y, pLT.x, pLT.y, paintMagenta);
                canvas.drawLine(pLT.x, pLT.y, pRT.x, pRT.y, paintMagenta);
                canvas.drawLine(pRT.x, pRT.y, pRB.x, pRB.y, paintMagenta);
                canvas.drawLine(pRB.x, pRB.y, pLB.x, pLB.y, paintMagenta);
                //*/
                /*
                canvas.drawCircle(pLB.x, pLB.y, 3, paintOrange);
                canvas.drawCircle(pLT.x, pLT.y, 3, paintGreen);
                canvas.drawCircle(pRB.x, pRB.y, 3, paintMagenta);
                canvas.drawCircle(pRT.x, pRT.y, 3, paintYellow);
                //*/

                //*
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        canvas.drawCircle(gridPoints[i][j].x, gridPoints[i][j].y, 3, paintOrange);
                    }
                }
                //*/

                //*/
                for (int j = 0; j < 9; j++) {
                    for (int i = 0; i < 9; i++) {
                        if (sudokuNums[j][i] == 0) {
                            canvas.drawLine(gridPoints[i][j + 1].x, gridPoints[i][j + 1].y, gridPoints[i + 1][j].x, gridPoints[i + 1][j].y, paintGreen);
                            canvas.drawLine(gridPoints[i + 1][j + 1].x, gridPoints[i + 1][j + 1].y, gridPoints[i][j].x, gridPoints[i][j].y, paintGreen);
                        }
                    }
                }
                //*/
            }
        }

        /*
		canvas.drawText("Parse YUV: " + debugInfo.parseYUVImageTime + " ms", 20, 20, paintMagenta);
        canvas.drawText("Black&White: " + debugInfo.convertToBlackWhiteTime + " ms", 20, 40, paintMagenta);
        canvas.drawText("LineVoting: " + debugInfo.detectAngleTime + " ms. Angle: " + normLine.theta, 20, 60, paintMagenta);
        canvas.drawText("Draw: " + debugInfo.drawOnCanvasTime + " ms", 20, 80, paintMagenta);
        //*/

        super.onDraw(canvas);
        readyForNextFrame = true;

    } // end onDraw method

    private boolean DetectAngle() {
        Date date = new Date();
        long startTime = date.getTime();

        Accumulator accumulator = new Accumulator(canvasWidth, canvasHeight);

        //TODO: +- angle selection
        for (int y = canvasHeight / 4; y < canvasHeight * 3 / 4; y++) {
            for (int x = canvasWidth / 3; x < canvasWidth * 2 / 3; x += 3) {
                if (isBlackPixel[x][y]) {
                    accumulator.Add(x, y, 60, 120);
                }
            }
        }

        normLine.rho = 0;
        normLine.theta = 0;
        long maxVote = 0;
        for (int rho = 0; rho < accumulator.rhoSize; rho++) {
            for (int angle = 60; angle <= 120; angle++) {
                if (accumulator.votes[rho][angle] > maxVote) {
                    maxVote = accumulator.votes[rho][angle];
                    normLine.rho = rho;
                    normLine.theta = angle;
                }
            }
        }

        date = new Date();
        debugInfo.detectAngleTime = date.getTime() - startTime;

        return maxVote != 0;
    }

    private boolean DetectWhiteBorder() {
        int imageAngle = normLine.theta;
        int[] whiteLengthVertical;
        int[] whiteLengthHorizontal;

        NormLine topLine, bottomLine, leftLine, rightLine;
        topLine = new NormLine();
        bottomLine = new NormLine();
        leftLine = new NormLine();
        rightLine = new NormLine();
        topLine.theta = bottomLine.theta = 180 - imageAngle;
        leftLine.theta = rightLine.theta = imageAngle;

        //Horizontal line
        final double sinAngleHorizontal = Math.sin(Math.toRadians(topLine.theta));
        final double cosAngleHorizontal = Math.cos(Math.toRadians(topLine.theta));

        whiteLengthHorizontal = horizontalLineLengths(sinAngleHorizontal, cosAngleHorizontal);

        //2 peeks detection
        int max = 0;
        for (int y = 1 + canvasHeight / 2; y > 0; y--) {
            if (max < whiteLengthHorizontal[y]) {
                max = whiteLengthHorizontal[y];
                topLine.rho = y + rhoCenter;
            }
        }
        max = 0;
        for (int y = canvasHeight / 2; y < canvasHeight - 1; y++) {
            if (max < whiteLengthHorizontal[y]) {
                max = whiteLengthHorizontal[y];
                bottomLine.rho = y + rhoCenter;
            }
        }

        //vertical lines
        final double sinAngleVertical = Math.sin(Math.toRadians(leftLine.theta));
        final double cosAngleVertical = Math.cos(Math.toRadians(leftLine.theta));

        whiteLengthVertical = verticalLineLengths(bottomLine.rho, topLine.rho, sinAngleHorizontal, cosAngleHorizontal, sinAngleVertical, cosAngleVertical);

        //2 peeks detection
        max = 0;
        for (int x = 1 + canvasWidth / 2; x > 0; x--) {
            if (max < whiteLengthVertical[x]) {
                max = whiteLengthVertical[x];
                leftLine.rho = x + rhoCenter;
            }
        }
        max = 0;
        for (int x = canvasWidth / 2; x < canvasWidth - 1; x++) {
            if (max < whiteLengthVertical[x]) {
                max = whiteLengthVertical[x];
                rightLine.rho = x + rhoCenter;
            }
        }

        // (R)ight, (L)eft, (T)op, (B)ottom
        //Point pRT, pLT, pRB, pLB;
        pLT = InterceptionPoint(leftLine, topLine);
        pRT = InterceptionPoint(rightLine, topLine);
        pRB = InterceptionPoint(rightLine, bottomLine);
        pLB = InterceptionPoint(leftLine, bottomLine);

        mLeftLine = leftLine;
        mRightLine = rightLine;
        mTopLine = topLine;
        mBottomLine = bottomLine;

        //Detect all lines
        return DetectSudokuLines(leftLine, rightLine, bottomLine, topLine);
    }

    private boolean DetectSudokuLines(NormLine leftLine, NormLine rightLine,
                                      NormLine bottomLine, NormLine topLine) {
        //TODO: check, if we detect square

        NormLine tempLine = new NormLine();

        //VERTICAL
        //left
        int offsetBottom = pLB.y - (pLB.y - pLT.y) / 9;
        int offsetTop = pLT.y + (pLB.y - pLT.y) / 9;
        verticalLines[0] = HoughTransform(leftLine, (rightLine.rho - leftLine.rho) / 9, offsetTop, offsetBottom, 4, true);
        //right
        offsetBottom = pRB.y - (pRB.y - pRT.y) / 9;
        offsetTop = pRT.y + (pRB.y - pRT.y) / 9;
        verticalLines[9] = HoughTransform(rightLine, (leftLine.rho - rightLine.rho) / 9, offsetTop, offsetBottom, 4, true);
        //third
        tempLine.theta = verticalLines[0].theta + (verticalLines[9].theta - verticalLines[0].theta) / 3;
        tempLine.rho = verticalLines[0].rho
                + (verticalLines[9].rho - verticalLines[0].rho) / 3
                - (verticalLines[9].rho - verticalLines[0].rho) / 26;
        offsetBottom = pLB.y + (pRB.y - pLB.y) / 3 - (pLB.y - pLT.y) / 7;
        offsetTop = pLT.y + (pRT.y - pLT.y) / 3 + (pLB.y - pLT.y) / 7;
        verticalLines[3] = HoughTransform(tempLine, (verticalLines[9].rho - verticalLines[0].rho) / 13, offsetTop, offsetBottom, 2, true);
        //sixth
        tempLine.theta = verticalLines[0].theta + (verticalLines[9].theta - verticalLines[0].theta) * 2 / 3;
        tempLine.rho = verticalLines[0].rho
                + (verticalLines[9].rho - verticalLines[0].rho) * 2 / 3
                - (verticalLines[9].rho - verticalLines[0].rho) / 26;
        offsetBottom = pLB.y + (pRB.y - pLB.y) * 2 / 3 - (pLB.y - pLT.y) / 7;
        offsetTop = pLT.y + (pRT.y - pLT.y) * 2 / 3 + (pLB.y - pLT.y) / 7;
        verticalLines[6] = HoughTransform(tempLine, (verticalLines[9].rho - verticalLines[0].rho) / 13, offsetTop, offsetBottom, 2, true);

        //HORIZONTAL
        //top
        int offsetLeft = pLT.x + (pRT.x - pLT.x) / 9;
        int offsetRight = pRT.x - (pRT.x - pLT.x) / 9;
        horizontalLines[0] = HoughTransform(topLine, (bottomLine.rho - topLine.rho) / 9, offsetLeft, offsetRight, 4, false);
        //bottom
        offsetLeft = pLB.x + (pRB.x - pLB.x) / 9;
        offsetRight = pRB.x - (pRB.x - pLB.x) / 9;
        horizontalLines[9] = HoughTransform(bottomLine, (topLine.rho - bottomLine.rho) / 9, offsetLeft, offsetRight, 4, false);
        //third
        tempLine.theta = horizontalLines[0].theta + (horizontalLines[9].theta - horizontalLines[0].theta) / 3;
        tempLine.rho = horizontalLines[0].rho
                + (horizontalLines[9].rho - horizontalLines[0].rho) / 3
                - (horizontalLines[9].rho - horizontalLines[0].rho) / 25;
        offsetLeft = pLB.x + (pLT.x - pLB.x) / 3 + (pRB.x - pLB.x) / 7;
        offsetRight = pRB.x + (pRT.x - pRB.x) / 3 - (pRB.x - pLB.x) / 7;
        horizontalLines[3] = HoughTransform(tempLine, (horizontalLines[9].rho - horizontalLines[0].rho) / 12, offsetLeft, offsetRight, 2, false);
        //sixth
        tempLine.theta = horizontalLines[0].theta + (horizontalLines[9].theta - horizontalLines[0].theta) * 2 / 3;
        tempLine.rho = horizontalLines[0].rho
                + (horizontalLines[9].rho - horizontalLines[0].rho) * 2 / 3
                - (horizontalLines[9].rho - horizontalLines[0].rho) / 25;
        offsetLeft = pLB.x + (pLT.x - pLB.x) * 2 / 3 + (pRB.x - pLB.x) / 7;
        offsetRight = pRB.x + (pRT.x - pRB.x) * 2 / 3 - (pRB.x - pLB.x) / 7;
        horizontalLines[6] = HoughTransform(tempLine, (horizontalLines[9].rho - horizontalLines[0].rho) / 12, offsetLeft, offsetRight, 2, false);

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if ((x % 3 == 0) && (y % 3 == 0)) {
                    gridPoints[x][y] = InterceptionPoint(verticalLines[y], horizontalLines[x]);
                } else {
                    gridPoints[x][y] = new Point();
                }
            }
        }

        InterpolateSudokuGridPoints();

        //TODO: check boundaries
        return true;
    }

    private boolean DoOCR() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sudokuNums[i][j] = DoOCRCell(i, j);
            }
        }

        return true;
    }

    private int DoOCRCell(int xCell, int yCell) {
        Point lt = gridPoints[yCell + 1][xCell];
        Point rt = gridPoints[yCell + 1][xCell + 1];
        Point lb = gridPoints[yCell][xCell];
        Point rb = gridPoints[yCell][xCell + 1];

        int cellWidth = rt.x - lt.x - 3;
        int cellHeight = lt.y - lb.y - 3;

        if (cellWidth < 5 || cellHeight < 9)
            return -1;

        // detect if cell is empty. How: sum the black pixels from the center of the cell. Sum must be greater than the threshold.
        int sumBlack = 0;
        for (int X = lt.x + 2 + cellWidth / 3; X < rt.x - 1 - cellWidth / 3; X++) {
            int Y = lb.y + ((X - lt.x) * (rt.y - lt.y)) / (rt.x - lt.x);
            for (int y = Y + 2 + cellHeight / 3; y < Y + (lt.y - lb.y) - 1 - cellHeight / 4; y++) {
                int x = X - (lt.x - lb.x) - ((y - Y) * (lb.x - lt.x)) / (lt.y - lb.y);
                //isBlackChecked[x][y] = true;
                if (isBlackPixel[x][y])
                    sumBlack++;
            }
        }
        if (sumBlack < (cellWidth * cellHeight) / 31) {    // threshold between empty cell and cell with a digit inside. Obtained empirically.
            return 0;
        }


        return 1;
    }

    private void InterpolateSudokuGridPoints() {
        for (int y = 0; y < 10; y += 3) {
            //X
            gridPoints[y][1].x = gridPoints[y][0].x + (gridPoints[y][3].x - gridPoints[y][0].x) / 3;
            gridPoints[y][2].x = gridPoints[y][3].x - (gridPoints[y][3].x - gridPoints[y][0].x) / 3;
            gridPoints[y][4].x = gridPoints[y][3].x + (gridPoints[y][6].x - gridPoints[y][3].x) / 3;
            gridPoints[y][5].x = gridPoints[y][6].x - (gridPoints[y][6].x - gridPoints[y][3].x) / 3;
            gridPoints[y][7].x = gridPoints[y][6].x + (gridPoints[y][9].x - gridPoints[y][6].x) / 3;
            gridPoints[y][8].x = gridPoints[y][9].x - (gridPoints[y][9].x - gridPoints[y][6].x) / 3;
            //Y
            gridPoints[y][1].y = gridPoints[y][0].y + (gridPoints[y][3].y - gridPoints[y][0].y) / 3;
            gridPoints[y][2].y = gridPoints[y][3].y - (gridPoints[y][3].y - gridPoints[y][0].y) / 3;
            gridPoints[y][4].y = gridPoints[y][3].y + (gridPoints[y][6].y - gridPoints[y][3].y) / 3;
            gridPoints[y][5].y = gridPoints[y][6].y - (gridPoints[y][6].y - gridPoints[y][3].y) / 3;
            gridPoints[y][7].y = gridPoints[y][6].y + (gridPoints[y][9].y - gridPoints[y][6].y) / 3;
            gridPoints[y][8].y = gridPoints[y][9].y - (gridPoints[y][9].y - gridPoints[y][6].y) / 3;
        }

        for (int x = 0; x < 10; x += 3) {
            //X
            gridPoints[1][x].x = gridPoints[0][x].x + (gridPoints[3][x].x - gridPoints[0][x].x) / 3;
            gridPoints[2][x].x = gridPoints[3][x].x - (gridPoints[3][x].x - gridPoints[0][x].x) / 3;
            gridPoints[4][x].x = gridPoints[3][x].x + (gridPoints[6][x].x - gridPoints[3][x].x) / 3;
            gridPoints[5][x].x = gridPoints[6][x].x - (gridPoints[6][x].x - gridPoints[3][x].x) / 3;
            gridPoints[7][x].x = gridPoints[6][x].x + (gridPoints[9][x].x - gridPoints[6][x].x) / 3;
            gridPoints[8][x].x = gridPoints[9][x].x - (gridPoints[9][x].x - gridPoints[6][x].x) / 3;
            //Y
            gridPoints[1][x].y = gridPoints[0][x].y + (gridPoints[3][x].y - gridPoints[0][x].y) / 3;
            gridPoints[2][x].y = gridPoints[3][x].y - (gridPoints[3][x].y - gridPoints[0][x].y) / 3;
            gridPoints[4][x].y = gridPoints[3][x].y + (gridPoints[6][x].y - gridPoints[3][x].y) / 3;
            gridPoints[5][x].y = gridPoints[6][x].y - (gridPoints[6][x].y - gridPoints[3][x].y) / 3;
            gridPoints[7][x].y = gridPoints[6][x].y + (gridPoints[9][x].y - gridPoints[6][x].y) / 3;
            gridPoints[8][x].y = gridPoints[9][x].y - (gridPoints[9][x].y - gridPoints[6][x].y) / 3;
        }

        for (int a = 1; a < 9; a++) {
            if (a != 3 && a != 6) {
                for (int b = 1; b < 9; b++) {
                    if (b == 1) {
                        gridPoints[a][b].x = gridPoints[a][0].x + (gridPoints[a][3].x - gridPoints[a][0].x) / 3;
                        gridPoints[b][a].y = gridPoints[0][a].y + (gridPoints[3][a].y - gridPoints[0][a].y) / 3;
                    } else if (b == 2) {
                        gridPoints[a][b].x = gridPoints[a][3].x - (gridPoints[a][3].x - gridPoints[a][0].x) / 3;
                        gridPoints[b][a].y = gridPoints[3][a].y - (gridPoints[3][a].y - gridPoints[0][a].y) / 3;
                    } else if (b == 4) {
                        gridPoints[a][b].x = gridPoints[a][3].x + (gridPoints[a][6].x - gridPoints[a][3].x) / 3;
                        gridPoints[b][a].y = gridPoints[3][a].y + (gridPoints[6][a].y - gridPoints[3][a].y) / 3;
                    } else if (b == 5) {
                        gridPoints[a][b].x = gridPoints[a][6].x - (gridPoints[a][6].x - gridPoints[a][3].x) / 3;
                        gridPoints[b][a].y = gridPoints[6][a].y - (gridPoints[6][a].y - gridPoints[3][a].y) / 3;
                    } else if (b == 7) {
                        gridPoints[a][b].x = gridPoints[a][6].x + (gridPoints[a][9].x - gridPoints[a][6].x) / 3;
                        gridPoints[b][a].y = gridPoints[6][a].y + (gridPoints[9][a].y - gridPoints[6][a].y) / 3;
                    } else if (b == 8) {
                        gridPoints[a][b].x = gridPoints[a][9].x - (gridPoints[a][9].x - gridPoints[a][6].x) / 3;
                        gridPoints[b][a].y = gridPoints[9][a].y - (gridPoints[9][a].y - gridPoints[6][a].y) / 3;
                    }
                }
            }
        }
    }

    private Point InterceptionPoint(final NormLine firstLine, final NormLine secondLine) {
        double c1, c2, t1, t2;
        c1 = (secondLine.rho - rhoCenter) / Math.sin(Math.toRadians(secondLine.theta));
        c2 = (firstLine.rho - rhoCenter) / Math.sin(Math.toRadians(firstLine.theta));
        t1 = Math.cos(Math.toRadians(secondLine.theta)) / Math.sin(Math.toRadians(secondLine.theta));
        t2 = Math.cos(Math.toRadians(firstLine.theta)) / Math.sin(Math.toRadians(firstLine.theta));

        int y = (int) ((c2 * t1 + c1) / (1 - t1 * t2));
        int x = (int) (y * t2 + c2);

        return new Point(x, y);
    }

    private int[] verticalLineLengths(final int bottomRho, final int topRho,
                                      final double sinAngleHorizontal, final double cosAngleHorizontal,
                                      final double sinAngleVertical, final double cosAngleVertical) {
        final int RECT = 1;
        int[] whiteLength = new int[canvasWidth];

        for (int rho = 0; rho < canvasWidth * sinAngleHorizontal; rho += 3) {
            int whiteLen = 0;
            int maxWhiteLen = 0;

            int yDown = (int) ((rho * cosAngleHorizontal + topRho - rhoCenter) / sinAngleHorizontal);
            int yUp = yDown + bottomRho - topRho;

            for (int y = yDown; y < yUp; y += 3) {
                int x = (int) ((cosAngleVertical * y + rho) / sinAngleVertical);
                if (x >= RECT && x < canvasWidth - RECT && y >= RECT && y < canvasHeight - RECT) {
                    int sum = sumBlackPixel(RECT, x, y);
                    if ((RECT * 2 + 1) > sum) {
                        whiteLen++;
                    } else {
                        if (maxWhiteLen < whiteLen) {
                            maxWhiteLen = whiteLen;
                        }
                        whiteLen = 0;
                    }
                }
                if (maxWhiteLen < whiteLen) {
                    maxWhiteLen = whiteLen;
                }
            }
            whiteLength[rho] = maxWhiteLen;
        }
        return whiteLength;
    }

    private int[] horizontalLineLengths(final double sinAngle, final double cosAngle) {
        final int RECT = 1;
        int[] whiteLength = new int[canvasHeight];

        for (int rho = 0; rho < canvasHeight * sinAngle; rho += 3) {
            int whiteLen = 0;
            int maxWhiteLen = 0;

            for (int x = RECT; x < canvasWidth - RECT; x += 3) {
                int y = (int) ((cosAngle * x + rho) / sinAngle);
                if (y >= RECT && y < canvasHeight - RECT) {
                    int sum = sumBlackPixel(RECT, x, y);
                    if ((RECT * 2 + 1) > sum) {
                        whiteLen++;
                    } else {
                        if (maxWhiteLen < whiteLen) {
                            maxWhiteLen = whiteLen;
                        }
                        whiteLen = 0;
                    }
                }
                if (maxWhiteLen < whiteLen) {
                    maxWhiteLen = whiteLen;
                }
            }
            whiteLength[rho] = maxWhiteLen;
        }
        return whiteLength;
    }

    private NormLine HoughTransform(NormLine inputLine, int offset,
                                    int startLimit, int endLimit,
                                    int thetaOffset, boolean isVertical) {
        NormLine resultLine = new NormLine();
        Accumulator accumulator = new Accumulator(canvasWidth, canvasHeight);

        int start, end;
        int x, y; //speedup ??

        if (offset > 0) {
            start = inputLine.rho;
            end = start + offset;
        } else {
            end = inputLine.rho;
            start = end + offset;
        }

        final double sinAngle = Math.sin(Math.toRadians(inputLine.theta));
        final double cosAngle = Math.cos(Math.toRadians(inputLine.theta));
        int lineAngle = 180 - inputLine.theta;

        if (isVertical) {
            for (int rho = start; rho < end; rho++) {
                for (y = startLimit; y < endLimit; y++) { //TODO: += 2
                    x = (int) ((y * cosAngle + rho - rhoCenter) / sinAngle);
                    if (x > 0 && x < canvasWidth && y > 0 && y < canvasHeight) {
                        //isBlackChecked[x][y] = true;
                        if (isBlackPixel[x][y]) {

                            accumulator.Add(y, x, lineAngle - thetaOffset, lineAngle + thetaOffset);
                        }
                    }
                }
            }
        } else {
            for (int rho = start; rho < end; rho++) {
                for (x = startLimit; x < endLimit; x++) { //TODO: += 2
                    y = (int) ((x * cosAngle + rho - rhoCenter) / sinAngle);
                    if (x > 0 && x < canvasWidth && y > 0 && y < canvasHeight) {
                        //isBlackChecked[x][y] = true;
                        if (isBlackPixel[x][y]) {

                            accumulator.Add(x, y, lineAngle - thetaOffset, lineAngle + thetaOffset);
                        }
                    }
                }
            }
        }

        long vote = -1;
        for (int rho = accumulator.rhoLow; rho < accumulator.rhoHigh; rho++) {
            for (int theta = lineAngle - thetaOffset; theta <= lineAngle + thetaOffset; theta++) {
                if (accumulator.votes[rho][theta] > vote) {
                    vote = accumulator.votes[rho][theta];
                    resultLine.rho = rho;
                    resultLine.theta = theta;
                }
            }
        }
        resultLine.theta = 180 - resultLine.theta;
        return resultLine;
    }

    private int sumBlackPixel(final int RECT, final int x, final int y) {
        int sum = 0;
        for (int yy = y - RECT; yy <= y + RECT; yy++) {
            for (int xx = x - RECT; xx <= x + RECT; xx++) {
                if (isBlackPixel[xx][yy]) {
                    sum++;
                }
            }
        }
        return sum;
    }

    private void ConvertToBlackWhite() {
        Date date = new Date();
        long startTime = date.getTime();

        isBlackPixel = new boolean[canvasWidth][canvasHeight];
        isBlackChecked = new boolean[canvasWidth][canvasHeight];
        // Draw intensity histogram
        for (int i = 0; i < canvasWidth; i++) {
            for (int j = 0; j < canvasHeight; j++) {
                if (grayscaleImage.getFinalImagePixel(i, j)
                        < evalLocalThreshold(i, j) * Constants.getInstance().localThresholdFactor) {
                    isBlackPixel[i][j] = true;
                }
            }
        }

        date = new Date();
        debugInfo.convertToBlackWhiteTime = date.getTime() - startTime;
    }

    private long evalLocalThreshold(final int i, final int j) {
        final int gridSize = 11;

        /*
          A *** B
          C *** D
          i = D - C - B + A
          */

        long A, B, C, D;
        int x1 = i - (gridSize / 2) - 1;
        int x2 = i + (gridSize / 2);
        int y1 = j - (gridSize / 2) - 1;
        int y2 = j + (gridSize / 2);

        if (x2 >= canvasWidth) {
            x2 = canvasWidth - 1;
        }
        if (y2 >= canvasHeight) {
            y2 = canvasHeight - 1;
        }

        A = (x1 < 0 || y1 < 0) ? 0 : grayscaleImage.getIntegralFormPixel(x1, y1);
        C = (x1 < 0) ? 0 : grayscaleImage.getIntegralFormPixel(x1, y2);
        B = (y1 < 0) ? 0 : grayscaleImage.getIntegralFormPixel(x2, y1);
        D = grayscaleImage.getIntegralFormPixel(x2, y2);

        long threshold = D - C - B + A;
        threshold /= gridSize * gridSize;
        return threshold;
    }

    public void decodeYUV420SP(final byte[] dataYUV) {
        Date date = new Date();
        long startTime = date.getTime();

        readyForNextFrame = false;
        int[] rgbImage = new int[canvasWidth * canvasHeight];

        for (int yp = 0; yp < canvasWidth * canvasHeight; yp++) {
            int y = (0xff & ((int) dataYUV[yp])) - 16;
            if (y < 0) {
                y = 0;
            }
            rgbImage[yp] = y;
        }

        grayscaleImage = new GrayscaleImage(rgbImage, canvasWidth, canvasHeight);

        date = new Date();
        debugInfo.parseYUVImageTime = date.getTime() - startTime;
    }
}
