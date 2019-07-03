package org.egov.edcr.feature;

import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_A1;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_A2;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_A3;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_A4;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_A5;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_B1;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_B2;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_B3;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_C;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_C1;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_C2;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_C3;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_D;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_D1;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_D2;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_E;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_F;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_F1;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_F2;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_F3;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_F4;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_G1;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_G2;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_H;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_I1;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_I2;
import static org.egov.edcr.utility.DcrConstants.BSMT_REAR_YARD_DESC;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.NA;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.REAR_YARD_DESC;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.YES;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Plot;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.utility.SetBack;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.springframework.stereotype.Service;

@Service
public class RearYardService extends GeneralRule {
    private static final double VALUE_0_5 = 0.5;
    private static final String LEVEL = " Level ";
    private static final String RULE_24_4 = "24(4)";
    private static final String RULE_54_3_II = "54-(3-ii)";

    private static final String RULE_55_2_1 = "55-2-(1)";
    private static final String RULE_55_2_2 = "55-2-(2)";
    private static final String RULE_55_2_PROV = "55-2(Prov)";
    private static final String RULE_55_2_3 = "55-2-(3)";
    private static final String RULE563D = "56-(3d)";
    private static final String RULE_57_4 = "57-(4)";
    private static final String RULE_59_3 = "59-(3)";
    private static final String RULE_62_1_A = "62-(1-a)";
    private static final String RULE_62_3 = "62-(3)";
    private static final String RULE_59_11 = "59-(11)";

    private static final String SUB_RULE_24_4 = "24(4)";
    private static final String SUB_RULE_24_5 = "24(5)";
    private static final String SUB_RULE_24_12 = "24(12)";
    private static final String SUB_RULE_24_12_DESCRIPTION = "Basement rear yard distance";

    private static final String SUB_RULE_24_4_DESCRIPTION = "Rear yard distance";
    private static final String MEANMINIMUMLABEL = "(Minimum distance,Mean distance) ";
    private static final String MINIMUMLABEL = "Minimum distance ";

    private static final BigDecimal FIVE = BigDecimal.valueOf(5);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE_0 = BigDecimal.valueOf(0);
    private static final BigDecimal REARYARDMINIMUM_DISTANCE_1_5 = BigDecimal.valueOf(1.5);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE_1 = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMINIMUM_DISTANCE_FIFTY_CM = BigDecimal.valueOf(0.50);
    private static final BigDecimal REARYARDMINIMUM_DISTANCE_SEVENTYFIVE_CM = BigDecimal.valueOf(0.75);
    private static final BigDecimal REARYARDMINIMUM_DISTANCE_3 = BigDecimal.valueOf(3);
    private static final BigDecimal REARYARDMINIMUM_DISTANCE_5 = FIVE;
    private static final BigDecimal REARYARDMINIMUM_DISTANCE_7_5 = BigDecimal.valueOf(7.5);

    private static final BigDecimal REARYARDMEAN_DISTANCE_0 = BigDecimal.valueOf(0);
    private static final BigDecimal REARYARDMEAN_DISTANCE_1 = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMEAN_DISTANCE_1_5 = BigDecimal.valueOf(1.5);
    private static final BigDecimal REARYARDMEAN_DISTANCE_SEVENTYFIVE_CM = BigDecimal.valueOf(0.75);
    private static final BigDecimal REARYARDMEAN_DISTANCE_2 = BigDecimal.valueOf(2);
    private static final BigDecimal REARYARDMEAN_DISTANCE_3 = BigDecimal.valueOf(3);
    private static final BigDecimal REARYARDMEAN_DISTANCE_5 = BigDecimal.valueOf(5);
    private static final BigDecimal REARYARDMEAN_DISTANCE_7_5 = BigDecimal.valueOf(7.5);
    private static final BigDecimal ONEHUNDREDFIFTY = BigDecimal.valueOf(150);
    private static final BigDecimal THREEHUNDRED = BigDecimal.valueOf(300);

    private static final int SITEAREA_125 = 125;
    private static final int BUILDUPAREA_300 = 300;
    private static final int BUILDUPAREA_150 = 150;

    private static final int FLOORAREA_800 = 800;
    private static final int FLOORAREA_500 = 500;
    private static final int FLOORAREA_300 = 300;

    private class RearYardResult {
        String rule;
        String subRule;
        Integer level;
        BigDecimal actualMeanDistance = BigDecimal.ZERO;
        BigDecimal actualMinDistance = BigDecimal.ZERO;
        String occupancy;
        BigDecimal expectedminimumDistance = BigDecimal.ZERO;
        BigDecimal expectedmeanDistance = BigDecimal.ZERO;
        boolean status = false;
    }

    public void processRearYard(final PlanDetail planDetail) {

        final Plot plot = planDetail.getPlot();
        if (plot == null)
            return;

        validateRearYard(planDetail);

        if (plot != null && !planDetail.getBlocks().isEmpty())
            for (Block block : planDetail.getBlocks()) {  // for each block

                scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, LEVEL);
                scrutinyDetail.addColumnHeading(3, OCCUPANCY);
                scrutinyDetail.addColumnHeading(4, FIELDVERIFIED);
                scrutinyDetail.addColumnHeading(5, REQUIRED);
                scrutinyDetail.addColumnHeading(6, PROVIDED);
                scrutinyDetail.addColumnHeading(7, STATUS);
                RearYardResult rearYardResult = new RearYardResult();

                for (SetBack setback : block.getSetBacks()) {
                    BigDecimal min;
                    BigDecimal mean;

                    if (setback.getRearYard() != null
                            && setback.getRearYard().getMean().compareTo(BigDecimal.ZERO) > 0) {
                        min = setback.getRearYard().getMinimumDistance();
                        mean = setback.getRearYard().getMean();

                        // if height defined at rear yard level, then use elase use buidling height.
                        BigDecimal buildingHeight = setback.getRearYard().getHeight() != null
                                && setback.getRearYard().getHeight().compareTo(BigDecimal.ZERO) > 0
                                        ? setback.getRearYard().getHeight()
                                        : block.getBuilding().getBuildingHeight();

                        if (buildingHeight != null && (min.doubleValue() > 0 || mean.doubleValue() > 0)) {
                            List<Occupancy> occupanciesList = groupOccupanciesForOccupancy_C_D(block);

                            for (final Occupancy occupancy : occupanciesList) {
                                OccupancyType occpncy = occupancy.getType();

                                if (occupancy.getBuiltUpArea() != null && OCCUPANCY_D.equals(occupancy.getType())
                                        && occupancy.getBuiltUpArea().compareTo(ONEHUNDREDFIFTY) <= 0)
                                    occpncy = OCCUPANCY_F;
                                else if (OCCUPANCY_C.equals(occupancy.getType()) && occupancy.getBuiltUpArea() != null &&
                                        occupancy.getBuiltUpArea().compareTo(ONEHUNDREDFIFTY) <= 0)
                                    occpncy = OCCUPANCY_F;
                                else if (OCCUPANCY_H.equals(occupancy.getType()))
                                    if (occupancy.getBuiltUpArea() != null
                                            && occupancy.getBuiltUpArea().compareTo(THREEHUNDRED) <= 0)
                                        occpncy = OCCUPANCY_F;
                                    else
                                        occpncy = OCCUPANCY_H;
                                scrutinyDetail.setKey("Block_" + block.getName() + "_" + "Rear Yard");

                                if (-1 == setback.getLevel()) {
                                    scrutinyDetail.setKey("Block_" + block.getName() + "_" + "Basement Rear Yard");

                                    checkRearYardLessThanTenMts(planDetail, block.getBuilding(), block, setback.getLevel(), plot,
                                            BSMT_REAR_YARD_DESC, min, mean,
                                            occpncy, rearYardResult, BigDecimal.valueOf(10));

                                } else if (buildingHeight.compareTo(BigDecimal.valueOf(10)) <= 0)
                                    checkRearYardLessThanTenMts(planDetail, block.getBuilding(), block, setback.getLevel(), plot,
                                            REAR_YARD_DESC, min, mean,
                                            occpncy, rearYardResult, buildingHeight);
                                else if (buildingHeight.compareTo(BigDecimal.valueOf(10)) > 0
                                        && buildingHeight.compareTo(BigDecimal.valueOf(16)) <= 0)
                                    checkRearYardBetweenTenToSixteenMts(setback, block.getBuilding(), planDetail, block,
                                            setback.getLevel(), plot, REAR_YARD_DESC,
                                            SUB_RULE_24_4_DESCRIPTION,
                                            min, mean,
                                            occpncy, rearYardResult);
                                else if (buildingHeight.compareTo(BigDecimal.valueOf(16)) > 0)
                                    checkRearYardMoreThanSixteenMts(setback, block.getBuilding(),
                                            planDetail, block, setback.getLevel(), plot, REAR_YARD_DESC, min,
                                            mean,
                                            occpncy, rearYardResult);

                            }
                            Map<String, String> details = new HashMap<>();
                            details.put(RULE_NO, rearYardResult.subRule);
                            details.put(LEVEL, rearYardResult.level != null ? rearYardResult.level.toString() : "");
                            details.put(OCCUPANCY, rearYardResult.occupancy);
                            if (rearYardResult.expectedmeanDistance != null &&
                                    rearYardResult.expectedmeanDistance.compareTo(BigDecimal.valueOf(0)) == 0) {
                                details.put(FIELDVERIFIED, MINIMUMLABEL);
                                details.put(REQUIRED, rearYardResult.expectedminimumDistance.toString());
                                details.put(PROVIDED, rearYardResult.actualMinDistance.toString());

                            } else {
                                details.put(FIELDVERIFIED, MEANMINIMUMLABEL);
                                details.put(REQUIRED, "(" + rearYardResult.expectedminimumDistance + ","
                                        + rearYardResult.expectedmeanDistance + ")");
                                details.put(PROVIDED,
                                        "(" + rearYardResult.actualMinDistance + "," + rearYardResult.actualMeanDistance + ")");
                            }
                            if (rearYardResult.status)
                                details.put(STATUS, Result.Accepted.getResultVal());
                            else
                                details.put(STATUS, Result.Not_Accepted.getResultVal());
                            scrutinyDetail.getDetail().add(details);
                            planDetail.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

                        }
                    }
                }
            }

    }

    private List<Occupancy> groupOccupanciesForOccupancy_C_D(Block block) {
        List<Occupancy> occupanciesList = new ArrayList<>();
        BigDecimal totalFloorArea_C = BigDecimal.ZERO;
        BigDecimal totalBuiltUpArea_C = BigDecimal.ZERO;
        BigDecimal totalExistingFloorArea_C = BigDecimal.ZERO;
        BigDecimal totalExistingBuiltUpArea_C = BigDecimal.ZERO;

        BigDecimal totalFloorArea_D = BigDecimal.ZERO;
        BigDecimal totalBuiltUpArea_D = BigDecimal.ZERO;
        BigDecimal totalExistingFloorArea_D = BigDecimal.ZERO;
        BigDecimal totalExistingBuiltUpArea_D = BigDecimal.ZERO;

        for (Occupancy occupancy : block.getBuilding().getTotalArea())
            if (occupancy.getType().equals(OccupancyType.OCCUPANCY_C) ||
                    occupancy.getType().equals(OccupancyType.OCCUPANCY_C1) ||
                    occupancy.getType().equals(OccupancyType.OCCUPANCY_C2) ||
                    occupancy.getType().equals(OccupancyType.OCCUPANCY_C3)) {
                totalFloorArea_C = totalFloorArea_C.add(occupancy.getFloorArea());
                totalBuiltUpArea_C = totalBuiltUpArea_C
                        .add(occupancy.getBuiltUpArea() == null ? BigDecimal.valueOf(0) : occupancy.getBuiltUpArea());
                totalExistingFloorArea_C = totalExistingFloorArea_C.add(occupancy.getExistingFloorArea());
                totalExistingBuiltUpArea_C = totalExistingBuiltUpArea_C.add(
                        occupancy.getExistingBuiltUpArea() == null ? BigDecimal.valueOf(0) : occupancy.getExistingBuiltUpArea());
            } else if (occupancy.getType().equals(OccupancyType.OCCUPANCY_D) ||
                    occupancy.getType().equals(OccupancyType.OCCUPANCY_D1) ||
                    occupancy.getType().equals(OccupancyType.OCCUPANCY_D2)) {
                totalFloorArea_D = totalFloorArea_D.add(occupancy.getFloorArea());
                totalBuiltUpArea_D = totalBuiltUpArea_D
                        .add(occupancy.getBuiltUpArea() == null ? BigDecimal.valueOf(0) : occupancy.getBuiltUpArea());
                totalExistingFloorArea_D = totalExistingFloorArea_D.add(occupancy.getExistingFloorArea());
                totalExistingBuiltUpArea_D = totalExistingBuiltUpArea_D.add(
                        occupancy.getExistingBuiltUpArea() == null ? BigDecimal.valueOf(0) : occupancy.getExistingBuiltUpArea());
            } else
                occupanciesList.add(occupancy);
        if (totalFloorArea_C.compareTo(BigDecimal.ZERO) > 0 && totalBuiltUpArea_C.compareTo(BigDecimal.ZERO) > 0) {
            Occupancy occupancy = new Occupancy();
            occupancy.setFloorArea(totalFloorArea_C);
            occupancy.setCarpetArea(totalFloorArea_C.multiply(BigDecimal.valueOf(0.80)));
            occupancy.setType(OccupancyType.OCCUPANCY_C);
            occupancy.setBuiltUpArea(totalBuiltUpArea_C);
            occupancy.setExistingBuiltUpArea(totalExistingBuiltUpArea_C);
            occupancy.setExistingFloorArea(totalExistingFloorArea_C);
            occupancy.setExistingCarpetArea(totalExistingFloorArea_C.multiply(BigDecimal.valueOf(0.80)));
            occupanciesList.add(occupancy);
        }
        if (totalFloorArea_D.compareTo(BigDecimal.ZERO) > 0 && totalBuiltUpArea_D.compareTo(BigDecimal.ZERO) > 0) {
            Occupancy occupancy = new Occupancy();
            occupancy.setFloorArea(totalFloorArea_D);
            occupancy.setCarpetArea(totalFloorArea_D.multiply(BigDecimal.valueOf(0.80)));
            occupancy.setType(OccupancyType.OCCUPANCY_D);
            occupancy.setBuiltUpArea(totalBuiltUpArea_D);
            occupancy.setExistingBuiltUpArea(totalExistingBuiltUpArea_D);
            occupancy.setExistingFloorArea(totalExistingFloorArea_D);
            occupancy.setExistingCarpetArea(totalExistingFloorArea_D.multiply(BigDecimal.valueOf(0.80)));
            occupanciesList.add(occupancy);
        }
        return occupanciesList;
    }

    private Boolean checkRearYardMoreThanSixteenMts(SetBack setback, Building building, final PlanDetail planDetail, Block block,
            Integer level, final Plot plot, final String rearYardFieldName,
            final BigDecimal min, final BigDecimal mean, final OccupancyType mostRestrictiveOccupancy,
            RearYardResult rearYardResult) {
        Boolean valid = false;
        final String subRuleDesc = SUB_RULE_24_4_DESCRIPTION;
        BigDecimal buildingHeight = setback.getRearYard().getHeight() != null
                && setback.getRearYard().getHeight().compareTo(BigDecimal.ZERO) > 0 ? setback.getRearYard().getHeight()
                        : building.getBuildingHeight();

        if (mostRestrictiveOccupancy.equals(OCCUPANCY_A1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A4) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A5) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_F) || mostRestrictiveOccupancy.equals(OCCUPANCY_F3))
            processRearYardForOccupancyA1A2FHightGtThanTenMtrs(setback, buildingHeight, planDetail, mostRestrictiveOccupancy,
                    block, level, plot, rearYardFieldName, subRuleDesc, false, rearYardResult);
        else {

            final BigDecimal distanceIncrementBasedOnHeight = BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal
                            .valueOf(Math.ceil(buildingHeight.subtract(BigDecimal.TEN)
                                    .divide(BigDecimal.valueOf(3), DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS)
                                    .doubleValue())));

            valid = processRearYardForOccupanciesOtherThanA1A2F(planDetail, building, block, level, plot, rearYardFieldName,
                    subRuleDesc, min, mean,
                    mostRestrictiveOccupancy, distanceIncrementBasedOnHeight, false, rearYardResult, buildingHeight);

        }
        return valid;
    }

    private Boolean checkRearYardLessThanTenMts(final PlanDetail planDetail, Building building, Block block, Integer level,
            final Plot plot, final String rearYardFieldName,
            final BigDecimal min, final BigDecimal mean, final OccupancyType mostRestrictiveOccupancy,
            RearYardResult rearYardResult, BigDecimal buildingHeight) {
        String subRule = SUB_RULE_24_4;
        String rule = REAR_YARD_DESC;
        String subRuleDesc = SUB_RULE_24_4_DESCRIPTION;
        Boolean valid = false;
        BigDecimal minVal = BigDecimal.valueOf(0);
        BigDecimal meanVal = BigDecimal.valueOf(0);

        if (mostRestrictiveOccupancy.equals(OCCUPANCY_A1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_F3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A4) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A5) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A3)) {
            if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {

                if (building.getFloorsAboveGround().compareTo(BigDecimal.valueOf(3)) <= 0) {
                    // rule = RULE62;
                    subRule = RULE_62_3;
                    minVal = REARYARDMINIMUM_DISTANCE_FIFTY_CM;
                    meanVal = REARYARDMEAN_DISTANCE_1;

                } else if (planDetail.getPlanInformation().getOpeningOnRearBelow2mtsDesc().equalsIgnoreCase(YES) ||
                        planDetail.getPlanInformation().getOpeningOnRearBelow2mtsDesc().equalsIgnoreCase(NA)) {
                    minVal = REARYARDMINIMUM_DISTANCE_1;
                    meanVal = REARYARDMEAN_DISTANCE_2;

                } else if (planDetail.getPlanInformation().getOpeningOnRearAbove2mtsDesc().equalsIgnoreCase(YES) ||
                        planDetail.getPlanInformation().getOpeningOnRearAbove2mtsDesc().equalsIgnoreCase(NA)) {
                    minVal = REARYARDMINIMUM_DISTANCE_SEVENTYFIVE_CM;
                    meanVal = REARYARDMEAN_DISTANCE_SEVENTYFIVE_CM;
                } else if (planDetail.getPlanInformation().getNocToAbutRearDesc().equalsIgnoreCase(YES)) {
                    minVal = REARYARDMINIMUM_DISTANCE_0;
                    meanVal = REARYARDMEAN_DISTANCE_0;

                } else {
                    minVal = REARYARDMINIMUM_DISTANCE_SEVENTYFIVE_CM;
                    meanVal = REARYARDMEAN_DISTANCE_SEVENTYFIVE_CM;

                }

            } else {
                subRule = SUB_RULE_24_4;
                // rule = DcrConstants.RULE24;
                // Plot area greater than 125 mts

                if (buildingHeight.compareTo(BigDecimal.valueOf(7)) <= 0) {

                    if (planDetail.getPlanInformation().getOpeningOnRearBelow2mtsDesc().equalsIgnoreCase(YES) ||
                            planDetail.getPlanInformation().getOpeningOnRearBelow2mtsDesc().equalsIgnoreCase(NA)) {
                        minVal = REARYARDMINIMUM_DISTANCE_1;
                        meanVal = REARYARDMEAN_DISTANCE_1_5;

                    } else if (planDetail.getPlanInformation().getOpeningOnRearAbove2mtsDesc().equalsIgnoreCase(DcrConstants.YES)
                            ||
                            planDetail.getPlanInformation().getOpeningOnRearAbove2mtsDesc().equalsIgnoreCase(DcrConstants.NA)) {
                        minVal = REARYARDMINIMUM_DISTANCE_SEVENTYFIVE_CM;
                        meanVal = REARYARDMEAN_DISTANCE_SEVENTYFIVE_CM;

                    } else if (planDetail.getPlanInformation().getNocToAbutRearDesc().equalsIgnoreCase(DcrConstants.YES)) {
                        minVal = REARYARDMINIMUM_DISTANCE_0;
                        meanVal = REARYARDMEAN_DISTANCE_0;

                    } else {
                        minVal = REARYARDMINIMUM_DISTANCE_SEVENTYFIVE_CM;
                        meanVal = REARYARDMEAN_DISTANCE_SEVENTYFIVE_CM;
                    }

                } else {
                    minVal = REARYARDMINIMUM_DISTANCE_1;
                    meanVal = REARYARDMEAN_DISTANCE_2;

                }
            }
            valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
            if (-1 == level) {
                subRule = SUB_RULE_24_12;
                rule = BSMT_REAR_YARD_DESC;
                subRuleDesc = SUB_RULE_24_12_DESCRIPTION;
            }

            compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule, rule,
                    minVal,
                    meanVal, level);

        } else
            valid = processRearYardForOccupanciesOtherThanA1A2F(planDetail, building, block, level, plot, rearYardFieldName,
                    subRuleDesc, min, mean,
                    mostRestrictiveOccupancy, BigDecimal.ZERO, false, rearYardResult, buildingHeight);
        return valid;
    }

    private void compareRearYardResult(String blockName, BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy,
            RearYardResult rearYardResult, Boolean valid, String subRule, String rule, BigDecimal minVal, BigDecimal meanVal,
            Integer level) {
        if (minVal.compareTo(rearYardResult.expectedminimumDistance) >= 0) {
            if (minVal.compareTo(rearYardResult.expectedminimumDistance) == 0) {
                rearYardResult.rule = rearYardResult.rule != null ? rearYardResult.rule + "," + rule : rule;
                rearYardResult.occupancy = rearYardResult.occupancy != null
                        ? rearYardResult.occupancy + "," + mostRestrictiveOccupancy.getOccupancyTypeVal()
                        : mostRestrictiveOccupancy.getOccupancyTypeVal();

                if (meanVal.compareTo(rearYardResult.expectedmeanDistance) >= 0) {
                    rearYardResult.expectedmeanDistance = meanVal;
                    rearYardResult.actualMeanDistance = mean;
                }
            } else {
                rearYardResult.rule = rule;
                rearYardResult.occupancy = mostRestrictiveOccupancy.getOccupancyTypeVal();
                rearYardResult.expectedmeanDistance = meanVal;
                rearYardResult.actualMeanDistance = mean;

            }

            rearYardResult.subRule = subRule;
            rearYardResult.level = level;
            rearYardResult.expectedminimumDistance = minVal;
            rearYardResult.actualMinDistance = min;
            rearYardResult.status = valid;

        }
    }

    private Boolean checkRearYardBetweenTenToSixteenMts(SetBack setback, Building building, final PlanDetail planDetail,
            Block block, Integer level, final Plot plot,
            final String rearYardFieldName,
            final String subRuleDesc, final BigDecimal min, final BigDecimal mean, final OccupancyType mostRestrictiveOccupancy,
            RearYardResult rearYardResult) {
        Boolean valid = false;
        BigDecimal buildingHeight = setback.getRearYard().getHeight() != null
                && setback.getRearYard().getHeight().compareTo(BigDecimal.ZERO) > 0 ? setback.getRearYard().getHeight()
                        : building.getBuildingHeight();

        if (mostRestrictiveOccupancy.equals(OCCUPANCY_A1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_F3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A4) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A5))
            processRearYardForOccupancyA1A2FHightGtThanTenMtrs(setback, buildingHeight, planDetail, mostRestrictiveOccupancy,
                    block, level, plot, rearYardFieldName, subRuleDesc, false, rearYardResult);
        else {

            final BigDecimal distanceIncrementBasedOnHeight = BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal
                            .valueOf(Math.ceil(buildingHeight.subtract(BigDecimal.TEN)
                                    .divide(BigDecimal.valueOf(3), DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS)
                                    .doubleValue())));
            valid = processRearYardForOccupanciesOtherThanA1A2F(planDetail, building, block, level, plot, rearYardFieldName,
                    subRuleDesc, min, mean,
                    mostRestrictiveOccupancy, distanceIncrementBasedOnHeight, false, rearYardResult, buildingHeight);

        }
        return valid;
    }

    private void processRearYardForOccupancyA1A2FHightGtThanTenMtrs(SetBack setbacks, BigDecimal buildingHeight,
            final PlanDetail planDetail, OccupancyType mostRestrictiveOccupancy, Block block, Integer level, final Plot plot,
            final String rearYardFieldName,
            final String subRuleDesc, final boolean checkMinimumValue, RearYardResult rearYardResult) {
        String subRule = SUB_RULE_24_4;
        String rule = REAR_YARD_DESC;
        BigDecimal minval;
        BigDecimal meanval;
        if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) > 0) {
            // rule = RULE24;
            subRule = SUB_RULE_24_4;
            minval = BigDecimal.valueOf(1);
            meanval = BigDecimal.valueOf(2);

        } else {
            // rule = RULE_62;
            subRule = RULE_62_1_A;
            minval = BigDecimal.valueOf(0.5);
            meanval = BigDecimal.valueOf(1);
        }

        if (mostRestrictiveOccupancy.equals(OCCUPANCY_F) || mostRestrictiveOccupancy.equals(OCCUPANCY_F1)
                || mostRestrictiveOccupancy.equals(OCCUPANCY_F4)
                || mostRestrictiveOccupancy.equals(OCCUPANCY_F2)/* || mostRestrictiveOccupancy.equals(OCCUPANCY_F3) */) {
            minval = BigDecimal.valueOf(1.5);
            meanval = BigDecimal.valueOf(1.5);
        }

        if (buildingHeight.compareTo(BigDecimal.TEN) > 0) {
            BigDecimal minValue = BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal.valueOf(Math.ceil(buildingHeight.subtract(BigDecimal.TEN)
                            .divide(BigDecimal.valueOf(3), DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS).doubleValue())))
                    .add(minval);

            BigDecimal meanValue = BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal.valueOf(Math.ceil(buildingHeight.subtract(BigDecimal.TEN)
                            .divide(BigDecimal.valueOf(3), DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS).doubleValue())))
                    .add(meanval);

            if (checkMinimumValue) {
                minValue = minValue.compareTo(FIVE) <= 0 ? FIVE : minValue;
                meanValue = meanValue.compareTo(FIVE) <= 0 ? FIVE : meanValue;
            }

            if (setbacks.getRearYard().getMinimumDistance().compareTo(minValue) >= 0
                    && setbacks.getRearYard().getMean().compareTo(meanValue) >= 0)
                compareRearYardResult(block.getName(), setbacks.getRearYard().getMinimumDistance(),
                        setbacks.getRearYard().getMean(), mostRestrictiveOccupancy, rearYardResult, true, subRule, rule, minValue,
                        meanValue, level);
            else
                compareRearYardResult(block.getName(), setbacks.getRearYard().getMinimumDistance(),
                        setbacks.getRearYard().getMean(), mostRestrictiveOccupancy, rearYardResult, false, subRule, rule,
                        minValue,
                        meanValue, level);
        }
    }

    private Boolean processRearYardForOccupanciesOtherThanA1A2F(final PlanDetail planDetail, Building building, Block block,
            Integer level, final Plot plot,
            final String rearYardFieldName,
            final String subRuleDesc, final BigDecimal min, final BigDecimal mean, final OccupancyType mostRestrictiveOccupancy,
            final BigDecimal distanceIncrementBasedOnHeight,
            final Boolean checkMinimum5mtsCondition, RearYardResult rearYardResult, BigDecimal buildingHeight) {
        String subRule = SUB_RULE_24_5;
        String rule = REAR_YARD_DESC;
        BigDecimal minVal = BigDecimal.valueOf(0);
        BigDecimal meanVal = BigDecimal.valueOf(0);
        Boolean valid = false;

        if (mostRestrictiveOccupancy.equals(OCCUPANCY_B1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_B3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_B2)) {
            subRule = SUB_RULE_24_5;
            // rule = DcrConstants.RULE24;
            if (buildingHeight.compareTo(BigDecimal.valueOf(7)) <= 0) {
                if (building.getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) <= 0) {
                    if (planDetail.getPlanInformation().getOpeningOnRearBelow2mtsDesc().equalsIgnoreCase(YES)) {
                        minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                        meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1_5);

                    } else if (planDetail.getPlanInformation().getOpeningOnRearAbove2mtsDesc().equalsIgnoreCase(YES)) {

                        minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_SEVENTYFIVE_CM);
                        meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_SEVENTYFIVE_CM);

                    } else if (planDetail.getPlanInformation().getNocToAbutRearDesc().equalsIgnoreCase(YES)) {
                        minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_0);
                        meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_0);
                    } else {
                        minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_SEVENTYFIVE_CM);
                        meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_SEVENTYFIVE_CM);
                    }

                } else if (building.getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) > 0
                        &&
                        building.getTotalBuitUpArea()
                                .compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) <= 0) {
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1_5);
                } else {
                    // rule = RULE_54;
                    subRule = RULE_54_3_II;
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_3);
                }
            } else {
                // rule = RULE_54;
                subRule = RULE_54_3_II;
                if (building.getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) <= 0) {
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_2);
                } else if (building.getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) > 0
                        &&
                        building.getTotalBuitUpArea()
                                .compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) <= 0) {
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_2);
                } else {
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_3);
                }

            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_C)
                || mostRestrictiveOccupancy.equals(OCCUPANCY_C1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_C2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_C3)) {

            if (buildingHeight.compareTo(BigDecimal.valueOf(7)) <= 0) {
                subRule = SUB_RULE_24_5;
                // rule = DcrConstants.RULE24;
                if (building.getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) <= 0) {
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1_5);
                } else if (building.getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) > 0
                        &&
                        building.getTotalBuitUpArea()
                                .compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) <= 0) {
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1_5);
                } else {
                    // rule = RULE_54;
                    subRule = RULE_54_3_II;
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_3);
                }
            } else {
                // rule = RULE_54;
                subRule = RULE_54_3_II;
                if (building.getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) <= 0) {
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1_5);
                } else if (building.getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) > 0
                        &&
                        building.getTotalBuitUpArea()
                                .compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) <= 0) {
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_2);
                } else {
                    minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                    meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_3);
                }

            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_D) || mostRestrictiveOccupancy.equals(OCCUPANCY_D2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_D1)) {
            if (building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) > 0) {
                // rule = RULE_55;
                subRule = RULE_55_2_3;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_3);

            } else if (building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) > 0 &&
                    building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) <= 0) {
                // rule = RULE_55;
                subRule = RULE_55_2_2;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_3);
            } else if (building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_300)) > 0 &&
                    building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) <= 0) {
                // rule = RULE_55;
                subRule = RULE_55_2_1;

                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_2);
            } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
                // rule = RULE_55;
                subRule = RULE_55_2_PROV;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1_5);
            } else {
                // rule = RULE_55;
                subRule = RULE_55_2_PROV;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_2);

            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_E) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_H)) {
            // rule = RULE24;
            subRule = SUB_RULE_24_5;

            if (building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_300)) <= 0) {

                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1_5);
            } else {
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_3);
            }

        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_F) /* || mostRestrictiveOccupancy.equals(OCCUPANCY_F3) */) {
            if (Util.isSmallPlot(planDetail)) { // latest change asked by client on feb22
                subRule = RULE563D;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_FIFTY_CM);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1);
            } else {
                subRule = RULE_24_4;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1_5);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1_5);
            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_F1)) {
            // rule = RULE_56;
            subRule = RULE563D;
            minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_5);
            meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_5);

        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_F2)) {
            if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {
                // rule = RULE_56;
                subRule = RULE563D;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_FIFTY_CM);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1);
            } else {
                // rule = RULE_56;
                subRule = RULE563D;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1);
            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_G1)) {
            if (smallIndustrialBuilding(planDetail, building)) {
                subRule = RULE_24_4;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_2);
            } else {
                // rule = RULE_57;
                subRule = RULE_57_4;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_5);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_5);
            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_G2)) {
            // rule = RULE_57;
            if (smallIndustrialBuilding(planDetail, building)) {
                subRule = RULE_24_4;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_2);
            } else {
                subRule = RULE_57_4;
                minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_3);
                meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_3);
            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_I1)) {
            // rule = RULE_59;
            subRule = RULE_59_3;
            minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_3);
            meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_3);
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_I2)) {
            // rule = RULE_59;
            subRule = RULE_59_3;
            minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_7_5);
            meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_7_5);
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_F4)) {
            subRule = RULE_59_11;
            minVal = distanceIncrementBasedOnHeight.add(REARYARDMINIMUM_DISTANCE_1);
            meanVal = distanceIncrementBasedOnHeight.add(REARYARDMEAN_DISTANCE_1);
        }

        if (checkMinimum5mtsCondition) {
            minVal = minVal.compareTo(FIVE) <= 0 ? FIVE : minVal;
            meanVal = meanVal.compareTo(FIVE) <= 0 ? FIVE : meanVal;
        }

        valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
        if (-1 == level) {
            subRule = SUB_RULE_24_12;
            rule = BSMT_REAR_YARD_DESC;

        }

        compareRearYardResult(block.getName(), min, mean, mostRestrictiveOccupancy, rearYardResult, valid, subRule, rule, minVal,
                meanVal, level);
        return valid;
    }

    private Boolean smallIndustrialBuilding(PlanDetail planDetail, Building building) {
        BigDecimal totalarea = building.getTotalExistingBuiltUpArea() != null
                ? building.getTotalExistingBuiltUpArea().add(building.getTotalBuitUpArea())
                : building.getTotalBuitUpArea();
        if (building.getBuildingHeight().compareTo(BigDecimal.valueOf(10)) < 0
                && planDetail.getPlanInformation().getPowerUsedHp() != null &&
                planDetail.getPlanInformation().getPowerUsedHp() <= 30 &&
                planDetail.getPlanInformation().getNumberOfWorkers() != null &&
                planDetail.getPlanInformation().getNumberOfWorkers() <= 20
                && totalarea.compareTo(BigDecimal.valueOf(200)) < 0)
            return true;
        return false;
    }

    private Boolean validateMinimumAndMeanValue(final BigDecimal min, final BigDecimal mean, final BigDecimal minval,
            final BigDecimal meanval) {
        Boolean valid = false;
        if (min.compareTo(minval) >= 0 && mean.compareTo(meanval) >= 0)
            valid = true;
        return valid;
    }

    private void validateRearYard(final PlanDetail planDetail) {
        for (Block block : planDetail.getBlocks())
            if (!block.getCompletelyExisting()) {
                Boolean rearYardDefined = false;
                for (SetBack setback : block.getSetBacks())
                    if (setback.getRearYard() != null
                            && setback.getRearYard().getMean().compareTo(BigDecimal.valueOf(0)) > 0)
                        rearYardDefined = true;
                if (!rearYardDefined && !planDetail.getPlanInformation().getNocToAbutRearDesc().equalsIgnoreCase(YES)) {
                    HashMap<String, String> errors = new HashMap<>();
                    errors.put(REAR_YARD_DESC,
                            prepareMessage(OBJECTNOTDEFINED, REAR_YARD_DESC + " for Block " + block.getName()));
                    planDetail.addErrors(errors);
                }
            }

    }
}
