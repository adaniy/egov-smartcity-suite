package org.egov.edcr.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class RoadOutput implements Serializable {

    private static final long serialVersionUID = 39L;

    public String colourCode;
    public BigDecimal roadDistainceToPlot;
    public BigDecimal distance;
}
