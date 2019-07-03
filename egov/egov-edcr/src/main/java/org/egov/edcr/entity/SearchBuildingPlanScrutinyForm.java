package org.egov.edcr.entity;

import java.util.Date;
import java.util.List;

import org.egov.infra.web.support.search.DataTableSearchRequest;

public class SearchBuildingPlanScrutinyForm extends DataTableSearchRequest {
    private String buildingPlanScrutinyNumber;

    private String status;

    private Date fromDate;

    private Date toDate;

    private String buildingLicenceeType;

    private String buildingLicenceeName;

    private String dxfFileStoreId;

    private String markedDxfFileStoreId;

    private String reportOutputFileStoreId;

    private String dxfFileName;

    private String markedDxfFileName;

    private String reportOutputFileName;

    private List<Long> stakeHolderIds;

    private Long stakeHolderId;

    private String applicationNumber;

    private Date applicationDate;

    private String applicantName;

    private String bpaApplicationNumber;

    private String permitNumber;

    private Date uploadedDateAndTime;

    public List<Long> getStakeHolderIds() {
        return stakeHolderIds;
    }

    public void setStakeHolderIds(List<Long> stakeHolderIds) {
        this.stakeHolderIds = stakeHolderIds;
    }

    public String getDxfFileStoreId() {
        return dxfFileStoreId;
    }

    public void setDxfFileStoreId(String dxfFileStoreId) {
        this.dxfFileStoreId = dxfFileStoreId;
    }

    public String getMarkedDxfFileStoreId() {
        return markedDxfFileStoreId;
    }

    public void setMarkedDxfFileStoreId(String markedDxfFileStoreId) {
        this.markedDxfFileStoreId = markedDxfFileStoreId;
    }

    public String getReportOutputFileStoreId() {
        return reportOutputFileStoreId;
    }

    public void setReportOutputFileStoreId(String reportOutputFileStoreId) {
        this.reportOutputFileStoreId = reportOutputFileStoreId;
    }

    public String getDxfFileName() {
        return dxfFileName;
    }

    public void setDxfFileName(String dxfFileName) {
        this.dxfFileName = dxfFileName;
    }

    public String getMarkedDxfFileName() {
        return markedDxfFileName;
    }

    public void setMarkedDxfFileName(String markedDxfFileName) {
        this.markedDxfFileName = markedDxfFileName;
    }

    public String getReportOutputFileName() {
        return reportOutputFileName;
    }

    public void setReportOutputFileName(String reportOutputFileName) {
        this.reportOutputFileName = reportOutputFileName;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getBpaApplicationNumber() {
        return bpaApplicationNumber;
    }

    public void setBpaApplicationNumber(String bpaApplicationNumber) {
        this.bpaApplicationNumber = bpaApplicationNumber;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public String getBuildingLicenceeName() {
        return buildingLicenceeName;
    }

    public void setBuildingLicenceeName(String buildingLicenceeName) {
        this.buildingLicenceeName = buildingLicenceeName;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getBuildingLicenceeType() {
        return buildingLicenceeType;
    }

    public void setBuildingLicenceeType(String buildingLicenceeType) {
        this.buildingLicenceeType = buildingLicenceeType;
    }

    public String getBuildingPlanScrutinyNumber() {
        return buildingPlanScrutinyNumber;
    }

    public void setBuildingPlanScrutinyNumber(String buildingPlanScrutinyNumber) {
        this.buildingPlanScrutinyNumber = buildingPlanScrutinyNumber;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Long getStakeHolderId() {
        return stakeHolderId;
    }

    public void setStakeHolderId(Long stakeHolderId) {
        this.stakeHolderId = stakeHolderId;
    }

    public Date getUploadedDateAndTime() {
        return uploadedDateAndTime;
    }

    public void setUploadedDateAndTime(Date uploadedDateAndTime) {
        this.uploadedDateAndTime = uploadedDateAndTime;
    }
}
