package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class Occupancy extends Measurement {

    private static final long serialVersionUID = 273268209293922286L;
    private OccupancyType type;
    private BigDecimal deduction = BigDecimal.ZERO;
    private BigDecimal builtUpArea = BigDecimal.ZERO;
    private BigDecimal floorArea = BigDecimal.ZERO;
    private BigDecimal carpetArea = BigDecimal.ZERO;
    private BigDecimal existingBuiltUpArea = BigDecimal.ZERO;
    private BigDecimal existingFloorArea = BigDecimal.ZERO;
    private BigDecimal existingCarpetArea = BigDecimal.ZERO;
    private BigDecimal existingDeduction = BigDecimal.ZERO;
    private Boolean withAttachedBath = false;
    private Boolean withOutAttachedBath = false;
    private Boolean withDinningSpace = false;
    private List<Measurement> recreationalSpace = new ArrayList<>();

    public void setExistingBuiltUpArea(BigDecimal existingBuiltUpArea) {
        this.existingBuiltUpArea = existingBuiltUpArea;
    }

    public void setExistingFloorArea(BigDecimal existingFloorArea) {
        this.existingFloorArea = existingFloorArea;
    }

    public void setExistingCarpetArea(BigDecimal existingCarpetArea) {
        this.existingCarpetArea = existingCarpetArea;
    }

    public void setExistingDeduction(BigDecimal existingDeduction) {
        this.existingDeduction = existingDeduction;
    }

    public BigDecimal getExistingBuiltUpArea() {
        return existingBuiltUpArea;
    }

    public BigDecimal getExistingFloorArea() {
        return existingFloorArea;
    }

    public BigDecimal getExistingCarpetArea() {
        return existingCarpetArea;
    }

    public BigDecimal getExistingDeduction() {
        return existingDeduction;
    }

    public void setWithAttachedBath(Boolean withAttachedBath) {
        this.withAttachedBath = withAttachedBath;
    }

    public void setWithOutAttachedBath(Boolean withOutAttachedBath) {
        this.withOutAttachedBath = withOutAttachedBath;
    }

    public void setWithDinningSpace(Boolean withDinningSpace) {
        this.withDinningSpace = withDinningSpace;
    }

    public Boolean getWithAttachedBath() {
        return withAttachedBath;
    }

    public Boolean getWithOutAttachedBath() {
        return withOutAttachedBath;
    }

    public Boolean getWithDinningSpace() {
        return withDinningSpace;
    }

    public OccupancyType getType() {
        return type;
    }

    public void setType(OccupancyType type) {
        this.type = type;
    }

    public BigDecimal getDeduction() {

        return deduction;
    }

    public void setDeduction(BigDecimal deduction) {
        this.deduction = deduction;
    }

    public void setBuiltUpArea(BigDecimal builtUpArea) {
        this.builtUpArea = builtUpArea;
    }

    public void setFloorArea(BigDecimal floorArea) {
        this.floorArea = floorArea;
    }

    public void setCarpetArea(BigDecimal carpetArea) {
        this.carpetArea = carpetArea;
    }

    public BigDecimal getBuiltUpArea() {
        return builtUpArea;
    }

    public BigDecimal getFloorArea() {
        return floorArea;
    }

    public BigDecimal getCarpetArea() {
        return carpetArea;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Occupancy other = (Occupancy) obj;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public List<Measurement> getRecreationalSpace() {
        return recreationalSpace;
    }

    public void setRecreationalSpace(List<Measurement> recreationalSpace) {
        this.recreationalSpace = recreationalSpace;
    }
}
