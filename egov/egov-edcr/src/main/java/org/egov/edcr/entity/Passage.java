package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class Passage extends Measurement {

    private static final long serialVersionUID = 8495582638250473166L;

    private List<BigDecimal> passageDimensions;

    private List<BigDecimal> passageStairDimensions;

    public List<BigDecimal> getPassagePolyLines() {
        return passageDimensions;
    }

    public void setPassagePolyLines(List<BigDecimal> passagePolyLines) {
        passageDimensions = passagePolyLines;
    }

    public List<BigDecimal> getPassageStairPolyLines() {
        return passageStairDimensions;
    }

    public void setPassageStairPolyLines(List<BigDecimal> passageStairPolyLines) {
        passageStairDimensions = passageStairPolyLines;
    }

}
