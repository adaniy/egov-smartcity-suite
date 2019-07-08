package org.egov.edcr.entity;

import java.io.Serializable;

public class ParkingHelper implements Serializable {

    private static final long serialVersionUID = -6630682248553464443L;
    public Double totalRequiredCarParking = 0d;
    public Double carParkingForDACal = 0d;
    public Double a1TotalParking = 0d;
    public Double twoWheelerParking = 0d;
    public Double visitorParking = 0d;
    public Double loadingUnloadArea = 0d;
    public Double daParking = 0d;
    public Double mechanicalParking = 0d;
    public String ruleNo;
    public String ruleDescription;

}
