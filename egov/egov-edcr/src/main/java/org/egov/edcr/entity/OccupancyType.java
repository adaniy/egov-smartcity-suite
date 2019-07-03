package org.egov.edcr.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OccupancyType {

    OCCUPANCY_A1("Residential"),// singlefamily,
    OCCUPANCY_A2("Special Residential"), OCCUPANCY_A3("Hostel Educational"), OCCUPANCY_A4("Apartment/Flat"), OCCUPANCY_A5(
            "Professional Office"), OCCUPANCY_B1("Educational"), OCCUPANCY_B2("Educational HighSchool"), OCCUPANCY_B3(
                    "Higher Educational Institute"), OCCUPANCY_C("Medical/Hospital"), OCCUPANCY_C1("Medical IP"), OCCUPANCY_C2(
                            "Medical OP"), OCCUPANCY_C3("Medical Admin"), OCCUPANCY_D("Assembly"), OCCUPANCY_D1(
                                    "Assembly Worship"), OCCUPANCY_D2("Bus Terminal"), OCCUPANCY_E(
                                            "Office/Business"), OCCUPANCY_F("Mercantile / Commercial"), OCCUPANCY_F1(
                                                    "Commercial Parking Plaza"), OCCUPANCY_F2(
                                                            "Commercial Parking Appurtenant"), OCCUPANCY_F3(
                                                                    "Hotels"), OCCUPANCY_F4("Kiosk"), OCCUPANCY_G1(
                                                                            "Industrial"), OCCUPANCY_G2(
                                                                                    "Small Industrial"), OCCUPANCY_H(
                                                                                            "Storage"), OCCUPANCY_I1(
                                                                                                    "Hazardous (I1)"), OCCUPANCY_I2(
                                                                                                            "Hazardous (I2)");

    @JsonValue
    private final String occupancyTypeVal;

    OccupancyType(String aTypeVal) {
        occupancyTypeVal = aTypeVal;
    }

    public String getOccupancyType() {
        return occupancyTypeVal;
    }

    public String getOccupancyTypeVal() {
        return occupancyTypeVal;
    }
}
