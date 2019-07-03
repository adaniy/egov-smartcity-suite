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
import static org.egov.edcr.utility.DcrConstants.BSMT_FRONT_YARD_DESC;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.FOROCCUPANCY;
import static org.egov.edcr.utility.DcrConstants.FRONT_YARD_DESC;
import static org.egov.edcr.utility.DcrConstants.IN_METER;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.OFBLOCK;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;

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
import org.springframework.stereotype.Service;

@Service
public class FrontYardService extends GeneralRule {
    private static final double VALUE_0_5 = 0.5;
    private static final String RULE_54_3_I = "54-(3-i)";
    private static final String RULE_55_2_1 = "55-2-(1)";
    private static final String RULE_55_2_2 = "55-2-(2)";
    private static final String RULE_55_2_PROV = "55-2(Prov)";
    private static final String RULE_55_2_3 = "55-2-(3)";
    private static final String RULE563D = "56(3d)";
    private static final String RULE_57 = "57";
    private static final String RULE_57_4 = "57(4)";
    private static final String RULE_59_3 = "59(3)";
    private static final String RULE_62_1_A = "62-(1-a)";
    private static final String SUB_RULE_24_3 = "24(3)";
    private static final String SUB_RULE_24_12 = "24(12)";
    private static final String RULE_59_11 = "59(11)";
    private static final BigDecimal ONEHUNDREDFIFTY = BigDecimal.valueOf(150);
    private static final BigDecimal THREEHUNDRED = BigDecimal.valueOf(300);

    private static final String SUB_RULE_24_12_DESCRIPTION = "Basement front yard distance";
    private static final String SUB_RULE_24_3_DESCRIPTION = "Front yard distance";
    private static final String MEANMINIMUMLABEL = "(Minimum distance,Mean distance) ";
    private static final BigDecimal FIVE = BigDecimal.valueOf(5);
    private static final BigDecimal THREE = BigDecimal.valueOf(3);

    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1_8 = BigDecimal.valueOf(1.8);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1_2 = BigDecimal.valueOf(1.2);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_3 = BigDecimal.valueOf(3);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_4_5 = BigDecimal.valueOf(4.5);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_5 = FIVE;
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_6 = BigDecimal.valueOf(6);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_7_5 = BigDecimal.valueOf(7.5);

    private static final BigDecimal FRONTYARDMEAN_DISTANCE_1_8 = BigDecimal.valueOf(1.8);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_3 = BigDecimal.valueOf(3);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_5 = FIVE;
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_6 = BigDecimal.valueOf(6);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_7_5 = BigDecimal.valueOf(7.5);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_10_5 = BigDecimal.valueOf(10.5);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1 = BigDecimal.valueOf(1);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_1 = BigDecimal.valueOf(1);

    private static final int SITEAREA_125 = 125;
    private static final int BUILDUPAREA_300 = 300;

    private static final int FLOORAREA_800 = 800;
    private static final int FLOORAREA_500 = 500;
    private static final int FLOORAREA_300 = 300;

    private class FrontYardResult {
        String rule;
        String subRule;
        String blockName;
        Integer level;
        BigDecimal actualMeanDistance = BigDecimal.ZERO;
        BigDecimal actualMinDistance = BigDecimal.ZERO;
        String occupancy;
        BigDecimal expectedminimumDistance = BigDecimal.ZERO;
        BigDecimal expectedmeanDistance = BigDecimal.ZERO;
        boolean status = false;
    }

    public void processFrontYard(PlanDetail planDetail) {
        Plot plot = planDetail.getPlot();
        if (plot == null)
            return;
        // each blockwise, check height , floor area, buildup area. check most restricve based on occupancy and front yard values
        // of occupancies.
        // If floor area less than 150 mt and occupancy type D, then consider as commercial building.
        // In output show blockwise required and provided information.

        validateFrontYard(planDetail);

        if (plot != null && !planDetail.getBlocks().isEmpty())
            for (Block block : planDetail.getBlocks()) {  // for each block

                ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, LEVEL);
                scrutinyDetail.addColumnHeading(3, OCCUPANCY);
                scrutinyDetail.addColumnHeading(4, FIELDVERIFIED);
                scrutinyDetail.addColumnHeading(5, REQUIRED);
                scrutinyDetail.addColumnHeading(6, PROVIDED);
                scrutinyDetail.addColumnHeading(7, STATUS);
                // scrutinyDetail.setRemarks("Front yard description ");
                // scrutinyDetail.setSubHeading("Front yard subheading");
                scrutinyDetail.setHeading(FRONT_YARD_DESC);

                FrontYardResult frontYardResult = new FrontYardResult();

                for (SetBack setback : block.getSetBacks()) {
                    BigDecimal min;
                    BigDecimal mean;
                    // consider height,floor area,buildup area, different occupancies of block
                    // Get occupancies of perticular block and use the same.

                    if (setback.getFrontYard() != null
                            && setback.getFrontYard().getMean().compareTo(BigDecimal.ZERO) > 0) {
                        min = setback.getFrontYard().getMinimumDistance();
                        mean = setback.getFrontYard().getMean();

                        // if height defined at frontyard level, then use elase use buidling height.
                        BigDecimal buildingHeight = setback.getFrontYard().getHeight() != null
                                && setback.getFrontYard().getHeight().compareTo(BigDecimal.ZERO) > 0
                                        ? setback.getFrontYard().getHeight()
                                        : block.getBuilding().getBuildingHeight();

                        if (buildingHeight != null && (min.doubleValue() > 0 || mean.doubleValue() > 0)) {
                            List<Occupancy> occupanciesList = groupOccupanciesForOccupancy_C_D(block);

                            for (final Occupancy occupancy : occupanciesList) {
                                OccupancyType occpncy = occupancy.getType();

                                if (occupancy.getBuiltUpArea() != null
                                        && occupancy.getBuiltUpArea().compareTo(ONEHUNDREDFIFTY) <= 0
                                        && OCCUPANCY_D.equals(occupancy.getType()))
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

                                scrutinyDetail.setKey("Block_" + block.getName() + "_" + FRONT_YARD_DESC);

                                if (-1 == setback.getLevel()) {
                                    scrutinyDetail.setKey("Block_" + block.getName() + "_" + "Basement Front Yard");
                                    checkFrontYardLessThanTenMts(planDetail, block.getBuilding(), block.getName(),
                                            setback.getLevel(), plot, BSMT_FRONT_YARD_DESC, min, mean,
                                            occpncy, frontYardResult);

                                } else if (buildingHeight.compareTo(BigDecimal.valueOf(10)) <= 0)
                                    checkFrontYardLessThanTenMts(planDetail, block.getBuilding(), block.getName(),
                                            setback.getLevel(), plot, FRONT_YARD_DESC, min, mean,
                                            occpncy, frontYardResult);
                                else if (buildingHeight.compareTo(BigDecimal.valueOf(10)) > 0
                                        && buildingHeight.compareTo(BigDecimal.valueOf(16)) <= 0)
                                    checkFrontYardBetweenTenToSixteenMts(setback, block.getBuilding(),
                                            planDetail, setback.getLevel(), block.getName(), plot, FRONT_YARD_DESC,
                                            SUB_RULE_24_3_DESCRIPTION,
                                            min, mean,
                                            occpncy, frontYardResult);
                                else if (buildingHeight.compareTo(BigDecimal.valueOf(16)) > 0)
                                    checkFrontYardMoreThanSixteenMts(setback, block.getBuilding(), buildingHeight,
                                            planDetail, setback.getLevel(), block.getName(), plot, FRONT_YARD_DESC, min,
                                            mean,
                                            occpncy, frontYardResult);

                            }

                            Map<String, String> details = new HashMap<>();
                            details.put(RULE_NO, frontYardResult.subRule);
                            details.put(LEVEL, frontYardResult.level != null ? frontYardResult.level.toString() : "");
                            details.put(OCCUPANCY, frontYardResult.occupancy);
                            details.put(FIELDVERIFIED, MEANMINIMUMLABEL);
                            details.put(REQUIRED, "(" + frontYardResult.expectedminimumDistance + ","
                                    + frontYardResult.expectedmeanDistance + ")");
                            details.put(PROVIDED,
                                    "(" + frontYardResult.actualMinDistance + "," + frontYardResult.actualMeanDistance + ")");

                            if (frontYardResult.status) {
                                details.put(STATUS, Result.Accepted.getResultVal());
                                planDetail.reportOutput
                                        .add(buildRuleOutputWithSubRule(frontYardResult.rule, frontYardResult.subRule,
                                                FRONT_YARD_DESC + OFBLOCK + frontYardResult.blockName + LEVEL
                                                        + frontYardResult.level + FOROCCUPANCY +
                                                        frontYardResult.occupancy,
                                                FRONT_YARD_DESC + OFBLOCK + frontYardResult.blockName + LEVEL
                                                        + frontYardResult.level + FOROCCUPANCY +
                                                        frontYardResult.occupancy,
                                                MEANMINIMUMLABEL + "(" + frontYardResult.expectedminimumDistance + ","
                                                        + frontYardResult.expectedmeanDistance + ")" + IN_METER,
                                                "(" + frontYardResult.actualMinDistance + "," + frontYardResult.actualMeanDistance
                                                        + ")" + IN_METER,
                                                Result.Accepted, null));
                            } else {
                                details.put(STATUS, Result.Not_Accepted.getResultVal());
                                planDetail.reportOutput
                                        .add(buildRuleOutputWithSubRule(frontYardResult.rule, frontYardResult.subRule,
                                                FRONT_YARD_DESC + OFBLOCK + frontYardResult.blockName + LEVEL
                                                        + frontYardResult.level + FOROCCUPANCY +
                                                        frontYardResult.occupancy,
                                                FRONT_YARD_DESC + OFBLOCK + frontYardResult.blockName + LEVEL
                                                        + frontYardResult.level + FOROCCUPANCY +
                                                        frontYardResult.occupancy,
                                                MEANMINIMUMLABEL + "(" + frontYardResult.expectedminimumDistance + ","
                                                        + frontYardResult.expectedmeanDistance + ")" + IN_METER,
                                                "(" + frontYardResult.actualMinDistance + "," + frontYardResult.actualMeanDistance
                                                        + ")" + IN_METER,
                                                Result.Not_Accepted, null));
                            }
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

    private void validateFrontYard(PlanDetail planDetail) {

        // Front yard may not be mandatory at each level. We can check whether in any level front yard defined or not ?

        for (Block block : planDetail.getBlocks())
            if (!block.getCompletelyExisting()) {
                Boolean frontYardDefined = false;
                for (SetBack setback : block.getSetBacks())
                    if (setback.getFrontYard() != null
                            && setback.getFrontYard().getMean().compareTo(BigDecimal.valueOf(0)) > 0)
                        frontYardDefined = true;
                if (!frontYardDefined) {
                    HashMap<String, String> errors = new HashMap<>();
                    errors.put(FRONT_YARD_DESC,
                            prepareMessage(OBJECTNOTDEFINED, FRONT_YARD_DESC + " for Block " + block.getName()));
                    planDetail.addErrors(errors);
                }
            }

    }

    private Boolean checkFrontYardMoreThanSixteenMts(SetBack setback, Building building, BigDecimal blockBuildingHeight,
            PlanDetail planDetail, Integer level,
            String blockName, Plot plot, String frontYardFieldName,
            BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy, FrontYardResult frontYardResult) {
        Boolean valid = false;
        BigDecimal buildingHeight = setback.getFrontYard().getHeight() != null ? setback.getFrontYard().getHeight()
                : blockBuildingHeight;

        String subRuleDesc = SUB_RULE_24_3_DESCRIPTION;
        if (mostRestrictiveOccupancy.equals(OCCUPANCY_A1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A4) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A5) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_F) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_F3))
            processFrontYardForOccupancyA1A2FWithHightGtThanTenMtrs(setback, buildingHeight, planDetail, mostRestrictiveOccupancy,
                    blockName, level, plot, frontYardFieldName,
                    subRuleDesc, true, frontYardResult);
        else {

            BigDecimal distanceIncrementBasedOnHeight = BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal
                            .valueOf(Math.ceil(buildingHeight.subtract(BigDecimal.TEN)
                                    .divide(THREE, DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS).doubleValue())));

            valid = processFrontYardForOccupanciesOtherThanA1A2F(planDetail, building, blockName, level, plot, frontYardFieldName,
                    subRuleDesc, min, mean,
                    mostRestrictiveOccupancy, distanceIncrementBasedOnHeight, true, frontYardResult);

        }
        return valid;
    }

    private Boolean checkFrontYardLessThanTenMts(PlanDetail planDetail, Building building, String blockName, Integer level,
            Plot plot, String frontYardFieldName,
            BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy, FrontYardResult frontYardResult) {
        Boolean valid = false;
        String subRule = SUB_RULE_24_3;
        String rule = FRONT_YARD_DESC;
        String subRuleDesc = SUB_RULE_24_3_DESCRIPTION;
        BigDecimal minVal;
        BigDecimal meanVal;
        if (mostRestrictiveOccupancy.equals(OCCUPANCY_A1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A4) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A5) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_F) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_F3)) {
            if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {
                // rule = RULE_62;
                subRule = RULE_62_1_A;
                minVal = FRONTYARDMINIMUM_DISTANCE_1_2;
                meanVal = FRONTYARDMEAN_DISTANCE_1_8;

                valid = validateMinimumAndMeanValue(valid, min, mean, minVal, meanVal);

            } else {
                // rule = RULE24;
                subRule = SUB_RULE_24_3;
                minVal = FRONTYARDMINIMUM_DISTANCE_1_8;
                meanVal = FRONTYARDMEAN_DISTANCE_3;
                valid = validateMinimumAndMeanValue(valid, min, mean, minVal, meanVal);

            }
            if (-1 == level) {
                rule = BSMT_FRONT_YARD_DESC;
                subRuleDesc = SUB_RULE_24_12_DESCRIPTION;
                subRule = SUB_RULE_24_12;
            }

            compareFrontYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule, rule, minVal,
                    meanVal, level);

        } else
            valid = processFrontYardForOccupanciesOtherThanA1A2F(planDetail, building, blockName, level, plot, frontYardFieldName,
                    subRuleDesc, min, mean,
                    mostRestrictiveOccupancy, BigDecimal.ZERO, false, frontYardResult);
        return valid;
    }

    private void compareFrontYardResult(String blockName, BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy,
            FrontYardResult frontYardResult, Boolean valid, String subRule, String rule, BigDecimal minVal, BigDecimal meanVal,
            Integer level) {
        if (minVal.compareTo(frontYardResult.expectedminimumDistance) >= 0) {
            if (minVal.compareTo(frontYardResult.expectedminimumDistance) == 0) {
                frontYardResult.rule = frontYardResult.rule != null ? frontYardResult.rule + "," + rule : rule;
                frontYardResult.occupancy = frontYardResult.occupancy != null
                        ? frontYardResult.occupancy + "," + mostRestrictiveOccupancy.getOccupancyTypeVal()
                        : mostRestrictiveOccupancy.getOccupancyTypeVal();
            } else {
                frontYardResult.rule = rule;
                frontYardResult.occupancy = mostRestrictiveOccupancy.getOccupancyTypeVal();
            }

            frontYardResult.subRule = subRule;
            frontYardResult.blockName = blockName;
            frontYardResult.level = level;
            frontYardResult.expectedminimumDistance = minVal;
            frontYardResult.expectedmeanDistance = meanVal;
            frontYardResult.actualMinDistance = min;
            frontYardResult.actualMeanDistance = mean;
            frontYardResult.status = valid;

        }
    }

    private Boolean checkFrontYardBetweenTenToSixteenMts(SetBack setback, Building building, PlanDetail planDetail, Integer level,
            String blockName, Plot plot, String frontYardFieldName,
            String subRuleDesc, BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy,
            FrontYardResult frontYardResult) {
        Boolean valid = false;
        BigDecimal buildingHeight = setback.getFrontYard().getHeight() != null
                && setback.getFrontYard().getHeight().compareTo(BigDecimal.ZERO) > 0 ? setback.getFrontYard().getHeight()
                        : building.getBuildingHeight();

        if (mostRestrictiveOccupancy.equals(OCCUPANCY_A1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A4) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_A5) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_F) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_F3))
            processFrontYardForOccupancyA1A2FWithHightGtThanTenMtrs(setback, buildingHeight, planDetail, mostRestrictiveOccupancy,
                    blockName, level, plot, frontYardFieldName,
                    subRuleDesc, false, frontYardResult);
        else {
            BigDecimal distanceIncrementBasedOnHeight = BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal
                            .valueOf(Math.ceil(buildingHeight.subtract(BigDecimal.TEN)
                                    .divide(THREE, DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS).doubleValue())));

            valid = processFrontYardForOccupanciesOtherThanA1A2F(planDetail, building, blockName, level, plot, frontYardFieldName,
                    subRuleDesc, min, mean,
                    mostRestrictiveOccupancy, distanceIncrementBasedOnHeight, false, frontYardResult);

        }
        return valid;
    }

    private void processFrontYardForOccupancyA1A2FWithHightGtThanTenMtrs(SetBack setback, BigDecimal buildingHeight,
            PlanDetail planDetail, OccupancyType mostRestrictiveOccupancy, String blockName, Integer level, Plot plot,
            String frontYardFieldName,
            String subRuleDesc, boolean checkMinimumValue, FrontYardResult frontYardResult) {
        String subRule = SUB_RULE_24_3;
        String rule = FRONT_YARD_DESC;
        BigDecimal minval;
        BigDecimal meanval;
        if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) > 0) {
            // rule = RULE24;
            subRule = SUB_RULE_24_3;
            minval = FRONTYARDMINIMUM_DISTANCE_1_8;
            meanval = THREE;
        } else {
            // rule = RULE_62;
            subRule = RULE_62_1_A;
            minval = FRONTYARDMINIMUM_DISTANCE_1_2;
            meanval = FRONTYARDMEAN_DISTANCE_1_8;
        }

        if (buildingHeight.compareTo(BigDecimal.TEN) > 0) {
            BigDecimal minValue = BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal.valueOf(Math.ceil(buildingHeight.subtract(BigDecimal.TEN)
                            .divide(THREE, DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS).doubleValue())))
                    .add(minval);

            BigDecimal meanValue = BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal.valueOf(Math.ceil(buildingHeight.subtract(BigDecimal.TEN)
                            .divide(THREE, DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS).doubleValue())))
                    .add(meanval);
            if (checkMinimumValue) {
                minValue = minValue.compareTo(FIVE) <= 0 ? FIVE : minValue;
                meanValue = meanValue.compareTo(FIVE) <= 0 ? FIVE : meanValue;
            }

            if (setback.getFrontYard().getMinimumDistance().compareTo(minValue) >= 0
                    && setback.getFrontYard().getMean().compareTo(meanValue) >= 0)
                compareFrontYardResult(blockName, setback.getFrontYard().getMinimumDistance(), setback.getFrontYard().getMean(),
                        mostRestrictiveOccupancy, frontYardResult, true, subRule, rule, minValue,
                        meanValue, level);
            else
                compareFrontYardResult(blockName, setback.getFrontYard().getMinimumDistance(), setback.getFrontYard().getMean(),
                        mostRestrictiveOccupancy, frontYardResult, false, subRule, rule, minValue,
                        meanValue, level);
        }

    }

    private Boolean processFrontYardForOccupanciesOtherThanA1A2F(PlanDetail planDetail, Building building, String blockName,
            Integer level, Plot plot, String frontYardFieldName,
            String subRuleDesc, BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy,
            BigDecimal distanceIncrementBasedOnHeight,
            Boolean checkMinimum5mtsCondition, FrontYardResult frontYardResult) {
        String subRule = SUB_RULE_24_3;
        String rule = FRONT_YARD_DESC;
        BigDecimal minVal = BigDecimal.valueOf(0);
        BigDecimal meanVal = BigDecimal.valueOf(0);
        Boolean valid = false;
        if (mostRestrictiveOccupancy.equals(OCCUPANCY_B1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_B2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_B3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_C) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_C1) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_C2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_C3) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_E) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_H)) {

            if (building.getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) > 0) {
                // rule = RULE_54;
                subRule = RULE_54_3_I;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_4_5);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_6);
            } else {
                // rule = RULE24;
                subRule = SUB_RULE_24_3;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
            }

        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_D) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_D2) ||
                mostRestrictiveOccupancy.equals(OCCUPANCY_D1)) {
            if (building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) > 0) {
                // rule = RULE_55;
                subRule = RULE_55_2_3;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_6);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_10_5);

            } else if (building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) > 0 &&
                    building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) <= 0) {
                // rule = RULE_55;
                subRule = RULE_55_2_2;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_7_5);
            } else if (building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_300)) > 0 &&
                    building.getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) <= 0) {
                // rule = RULE_55;
                subRule = RULE_55_2_1;

                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_4_5);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_6);
            } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
                // rule = RULE_55;
                subRule = RULE_55_2_PROV;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
            } else {
                // rule = RULE_55;
                subRule = RULE_55_2_PROV;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);

            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_F1)) {
            // rule = RULE_56;
            subRule = RULE563D;
            minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
            meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_5);

        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_F2)) {
            if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {
                // rule = RULE_56;
                subRule = RULE563D;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_2);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_1_8);
            } else {
                // rule = RULE_56;
                subRule = RULE563D;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
            }

        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_G1)) {
            if (smallIndustrialBuilding(planDetail, building)) {
                rule = SUB_RULE_24_3;
                subRule = SUB_RULE_24_3;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
            } else {
                rule = RULE_57;
                subRule = RULE_57_4;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_5);
            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_G2)) {
            // rule = RULE_57;
            if (smallIndustrialBuilding(planDetail, building)) {
                rule = SUB_RULE_24_3;
                subRule = SUB_RULE_24_3;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
            } else {
                subRule = RULE_57_4;
                minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
                meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
            }
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_I1)) {
            // rule = RULE_59;
            subRule = RULE_59_3;
            minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
            meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_I2)) {
            // rule = RULE_59;
            subRule = RULE_59_3;
            minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_7_5);
            meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_7_5);
        } else if (mostRestrictiveOccupancy.equals(OCCUPANCY_F4)) {
            subRule = RULE_59_11;
            minVal = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1);
            meanVal = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_1);
        }

        if (checkMinimum5mtsCondition) {
            minVal = minVal.compareTo(FIVE) <= 0 ? FIVE : minVal;
            meanVal = meanVal.compareTo(FIVE) <= 0 ? FIVE : meanVal;
        }

        valid = validateMinimumAndMeanValue(valid, min, mean, minVal, meanVal);
        if (-1 == level) {
            rule = BSMT_FRONT_YARD_DESC;
            subRuleDesc = SUB_RULE_24_12_DESCRIPTION;
            subRule = SUB_RULE_24_12;
        }

        compareFrontYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule, rule, minVal,
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

    private Boolean validateMinimumAndMeanValue(Boolean valid, BigDecimal min, BigDecimal mean, BigDecimal minval,
            BigDecimal meanval) {
        if (min.compareTo(minval) >= 0 && mean.compareTo(meanval) >= 0)
            valid = true;
        return valid;
    }
}
