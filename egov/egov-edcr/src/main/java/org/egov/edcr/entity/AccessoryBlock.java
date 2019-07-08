package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;
import org.kabeja.dxf.DXFLWPolyline;

public class AccessoryBlock extends Measurement {


    private static final long serialVersionUID = 1521692555939625281L;
    private String number;
    private transient List<DXFLWPolyline> polylineList = new ArrayList<>();

    private AccessoryBuilding accessoryBuilding = new AccessoryBuilding();

    public String getNumber() {
        return number;
    }

    public AccessoryBuilding getAccessoryBuilding() {
        return accessoryBuilding;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setAccessoryBuilding(AccessoryBuilding accessoryBuilding) {
        this.accessoryBuilding = accessoryBuilding;
    }

    public List<DXFLWPolyline> getPolylineList() {
        return polylineList;
    }

    public void setPolylineList(List<DXFLWPolyline> polylineList) {
        this.polylineList = polylineList;
    }
}
