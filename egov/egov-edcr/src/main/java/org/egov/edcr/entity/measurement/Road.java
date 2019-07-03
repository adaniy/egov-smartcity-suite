package org.egov.edcr.entity.measurement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Road extends Measurement {

    private static final long serialVersionUID = 66L;
    protected List<BigDecimal> shortestDistanceToRoad = new ArrayList<>();
    protected List<BigDecimal> distancesFromCenterToPlot = new ArrayList<>();
    protected List<BigDecimal> distanceFromAccessoryBlock = new ArrayList<>();

    public List<BigDecimal> getDistancesFromCenterToPlot() {
        return distancesFromCenterToPlot;
    }

    public void setDistancesFromCenterToPlot(List<BigDecimal> distancesFromCenterToPlot) {
        this.distancesFromCenterToPlot = distancesFromCenterToPlot;
    }

    public void addDistancesFromCenterToPlot(BigDecimal distancesFromCenterToPlot) {
        getDistancesFromCenterToPlot().add(distancesFromCenterToPlot);
    }

    public List<BigDecimal> getShortestDistanceToRoad() {
        return shortestDistanceToRoad;
    }

    public void setShortestDistanceToRoad(List<BigDecimal> shortestDistanceToRoad) {
        this.shortestDistanceToRoad = shortestDistanceToRoad;
    }

    public void addShortestDistanceToRoad(BigDecimal shortestDistanceToRoad) {
        getShortestDistanceToRoad().add(shortestDistanceToRoad);
    }

    public List<BigDecimal> getDistanceFromAccessoryBlock() {
        return distanceFromAccessoryBlock;
    }

    public void addDistanceFromAccessoryBlock(BigDecimal distanceFromAccessoryBlock) {
        this.distanceFromAccessoryBlock.add(distanceFromAccessoryBlock);
    }

}