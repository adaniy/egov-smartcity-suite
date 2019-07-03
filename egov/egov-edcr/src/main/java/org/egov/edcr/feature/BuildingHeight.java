package org.egov.edcr.feature;

import static org.egov.edcr.utility.DcrConstants.BUILDING_HEIGHT;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.HEIGHT_OF_BUILDING;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.RULE32;
import static org.egov.edcr.utility.DcrConstants.SECURITY_ZONE;
import static org.egov.edcr.utility.DcrConstants.SHORTESTDISTINACETOBUILDINGFOOTPRINT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.CulDeSacRoad;
import org.egov.edcr.entity.measurement.Lane;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.springframework.stereotype.Service;

@Service
public class BuildingHeight extends GeneralRule implements RuleService {
    private static final String RULE_EXPECTED_KEY = "buildingheight.expected";
    private static final String RULE_ACTUAL_KEY = "buildingheight.actual";
    private static final String SECURITYZONE_RULE_EXPECTED_KEY = "securityzone.expected";
    private static final String SECURITYZONE_RULE_ACTUAL_KEY = "securityzone.actual";

    private static final String SUB_RULE_32_1A = "32(1A)";
    private static final String SUB_RULE_32_3 = "32(3)";
    public static final String UPTO = "Up To";
    public static final String DECLARED = "Declared";
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal TEN = BigDecimal.valueOf(10);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        extractDistanceFromBuildingToRoadEnd(doc, pl);
        return pl;

    }

    private void extractDistanceFromBuildingToRoadEnd(DXFDocument doc, PlanDetail pl) {
        for (Block block : pl.getBlocks())
            if (!block.getCompletelyExisting()) {
                String layerName = String.format(DxfFileConstants.LAYER_NAME_MAX_HEIGHT_CAL, block.getNumber());
                String heightSetBack = String.format(DxfFileConstants.LAYER_NAME_MAX_HEIGHT_CAL_SET_BACK, block.getNumber());

                List<BigDecimal> maxHeightCal = Util.getListOfDimensionValueByLayer(doc, layerName);
                if (!maxHeightCal.isEmpty())
                    block.getBuilding().setDistanceFromBuildingFootPrintToRoadEnd(maxHeightCal);
                List<BigDecimal> maxHeightSetBack = Util.getListOfDimensionValueByLayer(doc, heightSetBack);
                if (!maxHeightSetBack.isEmpty())
                    block.getBuilding().setDistanceFromSetBackToBuildingLine(maxHeightSetBack);
            }

    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (!Util.isSmallPlot(pl))
            for (Block block : pl.getBlocks())
                if (!block.getCompletelyExisting()) {
                    if (block.getBuilding() != null && (block.getBuilding().getBuildingHeight() == null ||
                            block.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) <= 0)) {
                        errors.put(BUILDING_HEIGHT + block.getNumber(),
                                prepareMessage(OBJECTNOTDEFINED, BUILDING_HEIGHT + " for block " + block.getNumber()));
                        pl.addErrors(errors);
                    }
                    // distance from end of road to foot print is mandatory.
                    if (block.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().isEmpty()) {
                        errors.put(SHORTESTDISTINACETOBUILDINGFOOTPRINT + block.getNumber(),
                                prepareMessage(OBJECTNOTDEFINED,
                                        SHORTESTDISTINACETOBUILDINGFOOTPRINT + " for block " + block.getNumber()));
                        pl.addErrors(errors);
                    }
                }
        return pl;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        validate(planDetail);
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey("Common_Height of Building");
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, UPTO);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);

        if (!Util.isSmallPlot(planDetail))
            checkBuildingHeight(planDetail);
        checkBuildingInSecurityZoneArea(planDetail);
        return planDetail;
    }

    private void checkBuildingHeight(PlanDetail planDetail) {
        String subRule = SUB_RULE_32_1A;
        String rule = HEIGHT_OF_BUILDING;

        BigDecimal maximumDistanceToRoad = BigDecimal.ZERO;

        // Get Maximum road distane from plot.
        maximumDistanceToRoad = getMaximimShortestdistanceFromRoad(planDetail, maximumDistanceToRoad);

        // get maximum height from buildings.
        for (Block block : planDetail.getBlocks())
            if (!block.getCompletelyExisting()) {

                BigDecimal maximumDistanceToRoadEdge = BigDecimal.ZERO;
                BigDecimal maximumSetBackToBuildingLine = BigDecimal.ZERO;
                BigDecimal exptectedDistance = BigDecimal.ZERO;
                BigDecimal actualDistance = BigDecimal.ZERO;

                // Get Maximum distance to road Edge
                maximumDistanceToRoadEdge = getMaximumDistanceFromRoadEdge(maximumDistanceToRoadEdge, block);
                maximumSetBackToBuildingLine = getMaximumDistanceFromSetBackToBuildingLine(maximumSetBackToBuildingLine, block);
                actualDistance = block.getBuilding().getBuildingHeight();
                if (maximumDistanceToRoadEdge != null)
                    if (maximumDistanceToRoad.compareTo(TWELVE) <= 0)
                        if (maximumSetBackToBuildingLine != null && maximumSetBackToBuildingLine.compareTo(BigDecimal.ZERO) > 0)
                            exptectedDistance = maximumDistanceToRoadEdge
                                    .multiply(BigDecimal.valueOf(2))
                                    .add(BigDecimal.valueOf(3).multiply(maximumSetBackToBuildingLine
                                            .divide(BigDecimal.valueOf(0.5), 0, RoundingMode.DOWN)))
                                    .setScale(DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);
                        else
                            exptectedDistance = maximumDistanceToRoadEdge.multiply(BigDecimal.valueOf(2))
                                    .setScale(DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);
                // Show for each block height
                if (exptectedDistance.compareTo(BigDecimal.ZERO) > 0) {
                    String actualResult = getLocaleMessage(RULE_ACTUAL_KEY, actualDistance.toString());
                    String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, exptectedDistance.toString());

                    if (actualDistance.compareTo(exptectedDistance) > 0) {
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(rule, subRule,
                                        HEIGHT_OF_BUILDING + " for Block " + block.getNumber(), HEIGHT_OF_BUILDING,
                                        expectedResult, actualResult, Result.Not_Accepted, null));
                        Map<String, String> details = new HashMap<>();
                        details.put(RULE_NO, subRule);
                        details.put(DESCRIPTION, HEIGHT_OF_BUILDING + " for Block " + block.getNumber());
                        details.put(UPTO, expectedResult);
                        details.put(PROVIDED, actualResult);
                        details.put(STATUS, Result.Not_Accepted.getResultVal());
                        scrutinyDetail.getDetail().add(details);
                        planDetail.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

                    } else {
                        Map<String, String> details = new HashMap<>();
                        details.put(RULE_NO, subRule);
                        details.put(DESCRIPTION, HEIGHT_OF_BUILDING + " for Block " + block.getNumber());
                        details.put(UPTO, expectedResult);
                        details.put(PROVIDED, actualResult);
                        details.put(STATUS, Result.Verify.getResultVal());
                        scrutinyDetail.getDetail().add(details);
                        planDetail.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
                        ;

                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(rule, subRule,
                                        HEIGHT_OF_BUILDING + " for Block " + block.getNumber(), HEIGHT_OF_BUILDING,
                                        expectedResult, actualResult, Result.Verify, null));

                    }
                }
            }
    }

    private void checkBuildingInSecurityZoneArea(PlanDetail planDetail) {

        if (planDetail.getPlanInformation().getSecurityZone()) {
            BigDecimal maxBuildingHeight = BigDecimal.ZERO;
            for (Block block : planDetail.getBlocks())
                if (!block.getCompletelyExisting())
                    if (maxBuildingHeight.compareTo(BigDecimal.ZERO) == 0 ||
                            block.getBuilding().getBuildingHeight().compareTo(maxBuildingHeight) >= 0)
                        maxBuildingHeight = block.getBuilding().getBuildingHeight();
            if (maxBuildingHeight.compareTo(BigDecimal.ZERO) > 0) {

                scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.setKey("Common_Security Zone");
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, DESCRIPTION);
                scrutinyDetail.addColumnHeading(3, REQUIRED);
                scrutinyDetail.addColumnHeading(4, PROVIDED);
                scrutinyDetail.addColumnHeading(5, STATUS);

                String actualResult = getLocaleMessage(SECURITYZONE_RULE_ACTUAL_KEY,
                        maxBuildingHeight.toString());
                String expectedResult = getLocaleMessage(SECURITYZONE_RULE_EXPECTED_KEY, TEN.toString());

                if (maxBuildingHeight.compareTo(TEN) <= 0) {
                    Map<String, String> details = new HashMap<>();
                    details.put(RULE_NO, SUB_RULE_32_3);
                    details.put(DESCRIPTION, SECURITY_ZONE);
                    details.put(REQUIRED, expectedResult);
                    details.put(PROVIDED, actualResult);
                    details.put(STATUS, Result.Verify.getResultVal());
                    scrutinyDetail.getDetail().add(details);
                    planDetail.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(RULE32, SUB_RULE_32_3, SECURITY_ZONE, SECURITY_ZONE,
                                    expectedResult, actualResult, Result.Verify, null));
                } else {
                    Map<String, String> details = new HashMap<>();
                    details.put(RULE_NO, SUB_RULE_32_3);
                    details.put(DESCRIPTION, SECURITY_ZONE);
                    details.put(REQUIRED, expectedResult);
                    details.put(PROVIDED, actualResult);
                    details.put(STATUS, Result.Not_Accepted.getResultVal());
                    scrutinyDetail.getDetail().add(details);
                    planDetail.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(RULE32, SUB_RULE_32_3, SECURITY_ZONE, SECURITY_ZONE,
                                    expectedResult, actualResult, Result.Not_Accepted, null));
                }
            }
        } else {
            scrutinyDetail = new ScrutinyDetail();
            scrutinyDetail.setKey("Common_Security Zone");
            scrutinyDetail.addColumnHeading(1, RULE_NO);
            scrutinyDetail.addColumnHeading(2, DESCRIPTION);
            scrutinyDetail.addColumnHeading(3, DECLARED);
            scrutinyDetail.addColumnHeading(4, STATUS);

            Map<String, String> details = new HashMap<>();
            details.put(RULE_NO, SUB_RULE_32_3);
            details.put(DESCRIPTION, SECURITY_ZONE);
            details.put(DECLARED, "No");
            details.put(STATUS, Result.Verify.getResultVal());
            scrutinyDetail.getDetail().add(details);
            planDetail.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

        }

    }

    private BigDecimal getMaximumDistanceFromRoadEdge(BigDecimal maximumDistanceToRoadEdge, Block block) {
        if (block.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd() != null)
            for (BigDecimal distanceFromroadEnd : block.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd())
                if (distanceFromroadEnd.compareTo(maximumDistanceToRoadEdge) > 0)
                    maximumDistanceToRoadEdge = distanceFromroadEnd;
        return maximumDistanceToRoadEdge;
    }

    private BigDecimal getMaximumDistanceFromSetBackToBuildingLine(BigDecimal distancceFromSetbackToBuildingLine, Block block) {
        if (block.getBuilding().getDistanceFromSetBackToBuildingLine() != null)
            for (BigDecimal distance : block.getBuilding().getDistanceFromSetBackToBuildingLine())
                if (distance.compareTo(distancceFromSetbackToBuildingLine) > 0)
                    distancceFromSetbackToBuildingLine = distance;
        return distancceFromSetbackToBuildingLine;
    }

    private BigDecimal getMaximimShortestdistanceFromRoad(PlanDetail planDetail, BigDecimal maximumDistanceToRoad) {
        if (planDetail.getNonNotifiedRoads() != null)
            for (NonNotifiedRoad nonnotifiedRoad : planDetail.getNonNotifiedRoads())
                for (BigDecimal shortDistance : nonnotifiedRoad.getShortestDistanceToRoad())
                    if (shortDistance.compareTo(maximumDistanceToRoad) > 0)
                        maximumDistanceToRoad = shortDistance;
        if (planDetail.getNotifiedRoads() != null)
            for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads())
                for (BigDecimal shortDistance : notifiedRoad.getShortestDistanceToRoad())
                    if (shortDistance.compareTo(maximumDistanceToRoad) > 0)
                        maximumDistanceToRoad = shortDistance;
        if (planDetail.getCuldeSacRoads() != null)
            for (CulDeSacRoad culdRoad : planDetail.getCuldeSacRoads())
                for (BigDecimal shortDistance : culdRoad.getShortestDistanceToRoad())
                    if (shortDistance.compareTo(maximumDistanceToRoad) > 0)
                        maximumDistanceToRoad = shortDistance;
        if (planDetail.getLaneRoads() != null)
            for (Lane lane : planDetail.getLaneRoads())
                for (BigDecimal shortDistance : lane.getShortestDistanceToRoad())
                    if (shortDistance.compareTo(maximumDistanceToRoad) > 0)
                        maximumDistanceToRoad = shortDistance;
        return maximumDistanceToRoad;
    }

}
