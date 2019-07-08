package org.egov.edcr.entity;

import static org.egov.edcr.utility.DcrConstants.SIDE_YARD1_DESC;
import static org.egov.edcr.utility.DcrConstants.SIDE_YARD2_DESC;

import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.utility.BlockDistances;
import org.egov.edcr.entity.utility.SetBack;

public class Block extends Measurement {

    private static final long serialVersionUID = 3126433375694514278L;
    private Building building = new Building();
    private String name;
    private String number;
    private String numberOfLifts;
    private List<SetBack> setBacks = new ArrayList<>();
    private List<Measurement> coverage = new ArrayList<>();
    private List<Measurement> coverageDeductions = new ArrayList<>();
    private List<TypicalFloor> typicalFloor = new ArrayList<>();
    private List<BlockDistances> disBetweenBlocks = new ArrayList<>();
    private List<Measurement> hallAreas = new ArrayList<>();
    private List<Measurement> diningSpaces = new ArrayList<>();
    private List<Measurement> balconyAreas = new ArrayList<>();
    private SanityDetails sanityDetails = new SanityDetails();
    private transient Boolean singleFamilyBuilding = false;
    private transient Boolean residentialBuilding = false;
    private transient Boolean residentialOrCommercialBuilding = false;
    private transient Boolean highRiseBuilding = false;
    private transient Boolean completelyExisting = false;

    private List<DARamp> daRamps = new ArrayList<>();

    private List<Measurement> openStairs = new ArrayList<>();

    @Override
    public String toString() {
        return "Block [building=" + building + ", name=" + name + ", number=" + number + ", setBacks=" + setBacks
                + ", presentInDxf=" + presentInDxf + "]";
    }

    public List<SetBack> getSetBacks() {
        return setBacks;
    }

    public SetBack getLevelZeroSetBack() {
        SetBack setBack = null;

        for (SetBack setback : getSetBacks())
            if (setback.getLevel() == 0)
                return setback;
        return setBack;
    }

    public String getNumberOfLifts() {
        return numberOfLifts;
    }

    public void setNumberOfLifts(String numberOfLifts) {
        this.numberOfLifts = numberOfLifts;
    }

    public void setDisBetweenBlocks(List<BlockDistances> disBetweenBlocks) {
        this.disBetweenBlocks = disBetweenBlocks;
    }

    public SetBack getSetBackByLevel(String level) {

        SetBack setBack = null;
        Integer lvl = Integer.valueOf(level);
        for (SetBack setback : getSetBacks())
            if (setback.getLevel() == lvl)
                return setback;
        return setBack;
    }

    public Boolean getResidentialOrCommercialBuilding() {
        return residentialOrCommercialBuilding;
    }

    public void setResidentialOrCommercialBuilding(Boolean residentialOrCommercialBuilding) {
        this.residentialOrCommercialBuilding = residentialOrCommercialBuilding;
    }

    public List<DARamp> getDARamps() {
        return daRamps;
    }

    public void addDARamps(DARamp daRamps) {
        this.daRamps.add(daRamps);
    }

    public List<BlockDistances> getDistanceBetweenBlocks() {
        return disBetweenBlocks;
    }

    public void setSetBacks(List<SetBack> setBacks) {
        this.setBacks = setBacks;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Boolean getResidentialBuilding() {
        return residentialBuilding;
    }

    public void setResidentialBuilding(Boolean residentialBuilding) {
        this.residentialBuilding = residentialBuilding;
    }

    public SetBack getLowerLevelSetBack(Integer level, String yardDesc) {

        SetBack setBack = null;
        if (level == 0)
            return null;

        while (level > 0) {
            level--;
            for (SetBack setback : getSetBacks())
                if (setback.getLevel() == level && yardDesc.equalsIgnoreCase(SIDE_YARD1_DESC)
                        && setback.getSideYard1() != null)
                    return setback;
                else if (setback.getLevel() == level && yardDesc.equalsIgnoreCase(SIDE_YARD2_DESC)
                        && setback.getSideYard2() != null)
                    return setback;

        }
        return setBack;

    }

    public List<TypicalFloor> getTypicalFloor() {
        return typicalFloor;
    }

    public void setTypicalFloor(List<TypicalFloor> typicalFloor) {
        this.typicalFloor = typicalFloor;
    }

    public List<Measurement> getHallAreas() {
        return hallAreas;
    }

    public void setHallAreas(List<Measurement> hallAreas) {
        this.hallAreas = hallAreas;
    }

    public List<Measurement> getDiningSpaces() {
        return diningSpaces;
    }

    public void setDiningSpaces(List<Measurement> diningSpaces) {
        this.diningSpaces = diningSpaces;
    }

    public List<Measurement> getBalconyAreas() {
        return balconyAreas;
    }

    public void setBalconyAreas(List<Measurement> balconyAreas) {
        this.balconyAreas = balconyAreas;
    }

    public SanityDetails getSanityDetails() {
        return sanityDetails;
    }

    public void setSanityDetails(SanityDetails sanityDetails) {
        this.sanityDetails = sanityDetails;
    }

    public Boolean getSingleFamilyBuilding() {
        return singleFamilyBuilding;
    }

    public void setSingleFamilyBuilding(Boolean singleFamilyBuilding) {
        this.singleFamilyBuilding = singleFamilyBuilding;
    }

    public Boolean getHighRiseBuilding() {
        return highRiseBuilding;
    }

    public void setHighRiseBuilding(Boolean highRiseBuilding) {
        this.highRiseBuilding = highRiseBuilding;
    }

    public List<Measurement> getCoverage() {
        return coverage;
    }

    public List<Measurement> getCoverageDeductions() {
        return coverageDeductions;
    }

    public List<BlockDistances> getDisBetweenBlocks() {
        return disBetweenBlocks;
    }

    public void setCoverage(List<Measurement> coverage) {
        this.coverage = coverage;
    }

    public void setCoverageDeductions(List<Measurement> coverageDeductions) {
        this.coverageDeductions = coverageDeductions;
    }

    public void setDaRamps(List<DARamp> daRamps) {
        this.daRamps = daRamps;
    }

    public Boolean getCompletelyExisting() {
        return completelyExisting;
    }

    public void setCompletelyExisting(Boolean completelyExisting) {
        this.completelyExisting = completelyExisting;
    }

    public List<Measurement> getOpenStairs() {
        return openStairs;
    }

    public void setOpenStairs(List<Measurement> openStairs) {
        this.openStairs = openStairs;
    }
}
