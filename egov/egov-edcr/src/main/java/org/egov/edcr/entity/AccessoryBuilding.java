package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class AccessoryBuilding extends Measurement {

    private static final long serialVersionUID = 41L;

    private List<BigDecimal> distanceFromPlotBoundary = new ArrayList<>();

    public List<BigDecimal> getDistanceFromPlotBoundary() {
        return distanceFromPlotBoundary;
    }

    public void setDistanceFromPlotBoundary(List<BigDecimal> distanceFromPlotBoundary) {
        this.distanceFromPlotBoundary = distanceFromPlotBoundary;
    }
}
