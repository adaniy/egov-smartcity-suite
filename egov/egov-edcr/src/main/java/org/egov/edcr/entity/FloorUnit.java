package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class FloorUnit extends Measurement {

    private static final long serialVersionUID = 27L;

    private Occupancy occupancy;
    private List<Measurement> deductions = new ArrayList<>();
    private BigDecimal totalUnitDeduction;

    public Occupancy getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(Occupancy occupancy) {
        this.occupancy = occupancy;
    }

    public BigDecimal getTotalUnitDeduction() {
        return totalUnitDeduction;
    }

    public void setTotalUnitDeduction(BigDecimal totalDeduction) {
        totalUnitDeduction = totalDeduction;
    }

    public List<Measurement> getDeductions() {
        return deductions;
    }

    public void setDeductions(List<Measurement> deductions) {
        this.deductions = deductions;
    }

}
