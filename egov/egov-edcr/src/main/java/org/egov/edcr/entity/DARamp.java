package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;
import org.kabeja.dxf.DXFLWPolyline;

public class DARamp extends Measurement {

    private static final long serialVersionUID = -5720070466442451432L;
    private Integer number;
    private transient List<DXFLWPolyline> polylines;
    private BigDecimal slope;

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

    public BigDecimal getSlope() {
        return slope;
    }

    public void setSlope(BigDecimal slope) {
        this.slope = slope;
    }
}
