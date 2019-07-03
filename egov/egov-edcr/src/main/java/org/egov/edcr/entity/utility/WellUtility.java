package org.egov.edcr.entity.utility;

import org.egov.edcr.entity.measurement.Measurement;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFLWPolyline;

public class WellUtility extends Measurement {
    private static final long serialVersionUID = 46L;
    protected transient DXFCircle circle;

    protected transient DXFLWPolyline polygon;

    private String type;

    public DXFCircle getCircle() {
        return circle;
    }

    public void setCircle(DXFCircle circle) {
        this.circle = circle;
    }

    public DXFLWPolyline getPolygon() {
        return polygon;
    }

    public void setPolygon(DXFLWPolyline polygon) {
        this.polygon = polygon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
