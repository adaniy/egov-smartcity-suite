package org.egov.edcr.entity.measurement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Yard extends Measurement {

    private static final long serialVersionUID = 15L;

    /**
     * Each yard can at different level Level 0 is considered ground Level -1 is basement
     */

    private Integer level = 0;
    List<BigDecimal> dimensions = new ArrayList<>();

    @Override
    public String toString() {
        return "Yard : presentInDxf=" + presentInDxf + ", minimumDistance=" + minimumDistance + ", mean=" + mean + ", area="
                + area
                + "";
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<BigDecimal> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<BigDecimal> dimensions) {
        this.dimensions = dimensions;
    }
}
