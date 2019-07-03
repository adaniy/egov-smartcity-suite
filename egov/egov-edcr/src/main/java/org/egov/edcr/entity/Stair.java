package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLine;

public class Stair extends Measurement {

    private static final long serialVersionUID = 34L;

    private String number;

    private transient List<DXFLWPolyline> stairPolylines;

    private transient List<Measurement> flightPolyLines;

    private transient List<DXFLine> lines;

    private BigDecimal noOfRises;

    private Boolean flightPolyLineClosed = false;

    private transient List<BigDecimal> lengthOfFlights;

    private transient List<BigDecimal> widthOfFlights;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<DXFLWPolyline> getStairPolylines() {
        return stairPolylines;
    }

    public void setStairPolylines(List<DXFLWPolyline> stairPolylines) {
        this.stairPolylines = stairPolylines;
    }

    public List<Measurement> getFlightPolyLines() {
        return flightPolyLines;
    }

    public void setFlightPolyLines(List<Measurement> flightPolyLines) {
        this.flightPolyLines = flightPolyLines;
    }

    public List<DXFLine> getLines() {
        return lines;
    }

    public void setLines(List<DXFLine> lines) {
        this.lines = lines;
    }

    public BigDecimal getNoOfRises() {
        return noOfRises;
    }

    public void setNoOfRises(BigDecimal noOfRises) {
        this.noOfRises = noOfRises;
    }

    public Boolean getFlightPolyLineClosed() {
        return flightPolyLineClosed;
    }

    public void setFlightPolyLineClosed(Boolean flightPolyLineClosed) {
        this.flightPolyLineClosed = flightPolyLineClosed;
    }

    public List<BigDecimal> getLengthOfFlights() {
        return lengthOfFlights;
    }

    public void setLengthOfFlights(List<BigDecimal> lengthOfFlights) {
        this.lengthOfFlights = lengthOfFlights;
    }

    public List<BigDecimal> getWidthOfFlights() {
        return widthOfFlights;
    }

    public void setWidthOfFlights(List<BigDecimal> widthOfFlights) {
        this.widthOfFlights = widthOfFlights;
    }

}
