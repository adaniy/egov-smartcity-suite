package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.RAINWATER_HARWESTING;
import static org.egov.edcr.constants.DxfFileConstants.RWH_CAPACITY_L;
import static org.egov.edcr.utility.DcrConstants.IN_LITRE;
import static org.egov.edcr.utility.DcrConstants.OBJECTDEFINED_DESC;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED_DESC;
import static org.egov.edcr.utility.DcrConstants.RAINWATER_HARVESTING;
import static org.egov.edcr.utility.DcrConstants.RAINWATER_HARVES_TANKCAPACITY;
import static org.egov.edcr.utility.DcrConstants.RULE109;
import static org.egov.edcr.utility.ParametersConstants.COVERAGE_AREA;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_AREA;
import static org.egov.edcr.utility.ParametersConstants.PLOT_AREA;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RainWaterHarvesting extends GeneralRule implements RuleService {
    private static final BigDecimal THREEHUNDREDANDTWENTY = BigDecimal.valueOf(320);
    private static final String SUB_RULE_109_B_DESCRIPTION = "RainWater Storage Arrangement ";

    private static final String SUB_RULE_109_B = "109(B)";
    private static final String RAINWATER_HARVESTING_TANK_CAPACITY = "Minimum capacity of Rain Water Harvesting Tank";
    private static final String OCCUPANCY = "Occupancy";
    private static final BigDecimal ONEHUNDREDFIFTY = BigDecimal.valueOf(150);
    private static final BigDecimal TWENTYFIVE = BigDecimal.valueOf(25);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // Rain water harvesting Utility
        List<DXFLWPolyline> rainWaterHarvesting = Util.getPolyLinesByLayer(doc, RAINWATER_HARWESTING);
        if (rainWaterHarvesting != null && !rainWaterHarvesting.isEmpty())
            for (DXFLWPolyline pline : rainWaterHarvesting) {
                org.egov.edcr.entity.utility.RainWaterHarvesting rwh = new org.egov.edcr.entity.utility.RainWaterHarvesting();
                rwh.setPresentInDxf(true);
                rwh.setPolyLine(pline);
                pl.getUtility().addRainWaterHarvest(rwh);
            }

        if (doc.containsDXFLayer(RAINWATER_HARWESTING)) {
            String tankCapacity = Util.getMtextByLayerName(doc, RAINWATER_HARWESTING, RWH_CAPACITY_L);
            if (tankCapacity != null && !tankCapacity.isEmpty())
                try {
                    if (tankCapacity.contains(";")) {
                        String[] textSplit = tankCapacity.split(";");
                        int length = textSplit.length;

                        if (length >= 1) {
                            int index = length - 1;
                            tankCapacity = textSplit[index];
                            tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");
                        } else
                            tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");
                    } else
                        tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");

                    if (!tankCapacity.isEmpty())
                        pl.getUtility().setRainWaterHarvestingTankCapacity(BigDecimal.valueOf(Double.parseDouble(tankCapacity)));

                } catch (NumberFormatException e) {
                    pl.addError(RAINWATER_HARWESTING,
                            "Rain water Harwesting tank capity value contains non numeric character.");
                }
        }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && pl.getUtility() != null)
            // rain water harvest defined or not
            if (!pl.getVirtualBuilding().getOccupancies().isEmpty())
                for (OccupancyType occupancyType : pl.getVirtualBuilding().getOccupancies())
                    if (checkOccupancyTypeForRWH(occupancyType)) {
                        if (validateRWH(pl, errors))
                            break;
                    } else if ((occupancyType.equals(OccupancyType.OCCUPANCY_A1)
                            || occupancyType.equals(OccupancyType.OCCUPANCY_A4) ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_A5))
                            && (pl.getVirtualBuilding().getTotalFloorArea().compareTo(ONEHUNDREDFIFTY) > 0 ||
                                    pl.getPlot().getArea().compareTo(THREEHUNDREDANDTWENTY) > 0)) {
                        if (validateRWH(pl, errors))
                            break;
                    } else if ((occupancyType.equals(OccupancyType.OCCUPANCY_F)
                            || occupancyType.equals(OccupancyType.OCCUPANCY_F1) ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_F2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3)
                            ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_F4))
                            && pl.getVirtualBuilding().getTotalFloorArea() != null &&
                            pl.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(100)) > 0 &&
                            pl.getPlot().getArea().compareTo(BigDecimal.valueOf(200)) > 0)
                        if (validateRWH(pl, errors))
                            break;
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("RWH");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(PLOT_AREA);
        parameters.add(FLOOR_AREA);
        parameters.add(COVERAGE_AREA);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        validate(planDetail);
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, OCCUPANCY);
        scrutinyDetail.addColumnHeading(4, REQUIRED);
        scrutinyDetail.addColumnHeading(5, PROVIDED);
        scrutinyDetail.addColumnHeading(6, STATUS);
        scrutinyDetail.setKey("Common_Rain Water Harvesting");
        String rule = RULE109;
        String subRule = SUB_RULE_109_B;
        String subRuleDesc = SUB_RULE_109_B_DESCRIPTION;
        BigDecimal expectedTankCapacity = BigDecimal.ZERO;
        if (!planDetail.getVirtualBuilding().getOccupancies().isEmpty())
            for (OccupancyType occupancyType : planDetail.getVirtualBuilding().getOccupancies())
                if (checkOccupancyTypeForRWH(occupancyType)) {
                    if (processRWH(planDetail, rule, subRule, subRuleDesc))
                        break;
                } else if ((occupancyType.equals(OccupancyType.OCCUPANCY_A1) || occupancyType.equals(OccupancyType.OCCUPANCY_A4)
                        ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_A5))
                        && (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(ONEHUNDREDFIFTY) > 0 ||
                                planDetail.getPlot().getArea().compareTo(THREEHUNDREDANDTWENTY) > 0)) {
                    if (processRWH(planDetail, rule, subRule, subRuleDesc))
                        break;
                } else if ((occupancyType.equals(OccupancyType.OCCUPANCY_F) || occupancyType.equals(OccupancyType.OCCUPANCY_F1) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_F2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_F4))
                        && planDetail.getVirtualBuilding().getTotalFloorArea() != null &&
                        planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(100)) > 0 &&
                        planDetail.getPlot().getArea().compareTo(BigDecimal.valueOf(200)) > 0)
                    if (processRWH(planDetail, rule, subRule, subRuleDesc))
                        break;
        List<Map<String, Object>> listOfMapOfAllOccupanciesAndTankCapacity = new ArrayList<>();
        if (planDetail.getUtility() != null && !planDetail.getUtility().getRainWaterHarvest().isEmpty() &&
                planDetail.getUtility().getRainWaterHarvestingTankCapacity() != null)
            if (!planDetail.getVirtualBuilding().getOccupancies().isEmpty()) {
                for (OccupancyType occupancyType : planDetail.getVirtualBuilding().getOccupancies()) {
                    Map<String, Object> mapOfAllOccupancyAndTankCapacity = new HashMap<>();
                    if ((occupancyType.equals(OccupancyType.OCCUPANCY_F) || occupancyType.equals(OccupancyType.OCCUPANCY_F1) ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_F2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3)
                            ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_F4))
                            && planDetail.getVirtualBuilding().getTotalFloorArea() != null &&
                            planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(100)) > 0 &&
                            planDetail.getPlot().getArea().compareTo(BigDecimal.valueOf(200)) > 0
                            && !planDetail.getUtility().getRainWaterHarvest().isEmpty()
                            && planDetail.getUtility().getRainWaterHarvestingTankCapacity() != null
                            && planDetail.getVirtualBuilding().getTotalCoverageArea() != null
                            && planDetail.getVirtualBuilding().getTotalCoverageArea().compareTo(BigDecimal.valueOf(0)) > 0) {
                        expectedTankCapacity = TWENTYFIVE.multiply(planDetail.getVirtualBuilding().getTotalCoverageArea())
                                .setScale(2,
                                        RoundingMode.HALF_UP);
                        mapOfAllOccupancyAndTankCapacity.put("occupancy", occupancyType.getOccupancyTypeVal());
                        mapOfAllOccupancyAndTankCapacity.put("expectedTankCapacity", expectedTankCapacity);
                    } else if ((occupancyType.equals(OccupancyType.OCCUPANCY_A1)
                            || occupancyType.equals(OccupancyType.OCCUPANCY_A4) ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_A5))
                            && (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(ONEHUNDREDFIFTY) > 0 ||
                                    planDetail.getPlot().getArea().compareTo(THREEHUNDREDANDTWENTY) > 0)
                            && !planDetail.getUtility().getRainWaterHarvest().isEmpty()
                            && planDetail.getUtility().getRainWaterHarvestingTankCapacity() != null
                            && planDetail.getVirtualBuilding().getTotalCoverageArea() != null
                            && planDetail.getVirtualBuilding().getTotalCoverageArea().compareTo(BigDecimal.valueOf(0)) > 0) {
                        expectedTankCapacity = TWENTYFIVE.multiply(planDetail.getVirtualBuilding().getTotalCoverageArea())
                                .setScale(2,
                                        RoundingMode.HALF_UP);
                        mapOfAllOccupancyAndTankCapacity.put("occupancy", occupancyType.getOccupancyTypeVal());
                        mapOfAllOccupancyAndTankCapacity.put("expectedTankCapacity", expectedTankCapacity);
                    } else if (checkOccupancyTypeForRWH(occupancyType)
                            && !planDetail.getUtility().getRainWaterHarvest().isEmpty()
                            && planDetail.getUtility().getRainWaterHarvestingTankCapacity() != null
                            && planDetail.getVirtualBuilding().getTotalCoverageArea() != null
                            && planDetail.getVirtualBuilding().getTotalCoverageArea().compareTo(BigDecimal.valueOf(0)) > 0) {
                        if (occupancyType.equals(OccupancyType.OCCUPANCY_A2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3)
                                || occupancyType.equals(OccupancyType.OCCUPANCY_A3) ||
                                occupancyType.equals(OccupancyType.OCCUPANCY_I1)
                                || occupancyType.equals(OccupancyType.OCCUPANCY_I2))
                            expectedTankCapacity = TWENTYFIVE.multiply(planDetail.getVirtualBuilding().getTotalCoverageArea())
                                    .setScale(2,
                                            RoundingMode.HALF_UP);
                        else
                            expectedTankCapacity = BigDecimal.valueOf(50)
                                    .multiply(planDetail.getVirtualBuilding().getTotalCoverageArea()).setScale(2,
                                            RoundingMode.HALF_UP);
                        mapOfAllOccupancyAndTankCapacity.put("occupancy", occupancyType.getOccupancyTypeVal());
                        mapOfAllOccupancyAndTankCapacity.put("expectedTankCapacity", expectedTankCapacity);
                    }
                    if (!mapOfAllOccupancyAndTankCapacity.isEmpty())
                        listOfMapOfAllOccupanciesAndTankCapacity.add(mapOfAllOccupancyAndTankCapacity);
                }
                Map<String, Object> mapOfMostRestrictiveOccupancyAndItsTankCapacity = new HashMap<>();
                if (!listOfMapOfAllOccupanciesAndTankCapacity.isEmpty()) {
                    mapOfMostRestrictiveOccupancyAndItsTankCapacity = listOfMapOfAllOccupanciesAndTankCapacity.get(0);
                    for (Map<String, Object> mapOfOccupancyAndTankCapacity : listOfMapOfAllOccupanciesAndTankCapacity)
                        if (mapOfOccupancyAndTankCapacity.get("expectedTankCapacity")
                                .equals(mapOfMostRestrictiveOccupancyAndItsTankCapacity.get("expectedTankCapacity"))) {
                            if (!mapOfOccupancyAndTankCapacity.get("occupancy")
                                    .equals(mapOfMostRestrictiveOccupancyAndItsTankCapacity.get("occupancy"))) {
                                SortedSet<String> uniqueOccupancies = new TreeSet<>();
                                String[] occupancyString = (mapOfOccupancyAndTankCapacity.get("occupancy") + " , "
                                        + mapOfMostRestrictiveOccupancyAndItsTankCapacity.get("occupancy")).split(" , ");
                                for (String str : occupancyString)
                                    uniqueOccupancies.add(str);
                                StringBuffer str = new StringBuffer();
                                List<String> unqList = new ArrayList<>(uniqueOccupancies);
                                for (String unique : unqList) {
                                    str.append(unique);
                                    if (!unique.equals(unqList.get(unqList.size() - 1)))
                                        str.append(" , ");
                                }
                                mapOfMostRestrictiveOccupancyAndItsTankCapacity.put("occupancy", str.toString());
                            }
                            continue;
                        } else if (((BigDecimal) mapOfMostRestrictiveOccupancyAndItsTankCapacity.get("expectedTankCapacity"))
                                .compareTo((BigDecimal) mapOfOccupancyAndTankCapacity.get("expectedTankCapacity")) < 0)
                            mapOfMostRestrictiveOccupancyAndItsTankCapacity.putAll(mapOfOccupancyAndTankCapacity);
                }
                Boolean valid = false;
                if (!mapOfMostRestrictiveOccupancyAndItsTankCapacity.isEmpty()
                        && mapOfMostRestrictiveOccupancyAndItsTankCapacity.get("occupancy") != null &&
                        mapOfMostRestrictiveOccupancyAndItsTankCapacity.get("expectedTankCapacity") != null) {
                    if (((BigDecimal) mapOfMostRestrictiveOccupancyAndItsTankCapacity.get("expectedTankCapacity"))
                            .compareTo(planDetail.getUtility().getRainWaterHarvestingTankCapacity()) <= 0)
                        valid = true;
                    processRWHTankCapacity(planDetail, rule, subRule, subRuleDesc,
                            (BigDecimal) mapOfMostRestrictiveOccupancyAndItsTankCapacity.get("expectedTankCapacity"),
                            valid, mapOfMostRestrictiveOccupancyAndItsTankCapacity.get("occupancy"));
                }
            }
        return planDetail;
    }

    private void processRWHTankCapacity(PlanDetail planDetail, String rule, String subRule, String subRuleDesc,
            BigDecimal expectedTankCapacity, Boolean valid, Object occupancyType) {
        if (expectedTankCapacity.compareTo(BigDecimal.valueOf(0)) > 0)
            if (valid) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule,
                                RAINWATER_HARVESTING + " tank capacity for occupancy " + occupancyType,
                                RAINWATER_HARVESTING + " tank capacity for occupancy " + occupancyType,
                                expectedTankCapacity.toString(),
                                planDetail.getUtility().getRainWaterHarvestingTankCapacity().toString() + IN_LITRE,
                                Result.Accepted, null));
                setReportOutputDetails(planDetail, subRule, RAINWATER_HARVESTING_TANK_CAPACITY, occupancyType.toString(),
                        expectedTankCapacity.toString(),
                        planDetail.getUtility().getRainWaterHarvestingTankCapacity().toString(), Result.Accepted.getResultVal());
            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule,
                                RAINWATER_HARVESTING + " tank capacity for occupancy " + occupancyType,
                                RAINWATER_HARVESTING + " tank capacity for occupancy " + occupancyType,
                                expectedTankCapacity.toString(),
                                planDetail.getUtility().getRainWaterHarvestingTankCapacity().toString() + IN_LITRE,
                                Result.Not_Accepted, null));
                setReportOutputDetails(planDetail, subRule, RAINWATER_HARVESTING_TANK_CAPACITY, occupancyType.toString(),
                        expectedTankCapacity.toString() + IN_LITRE,
                        planDetail.getUtility().getRainWaterHarvestingTankCapacity().toString() + IN_LITRE,
                        Result.Not_Accepted.getResultVal());
            }
    }

    private boolean processRWH(PlanDetail planDetail, String rule, String subRule, String subRuleDesc) {
        if (!planDetail.getUtility().getRainWaterHarvest().isEmpty()) {
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                            RAINWATER_HARVESTING,
                            null,
                            null,
                            Result.Accepted, OBJECTDEFINED_DESC));
            setReportOutputDetails(planDetail, subRule, subRuleDesc, "", "",
                    OBJECTDEFINED_DESC, Result.Accepted.getResultVal());
            return true;
        } else if (planDetail.getUtility().getRainWaterHarvest().isEmpty()) {
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                            RAINWATER_HARVESTING,
                            null,
                            null,
                            Result.Not_Accepted, OBJECTNOTDEFINED_DESC));
            setReportOutputDetails(planDetail, subRule, subRuleDesc, "", "",
                    OBJECTNOTDEFINED_DESC, Result.Not_Accepted.getResultVal());
            return true;
        }
        return false;
    }

    private boolean checkOccupancyTypeForRWH(OccupancyType occupancyType) {
        return occupancyType.equals(OccupancyType.OCCUPANCY_A2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3)
                || occupancyType.equals(OccupancyType.OCCUPANCY_A3)
                || occupancyType.equals(OccupancyType.OCCUPANCY_B1) || occupancyType.equals(OccupancyType.OCCUPANCY_B2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_B3) || occupancyType.equals(OccupancyType.OCCUPANCY_C) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C1) || occupancyType.equals(OccupancyType.OCCUPANCY_C2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C3) || occupancyType.equals(OccupancyType.OCCUPANCY_D) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_D1) || occupancyType.equals(OccupancyType.OCCUPANCY_D2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_E) || occupancyType.equals(OccupancyType.OCCUPANCY_G1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_G2) || occupancyType.equals(OccupancyType.OCCUPANCY_I1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_I2);
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDesc, String occupancy, String expected,
            String actual, String status) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(OCCUPANCY, occupancy);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

    private boolean validateRWH(PlanDetail pl, HashMap<String, String> errors) {
        if (pl.getUtility().getRainWaterHarvest().isEmpty()) {
            errors.put(RAINWATER_HARVESTING,
                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                            RAINWATER_HARVESTING }, LocaleContextHolder.getLocale()));
            pl.addErrors(errors);
            return true;
        } else if (!pl.getUtility().getRainWaterHarvest().isEmpty() &&
                pl.getUtility().getRainWaterHarvestingTankCapacity() == null) {
            errors.put(RAINWATER_HARVES_TANKCAPACITY,
                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                            RAINWATER_HARVES_TANKCAPACITY }, LocaleContextHolder.getLocale()));
            pl.addErrors(errors);
            return true;
        }
        return false;
    }

}
