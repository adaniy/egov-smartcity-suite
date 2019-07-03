package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;
import org.kabeja.dxf.DXFLWPolyline;

public class Floor extends Measurement {

    private static final long serialVersionUID = 26L;

    private List<Occupancy> occupancies = new ArrayList<>();
    private List<Occupancy> convertedOccupancies = new ArrayList<>();
    private List<FloorUnit> units = new ArrayList<>();
    private List<Room> habitableRooms = new ArrayList<>();
    private List<DARoom> daRooms = new ArrayList<>();
    private List<Ramp> ramps = new ArrayList<>();
    private List<Lift> lifts = new ArrayList<>();
    private Measurement exterior;
    private List<Measurement> openSpaces = new ArrayList<>();
    // this is for differnetly abled people
    private List<Measurement> specialWaterClosets = new ArrayList<>();
    private List<Measurement> coverageDeduct = new ArrayList<>();
    private String name;
    private Integer number;
    private List<BigDecimal> exitWidthDoor = new ArrayList<>();
    private List<BigDecimal> exitWidthStair = new ArrayList<>();
    private List<MezzanineFloor> mezzanineFloor = new ArrayList<>();
    private List<Hall> halls = new ArrayList();
    private List<FireStair> fireStairs = new ArrayList<>();
    private List<GeneralStair> generalStairs = new ArrayList<>();
    private List<SpiralStair> spiralStairs = new ArrayList<>();
    private transient List<DXFLWPolyline> builtUpAreaPolyLine = new ArrayList<>();
    private List<BigDecimal> floorHeights;
    private List<Measurement> washBasins = new ArrayList<>();
    private Boolean terrace = false;
    private Boolean upperMost = false;

    public void setExitWidthStair(List<BigDecimal> exitWidthStair) {
        this.exitWidthStair = exitWidthStair;
    }

    public List<Occupancy> getConvertedOccupancies() {
        return convertedOccupancies;
    }

    public List<MezzanineFloor> getMezzanineFloor() {
        return mezzanineFloor;
    }

    public List<Hall> getHalls() {
        return halls;
    }

    public void setConvertedOccupancies(List<Occupancy> convertedOccupancies) {
        this.convertedOccupancies = convertedOccupancies;
    }

    public List<Lift> getLifts() {
        return lifts;
    }

    public void setLifts(List<Lift> lifts) {
        this.lifts = lifts;
    }

    public void addLifts(Lift lift) {
        lifts.add(lift);
    }

    public List<Ramp> getRamps() {
        return ramps;
    }

    public void setRamps(List<Ramp> ramps) {
        this.ramps = ramps;
    }

    public List<DARoom> getDaRooms() {
        return daRooms;
    }

    public void setMezzanineFloor(List<MezzanineFloor> mezzanineFloor) {
        this.mezzanineFloor = mezzanineFloor;
    }

    public void setHalls(List<Hall> halls) {
        this.halls = halls;
    }

    public List<BigDecimal> getExitWidthStair() {
        return exitWidthStair;
    }

    public void addBuiltUpArea(Occupancy occupancy) {
        if (occupancies == null) {
            occupancies = new ArrayList<>();
            occupancies.add(occupancy);
        } else if (occupancies.contains(occupancy)) {
            occupancies.get(occupancies.indexOf(occupancy))
                    .setBuiltUpArea((occupancies.get(occupancies.indexOf(occupancy)).getBuiltUpArea() == null ? BigDecimal.ZERO
                            : occupancies.get(occupancies.indexOf(occupancy)).getBuiltUpArea())
                                    .add(occupancy.getBuiltUpArea()));
            occupancies.get(occupancies.indexOf(occupancy)).setExistingBuiltUpArea(
                    (occupancies.get(occupancies.indexOf(occupancy)).getExistingBuiltUpArea() == null ? BigDecimal.ZERO
                            : occupancies.get(occupancies.indexOf(occupancy)).getExistingBuiltUpArea())
                                    .add(occupancy.getExistingBuiltUpArea()));

        } else
            occupancies.add(occupancy);

    }

    public void addDeductionArea(Occupancy occupancy) {
        if (occupancies == null) {
            occupancies = new ArrayList<>();
            occupancies.add(occupancy);
        } else if (occupancies.contains(occupancy)) {
            occupancies.get(occupancies.indexOf(occupancy)).setDeduction(
                    (occupancies.get(occupancies.indexOf(occupancy)).getDeduction() == null ? BigDecimal.ZERO
                            : occupancies.get(occupancies.indexOf(occupancy)).getDeduction())
                                    .add(occupancy.getDeduction()));
            occupancies.get(occupancies.indexOf(occupancy)).setExistingDeduction(
                    (occupancies.get(occupancies.indexOf(occupancy)).getExistingDeduction() == null ? BigDecimal.ZERO
                            : occupancies.get(occupancies.indexOf(occupancy)).getExistingDeduction())
                                    .add(occupancy.getExistingDeduction()));
        } else
            occupancies.add(occupancy);

    }

    public List<Occupancy> getOccupancies() {
        return occupancies;
    }

    public void setOccupancies(List<Occupancy> occupancies) {
        this.occupancies = occupancies;
    }

    public List<FloorUnit> getUnits() {
        return units;
    }

    public void setUnits(List<FloorUnit> units) {
        this.units = units;
    }

    public void setExitWidthDoor(List<BigDecimal> exitWidthDoor) {
        this.exitWidthDoor = exitWidthDoor;
    }

    public List<BigDecimal> getExitWidthDoor() {
        return exitWidthDoor;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public List<Room> getHabitableRooms() {
        return habitableRooms;
    }

    public void setHabitableRooms(List<Room> habitableRooms) {
        this.habitableRooms = habitableRooms;
    }

    public Measurement getExterior() {
        return exterior;
    }

    public void setExterior(Measurement exterior) {
        this.exterior = exterior;
    }

    public List<Measurement> getOpenSpaces() {
        return openSpaces;
    }

    public void setOpenSpaces(List<Measurement> openSpaces) {
        this.openSpaces = openSpaces;
    }

    @Override
    public String toString() {

        return "Floor :" + number + " [habitableRooms Count" + habitableRooms.size() + "\n exterior=" + exterior
                + "\n openSpaces Count=" + openSpaces.size() + "]";

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {

        return super.clone();

    }

    public List<Measurement> getCoverageDeduct() {
        return coverageDeduct;
    }

    public void setCoverageDeduct(List<Measurement> coverageDeduct) {
        this.coverageDeduct = coverageDeduct;
    }

    public List<Measurement> getSpecialWaterClosets() {
        return specialWaterClosets;
    }

    public void setSpecialWaterClosets(List<Measurement> specialWaterClosets) {
        this.specialWaterClosets = specialWaterClosets;
    }

    public void addDaRoom(DARoom daRoom) {
        daRooms.add(daRoom);
    }

    public void addRamps(Ramp ramp) {
        ramps.add(ramp);
    }

    public void setDaRooms(List<DARoom> daRooms) {
        this.daRooms = daRooms;
    }

    public List<FireStair> getFireStairs() {
        return fireStairs;
    }

    public void setFireStairs(List<FireStair> fireStairs) {
        this.fireStairs = fireStairs;
    }

    public void addFireStair(FireStair fireStair) {
        fireStairs.add(fireStair);
    }

    public List<DXFLWPolyline> getBuiltUpAreaPolyLine() {
        return builtUpAreaPolyLine;
    }

    public void setBuiltUpAreaPolyLine(List<DXFLWPolyline> builtUpAreaPolyLine) {
        this.builtUpAreaPolyLine = builtUpAreaPolyLine;
    }

    public List<BigDecimal> getFloorHeights() {
        return floorHeights;
    }

    public void setFloorHeights(List<BigDecimal> floorHeights) {
        this.floorHeights = floorHeights;
    }

    public List<GeneralStair> getGeneralStairs() {
        return generalStairs;
    }

    public void setGeneralStairs(List<GeneralStair> generalStairs) {
        this.generalStairs = generalStairs;
    }

    public void addGeneralStair(GeneralStair generalStair) {
        generalStairs.add(generalStair);
    }

    public List<Measurement> getWashBasins() {
        return washBasins;
    }

    public void setWashBasins(List<Measurement> washBasins) {
        this.washBasins = washBasins;
    }

    public Boolean getTerrace() {
        return terrace;
    }

    public void setTerrace(Boolean terrace) {
        this.terrace = terrace;
    }

    public List<SpiralStair> getSpiralStairs() {
        return spiralStairs;
    }

    public void setSpiralStairs(List<SpiralStair> spiralStairs) {
        this.spiralStairs = spiralStairs;
    }

    public Boolean getUpperMost() {
        return upperMost;
    }

    public void setUpperMost(Boolean upperMost) {
        this.upperMost = upperMost;
    }
}
