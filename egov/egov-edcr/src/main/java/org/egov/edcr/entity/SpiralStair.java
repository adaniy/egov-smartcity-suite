package org.egov.edcr.entity;

import java.util.List;

import org.kabeja.dxf.DXFCircle;

public class SpiralStair extends Stair {

    private static final long serialVersionUID = 37L;

    private transient List<DXFCircle> spiralPolyLines;

    public List<DXFCircle> getSpiralPolyLines() {
        return spiralPolyLines;
    }

    public void setSpiralPolyLines(List<DXFCircle> spiralPolyLines) {
        this.spiralPolyLines = spiralPolyLines;
    }
}
