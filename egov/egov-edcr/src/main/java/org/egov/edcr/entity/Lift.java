package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;
import org.kabeja.dxf.DXFLWPolyline;

public class Lift extends Measurement {

    private static final long serialVersionUID = 5643661327987813409L;

    private Integer number;

    private transient List<DXFLWPolyline> polylines = new ArrayList<>();

    private transient List<Measurement> liftPolyLines;

    private Boolean liftPolyLineClosed = false;

    public List<DXFLWPolyline> getPolylines() {
        return polylines;
    }

    public void setPolylines(List<DXFLWPolyline> polylines) {
        this.polylines = polylines;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public List<Measurement> getLiftPolyLines() {
        return liftPolyLines;
    }

    public void setLiftPolyLines(List<Measurement> liftPolyLines) {
        this.liftPolyLines = liftPolyLines;
    }

    public Boolean getLiftPolyLineClosed() {
        return liftPolyLineClosed;
    }

    public void setLiftPolyLineClosed(Boolean rampPolyLineClosed) {
        liftPolyLineClosed = rampPolyLineClosed;
    }
}
