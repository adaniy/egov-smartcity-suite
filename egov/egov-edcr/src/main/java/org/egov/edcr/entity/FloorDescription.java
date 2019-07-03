package org.egov.edcr.entity;

public enum FloorDescription {

    CELLAR_FLOOR("Cellar Floor"), GROUND_FLOOR("Ground Floor"), UPPER_FLOOR("Upper Floor"), MEZZANINE_FLOOR(
            "Mezzanine Floor"), TERRACE_FLOOR("Terrace Floor");

    private final String floorDescriptionVal;

    FloorDescription(String floorDescVal) {
        floorDescriptionVal = floorDescVal;
    }

    public String getFloorDescriptionVal() {
        return floorDescriptionVal;
    }
}