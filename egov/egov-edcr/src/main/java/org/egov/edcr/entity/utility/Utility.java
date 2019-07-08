package org.egov.edcr.entity.utility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.BiometricWasteTreatment;
import org.egov.edcr.entity.RoadOutput;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.WasteDisposal;

public class Utility extends Measurement {
    private static final long serialVersionUID = -5119633549293595758L;
    private List<WasteDisposal> wasteDisposalUnits = new ArrayList<>();
    private List<WasteWaterRecyclePlant> wasteWaterRecyclePlant = new ArrayList<>();
    private List<LiquidWasteTreatementPlant> liquidWasteTreatementPlant = new ArrayList<>();
    private List<WellUtility> wells = new ArrayList<>();
    private List<RoadOutput> wellDistance = new ArrayList<>();
    private List<RainWaterHarvesting> rainWaterHarvest = new ArrayList<>();
    private List<Solar> solar = new ArrayList<>();
    private BigDecimal rainWaterHarvestingTankCapacity;
    private List<BiometricWasteTreatment> biometricWasteTreatment = new ArrayList<>();
    private List<SolidLiqdWasteTrtmnt> solidLiqdWasteTrtmnt = new ArrayList<>();

    public void setBiometricWasteTreatment(List<BiometricWasteTreatment> biometricWasteTreatment) {
        this.biometricWasteTreatment = biometricWasteTreatment;
    }

    public List<BiometricWasteTreatment> getBiometricWasteTreatment() {

        return biometricWasteTreatment;
    }

    public void addBiometricWasteTreatment(BiometricWasteTreatment biometricWasteTrtmnt) {
        biometricWasteTreatment.add(biometricWasteTrtmnt);
    }

    public BigDecimal getRainWaterHarvestingTankCapacity() {
        return rainWaterHarvestingTankCapacity;
    }

    public void setRainWaterHarvestingTankCapacity(BigDecimal rainWaterHarvestingTankCapacity) {
        this.rainWaterHarvestingTankCapacity = rainWaterHarvestingTankCapacity;
    }

    public List<WasteDisposal> getWasteDisposalUnits() {
        return wasteDisposalUnits;
    }

    public List<LiquidWasteTreatementPlant> getLiquidWasteTreatementPlant() {
        return liquidWasteTreatementPlant;
    }

    public void addLiquidWasteTreatementPlant(LiquidWasteTreatementPlant lqWastTrtPlant) {
        liquidWasteTreatementPlant.add(lqWastTrtPlant);

    }

    public void addWasteDisposal(WasteDisposal wasteDisposal) {
        wasteDisposalUnits.add(wasteDisposal);
    }

    public void addWasteWaterRecyclePlant(WasteWaterRecyclePlant waterRecyclePlant) {
        wasteWaterRecyclePlant.add(waterRecyclePlant);

    }

    public List<WasteWaterRecyclePlant> getWasteWaterRecyclePlant() {
        return wasteWaterRecyclePlant;
    }

    public void addWells(WellUtility wellUtility) {
        wells.add(wellUtility);

    }

    public List<WellUtility> getWells() {
        return wells;
    }

    public List<RoadOutput> getWellDistance() {
        return wellDistance;
    }

    public void setWellDistance(List<RoadOutput> wellDistance) {
        this.wellDistance = wellDistance;
    }

    public void addSolar(Solar solarsystem) {
        solar.add(solarsystem);

    }

    public List<Solar> getSolar() {
        return solar;
    }

    public List<RainWaterHarvesting> getRainWaterHarvest() {
        return rainWaterHarvest;
    }

    public void addRainWaterHarvest(RainWaterHarvesting rwh) {
        rainWaterHarvest.add(rwh);

    }

    public List<SolidLiqdWasteTrtmnt> getSolidLiqdWasteTrtmnt() {
        return solidLiqdWasteTrtmnt;
    }

    public void addSolidLiqdWasteTrtmnt(SolidLiqdWasteTrtmnt solidLiqdWasteTrtmnt) {
        this.solidLiqdWasteTrtmnt.add(solidLiqdWasteTrtmnt);
    }
}
