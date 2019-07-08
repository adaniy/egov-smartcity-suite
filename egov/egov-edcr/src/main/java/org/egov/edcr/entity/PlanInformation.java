package org.egov.edcr.entity;

import static org.egov.edcr.utility.DcrConstants.NA;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.egov.infra.persistence.entity.AbstractAuditable;
import org.hibernate.validator.constraints.SafeHtml;

@Entity
@Table(name = "EDCR_PLANINFO")
@SequenceGenerator(name = PlanInformation.SEQ_EDCR_PLANINFO, sequenceName = PlanInformation.SEQ_EDCR_PLANINFO, allocationSize = 1)
public class PlanInformation extends AbstractAuditable implements Serializable {

    private static final long serialVersionUID = 2851586270698193495L;
    public static final String SEQ_EDCR_PLANINFO = "SEQ_EDCR_PLANINFO";
    @Id
    @GeneratedValue(generator = SEQ_EDCR_PLANINFO, strategy = GenerationType.SEQUENCE)
    private Long id;

    private BigDecimal plotArea = BigDecimal.ZERO;
    @SafeHtml
    private String ownerName;
    @SafeHtml
    private String occupancy;
    @SafeHtml
    private String serviceType;
    @SafeHtml
    private String amenities;
    @SafeHtml
    private String architectInformation;

    private Long acchitectId;
    @SafeHtml
    private String applicantName;

    private Boolean crzZoneArea = true;
    @SafeHtml
    private transient String crzZoneDesc = NA;

    private BigDecimal demolitionArea = BigDecimal.ZERO;

    private transient Boolean depthCutting;
    @SafeHtml
    private transient String depthCuttingDesc = NA;

    private transient Boolean governmentOrAidedSchool;

    private transient Boolean securityZone = true;
    @SafeHtml
    private transient String securityZoneDesc = NA;

    private transient BigDecimal accessWidth;

    private transient BigDecimal noOfBeds;
    @SafeHtml
    private transient String nocToAbutSideDesc = NA;
    @SafeHtml
    private transient String nocToAbutRearDesc = NA;

    private transient Boolean openingOnSide = false;
    @SafeHtml
    private transient String openingOnSideBelow2mtsDesc = NA;
    @SafeHtml
    private transient String openingOnSideAbove2mtsDesc = NA;
    @SafeHtml
    private transient String openingOnRearBelow2mtsDesc = NA;
    @SafeHtml
    private transient String openingOnRearAbove2mtsDesc = NA;
    @SafeHtml
    private transient String plotInCommercialZone = NA;
    @SafeHtml
    private transient String commercialZoneBldgOpenOnSide1 = NA;
    @SafeHtml
    private transient String commercialZoneBldgOpenOnSide2 = NA;

    /*
     * private transient Boolean nocToAbutAdjascentSide = false;
     */
    private transient Boolean openingOnRear = false;

    private transient Boolean parkingToMainBuilding = false;

    private transient Integer noOfSeats = 0;

    private transient Integer noOfMechanicalParking = 0;
    private transient Integer powerUsedHp = 0;
    private transient Integer numberOfWorkers = 0;

    private transient Boolean singleFamilyBuilding;
    @SafeHtml
    private String reSurveyNo;
    @SafeHtml
    private String revenueWard;
    @SafeHtml
    private String desam;
    @SafeHtml
    private String village;

    public Boolean getParkingToMainBuilding() {
        return parkingToMainBuilding;
    }

    public void setParkingToMainBuilding(Boolean parkingToMainBuilding) {
        this.parkingToMainBuilding = parkingToMainBuilding;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getGovernmentOrAidedSchool() {
        return governmentOrAidedSchool;
    }

    public void setGovernmentOrAidedSchool(Boolean governmentOrAidedSchool) {
        this.governmentOrAidedSchool = governmentOrAidedSchool;
    }

    public Boolean getCrzZoneArea() {
        return crzZoneArea;
    }

    public void setCrzZoneArea(Boolean crzZoneArea) {
        this.crzZoneArea = crzZoneArea;
    }

    public BigDecimal getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(BigDecimal plotArea) {
        this.plotArea = plotArea;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public String getArchitectInformation() {
        return architectInformation;
    }

    public void setArchitectInformation(String architectInformation) {
        this.architectInformation = architectInformation;
    }

    public Long getAcchitectId() {
        return acchitectId;
    }

    public void setAcchitectId(Long acchitectId) {
        this.acchitectId = acchitectId;
    }

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    public Boolean getSecurityZone() {
        return securityZone;
    }

    public void setSecurityZone(Boolean securityZone) {
        this.securityZone = securityZone;
    }

    public BigDecimal getAccessWidth() {
        return accessWidth;
    }

    public Boolean getDepthCutting() {
        return depthCutting;
    }

    public void setDepthCutting(Boolean depthCutting) {
        this.depthCutting = depthCutting;
    }

    public void setAccessWidth(BigDecimal accessWidth) {
        this.accessWidth = accessWidth;
    }

    public Boolean getOpeningOnSide() {
        return openingOnSide;
    }

    public void setOpeningOnSide(Boolean openingOnSide) {
        this.openingOnSide = openingOnSide;
    }

    public Boolean getOpeningOnRear() {
        return openingOnRear;
    }

    public void setOpeningOnRear(Boolean openingOnRear) {
        this.openingOnRear = openingOnRear;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public BigDecimal getNoOfBeds() {
        return noOfBeds;
    }

    public void setNoOfBeds(BigDecimal noOfBeds) {
        this.noOfBeds = noOfBeds;
    }

    public Integer getNoOfSeats() {
        return noOfSeats;
    }

    public void setNoOfSeats(Integer noOfSeats) {
        this.noOfSeats = noOfSeats;
    }

    public Integer getNoOfMechanicalParking() {
        return noOfMechanicalParking;
    }

    public void setNoOfMechanicalParking(Integer noOfMechanicalParking) {
        this.noOfMechanicalParking = noOfMechanicalParking;
    }

    public Boolean getSingleFamilyBuilding() {
        return singleFamilyBuilding;
    }

    public void setSingleFamilyBuilding(Boolean singleFamilyBuilding) {
        this.singleFamilyBuilding = singleFamilyBuilding;
    }

    public BigDecimal getDemolitionArea() {
        return demolitionArea;
    }

    public void setDemolitionArea(BigDecimal demolitionArea) {
        this.demolitionArea = demolitionArea;
    }

    public String getReSurveyNo() {
        return reSurveyNo;
    }

    public void setReSurveyNo(String reSurveyNo) {
        this.reSurveyNo = reSurveyNo;
    }

    public String getRevenueWard() {
        return revenueWard;
    }

    public void setRevenueWard(String revenueWard) {
        this.revenueWard = revenueWard;
    }

    public String getDesam() {
        return desam;
    }

    public void setDesam(String desam) {
        this.desam = desam;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getCrzZoneDesc() {
        return crzZoneDesc;
    }

    public void setCrzZoneDesc(String crzZoneDesc) {
        this.crzZoneDesc = crzZoneDesc;
    }

    public String getSecurityZoneDesc() {
        return securityZoneDesc;
    }

    public void setSecurityZoneDesc(String securityZoneDesc) {
        this.securityZoneDesc = securityZoneDesc;
    }

    public String getOpeningOnSideBelow2mtsDesc() {
        return openingOnSideBelow2mtsDesc;
    }

    public void setOpeningOnSideBelow2mtsDesc(String openingOnSideBelow2mtsDesc) {
        this.openingOnSideBelow2mtsDesc = openingOnSideBelow2mtsDesc;
    }

    public String getOpeningOnSideAbove2mtsDesc() {
        return openingOnSideAbove2mtsDesc;
    }

    public void setOpeningOnSideAbove2mtsDesc(String openingOnSideAbove2mtsDesc) {
        this.openingOnSideAbove2mtsDesc = openingOnSideAbove2mtsDesc;
    }

    public String getOpeningOnRearBelow2mtsDesc() {
        return openingOnRearBelow2mtsDesc;
    }

    public void setOpeningOnRearBelow2mtsDesc(String openingOnRearBelow2mtsDesc) {
        this.openingOnRearBelow2mtsDesc = openingOnRearBelow2mtsDesc;
    }

    public String getOpeningOnRearAbove2mtsDesc() {
        return openingOnRearAbove2mtsDesc;
    }

    public void setOpeningOnRearAbove2mtsDesc(String openingOnRearAbove2mtsDesc) {
        this.openingOnRearAbove2mtsDesc = openingOnRearAbove2mtsDesc;
    }

    public String getNocToAbutSideDesc() {
        return nocToAbutSideDesc;
    }

    public void setNocToAbutSideDesc(String nocToAbutSideDesc) {
        this.nocToAbutSideDesc = nocToAbutSideDesc;
    }

    public String getNocToAbutRearDesc() {
        return nocToAbutRearDesc;
    }

    public void setNocToAbutRearDesc(String nocToAbutRearDesc) {
        this.nocToAbutRearDesc = nocToAbutRearDesc;
    }

    public String getDepthCuttingDesc() {
        return depthCuttingDesc;
    }

    public void setDepthCuttingDesc(String depthCuttingDesc) {
        this.depthCuttingDesc = depthCuttingDesc;
    }

    public String getPlotInCommercialZone() {
        return plotInCommercialZone;
    }

    public void setPlotInCommercialZone(String plotInCommercialZone) {
        this.plotInCommercialZone = plotInCommercialZone;
    }

    public String getCommercialZoneBldgOpenOnSide1() {
        return commercialZoneBldgOpenOnSide1;
    }

    public void setCommercialZoneBldgOpenOnSide1(String commercialZoneBldgOpenOnSide1) {
        this.commercialZoneBldgOpenOnSide1 = commercialZoneBldgOpenOnSide1;
    }

    public String getCommercialZoneBldgOpenOnSide2() {
        return commercialZoneBldgOpenOnSide2;
    }

    public void setCommercialZoneBldgOpenOnSide2(String commercialZoneBldgOpenOnSide2) {
        this.commercialZoneBldgOpenOnSide2 = commercialZoneBldgOpenOnSide2;
    }

    public Integer getPowerUsedHp() {
        return powerUsedHp;
    }

    public void setPowerUsedHp(Integer powerUsedHp) {
        this.powerUsedHp = powerUsedHp;
    }

    public Integer getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public void setNumberOfWorkers(Integer numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

}
