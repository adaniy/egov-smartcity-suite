package org.egov.edcr.entity;

import java.math.BigDecimal;

import org.egov.edcr.entity.measurement.Measurement;

public class MezzanineFloor extends Measurement {

    private static final long serialVersionUID = 32L;

    private String number;

    private BigDecimal builtUpArea;

    private BigDecimal deductions;

    private BigDecimal carpetArea;

    private BigDecimal floorArea;

    private OccupancyType occupancyType;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public BigDecimal getBuiltUpArea() {
        return builtUpArea;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setBuiltUpArea(BigDecimal builtUpArea) {
        this.builtUpArea = builtUpArea;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public OccupancyType getOccupancyType() {
        return occupancyType;
    }

    public void setOccupancyType(OccupancyType occupancyType) {
        this.occupancyType = occupancyType;
    }

    public BigDecimal getCarpetArea() {
        return carpetArea;
    }

    public void setCarpetArea(BigDecimal carpetArea) {
        this.carpetArea = carpetArea;
    }

    public BigDecimal getFloorArea() {
        return floorArea;
    }

    public void setFloorArea(BigDecimal floorArea) {
        this.floorArea = floorArea;
    }
}
