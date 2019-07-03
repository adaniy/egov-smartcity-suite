package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

public class DcrReportBlockDetail {

    private String blockNo;

    private BigDecimal coverageArea;

    private BigDecimal buildingHeight;

    private List<DcrReportFloorDetail> dcrReportFloorDetails;

    public String getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(String blockNo) {
        this.blockNo = blockNo;
    }

    public BigDecimal getCoverageArea() {
        return coverageArea;
    }

    public BigDecimal getBuildingHeight() {
        return buildingHeight;
    }

    public void setBuildingHeight(BigDecimal buildingHeight) {
        this.buildingHeight = buildingHeight;
    }

    public void setCoverageArea(BigDecimal coverageArea) {
        this.coverageArea = coverageArea;
    }

    public List<DcrReportFloorDetail> getDcrReportFloorDetails() {
        return dcrReportFloorDetails;
    }

    public void setDcrReportFloorDetails(List<DcrReportFloorDetail> dcrReportFloorDetails) {
        this.dcrReportFloorDetails = dcrReportFloorDetails;
    }
}
