package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class Building extends Measurement {

    private static final long serialVersionUID = 13L;

    private BigDecimal buildingHeight;

    private BigDecimal buildingTopMostHeight;

    private BigDecimal totalFloorArea;

    private BigDecimal totalExistingFloorArea;

    private Measurement exteriorWall;

    private Measurement shade;

    private BigDecimal far;

    private BigDecimal coverage;

    private BigDecimal coverageArea;
    /*
     * Maximum number of floors
     */
    private BigDecimal maxFloor;
    /*
     * Total number of floors including celler
     */
    private BigDecimal totalFloors;

    private List<Floor> floors = new ArrayList<>();

    private BigDecimal floorsAboveGround;

    private List<BigDecimal> distanceFromBuildingFootPrintToRoadEnd = new ArrayList<>();
    private List<BigDecimal> distanceFromSetBackToBuildingLine = new ArrayList<>();

    private BigDecimal totalBuitUpArea;

    private BigDecimal totalExistingBuiltUpArea;

    private OccupancyType mostRestrictiveOccupancy;
    // this is converted Occupancies to base type
    private List<Occupancy> occupancies = new ArrayList<>();

    // This would be plain sum of occupancies without converting
    private List<Occupancy> totalArea = new ArrayList<>();

    // Mezzanine occupancies area
    private List<Occupancy> mezzanineOccupancies = new ArrayList<>();

    private Passage passage;

    public List<Occupancy> getOccupancies() {
        return occupancies;
    }

    public void setOccupancies(List<Occupancy> occupancies) {
        this.occupancies = occupancies;
    }

    public OccupancyType getMostRestrictiveOccupancy() {
        return mostRestrictiveOccupancy;
    }

    public void setMostRestrictiveOccupancy(OccupancyType mostRestrictiveOccupancy) {
        this.mostRestrictiveOccupancy = mostRestrictiveOccupancy;
    }

    public BigDecimal getTotalExistingFloorArea() {
        return totalExistingFloorArea;
    }

    public BigDecimal getTotalExistingBuiltUpArea() {
        return totalExistingBuiltUpArea;
    }

    public void setTotalExistingFloorArea(BigDecimal totalExistingFloorArea) {
        this.totalExistingFloorArea = totalExistingFloorArea;
    }

    public void setTotalExistingBuiltUpArea(BigDecimal totalExistingBuiltUpArea) {
        this.totalExistingBuiltUpArea = totalExistingBuiltUpArea;
    }

    public BigDecimal getBuildingHeight() {
        return buildingHeight;
    }

    public void setBuildingHeight(BigDecimal buildingHeight) {
        this.buildingHeight = buildingHeight;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void sortFloorByName() {
        if (!floors.isEmpty())
            Collections.sort(floors, (c1, c2) -> c1.getNumber()
                    .compareTo(c2.getNumber()));

    }

    public Floor getFloorNumber(int floorNo) {
        for (Floor f : floors)
            if (f.getNumber() != null && f.getNumber().intValue() == floorNo)
                return f;
        return null;
    }

    public BigDecimal getCoverageArea() {
        return coverageArea;
    }

    public void setCoverageArea(BigDecimal coverageArea) {
        this.coverageArea = coverageArea;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    public BigDecimal getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(BigDecimal totalFloors) {
        this.totalFloors = totalFloors;
    }

    public BigDecimal getMaxFloor() {
        return maxFloor;
    }

    public void setMaxFloor(BigDecimal maxFloor) {
        this.maxFloor = maxFloor;
    }

    public BigDecimal getBuildingTopMostHeight() {
        return buildingTopMostHeight;
    }

    public void setBuildingTopMostHeight(BigDecimal buildingHeightTopMost) {
        buildingTopMostHeight = buildingHeightTopMost;
    }

    public BigDecimal getTotalFloorArea() {
        return totalFloorArea;
    }

    public void setTotalFloorArea(BigDecimal totalFloorArea) {
        this.totalFloorArea = totalFloorArea;
    }

    public BigDecimal getFar() {
        return far;
    }

    public void setFar(BigDecimal far) {
        this.far = far;
    }

    public BigDecimal getCoverage() {
        return coverage;
    }

    public void setCoverage(BigDecimal coverage) {
        this.coverage = coverage;
    }

    public Measurement getExteriorWall() {
        return exteriorWall;
    }

    public void setExteriorWall(Measurement exteriorWall) {
        this.exteriorWall = exteriorWall;
    }

    public BigDecimal getFloorsAboveGround() {
        return floorsAboveGround;
    }

    public void setFloorsAboveGround(BigDecimal floorsAboveGround) {
        this.floorsAboveGround = floorsAboveGround;
    }

    public Measurement getShade() {
        return shade;
    }

    public void setShade(Measurement shade) {
        this.shade = shade;
    }

    public BigDecimal getTotalBuitUpArea() {
        return totalBuitUpArea;
    }

    public void setTotalBuitUpArea(BigDecimal totalBuitUpArea) {
        this.totalBuitUpArea = totalBuitUpArea;
    }

    @Override
    public String toString() {
        String newLine = "\n";
        StringBuilder str = new StringBuilder();
        str.append("Building :")
                .append(newLine)
                .append("buildingHeight:").append(buildingHeight).append(newLine)
                .append("totalFloorArea:").append(totalFloorArea).append(newLine)
                .append("far:").append(far).append(newLine)
                .append("Coverage:").append(coverage).append(newLine)
                .append("totalFloors:").append(totalFloors).append(newLine)
                .append("floorsAboveGround:").append(floorsAboveGround).append(newLine)
                .append("maxFloor:").append(maxFloor).append(newLine)
                .append("area:").append(area).append(newLine)
                .append("Floors Count:").append(floors.size()).append(newLine)
                .append("Exterior wall:").append(exteriorWall).append(newLine)
                .append("Floors:").append(floors).append(newLine);
        return str.toString();
    }

    public List<BigDecimal> getDistanceFromBuildingFootPrintToRoadEnd() {
        return distanceFromBuildingFootPrintToRoadEnd;
    }

    public void setDistanceFromBuildingFootPrintToRoadEnd(List<BigDecimal> distanceFromBuildingFootPrintToRoadEnd) {
        this.distanceFromBuildingFootPrintToRoadEnd = distanceFromBuildingFootPrintToRoadEnd;
    }

    public List<BigDecimal> getDistanceFromSetBackToBuildingLine() {
        return distanceFromSetBackToBuildingLine;
    }

    public void setDistanceFromSetBackToBuildingLine(List<BigDecimal> distanceFromSetBackToBuildingLine) {
        this.distanceFromSetBackToBuildingLine = distanceFromSetBackToBuildingLine;
    }

    public void addDistanceFromBuildingFootPrintToRoadEnd(BigDecimal distanceFromBuildingFootPrintToRoadEnd) {
        getDistanceFromBuildingFootPrintToRoadEnd().add(distanceFromBuildingFootPrintToRoadEnd);
    }

    public List<Occupancy> getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(List<Occupancy> totalArea) {
        this.totalArea = totalArea;
    }

    public Passage getPassage() {
        return passage;
    }

    public void setPassage(Passage passage) {
        this.passage = passage;
    }

    public List<Occupancy> getMezzanineOccupancies() {
        return mezzanineOccupancies;
    }

    public void setMezzanineOccupancies(List<Occupancy> mezzanineOccupancies) {
        this.mezzanineOccupancies = mezzanineOccupancies;
    }

}
