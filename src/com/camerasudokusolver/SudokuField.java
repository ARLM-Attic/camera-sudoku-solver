package com.camerasudokusolver;

import com.camerasudokusolver.Utils.CandidatesMap;

/**
 * Created by IntelliJ IDEA.
 * User: SmartCat
 * Date: 14.01.12
 * Time: 15:00
 * To change this template use File | Settings | File Templates.
 */
public class SudokuField {
    private int fieldSize;
    private int solvedCells;
    private int field[][];
    private CandidatesMap candidatesMap[][];

    public SudokuField (int size) {
        fieldSize = size;
        solvedCells = 0;
        field = new int[size][size];
        candidatesMap = new CandidatesMap[size][size];
        ClearField();
    }

    private void ClearField()
    {
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                field[i][j] = 0;
                candidatesMap[i][j].setFieldSize(fieldSize);
            }
        }
    }

    public boolean addDigitToField (int x, int y, int num) {
        field[x][y] = num;
        //candidatesMap[x][y].clear();
        solvedCells++;
        return processCandidates(x, y, num);
    }

    private boolean processCandidates (int initialX, int initialY, int num) {
        boolean result = processHorizontalCandidates(initialY, num);
        result &= processVerticalCandidates(initialX, num);
        result &= processSquareCandidates(initialX, initialY, num);
        return result;
    }

    private boolean processHorizontalCandidates (int initialY, int num) {
        for (int i = 0; i < fieldSize; i++) {
            if (field[i][initialY] == 0) {
                candidatesMap[i][initialY].removeCandidate(num);
                if (candidatesMap[i][initialY].isEmpty()) return false;
            }
        }
        return true;
    }

    private boolean processVerticalCandidates (int initialX, int num) {
        for (int i = 0; i < fieldSize; i++) {
            if (field[initialX][i] == 0) {
                candidatesMap[initialX][i].removeCandidate(num);
                if (candidatesMap[initialX][i].isEmpty()) return false;
            }
        }
        return true;
    }

    private boolean processSquareCandidates (int initialX, int initialY, int num) {
        int squareSide = (int) Math.sqrt(fieldSize);
        int squareX = initialX / squareSide;
        int squareY = initialY / squareSide;
        for (int i = squareX*squareSide; i < (squareX+1)*squareSide; i++) {
            for (int j = squareY*squareSide; j < (squareY+1)*squareSide; j++) {
                if (field[i][j] == 0) {
                    candidatesMap[i][j].removeCandidate(num);
                    if (candidatesMap[i][j].isEmpty()) return false;
                }
            }
        }
        return true;
    }

    public boolean solveField() {

        return true;
    }


}
