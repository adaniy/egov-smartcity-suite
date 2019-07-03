package org.egov.edcr.rule;

import static org.egov.edcr.utility.ParametersConstants.BLOCK_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.COVERAGE_AREA;
import static org.egov.edcr.utility.ParametersConstants.PLOT_AREA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.AccessoryBlock;
import org.egov.edcr.entity.AccessoryBuilding;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RoadOutput;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.CulDeSacRoad;
import org.egov.edcr.entity.measurement.Lane;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.feature.GeneralRule;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFMText;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccessoryBuildingService extends GeneralRule implements RuleService {

    private static final String SUBRULE_88_1_DESC = "Maximum area of accessory block %s";
    private static final String SUBRULE_88_3_DESC = "Maximum height of accessory block %s";

    private static final String SUBRULE_88_1 = "88(1)";
    private static final String SUBULE_88_3 = "88(3)";
    private static final String SUBRULE_88_4 = "88(4)";
    private static final String SUBRULE_88_5 = "88(5)";

    private static final String MIN_DIS_NOTIFIED_ROAD_FROM_ACC_BLDG = "Minimum distance from accessory block to notified road";
    private static final String MIN_DIS_NON_NOTIFIED_ROAD_FROM_ACC_BLDG = "Minimum distance from accessory building to non notified road";
    private static final String MIN_DIS_CULDESAC_ROAD_FROM_ACC_BLDG = "Minimum distance from accessory building to culdesac road";
    private static final String MIN_DIS_LANE_ROAD_FROM_ACC_BLDG = "Minimum distance from accessory building to lane road";
    private static final String SUBRULE_88_5_DESC = "Minimum distance from accessory block %s to plot boundary";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        String layerNameRegex = DxfFileConstants.LAYER_ACCESSORY_BUILDING + "_+\\d";
        List<String> layerNamesAccessory = Util.getLayerNamesLike(doc, layerNameRegex);
        if (!layerNamesAccessory.isEmpty())
            for (String layerName : layerNamesAccessory) {
                List<DXFLWPolyline> polyLines = Util.getPolyLinesByLayer(doc, layerName);
                if (!polyLines.isEmpty() && polyLines != null) {
                    AccessoryBlock accessoryBlock = new AccessoryBlock();
                    accessoryBlock.setPolylineList(polyLines);
                    AccessoryBuilding accessoryBuilding = new AccessoryBuilding();
                    String[] strings = layerName.split("_", 2);
                    if (strings[1] != null && !strings[1].isEmpty())
                        accessoryBlock.setNumber(strings[1]);
                    BigDecimal totalArea = BigDecimal.ZERO;
                    for (DXFLWPolyline polyline : polyLines)
                        totalArea = totalArea
                                .add(Util.getPolyLineArea(polyline) == null ? BigDecimal.ZERO : Util.getPolyLineArea(polyline));
                    accessoryBuilding.setArea(totalArea);
                    String accessoryBlockHeightText = Util.getMtextByLayerName(doc, layerName);
                    if (accessoryBlockHeightText != null && !accessoryBlockHeightText.isEmpty()) {
                        String stringArray[] = accessoryBlockHeightText.split("=", 2);
                        if (stringArray[0] != null && !stringArray[0].isEmpty()) {
                            String text = stringArray[0].replaceAll("[^\\d.]", "");
                            if (text != null && text.equals(accessoryBlock.getNumber()) && stringArray[1] != null
                                    && !stringArray[1].isEmpty())
                                accessoryBuilding.setHeight(BigDecimal.valueOf(Double.valueOf(stringArray[1])));
                        }
                    }
                    accessoryBlock.setAccessoryBuilding(accessoryBuilding);
                    pl.getAccessoryBlocks().add(accessoryBlock);
                }
            }
        extractDistanceOfAccessoryBlockToRoads(pl, doc);
        extractDistanceOfAccessoryBlockToPlotBoundary(pl, doc);
        return pl;
    }

    private void extractDistanceOfAccessoryBlockToPlotBoundary(PlanDetail pl, DXFDocument doc) {
        if (pl != null && !pl.getAccessoryBlocks().isEmpty())
            for (AccessoryBlock accessoryBlock : pl.getAccessoryBlocks())
                if (accessoryBlock.getNumber() != null) {
                    String accessoryBlockLayerName = String.format(DxfFileConstants.LAYER_ACCESSORY_DIST_TO_PLOT_BNDRY,
                            accessoryBlock.getNumber());
                    List<BigDecimal> distanceToPlotList = Util.getListOfDimensionValueByLayer(doc, accessoryBlockLayerName);
                    accessoryBlock.getAccessoryBuilding().setDistanceFromPlotBoundary(distanceToPlotList);
                }
    }

    private void extractDistanceOfAccessoryBlockToRoads(PlanDetail pl, DXFDocument doc) {
        String layerAccShortestDist = DxfFileConstants.LAYER_ACCESSORY_SHORTEST_DISTANCE;
        List<DXFDimension> dimensionList = Util.getDimensionsByLayer(doc, layerAccShortestDist);
        List<RoadOutput> distancesWithColorCode = extractDistanceWithColourCode(doc, dimensionList);
        List<BigDecimal> notifiedRoadDistances = new ArrayList<>();
        List<BigDecimal> nonNotifiedRoadDistances = new ArrayList<>();
        List<BigDecimal> culdesacRoadDistances = new ArrayList<>();
        List<BigDecimal> laneRoadDistances = new ArrayList<>();
        for (RoadOutput dimension : distancesWithColorCode)
            if (Integer.valueOf(dimension.colourCode) == DxfFileConstants.COLOUR_CODE_NOTIFIEDROAD)
                notifiedRoadDistances.add(dimension.distance);
            else if (Integer.valueOf(dimension.colourCode) == DxfFileConstants.COLOUR_CODE_NONNOTIFIEDROAD)
                nonNotifiedRoadDistances.add(dimension.distance);
            else if (Integer.valueOf(dimension.colourCode) == DxfFileConstants.COLOUR_CODE_CULDESAC)
                culdesacRoadDistances.add(dimension.distance);
            else if (Integer.valueOf(dimension.colourCode) == DxfFileConstants.COLOUR_CODE_LANE)
                laneRoadDistances.add(dimension.distance);
        if (!notifiedRoadDistances.isEmpty() && pl.getNotifiedRoads().isEmpty()) {
            NotifiedRoad notifiedRoad = new NotifiedRoad();
            notifiedRoad.setPresentInDxf(true);
            pl.getNotifiedRoads().add(notifiedRoad);
        } else if (!nonNotifiedRoadDistances.isEmpty() && pl.getNonNotifiedRoads().isEmpty()) {
            NonNotifiedRoad nonNotifiedRoad = new NonNotifiedRoad();
            nonNotifiedRoad.setPresentInDxf(true);
            pl.getNonNotifiedRoads().add(nonNotifiedRoad);
        } else if (!culdesacRoadDistances.isEmpty() && pl.getCuldeSacRoads().isEmpty()) {
            CulDeSacRoad culDeSacRoad = new CulDeSacRoad();
            culDeSacRoad.setPresentInDxf(true);
            pl.getCuldeSacRoads().add(culDeSacRoad);
        } else if (!laneRoadDistances.isEmpty() && pl.getLaneRoads().isEmpty()) {
            Lane lane = new Lane();
            lane.setPresentInDxf(true);
            pl.getLaneRoads().add(lane);
        }
        for (BigDecimal notifiedDistance : notifiedRoadDistances)
            if (!pl.getNotifiedRoads().isEmpty())
                pl.getNotifiedRoads().get(0).addDistanceFromAccessoryBlock(notifiedDistance);
        for (BigDecimal nonNotifiedDistance : nonNotifiedRoadDistances)
            if (!pl.getNonNotifiedRoads().isEmpty())
                pl.getNonNotifiedRoads().get(0).addDistanceFromAccessoryBlock(nonNotifiedDistance);
        for (BigDecimal culdesacDistance : culdesacRoadDistances)
            if (!pl.getCuldeSacRoads().isEmpty())
                pl.getCuldeSacRoads().get(0).addDistanceFromAccessoryBlock(culdesacDistance);
        for (BigDecimal laneDistance : laneRoadDistances)
            if (!pl.getLaneRoads().isEmpty())
                pl.getLaneRoads().get(0).addDistanceFromAccessoryBlock(laneDistance);
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && !pl.getAccessoryBlocks().isEmpty()) {
            for (AccessoryBlock accessoryBlock : pl.getAccessoryBlocks())
                if (accessoryBlock.getAccessoryBuilding() != null
                        && accessoryBlock.getAccessoryBuilding().getHeight().compareTo(BigDecimal.valueOf(0)) == 0) {
                    errors.put(String.format(DcrConstants.ACCESSORRY_BLK_HGHT, accessoryBlock.getNumber()),
                            edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                    new String[] { String.format(DcrConstants.ACCESSORRY_BLK_HGHT, accessoryBlock.getNumber()) },
                                    LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);

                }
            boolean shortestDistanceDefined = false;
            if (!pl.getNotifiedRoads().isEmpty())
                for (NotifiedRoad notifiedRoad : pl.getNotifiedRoads())
                    for (BigDecimal shortestDistanceToRoad : notifiedRoad.getDistanceFromAccessoryBlock())
                        if (shortestDistanceToRoad.compareTo(BigDecimal.ZERO) > 0) {
                            shortestDistanceDefined = true;
                            break;
                        }
            if (!pl.getNonNotifiedRoads().isEmpty())
                for (NonNotifiedRoad nonNotifiedRoad : pl.getNonNotifiedRoads())
                    for (BigDecimal shortestDistanceToRoad : nonNotifiedRoad.getDistanceFromAccessoryBlock())
                        if (shortestDistanceToRoad.compareTo(BigDecimal.ZERO) > 0) {
                            shortestDistanceDefined = true;
                            break;
                        }
            if (!pl.getLaneRoads().isEmpty())
                for (Lane laneRoad : pl.getLaneRoads())
                    for (BigDecimal shortestDistanceToRoad : laneRoad.getDistanceFromAccessoryBlock())
                        if (shortestDistanceToRoad.compareTo(BigDecimal.ZERO) > 0) {
                            shortestDistanceDefined = true;
                            break;
                        }
            if (!pl.getCuldeSacRoads().isEmpty())
                for (CulDeSacRoad culdSac : pl.getCuldeSacRoads())
                    for (BigDecimal shortestDistanceToRoad : culdSac.getDistanceFromAccessoryBlock())
                        if (shortestDistanceToRoad.compareTo(BigDecimal.ZERO) > 0) {
                            shortestDistanceDefined = true;
                            break;
                        }

            if (!shortestDistanceDefined) {
                errors.put(DcrConstants.SHORTESTDISTANCETOROAD, prepareMessage(DcrConstants.OBJECTNOTDEFINED,
                        DcrConstants.SHORTESTDISTANCETOROAD));
                pl.addErrors(errors);
            }
        }
        validateMinimumDistanceOfAccBlkToPlotBndry(pl, errors);
        return pl;
    }

    private void validateMinimumDistanceOfAccBlkToPlotBndry(PlanDetail pl, HashMap<String, String> errors) {
        if (pl != null && !pl.getAccessoryBlocks().isEmpty())
            for (AccessoryBlock accessoryBlock : pl.getAccessoryBlocks())
                if (accessoryBlock.getNumber() != null && accessoryBlock.getAccessoryBuilding() != null
                        && accessoryBlock.getAccessoryBuilding().getDistanceFromPlotBoundary().isEmpty()) {
                    errors.put(String.format(DcrConstants.ACCESSORRY_BLK_DIST_FRM_PLOT_BNDRY, accessoryBlock.getNumber()),
                            edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                    new String[] { String.format(DcrConstants.ACCESSORRY_BLK_DIST_FRM_PLOT_BNDRY,
                                            accessoryBlock.getNumber()) },
                                    LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);
                }
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("ACCBLK_%");
        layers.add("ACC_SHORTEST_DIST_TO_ROAD");
        layers.add("ACCBLK_%_DIST_BOUNDARY");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(BLOCK_LEVEL_CHECK);
        parameters.add(PLOT_AREA);
        parameters.add(COVERAGE_AREA);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        processAreaOfAccessoryBlock(pl);
        processHeightOfAccessoryBlock(pl);
        processShortestDistanceOfAccBlkFromRoad(pl);
        processShortestDistanceOfAccBlkFromPlotBoundary(pl);
        return pl;
    }

    private void processShortestDistanceOfAccBlkFromPlotBoundary(PlanDetail pl) {
        String subRule = SUBRULE_88_5;
        ScrutinyDetail scrutinyDetail3 = new ScrutinyDetail();
        scrutinyDetail3.setKey("Common_Accessory Block - Minimum distance from plot boundary");
        scrutinyDetail3.addColumnHeading(1, RULE_NO);
        scrutinyDetail3.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail3.addColumnHeading(3, REQUIRED);
        scrutinyDetail3.addColumnHeading(4, PROVIDED);
        scrutinyDetail3.addColumnHeading(5, STATUS);
        if (pl != null && !pl.getAccessoryBlocks().isEmpty())
            for (AccessoryBlock accessoryBlock : pl.getAccessoryBlocks()) {
                boolean valid = false;
                if (accessoryBlock.getAccessoryBuilding() != null
                        && !accessoryBlock.getAccessoryBuilding().getDistanceFromPlotBoundary().isEmpty()) {
                    BigDecimal minimumAccBlkDisFromPlotBoundary = accessoryBlock.getAccessoryBuilding()
                            .getDistanceFromPlotBoundary().get(0);
                    for (BigDecimal disOfAccBlkFromPlotBndry : accessoryBlock.getAccessoryBuilding()
                            .getDistanceFromPlotBoundary())
                        if (minimumAccBlkDisFromPlotBoundary.compareTo(disOfAccBlkFromPlotBndry) > 0)
                            minimumAccBlkDisFromPlotBoundary = disOfAccBlkFromPlotBndry;
                    if (minimumAccBlkDisFromPlotBoundary.compareTo(BigDecimal.valueOf(1)) >= 0)
                        valid = true;
                    if (valid)
                        setReportOutputDetails(pl, subRule, String.format(SUBRULE_88_5_DESC, accessoryBlock.getNumber()),
                                String.valueOf(1),
                                minimumAccBlkDisFromPlotBoundary.toString(), Result.Accepted.getResultVal(), scrutinyDetail3);
                    else
                        setReportOutputDetails(pl, subRule, String.format(SUBRULE_88_5_DESC, accessoryBlock.getNumber()),
                                String.valueOf(1),
                                minimumAccBlkDisFromPlotBoundary.toString(), Result.Not_Accepted.getResultVal(), scrutinyDetail3);
                }
            }
    }

    private void processShortestDistanceOfAccBlkFromRoad(PlanDetail pl) {
        String subRule = SUBRULE_88_4;
        ScrutinyDetail scrutinyDetail2 = new ScrutinyDetail();
        scrutinyDetail2.setKey("Common_Accessory Block - Minimum distance from the boundary abutting the road");
        scrutinyDetail2.addColumnHeading(1, RULE_NO);
        scrutinyDetail2.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail2.addColumnHeading(3, REQUIRED);
        scrutinyDetail2.addColumnHeading(4, PROVIDED);
        scrutinyDetail2.addColumnHeading(5, STATUS);
        if (pl != null && pl.getPlot() != null && pl.getPlot().getArea() != null
                && pl.getPlot().getArea().compareTo(BigDecimal.valueOf(0)) > 0)
            if (pl.getPlot().getArea().compareTo(BigDecimal.valueOf(125)) > 0) {
                processNotifiedRoads(pl, subRule, scrutinyDetail2);
                processNonNotifiedRoadsForPlotExceeding125(pl, subRule, scrutinyDetail2);
                processCuldesacRoad(pl, subRule, scrutinyDetail2);
                processLaneRoads(pl, subRule, scrutinyDetail2);
            } else if (pl.getPlot().getArea().compareTo(BigDecimal.valueOf(125)) < 0) {
                processNotifiedRoads(pl, subRule, scrutinyDetail2);
                processNonNotifiedRoadsPlotLessThan125(pl, subRule, scrutinyDetail2);
                processCuldesacRoad(pl, subRule, scrutinyDetail2);
                processLaneRoads(pl, subRule, scrutinyDetail2);
            }
    }

    private void processNonNotifiedRoadsPlotLessThan125(PlanDetail pl, String subRule, ScrutinyDetail scrutinyDetail2) {
        if (!pl.getNonNotifiedRoads().isEmpty() && !pl.getNonNotifiedRoads().get(0).getDistanceFromAccessoryBlock().isEmpty()) {
            boolean valid = false;
            BigDecimal minimumDisFmNonNotifiedRoad = pl.getNonNotifiedRoads().get(0).getDistanceFromAccessoryBlock().get(0);
            for (BigDecimal disFrmAccBldg : pl.getNonNotifiedRoads().get(0).getDistanceFromAccessoryBlock())
                if (disFrmAccBldg.compareTo(minimumDisFmNonNotifiedRoad) < 0)
                    minimumDisFmNonNotifiedRoad = disFrmAccBldg;
            if (minimumDisFmNonNotifiedRoad.compareTo(BigDecimal.valueOf(2)) >= 0)
                valid = true;
            if (valid)
                setReportOutputDetails(pl, subRule, MIN_DIS_NON_NOTIFIED_ROAD_FROM_ACC_BLDG, String.valueOf(2),
                        minimumDisFmNonNotifiedRoad.toString(), Result.Accepted.getResultVal(), scrutinyDetail2);
            else
                setReportOutputDetails(pl, subRule, MIN_DIS_NON_NOTIFIED_ROAD_FROM_ACC_BLDG, String.valueOf(2),
                        minimumDisFmNonNotifiedRoad.toString(), Result.Not_Accepted.getResultVal(), scrutinyDetail2);
        }
    }

    private void processNonNotifiedRoadsForPlotExceeding125(PlanDetail pl, String subRule, ScrutinyDetail scrutinyDetail2) {
        if (!pl.getNonNotifiedRoads().isEmpty() && !pl.getNonNotifiedRoads().get(0).getDistanceFromAccessoryBlock().isEmpty()) {
            boolean valid = false;
            BigDecimal minimumDisFmNonNotifiedRoad = pl.getNonNotifiedRoads().get(0).getDistanceFromAccessoryBlock().get(0);
            for (BigDecimal disFrmAccBldg : pl.getNonNotifiedRoads().get(0).getDistanceFromAccessoryBlock())
                if (disFrmAccBldg.compareTo(minimumDisFmNonNotifiedRoad) < 0)
                    minimumDisFmNonNotifiedRoad = disFrmAccBldg;
            if (minimumDisFmNonNotifiedRoad.compareTo(BigDecimal.valueOf(3)) >= 0)
                valid = true;
            if (valid)
                setReportOutputDetails(pl, subRule, MIN_DIS_NON_NOTIFIED_ROAD_FROM_ACC_BLDG, String.valueOf(3),
                        minimumDisFmNonNotifiedRoad.toString(), Result.Accepted.getResultVal(), scrutinyDetail2);
            else
                setReportOutputDetails(pl, subRule, MIN_DIS_NON_NOTIFIED_ROAD_FROM_ACC_BLDG, String.valueOf(3),
                        minimumDisFmNonNotifiedRoad.toString(), Result.Not_Accepted.getResultVal(), scrutinyDetail2);
        }
    }

    private void processLaneRoads(PlanDetail pl, String subRule, ScrutinyDetail scrutinyDetail2) {
        if (!pl.getLaneRoads().isEmpty() && !pl.getLaneRoads().get(0).getDistanceFromAccessoryBlock().isEmpty()) {
            boolean valid = false;
            BigDecimal minimumDisFmLaneRoad = pl.getLaneRoads().get(0).getDistanceFromAccessoryBlock().get(0);
            for (BigDecimal disFrmAccBldg : pl.getLaneRoads().get(0).getDistanceFromAccessoryBlock())
                if (disFrmAccBldg.compareTo(minimumDisFmLaneRoad) < 0)
                    minimumDisFmLaneRoad = disFrmAccBldg;
            if (minimumDisFmLaneRoad.compareTo(BigDecimal.valueOf(1.5)) >= 0)
                valid = true;
            if (valid)
                setReportOutputDetails(pl, subRule, MIN_DIS_LANE_ROAD_FROM_ACC_BLDG, String.valueOf(1.5),
                        minimumDisFmLaneRoad.toString(), Result.Accepted.getResultVal(), scrutinyDetail2);
            else
                setReportOutputDetails(pl, subRule, MIN_DIS_LANE_ROAD_FROM_ACC_BLDG, String.valueOf(1.5),
                        minimumDisFmLaneRoad.toString(), Result.Not_Accepted.getResultVal(), scrutinyDetail2);
        }
    }

    private void processCuldesacRoad(PlanDetail pl, String subRule, ScrutinyDetail scrutinyDetail2) {
        if (!pl.getCuldeSacRoads().isEmpty() && !pl.getCuldeSacRoads().get(0).getDistanceFromAccessoryBlock().isEmpty()) {
            boolean valid = false;
            BigDecimal minimumDisFmCuldesacRoad = pl.getCuldeSacRoads().get(0).getDistanceFromAccessoryBlock().get(0);
            for (BigDecimal disFrmAccBldg : pl.getCuldeSacRoads().get(0).getDistanceFromAccessoryBlock())
                if (disFrmAccBldg.compareTo(minimumDisFmCuldesacRoad) < 0)
                    minimumDisFmCuldesacRoad = disFrmAccBldg;
            if (minimumDisFmCuldesacRoad.compareTo(BigDecimal.valueOf(2)) >= 0)
                valid = true;
            if (valid)
                setReportOutputDetails(pl, subRule, MIN_DIS_CULDESAC_ROAD_FROM_ACC_BLDG, String.valueOf(2),
                        minimumDisFmCuldesacRoad.toString(), Result.Accepted.getResultVal(), scrutinyDetail2);
            else
                setReportOutputDetails(pl, subRule, MIN_DIS_CULDESAC_ROAD_FROM_ACC_BLDG, String.valueOf(2),
                        minimumDisFmCuldesacRoad.toString(), Result.Not_Accepted.getResultVal(), scrutinyDetail2);
        }
    }

    private void processNotifiedRoads(PlanDetail pl, String subRule, ScrutinyDetail scrutinyDetail2) {
        if (!pl.getNotifiedRoads().isEmpty() && !pl.getNotifiedRoads().get(0).getDistanceFromAccessoryBlock().isEmpty()) {
            boolean valid = false;
            BigDecimal minimumDisFmNotifiedRoad = pl.getNotifiedRoads().get(0).getDistanceFromAccessoryBlock().get(0);
            for (BigDecimal disFrmAccBldg : pl.getNotifiedRoads().get(0).getDistanceFromAccessoryBlock())
                if (disFrmAccBldg.compareTo(minimumDisFmNotifiedRoad) < 0)
                    minimumDisFmNotifiedRoad = disFrmAccBldg;
            if (minimumDisFmNotifiedRoad.compareTo(BigDecimal.valueOf(3)) >= 0)
                valid = true;
            if (valid)
                setReportOutputDetails(pl, subRule, MIN_DIS_NOTIFIED_ROAD_FROM_ACC_BLDG, String.valueOf(3),
                        minimumDisFmNotifiedRoad.toString(), Result.Accepted.getResultVal(), scrutinyDetail2);
            else
                setReportOutputDetails(pl, subRule, MIN_DIS_NOTIFIED_ROAD_FROM_ACC_BLDG, String.valueOf(3),
                        minimumDisFmNotifiedRoad.toString(), Result.Not_Accepted.getResultVal(), scrutinyDetail2);
        }
    }

    private void processHeightOfAccessoryBlock(PlanDetail pl) {
        ScrutinyDetail scrutinyDetail1 = new ScrutinyDetail();
        scrutinyDetail1.addColumnHeading(1, RULE_NO);
        scrutinyDetail1.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail1.addColumnHeading(3, REQUIRED);
        scrutinyDetail1.addColumnHeading(4, PROVIDED);
        scrutinyDetail1.addColumnHeading(5, STATUS);
        scrutinyDetail1.setKey("Common_Accessory Block - Maximum Height");
        String subRuleDesc = SUBRULE_88_3_DESC;
        String subRule = SUBULE_88_3;
        if (pl != null && !pl.getAccessoryBlocks().isEmpty())
            for (AccessoryBlock accessoryBlock : pl.getAccessoryBlocks()) {
                Boolean valid = false;
                if (accessoryBlock.getAccessoryBuilding() != null && accessoryBlock.getAccessoryBuilding().getHeight() != null
                        && accessoryBlock.getAccessoryBuilding().getHeight().compareTo(BigDecimal.valueOf(0)) > 0) {
                    if (accessoryBlock.getAccessoryBuilding().getHeight().compareTo(BigDecimal.valueOf(2.5)) <= 0)
                        valid = true;
                    if (valid) {
                        pl.reportOutput.add(buildRuleOutputWithSubRule(String.format(subRuleDesc, accessoryBlock.getNumber()),
                                subRule,
                                String.format(subRuleDesc, accessoryBlock.getNumber()),
                                String.format(subRuleDesc, accessoryBlock.getNumber()),
                                BigDecimal.valueOf(2.5) + DcrConstants.IN_METER,
                                accessoryBlock.getAccessoryBuilding().getHeight() + DcrConstants.IN_METER, Result.Accepted,
                                null));
                        setReportOutputDetails(pl, subRule, String.format(subRuleDesc, accessoryBlock.getNumber()),
                                BigDecimal.valueOf(2.5) + DcrConstants.IN_METER,
                                accessoryBlock.getAccessoryBuilding().getHeight() + DcrConstants.IN_METER,
                                Result.Accepted.getResultVal(), scrutinyDetail1);
                    } else {
                        pl.reportOutput.add(buildRuleOutputWithSubRule(String.format(subRuleDesc, accessoryBlock.getNumber()),
                                subRule,
                                String.format(subRuleDesc, accessoryBlock.getNumber()),
                                String.format(subRuleDesc, accessoryBlock.getNumber()),
                                BigDecimal.valueOf(2.5) + DcrConstants.IN_METER,
                                accessoryBlock.getAccessoryBuilding().getHeight() + DcrConstants.IN_METER, Result.Not_Accepted,
                                null));
                        setReportOutputDetails(pl, subRule, String.format(subRuleDesc, accessoryBlock.getNumber()),
                                BigDecimal.valueOf(2.5) + DcrConstants.IN_METER,
                                accessoryBlock.getAccessoryBuilding().getHeight() + DcrConstants.IN_METER,
                                Result.Not_Accepted.getResultVal(), scrutinyDetail1);

                    }
                }
            }
    }

    private void processAreaOfAccessoryBlock(PlanDetail pl) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.setKey("Common_Accessory Block - Maximum Area");
        String subRuleDesc = SUBRULE_88_1_DESC;
        String subRule = SUBRULE_88_1;
        if (pl != null && pl.getPlot() != null && pl.getPlot().getArea() != null && pl.getVirtualBuilding() != null
                && pl.getVirtualBuilding().getTotalCoverageArea() != null
                && pl.getVirtualBuilding().getTotalCoverageArea().compareTo(BigDecimal.valueOf(0)) > 0) {
            BigDecimal fifteenPercentOfEmptyArea = pl.getPlot().getArea().subtract(pl.getVirtualBuilding().getTotalCoverageArea())
                    .multiply(BigDecimal.valueOf(0.15));
            if (!pl.getAccessoryBlocks().isEmpty())
                for (AccessoryBlock accessoryBlock : pl.getAccessoryBlocks()) {
                    BigDecimal accessoryBlockArea = BigDecimal.ZERO;
                    if (accessoryBlock.getAccessoryBuilding() != null && accessoryBlock.getAccessoryBuilding().getArea() != null
                            && accessoryBlock.getAccessoryBuilding().getArea().compareTo(BigDecimal.valueOf(0)) > 0)
                        accessoryBlockArea = accessoryBlock.getAccessoryBuilding().getArea();
                    Boolean valid = false;
                    if (fifteenPercentOfEmptyArea != null &&
                            fifteenPercentOfEmptyArea.compareTo(BigDecimal.ZERO) > 0 && accessoryBlockArea != null &&
                            accessoryBlockArea.compareTo(BigDecimal.ZERO) > 0) {
                        if (fifteenPercentOfEmptyArea.compareTo(accessoryBlockArea) >= 0)
                            valid = true;
                        if (valid) {
                            pl.reportOutput.add(buildRuleOutputWithSubRule(String.format(subRuleDesc, accessoryBlock.getNumber()),
                                    subRule,
                                    String.format(subRuleDesc, accessoryBlock.getNumber()),
                                    String.format(subRuleDesc, accessoryBlock.getNumber()),
                                    fifteenPercentOfEmptyArea + DcrConstants.IN_METER_SQR,
                                    accessoryBlockArea + DcrConstants.IN_METER_SQR, Result.Accepted,
                                    null));
                            setReportOutputDetails(pl, subRule, String.format(subRuleDesc, accessoryBlock.getNumber()),
                                    fifteenPercentOfEmptyArea + DcrConstants.IN_METER_SQR,
                                    accessoryBlockArea + DcrConstants.IN_METER_SQR, Result.Accepted.getResultVal(),
                                    scrutinyDetail);
                        } else {
                            pl.reportOutput.add(buildRuleOutputWithSubRule(String.format(subRuleDesc, accessoryBlock.getNumber()),
                                    subRule,
                                    String.format(subRuleDesc, accessoryBlock.getNumber()),
                                    String.format(subRuleDesc, accessoryBlock.getNumber()),
                                    fifteenPercentOfEmptyArea + DcrConstants.IN_METER_SQR,
                                    accessoryBlockArea + DcrConstants.IN_METER_SQR, Result.Not_Accepted,
                                    null));
                            setReportOutputDetails(pl, subRule, String.format(subRuleDesc, accessoryBlock.getNumber()),
                                    fifteenPercentOfEmptyArea + DcrConstants.IN_METER_SQR,
                                    accessoryBlockArea + DcrConstants.IN_METER_SQR, Result.Not_Accepted.getResultVal(),
                                    scrutinyDetail);
                        }
                    }
                }
        }
    }

    private List<RoadOutput> extractDistanceWithColourCode(DXFDocument doc,
            List<DXFDimension> shortestDistanceCentralLineRoadDimension) {
        List<RoadOutput> roadOutputs = new ArrayList<>();

        if (null != shortestDistanceCentralLineRoadDimension)
            for (Object dxfEntity : shortestDistanceCentralLineRoadDimension) {
                BigDecimal value = BigDecimal.ZERO;

                DXFDimension line = (DXFDimension) dxfEntity;
                String dimensionBlock = line.getDimensionBlock();
                DXFBlock dxfBlock = doc.getDXFBlock(dimensionBlock);
                Iterator dxfEntitiesIterator = dxfBlock.getDXFEntitiesIterator();
                while (dxfEntitiesIterator.hasNext()) {
                    DXFEntity e = (DXFEntity) dxfEntitiesIterator.next();
                    if (e.getType().equals(DXFConstants.ENTITY_TYPE_MTEXT)) {
                        DXFMText text = (DXFMText) e;
                        String text2 = text.getText();
                        if (text2.contains(";"))
                            text2 = text2.split(";")[1];
                        else

                            text2 = text2.replaceAll("[^\\d`.]", "");
                        ;
                        if (!text2.isEmpty()) {
                            value = BigDecimal.valueOf(Double.parseDouble(text2));
                            RoadOutput roadOutput = new RoadOutput();
                            roadOutput.distance = value;
                            roadOutput.colourCode = String.valueOf(line.getColor());
                            roadOutputs.add(roadOutput);
                        }

                    }
                }
            }
        return roadOutputs;
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDesc, String expected, String actual,
            String status, ScrutinyDetail scrutinyDetail) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }
}
