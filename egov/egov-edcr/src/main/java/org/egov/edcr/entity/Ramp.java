package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;
import org.kabeja.dxf.DXFLWPolyline;

public class Ramp extends Measurement {

    private static final long serialVersionUID = 30L;

    private Integer number;

    private BigDecimal slope;

    private transient List<DXFLWPolyline> polylines;

    private transient List<Measurement> rampPolyLines;

    private Boolean rampPolyLineClosed = false;

    private BigDecimal floorHeight;

    public BigDecimal getFloorHeight() {
        return floorHeight;
    }

    public void setFloorHeight(BigDecimal floorHeight) {
        this.floorHeight = floorHeight;
    }

    public List<Measurement> getRampPolyLines() {
        return rampPolyLines;
    }

    public Boolean getRampPolyLineClosed() {
        return rampPolyLineClosed;
    }

    public void setRampPolyLines(List<Measurement> rampPolyLines) {
        this.rampPolyLines = rampPolyLines;
    }

    public void setRampPolyLineClosed(Boolean rampPolyLineClosed) {
        this.rampPolyLineClosed = rampPolyLineClosed;
    }

    public BigDecimal getSlope() {
        return slope;
    }

    public void setSlope(BigDecimal slope) {
        this.slope = slope;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public List<DXFLWPolyline> getPolylines() {
        return polylines;
    }

    public void setPolylines(List<DXFLWPolyline> polylines) {
        this.polylines = polylines;
    }
}
