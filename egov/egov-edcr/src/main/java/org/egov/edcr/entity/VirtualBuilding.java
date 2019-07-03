package org.egov.edcr.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.EnumSet;

public class VirtualBuilding implements Serializable {
    private static final long serialVersionUID = 7L;
    private BigDecimal buildingHeight;
    private EnumSet<OccupancyType> occupancies = EnumSet.noneOf(OccupancyType.class);
    private BigDecimal totalBuitUpArea;
    private BigDecimal totalFloorArea;
    private BigDecimal totalCarpetArea;
    private BigDecimal totalExistingBuiltUpArea;
    private BigDecimal totalExistingFloorArea;
    private BigDecimal totalExistingCarpetArea;
    private OccupancyType mostRestrictiveFar;
    private OccupancyType mostRestrictiveCoverage;
    private BigDecimal floorsAboveGround;
    private BigDecimal totalCoverageArea;
    private transient Boolean residentialOrCommercialBuilding = false;
    private transient Boolean residentialBuilding = false;

    public BigDecimal getTotalCarpetArea() {
        return totalCarpetArea;
    }

    public BigDecimal getTotalExistingBuiltUpArea() {
        return totalExistingBuiltUpArea;
    }

    public BigDecimal getTotalExistingFloorArea() {
        return totalExistingFloorArea;
    }

    public BigDecimal getTotalExistingCarpetArea() {
        return totalExistingCarpetArea;
    }

    public void setTotalCarpetArea(BigDecimal totalCarpetArea) {
        this.totalCarpetArea = totalCarpetArea;
    }

    public void setTotalExistingBuiltUpArea(BigDecimal totalExistingBuiltUpArea) {
        this.totalExistingBuiltUpArea = totalExistingBuiltUpArea;
    }

    public void setTotalExistingFloorArea(BigDecimal totalExistingFloorArea) {
        this.totalExistingFloorArea = totalExistingFloorArea;
    }

    public void setTotalExistingCarpetArea(BigDecimal totalExistingCarpetArea) {
        this.totalExistingCarpetArea = totalExistingCarpetArea;
    }

    public void setTotalCoverageArea(BigDecimal totalCoverageArea) {
        this.totalCoverageArea = totalCoverageArea;
    }

    public BigDecimal getTotalCoverageArea() {
        return totalCoverageArea;
    }

    public Boolean getResidentialBuilding() {
        return residentialBuilding;
    }

    public void setResidentialBuilding(Boolean residentialBuilding) {
        this.residentialBuilding = residentialBuilding;
    }

    public BigDecimal getFloorsAboveGround() {
        return floorsAboveGround;
    }

    public void setFloorsAboveGround(BigDecimal floorsAboveGround) {
        this.floorsAboveGround = floorsAboveGround;
    }

    public BigDecimal getTotalBuitUpArea() {
        return totalBuitUpArea;
    }

    public void setTotalBuitUpArea(BigDecimal totalBuitUpArea) {
        this.totalBuitUpArea = totalBuitUpArea;
    }

    public BigDecimal getTotalFloorArea() {
        return totalFloorArea;
    }

    public void setTotalFloorArea(BigDecimal totalFloorArea) {
        this.totalFloorArea = totalFloorArea;
    }

    public EnumSet<OccupancyType> getOccupancies() {
        return occupancies;
    }

    public void setOccupancies(EnumSet<OccupancyType> occupancies) {
        this.occupancies = occupancies;
    }

    public BigDecimal getBuildingHeight() {
        return buildingHeight;
    }

    public void setBuildingHeight(BigDecimal buildingHeight) {
        this.buildingHeight = buildingHeight;
    }

    public OccupancyType getMostRestrictiveFar() {
        return mostRestrictiveFar;
    }

    public void setMostRestrictiveFar(OccupancyType mostRestrictiveFar) {
        this.mostRestrictiveFar = mostRestrictiveFar;
    }

    public OccupancyType getMostRestrictiveCoverage() {
        return mostRestrictiveCoverage;
    }

    public void setMostRestrictiveCoverage(OccupancyType mostRestrictiveCoverage) {
        this.mostRestrictiveCoverage = mostRestrictiveCoverage;
    }

    public Boolean getResidentialOrCommercialBuilding() {
        return residentialOrCommercialBuilding;
    }

    public void setResidentialOrCommercialBuilding(Boolean residentialOrCommercialBuilding) {
        this.residentialOrCommercialBuilding = residentialOrCommercialBuilding;
    }
}
