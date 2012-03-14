package com.camerasudokusolver.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: SmartCat
 * Date: 14.01.12
 * Time: 16:09
 * To change this template use File | Settings | File Templates.
 */
public class CandidatesMap {
    private int totalCandidates;
    boolean candidates[];
    
    public CandidatesMap() {
    }

    public void setFieldSize(int fieldSize) {
        totalCandidates = fieldSize;
        candidates = new boolean[fieldSize];

        for (int i = 0; i < fieldSize; i++) {
            insertCandidate(i);
        }
    }
    
    public int getTotalCandidates () {return totalCandidates;}

    private void insertCandidate (int num) {
        candidates[num] = true;
    }

    public boolean removeCandidate (int num) {
        if (candidates[num-1]) {
            totalCandidates--;
            candidates[num-1] = false;
            return true;
        }
        return false;
    }

    public boolean isEmpty() {return totalCandidates == 0;}
}
