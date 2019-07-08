package org.egov.edcr.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

/**
 *
 * @author vinoth
 *
 */
public class ParkingDetails implements Serializable {
    private static final long serialVersionUID = -6799777425904904290L;
    private List<Measurement> cars = new ArrayList<>();
    private Integer validCarParkingSlots = 0;
    private Integer diningSeats = 0;
    private List<Measurement> loadUnload = new ArrayList<>();
    private List<Measurement> mechParking = new ArrayList<>();
    private List<Measurement> twoWheelers = new ArrayList<>();
    private List<Measurement> disabledPersons = new ArrayList<>();
    private Integer validDAParkingSlots = 0;
    private BigDecimal distFromDAToMainEntrance = BigDecimal.ZERO;

    public List<Measurement> getCars() {
        return cars;
    }

    public void setCars(List<Measurement> cars) {
        this.cars = cars;
    }

    public Integer getValidCarParkingSlots() {
        return validCarParkingSlots;
    }

    public void setValidCarParkingSlots(Integer validCarParkingSlots) {
        this.validCarParkingSlots = validCarParkingSlots;
    }

    public Integer getDiningSeats() {
        return diningSeats;
    }

    public void setDiningSeats(Integer diningSeats) {
        this.diningSeats = diningSeats;
    }

    public List<Measurement> getLoadUnload() {
        return loadUnload;
    }

    public void setLoadUnload(List<Measurement> loadUnload) {
        this.loadUnload = loadUnload;
    }

    public List<Measurement> getMechParking() {
        return mechParking;
    }

    public void setMechParking(List<Measurement> mechParking) {
        this.mechParking = mechParking;
    }

    public List<Measurement> getTwoWheelers() {
        return twoWheelers;
    }

    public void setTwoWheelers(List<Measurement> twoWheelers) {
        this.twoWheelers = twoWheelers;
    }

    public List<Measurement> getDisabledPersons() {
        return disabledPersons;
    }

    public void setDisabledPersons(List<Measurement> disabledPersons) {
        this.disabledPersons = disabledPersons;
    }

    public Integer getValidDAParkingSlots() {
        return validDAParkingSlots;
    }

    public void setValidDAParkingSlots(Integer validDAParkingSlots) {
        this.validDAParkingSlots = validDAParkingSlots;
    }

    public BigDecimal getDistFromDAToMainEntrance() {
        return distFromDAToMainEntrance;
    }

    public void setDistFromDAToMainEntrance(BigDecimal distFromDAToMainEntrance) {
        this.distFromDAToMainEntrance = distFromDAToMainEntrance;
    }
}