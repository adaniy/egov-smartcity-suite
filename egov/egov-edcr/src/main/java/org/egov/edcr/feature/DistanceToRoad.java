package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.DIST_CL_ROAD;
import static org.egov.edcr.constants.DxfFileConstants.SHORTEST_DISTANCE_TO_ROAD;
import static org.egov.edcr.utility.DcrConstants.CULDESAC_ROAD;
import static org.egov.edcr.utility.DcrConstants.CULDESAC_SHORTESTDISTINCTTOROADFROMCENTER;
import static org.egov.edcr.utility.DcrConstants.CULD_SAC_SHORTESTDISTINCTTOROAD;
import static org.egov.edcr.utility.DcrConstants.LANE_ROAD;
import static org.egov.edcr.utility.DcrConstants.LANE_SHORTESTDISTINCTTOROAD;
import static org.egov.edcr.utility.DcrConstants.LANE_SHORTESTDISTINCTTOROADFROMCENTER;
import static org.egov.edcr.utility.DcrConstants.NONNOTIFIED_ROAD;
import static org.egov.edcr.utility.DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD;
import static org.egov.edcr.utility.DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER;
import static org.egov.edcr.utility.DcrConstants.NOTIFIED_ROAD;
import static org.egov.edcr.utility.DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD;
import static org.egov.edcr.utility.DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RoadOutput;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.CulDeSacRoad;
import org.egov.edcr.entity.measurement.Lane;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFMText;
import org.kabeja.dxf.helpers.StyledTextParagraph;
import org.springframework.stereotype.Service;

@Service
public class DistanceToRoad extends GeneralRule implements RuleService {
    private static final String SUB_RULE_25_1 = "25(1)";
    private static final String SUB_RULE_25_1_PROVISIO = "25(1) Provisio";
    private static final String SUB_RULE_25_1_PROVISIO_DESC = "Distance from building to street boundary";
    private static final String SUB_RULE_26_DESCRIPTION = "Prohibition for constructions abutting public roads.";
    private static final String SUB_RULE_62_1DESCRIPTION = "Minimum distance between plot boundary and abutting Street.";
    private static final String SUB_RULE_26 = "26";
    private static final String RULE_62 = "62";
    private static final String SUB_RULE_62_1 = "62(1)";
    private static BigDecimal FIVE = BigDecimal.valueOf(5);
    private static BigDecimal THREE = BigDecimal.valueOf(3);
    private static BigDecimal SEVEN = BigDecimal.valueOf(7);
    private static BigDecimal TWO = BigDecimal.valueOf(2);
    private static BigDecimal ONEPOINTFIVE = BigDecimal.valueOf(1.5);
    private static final String RULE_EXPECTED_KEY = "meanofaccess.expected";
    private static final String RULE_ACTUAL_KEY = "meanofaccess.actual";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> notifiedRoads = Util.getPolyLinesByLayer(doc, DxfFileConstants.NOTIFIED_ROADS);
        for (DXFLWPolyline roadPline : notifiedRoads) {
            NotifiedRoad road = new NotifiedRoad();
            road.setPresentInDxf(true);
            road.setPolyLine(roadPline);
            pl.getNotifiedRoads().add(road);

        }
        List<DXFLWPolyline> nonNotifiedRoads = Util.getPolyLinesByLayer(doc, DxfFileConstants.NON_NOTIFIED_ROAD);
        for (DXFLWPolyline roadPline : nonNotifiedRoads) {

            NonNotifiedRoad road = new NonNotifiedRoad();
            road.setPresentInDxf(true);
            road.setPolyLine(roadPline);
            pl.getNonNotifiedRoads().add(road);

        }
        List<DXFLWPolyline> culdSacRoads = Util.getPolyLinesByLayer(doc, DxfFileConstants.CULD_1);
        for (DXFLWPolyline roadPline : culdSacRoads) {

            CulDeSacRoad road = new CulDeSacRoad();
            road.setPresentInDxf(true);
            road.setPolyLine(roadPline);
            pl.getCuldeSacRoads().add(road);

        }
        List<DXFLWPolyline> laneRoads = Util.getPolyLinesByLayer(doc, DxfFileConstants.LANE_1);
        for (DXFLWPolyline roadPline : laneRoads) {
            Lane road = new Lane();
            road.setPresentInDxf(true);
            road.setPolyLine(roadPline);
            pl.getLaneRoads().add(road);

        }

        extractShortestDistanceToPlotFromRoadCenter(doc, pl);
        extractShortestDistanceToPlot(doc, pl);
        return pl;

    }

    private void extractShortestDistanceToPlotFromRoadCenter(DXFDocument doc, PlanDetail pl) {
        List<DXFDimension> shortestDistanceCentralLineRoadDimension = Util.getDimensionsByLayer(doc,
                DIST_CL_ROAD);
        List<RoadOutput> shortDistainceFromCenter = new ArrayList<>();

        shortDistainceFromCenter = roadDistanceWithColourCode(doc, shortestDistanceCentralLineRoadDimension,
                shortDistainceFromCenter);

        List<BigDecimal> notifiedRoadDistance = new ArrayList<>();
        List<BigDecimal> nonNotifiedRoadDistance = new ArrayList<>();
        List<BigDecimal> culdesacRoadDistance = new ArrayList<>();
        List<BigDecimal> laneDistance = new ArrayList<>();

        for (RoadOutput roadOutput : shortDistainceFromCenter) {
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NOTIFIEDROAD)
                notifiedRoadDistance.add(roadOutput.roadDistainceToPlot);
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NONNOTIFIEDROAD)
                nonNotifiedRoadDistance.add(roadOutput.roadDistainceToPlot);
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_CULDESAC)
                culdesacRoadDistance.add(roadOutput.roadDistainceToPlot);
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_LANE)
                laneDistance.add(roadOutput.roadDistainceToPlot);
        }

        prepareRoadDetails(pl, notifiedRoadDistance, nonNotifiedRoadDistance, culdesacRoadDistance, laneDistance, DIST_CL_ROAD);
    }

    private void prepareRoadDetails(PlanDetail pl, List<BigDecimal> notifiedRoadDistance,
            List<BigDecimal> nonNotifiedRoadDistance, List<BigDecimal> culdesacRoadDistance, List<BigDecimal> laneDistance,
            String type) {

        if (!notifiedRoadDistance.isEmpty() && pl.getNotifiedRoads().isEmpty()) {
            NotifiedRoad road = new NotifiedRoad();
            road.setPresentInDxf(true);
            pl.getNotifiedRoads().add(road);
        }
        if (!nonNotifiedRoadDistance.isEmpty() && pl.getNonNotifiedRoads().isEmpty()) {
            NonNotifiedRoad road = new NonNotifiedRoad();
            road.setPresentInDxf(true);
            pl.getNonNotifiedRoads().add(road);
        }
        if (!culdesacRoadDistance.isEmpty() && pl.getCuldeSacRoads().isEmpty()) {
            CulDeSacRoad road = new CulDeSacRoad();
            road.setPresentInDxf(true);
            pl.getCuldeSacRoads().add(road);
        }
        if (!laneDistance.isEmpty() && pl.getLaneRoads().isEmpty()) {
            Lane road = new Lane();
            road.setPresentInDxf(true);
            pl.getLaneRoads().add(road);
        }
        // Adding multiple road distances into single notified road/non notified road/culdesac/lane road.
        for (BigDecimal notifyRoadDistnce : notifiedRoadDistance)
            if (!pl.getNotifiedRoads().isEmpty())
                if (SHORTEST_DISTANCE_TO_ROAD.equalsIgnoreCase(type))
                    pl.getNotifiedRoads().get(0).addShortestDistanceToRoad(notifyRoadDistnce);
                else
                    pl.getNotifiedRoads().get(0).addDistancesFromCenterToPlot(notifyRoadDistnce);
        for (BigDecimal nonNotifyRoadDistnce : nonNotifiedRoadDistance)
            if (!pl.getNonNotifiedRoads().isEmpty())
                if (SHORTEST_DISTANCE_TO_ROAD.equalsIgnoreCase(type))
                    pl.getNonNotifiedRoads().get(0).addShortestDistanceToRoad(nonNotifyRoadDistnce);
                else
                    pl.getNonNotifiedRoads().get(0).addDistancesFromCenterToPlot(nonNotifyRoadDistnce);
        for (BigDecimal culdesacRdDistance : culdesacRoadDistance)
            if (!pl.getCuldeSacRoads().isEmpty())
                if (SHORTEST_DISTANCE_TO_ROAD.equalsIgnoreCase(type))
                    pl.getCuldeSacRoads().get(0).addShortestDistanceToRoad(culdesacRdDistance);
                else
                    pl.getCuldeSacRoads().get(0).addDistancesFromCenterToPlot(culdesacRdDistance);
        for (BigDecimal laneDistnce : laneDistance)
            if (!pl.getLaneRoads().isEmpty())
                if (SHORTEST_DISTANCE_TO_ROAD.equalsIgnoreCase(type))
                    pl.getLaneRoads().get(0).addShortestDistanceToRoad(laneDistnce);
                else
                    pl.getLaneRoads().get(0).addDistancesFromCenterToPlot(laneDistnce);
    }

    private void extractShortestDistanceToPlot(DXFDocument doc, PlanDetail pl) {
        List<DXFDimension> shortestDistanceDimension = Util.getDimensionsByLayer(doc, SHORTEST_DISTANCE_TO_ROAD);
        List<RoadOutput> shortDistaineToPlot = new ArrayList<>();

        shortDistaineToPlot = roadDistanceWithColourCode(doc, shortestDistanceDimension, shortDistaineToPlot);

        List<BigDecimal> notifiedRoadDistance = new ArrayList<>();
        List<BigDecimal> nonNotifiedRoadDistance = new ArrayList<>();
        List<BigDecimal> culdesacRoadDistance = new ArrayList<>();
        List<BigDecimal> laneDistance = new ArrayList<>();

        for (RoadOutput roadOutput : shortDistaineToPlot) {
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NOTIFIEDROAD)
                notifiedRoadDistance.add(roadOutput.roadDistainceToPlot);
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NONNOTIFIEDROAD)
                nonNotifiedRoadDistance.add(roadOutput.roadDistainceToPlot);
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_CULDESAC)
                culdesacRoadDistance.add(roadOutput.roadDistainceToPlot);
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_LANE)
                laneDistance.add(roadOutput.roadDistainceToPlot);
        }

        prepareRoadDetails(pl, notifiedRoadDistance, nonNotifiedRoadDistance, culdesacRoadDistance, laneDistance,
                SHORTEST_DISTANCE_TO_ROAD);
    }

    private List<RoadOutput> roadDistanceWithColourCode(DXFDocument doc,
            List<DXFDimension> shortestDistanceCentralLineRoadDimension,
            List<RoadOutput> clcolourCodeWithDimension) {
        if (null != shortestDistanceCentralLineRoadDimension)
            for (Object dxfEntity : shortestDistanceCentralLineRoadDimension) {
                BigDecimal value;

                DXFDimension line = (DXFDimension) dxfEntity;
                String dimensionBlock = line.getDimensionBlock();
                DXFBlock dxfBlock = doc.getDXFBlock(dimensionBlock);
                Iterator dxfEntitiesIterator = dxfBlock.getDXFEntitiesIterator();
                while (dxfEntitiesIterator.hasNext()) {
                    DXFEntity e = (DXFEntity) dxfEntitiesIterator.next();
                    if (e.getType().equals(DXFConstants.ENTITY_TYPE_MTEXT)) {
                        DXFMText text = (DXFMText) e;
                        String text2 = text.getText();

                        Iterator styledParagraphIterator = text.getTextDocument().getStyledParagraphIterator();

                        while (styledParagraphIterator.hasNext()) {
                            StyledTextParagraph next = (StyledTextParagraph) styledParagraphIterator.next();
                            text2 = next.getText();
                        }

                        if (text2.contains(";")) {
                            String[] textSplit = text2.split(";");
                            int length = textSplit.length;

                            if (length >= 1) {
                                int index = length - 1;
                                text2 = textSplit[index];
                                text2 = text2.replaceAll("[^\\d.]", "");
                            } else
                                text2 = text2.replaceAll("[^\\d.]", "");
                        } else
                            text2 = text2.replaceAll("[^\\d.]", "");

                        if (!text2.isEmpty()) {
                            value = BigDecimal.valueOf(Double.parseDouble(text2));
                            RoadOutput roadOutput = new RoadOutput();
                            roadOutput.roadDistainceToPlot = value;
                            roadOutput.colourCode = String.valueOf(line.getColor());
                            clcolourCodeWithDimension.add(roadOutput);
                        }

                    }
                }

            }
        return clcolourCodeWithDimension;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();

        boolean shortestDistanceDefined = false;

        if (pl.getNotifiedRoads().isEmpty() && pl.getCuldeSacRoads().isEmpty() && pl.getLaneRoads().isEmpty() &&
                pl.getNonNotifiedRoads().isEmpty()) {
            errors.put(DcrConstants.ROAD,
                    prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.ROAD));
            pl.addErrors(errors);
        }

        if (!pl.getNotifiedRoads().isEmpty())
            for (NotifiedRoad notifiedRoad : pl.getNotifiedRoads())
                for (BigDecimal shortestDistanceToRoad : notifiedRoad.getShortestDistanceToRoad())
                    if (shortestDistanceToRoad.compareTo(BigDecimal.ZERO) > 0) {
                        shortestDistanceDefined = true;
                        continue;
                    }
        if (!pl.getNonNotifiedRoads().isEmpty())
            for (NonNotifiedRoad nonNotifiedRoad : pl.getNonNotifiedRoads())
                for (BigDecimal shortestDistanceToRoad : nonNotifiedRoad.getShortestDistanceToRoad())
                    if (shortestDistanceToRoad.compareTo(BigDecimal.ZERO) > 0) {
                        shortestDistanceDefined = true;
                        continue;
                    }
        if (!pl.getLaneRoads().isEmpty())
            for (Lane laneRoad : pl.getLaneRoads())
                for (BigDecimal shortestDistanceToRoad : laneRoad.getShortestDistanceToRoad())
                    if (shortestDistanceToRoad.compareTo(BigDecimal.ZERO) > 0) {
                        shortestDistanceDefined = true;
                        continue;
                    }
        if (!pl.getCuldeSacRoads().isEmpty())
            for (CulDeSacRoad culdSac : pl.getCuldeSacRoads())
                for (BigDecimal shortestDistanceToRoad : culdSac.getShortestDistanceToRoad())
                    if (shortestDistanceToRoad.compareTo(BigDecimal.ZERO) > 0) {
                        shortestDistanceDefined = true;
                        continue;

                    }

        if (!shortestDistanceDefined) {
            errors.put(DcrConstants.SHORTESTDISTINCTTOROAD, prepareMessage(DcrConstants.OBJECTNOTDEFINED,
                    DcrConstants.SHORTESTDISTINCTTOROAD));
            pl.addErrors(errors);
        }

        // Distance from center of road mandatory if road defined.
        /*
         * For building not more than 3 floor, with less than or equal to 125 plot area, occupancy either residential or
         * commercial are exempted from "Distance from center road" check
         */
        if (!Util.isSmallPlot(pl)) {
            if (!pl.getNotifiedRoads().isEmpty() && pl.getNotifiedRoads().get(0).getDistancesFromCenterToPlot().isEmpty()) {
                errors.put(DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER,
                        prepareMessage(OBJECTNOTDEFINED, DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER));
                pl.addErrors(errors);
            }
            if (!pl.getNonNotifiedRoads().isEmpty() && pl.getNonNotifiedRoads().get(0).getDistancesFromCenterToPlot().isEmpty()) {
                errors.put(DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER,
                        prepareMessage(OBJECTNOTDEFINED, DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER));
                pl.addErrors(errors);
            }
            BigDecimal minimumHeightOfBuilding = BigDecimal.ZERO;
            for (Block block : pl.getBlocks())
                if (minimumHeightOfBuilding.compareTo(BigDecimal.ZERO) == 0 ||
                        block.getBuilding().getBuildingHeight().compareTo(minimumHeightOfBuilding) < 0)
                    minimumHeightOfBuilding = block.getBuilding().getBuildingHeight();

            if (minimumHeightOfBuilding != null && minimumHeightOfBuilding.compareTo(BigDecimal.valueOf(7)) > 0) {
                if (!pl.getCuldeSacRoads().isEmpty() && pl.getCuldeSacRoads().get(0).getDistancesFromCenterToPlot().isEmpty()) {
                    errors.put(DcrConstants.CULDESAC_SHORTESTDISTINCTTOROADFROMCENTER,
                            prepareMessage(OBJECTNOTDEFINED, DcrConstants.CULDESAC_SHORTESTDISTINCTTOROADFROMCENTER));
                    pl.addErrors(errors);
                }
                if (!pl.getLaneRoads().isEmpty() && pl.getLaneRoads().get(0).getDistancesFromCenterToPlot().isEmpty()) {
                    errors.put(DcrConstants.LANE_SHORTESTDISTINCTTOROADFROMCENTER,
                            prepareMessage(OBJECTNOTDEFINED, DcrConstants.LANE_SHORTESTDISTINCTTOROADFROMCENTER));
                    pl.addErrors(errors);
                }
            }
        }
        return pl;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        validate(planDetail);
        BigDecimal exptectedDistance;

        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey("Common_Distance to Road");
        // detail.setHeading("Distance to Road");
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);

        BigDecimal minimumHeightOfBuilding = BigDecimal.ZERO;
        for (Block block : planDetail.getBlocks())
            if (minimumHeightOfBuilding.compareTo(BigDecimal.ZERO) == 0 ||
                    block.getBuilding().getBuildingHeight().compareTo(minimumHeightOfBuilding) < 0)
                minimumHeightOfBuilding = block.getBuilding().getBuildingHeight();

        // validating minimum distance in notified roads minimum 5m
        if (planDetail.getNotifiedRoads() != null && !planDetail.getNotifiedRoads().isEmpty()) {

            exptectedDistance = FIVE;
            if (!Util.isSmallPlot(planDetail))
                if (planDetail.getNotifiedRoads().get(0).getDistancesFromCenterToPlot() != null &&
                        !planDetail.getNotifiedRoads().get(0).getDistancesFromCenterToPlot().isEmpty())
                    checkBuildingDistanceFromRoad(planDetail, exptectedDistance,
                            planDetail.getNotifiedRoads().get(0).getDistancesFromCenterToPlot(),
                            NOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER, SUB_RULE_25_1, SUB_RULE_25_1,
                            NOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER);// SUB_RULE_25_1_DESCRIPTION
            if (planDetail.getNotifiedRoads().get(0).getShortestDistanceToRoad() != null
                    && !planDetail.getNotifiedRoads().get(0).getShortestDistanceToRoad().isEmpty())
                checkBuildingDistanceFromRoad(planDetail, THREE,
                        planDetail.getNotifiedRoads().get(0).getShortestDistanceToRoad(),
                        NOTIFIED_SHORTESTDISTINCTTOROAD, SUB_RULE_26, SUB_RULE_26, NOTIFIED_ROAD + SUB_RULE_26_DESCRIPTION);
        }
        // validating minimum distance in non-notified roads minimum 5m
        if (planDetail.getNonNotifiedRoads() != null && !planDetail.getNonNotifiedRoads().isEmpty()) {
            exptectedDistance = FIVE;
            if (!Util.isSmallPlot(planDetail))
                if (planDetail.getNonNotifiedRoads().get(0).getDistancesFromCenterToPlot() != null &&
                        !planDetail.getNonNotifiedRoads().get(0).getDistancesFromCenterToPlot().isEmpty())
                    checkBuildingDistanceFromRoad(planDetail, exptectedDistance,
                            planDetail.getNonNotifiedRoads().get(0).getDistancesFromCenterToPlot(),
                            NONNOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER, SUB_RULE_25_1, SUB_RULE_25_1,
                            NONNOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER);
            if (planDetail.getNonNotifiedRoads().get(0).getShortestDistanceToRoad() != null &&
                    !planDetail.getNonNotifiedRoads().get(0).getShortestDistanceToRoad().isEmpty())
                if (Util.isSmallPlot(planDetail))
                    checkBuildingDistanceFromRoad(planDetail, TWO,
                            planDetail.getNonNotifiedRoads().get(0).getShortestDistanceToRoad(),
                            NONNOTIFIED_SHORTESTDISTINCTTOROAD, RULE_62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION);
                else
                    checkBuildingDistanceFromRoad(planDetail, THREE,
                            planDetail.getNonNotifiedRoads().get(0).getShortestDistanceToRoad(),
                            NONNOTIFIED_SHORTESTDISTINCTTOROAD, SUB_RULE_25_1, SUB_RULE_25_1,
                            NONNOTIFIED_ROAD + SUB_RULE_25_1_PROVISIO_DESC);
        }
        // validating minimum distance in culd_sac_road minimum 2 or 3 based on height of building
        if (planDetail.getCuldeSacRoads() != null && !planDetail.getCuldeSacRoads().isEmpty()) {
            if (Util.isSmallPlot(planDetail))
                exptectedDistance = TWO;
            else if (minimumHeightOfBuilding.compareTo(SEVEN) <= 0)
                exptectedDistance = TWO;
            else
                exptectedDistance = THREE;
            if (planDetail.getCuldeSacRoads().get(0).getShortestDistanceToRoad() != null
                    && !planDetail.getCuldeSacRoads().get(0).getShortestDistanceToRoad().isEmpty())
                checkBuildingDistanceFromRoad(planDetail, exptectedDistance,
                        planDetail.getCuldeSacRoads().get(0).getShortestDistanceToRoad(),
                        CULD_SAC_SHORTESTDISTINCTTOROAD, SUB_RULE_25_1_PROVISIO, SUB_RULE_25_1_PROVISIO,
                        CULDESAC_ROAD + SUB_RULE_25_1_PROVISIO_DESC);

            if (minimumHeightOfBuilding.compareTo(SEVEN) > 0) {
                exptectedDistance = FIVE;
                if (!Util.isSmallPlot(planDetail))
                    if (planDetail.getCuldeSacRoads().get(0).getDistancesFromCenterToPlot() != null &&
                            !planDetail.getCuldeSacRoads().get(0).getDistancesFromCenterToPlot().isEmpty())
                        checkBuildingDistanceFromRoad(planDetail, exptectedDistance,
                                planDetail.getCuldeSacRoads().get(0).getDistancesFromCenterToPlot(),
                                CULDESAC_SHORTESTDISTINCTTOROADFROMCENTER, SUB_RULE_25_1, SUB_RULE_25_1,
                                CULDESAC_SHORTESTDISTINCTTOROADFROMCENTER);
            }
        }

        // validating minimum distance in lane roads minimum 5m
        if (planDetail.getLaneRoads() != null && !planDetail.getLaneRoads().isEmpty()) {
            if (Util.isSmallPlot(planDetail))
                exptectedDistance = ONEPOINTFIVE;
            else if (minimumHeightOfBuilding.compareTo(SEVEN) <= 0)
                exptectedDistance = ONEPOINTFIVE;
            else
                exptectedDistance = THREE;
            if (planDetail.getLaneRoads().get(0).getShortestDistanceToRoad() != null
                    && !planDetail.getLaneRoads().get(0).getShortestDistanceToRoad().isEmpty())
                checkBuildingDistanceFromRoad(planDetail, exptectedDistance,
                        planDetail.getLaneRoads().get(0).getShortestDistanceToRoad(),
                        LANE_SHORTESTDISTINCTTOROAD, SUB_RULE_25_1_PROVISIO, SUB_RULE_25_1_PROVISIO,
                        LANE_ROAD + SUB_RULE_25_1_PROVISIO_DESC);
            if (minimumHeightOfBuilding.compareTo(SEVEN) > 0) {
                exptectedDistance = FIVE;
                if (!Util.isSmallPlot(planDetail))
                    if (planDetail.getLaneRoads().get(0).getDistancesFromCenterToPlot() != null &&
                            !planDetail.getLaneRoads().get(0).getDistancesFromCenterToPlot().isEmpty())
                        checkBuildingDistanceFromRoad(planDetail, exptectedDistance,
                                planDetail.getLaneRoads().get(0).getDistancesFromCenterToPlot(),
                                LANE_SHORTESTDISTINCTTOROADFROMCENTER, SUB_RULE_25_1, SUB_RULE_25_1,
                                LANE_SHORTESTDISTINCTTOROADFROMCENTER);
            }
        }

        return planDetail;

    }

    private void checkBuildingDistanceFromRoad(PlanDetail planDetail, BigDecimal exptectedDistance,
            List<BigDecimal> roadDistances, String fieldVerified, String subRule, String rule, String subRuleDesc) {

        /*
         * BigDecimal minimumDistance =null; //Take minimum distance among road distance for (BigDecimal distance : roadDistances)
         * { if(minimumDistance==null) minimumDistance=distance; else if(distance.compareTo(minimumDistance)<0)
         * minimumDistance=distance; } if(minimumDistance!=null)
         */
        for (BigDecimal minimumDistance : roadDistances) {
            String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, exptectedDistance.toString());
            String actualResult = getLocaleMessage(RULE_ACTUAL_KEY, minimumDistance.toString());
            // compare minimum road distance with minimum expected value.
            if (exptectedDistance.compareTo(minimumDistance) > 0) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc, fieldVerified,
                                expectedResult, actualResult, Result.Not_Accepted, null));
                Map<String, String> details = new HashMap<>();
                details.put(RULE_NO, subRule);
                details.put(DESCRIPTION, subRuleDesc);
                details.put(REQUIRED, expectedResult);
                details.put(PROVIDED, actualResult);
                details.put(STATUS, Result.Not_Accepted.getResultVal());
                scrutinyDetail.getDetail().add(details);
                planDetail.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc, fieldVerified,
                                expectedResult, actualResult, Result.Not_Accepted, null));

            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc, fieldVerified,
                                expectedResult, actualResult, Result.Accepted, null));
                Map<String, String> details = new HashMap<>();
                details.put(RULE_NO, subRule);
                details.put(DESCRIPTION, subRuleDesc);
                details.put(REQUIRED, expectedResult);
                details.put(PROVIDED, actualResult);
                details.put(STATUS, Result.Accepted.getResultVal());
                scrutinyDetail.getDetail().add(details);
                planDetail.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc, fieldVerified,
                                expectedResult, actualResult, Result.Accepted, null));

            }

        }
    }

}
