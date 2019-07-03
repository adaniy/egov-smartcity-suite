package org.egov.edcr.entity;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.egov.infra.persistence.entity.AbstractAuditable;

/*Used to determine the rules to be validated for a building plan*/
@Entity
@Table(name = "EDCR_PLANRULE")
@SequenceGenerator(name = PlanRule.SEQ_EDCR_PLANRULE, sequenceName = PlanRule.SEQ_EDCR_PLANRULE, allocationSize = 1)
public class PlanRule extends AbstractAuditable {

    public static final String SEQ_EDCR_PLANRULE = "SEQ_EDCR_PLANRULE";
    private static final long serialVersionUID = 6649463259841178605L;

    @Id
    @GeneratedValue(generator = SEQ_EDCR_PLANRULE, strategy = GenerationType.SEQUENCE)
    private Long id;

    private String service;

    private BigDecimal minPlotArea;

    private BigDecimal maxPlotArea;

    private String occupancy;

    private Double minFloors;

    private Double maxFloors;

    private BigDecimal minBuildingHgt;

    private BigDecimal maxBuildingHgt;

    private String rules;

    private String familySize;

    private String abutingRoad;

    private BigDecimal minRoad;

    private BigDecimal maxRoad;

    @NotNull
    private Boolean active;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public BigDecimal getMinPlotArea() {
        return minPlotArea;
    }

    public void setMinPlotArea(BigDecimal minPlotArea) {
        this.minPlotArea = minPlotArea;
    }

    public BigDecimal getMaxPlotArea() {
        return maxPlotArea;
    }

    public void setMaxPlotArea(BigDecimal maxPlotArea) {
        this.maxPlotArea = maxPlotArea;
    }

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    public Double getMinFloors() {
        return minFloors;
    }

    public void setMinFloors(Double minFloors) {
        this.minFloors = minFloors;
    }

    public Double getMaxFloors() {
        return maxFloors;
    }

    public void setMaxFloors(Double maxFloors) {
        this.maxFloors = maxFloors;
    }

    public BigDecimal getMinBuildingHgt() {
        return minBuildingHgt;
    }

    public void setMinBuildingHgt(BigDecimal minBuildingHgt) {
        this.minBuildingHgt = minBuildingHgt;
    }

    public BigDecimal getMaxBuildingHgt() {
        return maxBuildingHgt;
    }

    public void setMaxBuildingHgt(BigDecimal maxBuildingHgt) {
        this.maxBuildingHgt = maxBuildingHgt;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getFamilySize() {
        return familySize;
    }

    public void setFamilySize(String familySize) {
        this.familySize = familySize;
    }

    public String getAbutingRoad() {
        return abutingRoad;
    }

    public void setAbutingRoad(String abutingRoad) {
        this.abutingRoad = abutingRoad;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BigDecimal getMinRoad() {
        return minRoad;
    }

    public void setMinRoad(BigDecimal minRoad) {
        this.minRoad = minRoad;
    }

    public BigDecimal getMaxRoad() {
        return maxRoad;
    }

    public void setMaxRoad(BigDecimal maxRoad) {
        this.maxRoad = maxRoad;
    }
}
