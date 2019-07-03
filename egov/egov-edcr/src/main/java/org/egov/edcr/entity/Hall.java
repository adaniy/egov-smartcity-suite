package org.egov.edcr.entity;

import java.math.BigDecimal;

import org.egov.edcr.entity.measurement.Measurement;

public class Hall extends Measurement {

    private static final long serialVersionUID = 33L;

    private String number;

    private BigDecimal builtUpArea;

    private BigDecimal deductions;

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
}
