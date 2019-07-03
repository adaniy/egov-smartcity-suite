package org.egov.edcr.entity.measurement;

import java.io.Serializable;
import java.math.BigDecimal;

import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;

public class Measurement implements Cloneable, Serializable {
    private static final long serialVersionUID = 3L;

    protected Boolean presentInDxf = false;

    protected BigDecimal minimumDistance = BigDecimal.ZERO;

    protected BigDecimal minimumSide = BigDecimal.ZERO;

    protected BigDecimal length = BigDecimal.ZERO;

    protected BigDecimal width = BigDecimal.ZERO;

    protected BigDecimal height = BigDecimal.ZERO;

    protected BigDecimal mean = BigDecimal.ZERO;

    protected BigDecimal area = BigDecimal.ZERO;

    protected Boolean isValid;

    protected StringBuffer invalidReason;

    protected int colorCode;

    protected transient DXFLWPolyline polyLine;

    public void setMinimumDistance(BigDecimal minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    public Boolean getPresentInDxf() {
        return presentInDxf;
    }

    public void setPresentInDxf(Boolean present) {
        presentInDxf = present;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public BigDecimal getMean() {
        return mean;
    }

    public void setMean(BigDecimal mean) {
        this.mean = mean;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getMinimumDistance() {
        return minimumDistance;
    }

    public DXFLWPolyline getPolyLine() {
        return polyLine;
    }

    public void setPolyLine(DXFLWPolyline polyLine) {
        this.polyLine = polyLine;
    }

    @Override
    public String toString() {
        return "Measurement : presentInDxf=" + presentInDxf + "\n polyLine found=" + polyLine == null ? "F" : "T" + "]";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Measurement(DXFLWPolyline polyLine, Boolean smallSide) {
        this.polyLine = polyLine;
        area = Util.getPolyLineArea(polyLine);
        if (smallSide) {
            Util.setDimension(this, polyLine);
            colorCode = polyLine.getColor();
            length = BigDecimal.valueOf(polyLine.getLength());
            // this.minimumSide = Util.getSmallestSide(polyLine);
        }
    }

    public Measurement(DXFLWPolyline polyLine) {
        this.polyLine = polyLine;
        area = Util.getPolyLineArea(polyLine);
    }

    public Measurement() {
    }

    public BigDecimal getMinimumSide() {
        return minimumSide;
    }

    public void setMinimumSide(BigDecimal minimumSide) {
        this.minimumSide = minimumSide;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public int getColorCode() {
        return colorCode;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public void setColorCode(int colorCode) {
        this.colorCode = colorCode;
    }

    public StringBuffer getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(StringBuffer invalidReason) {
        this.invalidReason = invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = new StringBuffer(invalidReason);
    }

    public void appendInvalidReason(String reason) {
        if (invalidReason == null)
            invalidReason = new StringBuffer();

        if (invalidReason.length() != 0)
            invalidReason.append(", ");
        invalidReason.append(reason);
    }

}
