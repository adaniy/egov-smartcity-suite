package org.egov.edcr.entity.utility;

import java.util.List;

import org.egov.edcr.entity.DcrReportBlockDetail;

public class DcrReportPlanDetail {

    private VirtualBuildingReport virtualBuildingReport;

    private List<DcrReportBlockDetail> dcrReportBlockDetailList;

    public VirtualBuildingReport getVirtualBuildingReport() {
        return virtualBuildingReport;
    }

    public void setVirtualBuildingReport(VirtualBuildingReport virtualBuildingReport) {
        this.virtualBuildingReport = virtualBuildingReport;
    }

    public List<DcrReportBlockDetail> getDcrReportBlockDetailList() {
        return dcrReportBlockDetailList;
    }

    public void setDcrReportBlockDetailList(List<DcrReportBlockDetail> dcrReportBlockDetailList) {
        this.dcrReportBlockDetailList = dcrReportBlockDetailList;
    }
}
