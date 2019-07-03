package org.egov.edcr.feature;

import static org.egov.edcr.utility.ParametersConstants.BUILDING_HEIGHT;
import static org.egov.edcr.utility.ParametersConstants.COUNT_CHECK;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_HEIGHT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.HEIGHT;
import static org.egov.edcr.utility.ParametersConstants.PLOT_AREA;
import static org.egov.edcr.utility.ParametersConstants.WIDTH;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.FireStair;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.Polygon;
import org.egov.edcr.utility.math.Ray;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FireStairService extends GeneralRule implements RuleService {
    private static final Logger LOG = Logger.getLogger(FireStairService.class);
    final Ray rayCasting = new Ray(new Point(-1.123456789, -1.987654321, 0d));
    private static final String FLOOR = "Floor";
    private static final String RULE114 = "114";
    private static final String RULE42 = "42";
    private static final String EXPECTED_WIDTH = "0.75";
    private static final String EXPECTED_LINE = "0.75";
    private static final String EXPECTED_TREAD = "0.15";
    private static final String EXPECTED_TREAD_HIGHRISE = "0.2";
    private static final String WIDTH_DESCRIPTION = "Minimum width for fire stair %s";
    private static final String TREAD_DESCRIPTION = "Minimum tread for fire stair %s";
    private static final String LINE_DESCRIPTION = "Minimum length of line for fire stair %s flight layer";
    private static final String HEIGHT_FLOOR_DESCRIPTION = "Height of floor in layer ";
    private static final String FLIGHT_POLYLINE_NOT_DEFINED_DESCRIPTION = "Flight polyline is not defined in layer ";
    private static final String FLIGHT_LENGTH_DEFINED_DESCRIPTION = "Flight polyline length is not defined in layer ";
    private static final String FLIGHT_WIDTH_DEFINED_DESCRIPTION = "Flight polyline width is not defined in layer ";

    @Override
    public PlanDetail extract(PlanDetail planDetail, DXFDocument doc) {
        return planDetail;
    }

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        for (Block block : planDetail.getBlocks())
            if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                for (Floor floor : block.getBuilding().getFloors()) {
                    List<FireStair> fireStairs = floor.getFireStairs();
                    if (fireStairs != null && !fireStairs.isEmpty())
                        for (FireStair fireStair : fireStairs) {
                            List<Measurement> flightPolyLines = fireStair.getFlightPolyLines();
                            if (flightPolyLines != null && !flightPolyLines.isEmpty())
                                validateDimensions(planDetail, block.getNumber(), floor.getNumber(), fireStair.getNumber(),
                                        flightPolyLines);
                        }
                }

        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        // validate(planDetail);
        HashMap<String, String> errors = new HashMap<>();
        blk: for (Block block : planDetail.getBlocks())
            if (block.getBuilding() != null && !block.getBuilding().getOccupancies().isEmpty()) {
                if (Util.singleFamilyWithLessThanOrEqualToThreeFloor(block))
                    continue blk;
                ScrutinyDetail scrutinyDetail2 = new ScrutinyDetail();
                scrutinyDetail2.addColumnHeading(1, RULE_NO);
                scrutinyDetail2.addColumnHeading(2, FLOOR);
                scrutinyDetail2.addColumnHeading(3, DESCRIPTION);
                scrutinyDetail2.addColumnHeading(4, REQUIRED);
                scrutinyDetail2.addColumnHeading(5, PROVIDED);
                scrutinyDetail2.addColumnHeading(6, STATUS);
                scrutinyDetail2.setKey("Block_" + block.getNumber() + "_" + "Fire Stair - Width");

                ScrutinyDetail scrutinyDetail3 = new ScrutinyDetail();
                scrutinyDetail3.addColumnHeading(1, RULE_NO);
                scrutinyDetail3.addColumnHeading(2, FLOOR);
                scrutinyDetail3.addColumnHeading(3, DESCRIPTION);
                scrutinyDetail3.addColumnHeading(4, REQUIRED);
                scrutinyDetail3.addColumnHeading(5, PROVIDED);
                scrutinyDetail3.addColumnHeading(6, STATUS);
                scrutinyDetail3.setKey("Block_" + block.getNumber() + "_" + "Fire Stair - Tread");

                ScrutinyDetail scrutinyDetail5 = new ScrutinyDetail();
                scrutinyDetail5.addColumnHeading(1, RULE_NO);
                scrutinyDetail5.addColumnHeading(2, FLOOR);
                scrutinyDetail5.addColumnHeading(3, REQUIRED);
                scrutinyDetail5.addColumnHeading(4, PROVIDED);
                scrutinyDetail5.addColumnHeading(5, STATUS);
                scrutinyDetail5.setKey("Block_" + block.getNumber() + "_" + "Spiral Fire Stair - Diameter");

                ScrutinyDetail scrutinyDetail4 = new ScrutinyDetail();
                scrutinyDetail4.addColumnHeading(1, RULE_NO);
                scrutinyDetail4.addColumnHeading(2, REQUIRED);
                scrutinyDetail4.addColumnHeading(3, PROVIDED);
                scrutinyDetail4.addColumnHeading(4, STATUS);
                scrutinyDetail4.setKey("Block_" + block.getNumber() + "_" + "Fire Stair - Defined Or Not");

                ScrutinyDetail scrutinyDetail6 = new ScrutinyDetail();
                scrutinyDetail6.addColumnHeading(1, RULE_NO);
                scrutinyDetail6.addColumnHeading(2, FLOOR);
                scrutinyDetail6.addColumnHeading(3, DESCRIPTION);
                scrutinyDetail6.addColumnHeading(4, REQUIRED);
                scrutinyDetail6.addColumnHeading(5, PROVIDED);
                scrutinyDetail6.addColumnHeading(6, STATUS);
                scrutinyDetail6.setKey("Block_" + block.getNumber() + "_" + "Fire Stair - Length Of Line In Flight Layer");

                ScrutinyDetail scrutinyDetail7 = new ScrutinyDetail();
                scrutinyDetail7.addColumnHeading(1, RULE_NO);
                scrutinyDetail7.addColumnHeading(2, REQUIRED);
                scrutinyDetail7.addColumnHeading(3, PROVIDED);
                scrutinyDetail7.addColumnHeading(4, STATUS);
                scrutinyDetail7.setKey("Block_" + block.getNumber() + "_" + "Fire Stair - Abuting");

                List<Occupancy> occupancies = block.getBuilding().getOccupancies();
                List<OccupancyType> collect = occupancies.stream().map(occupancy -> occupancy.getType())
                        .collect(Collectors.toList());
                OccupancyType mostRestrictiveOccupancy = Util.getMostRestrictiveOccupancy(collect);
                List<Boolean> abutingList = new ArrayList<>();
                int fireStairCount = 0;
                int spiralStairCount = 0;

                String occupancyType = mostRestrictiveOccupancy != null ? mostRestrictiveOccupancy.getOccupancyType() : null;

                List<Floor> floors = block.getBuilding().getFloors();
                BigDecimal floorSize = block.getBuilding().getFloorsAboveGround();
                Floor topMostFloor = floors.stream()
                        .filter(floor -> floor.getTerrace() || floor.getUpperMost())
                        .findAny()
                        .orElse(null);
                for (Floor floor : floors) {

                    boolean isTypicalRepititiveFloor = false;
                    Map<String, Object> typicalFloorValues = Util.getTypicalFloorValues(block, floor, isTypicalRepititiveFloor);

                    List<FireStair> fireStairs = floor.getFireStairs();
                    fireStairCount = fireStairCount + fireStairs.size();
                    spiralStairCount = spiralStairCount + floor.getSpiralStairs().size();
                    List<DXFLWPolyline> builtUpAreaPolyLine = floor.getBuiltUpAreaPolyLine();
                    if (fireStairs.size() != 0)
                        for (FireStair fireStair : fireStairs) {
                            List<Measurement> flightPolyLines = fireStair.getFlightPolyLines();
                            List<BigDecimal> flightLengths = fireStair.getLengthOfFlights();
                            List<BigDecimal> flightWidths = fireStair.getWidthOfFlights();
                            // Boolean flightPolyLineClosed = fireStair.getFlightPolyLineClosed();
                            List<DXFLWPolyline> fireStairPolylines = fireStair.getStairPolylines();
                            BigDecimal minTread = BigDecimal.ZERO;
                            BigDecimal minFlightWidth = BigDecimal.ZERO;
                            String flightLayerName = String.format(DxfFileConstants.LAYER_FIRESTAIR_FLIGHT_FLOOR,
                                    block.getNumber(), floor.getNumber(), fireStair.getNumber());

                            if (builtUpAreaPolyLine != null && builtUpAreaPolyLine.size() > 0 &&
                                    fireStairPolylines != null && fireStairPolylines.size() > 0)
                                for (DXFLWPolyline builtUpPolyLine : builtUpAreaPolyLine) {
                                    Polygon builtUpPolygon = Util.getPolygon(builtUpPolyLine);

                                    for (DXFLWPolyline fireStairPolyLine : fireStairPolylines) {
                                        Iterator vertexIterator = fireStairPolyLine.getVertexIterator();
                                        while (vertexIterator.hasNext()) {
                                            DXFVertex dxfVertex = (DXFVertex) vertexIterator.next();
                                            Point point = dxfVertex.getPoint();
                                            if (rayCasting.contains(point, builtUpPolygon))
                                                abutingList.add(true);
                                            else
                                                abutingList.add(false);
                                        }
                                    }
                                }
                            boolean belowTopMostFloor = false;
                            if (topMostFloor != null)
                                if (topMostFloor.getTerrace())
                                    belowTopMostFloor = floor.getNumber() >= topMostFloor.getNumber() - 1;
                                else
                                    belowTopMostFloor = floor.getNumber() == topMostFloor.getNumber();
                            // boolean belowTopMostFloor = topMostFloor != null ? floor.getTerrace()?( floor.getNumber() ==
                            // topMostFloor.getNumber() -1):(floor.getNumber() == topMostFloor.getNumber()): false;
                            if (!floor.getTerrace() && !floor.getUpperMost() && !belowTopMostFloor)
                                if (flightPolyLines != null && flightPolyLines.size() > 0) {
                                    // if (flightPolyLineClosed) {
                                    if (flightWidths != null && flightWidths.size() > 0) {
                                        BigDecimal flightPolyLine = flightWidths.stream().reduce(BigDecimal::min).get();

                                        boolean valid = false;

                                        if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor")) {
                                            minFlightWidth = Util.roundOffTwoDecimal(flightPolyLine);
                                            BigDecimal minimumWidth = Util.roundOffTwoDecimal(BigDecimal.valueOf(0.75));

                                            if (minFlightWidth.compareTo(minimumWidth) >= 0)
                                                valid = true;
                                            String value = typicalFloorValues.get("typicalFloors") != null
                                                    ? (String) typicalFloorValues.get("typicalFloors")
                                                    : " floor " + floor.getNumber();

                                            if (valid)
                                                setReportOutputDetailsFloorStairWise(planDetail, RULE114, value,
                                                        String.format(WIDTH_DESCRIPTION, fireStair.getNumber()), EXPECTED_WIDTH,
                                                        String.valueOf(minFlightWidth), Result.Accepted.getResultVal(),
                                                        scrutinyDetail2);
                                            else
                                                setReportOutputDetailsFloorStairWise(planDetail, RULE114, value,
                                                        String.format(WIDTH_DESCRIPTION, fireStair.getNumber()), EXPECTED_WIDTH,
                                                        String.valueOf(minFlightWidth), Result.Not_Accepted.getResultVal(),
                                                        scrutinyDetail2);
                                        }

                                    } else {
                                        errors.put("Flight PolyLine width" + flightLayerName,
                                                FLIGHT_WIDTH_DEFINED_DESCRIPTION + flightLayerName);
                                        planDetail.addErrors(errors);
                                    }

                                    /*
                                     * (Total length of polygons in layer BLK_n_FLR_i_FIRESTAIR_k_FLIGHT) / (Number of rises -
                                     * number of polygons in layer BLK_n_FLR_i_FIRESTAIR_k_FLIGHT - number of lines in layer
                                     * BLK_n_FLR_i_FIRESTAIR_k_FLIGHT) shall not be more than 0.15 m
                                     */

                                    if (flightLengths != null && flightLengths.size() > 0)
                                        try {
                                            BigDecimal totalLength = flightLengths.stream()
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                                            totalLength = Util.roundOffTwoDecimal(totalLength);

                                            if (fireStair.getNoOfRises() != null) {
                                                BigDecimal denominator = fireStair.getNoOfRises()
                                                        .subtract(BigDecimal.valueOf(flightLengths.size()))
                                                        .subtract(BigDecimal.valueOf(fireStair.getLines().size()));

                                                minTread = totalLength.divide(denominator,
                                                        DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                                                        DcrConstants.ROUNDMODE_MEASUREMENTS);

                                                boolean valid = false;

                                                if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor")) {
                                                    // High Rise Building
                                                    boolean highRise = Util
                                                            .roundOffTwoDecimal(block.getBuilding().getBuildingHeight())
                                                            .compareTo(BigDecimal.valueOf(16)) > 0;

                                                    if (highRise
                                                            && Util.roundOffTwoDecimal(minTread).compareTo(
                                                                    Util.roundOffTwoDecimal(BigDecimal.valueOf(0.2))) >= 0) {
                                                        scrutinyDetail3.setKey("Block_" + block.getNumber() + "_"
                                                                + "Fire Stair - Tread (High Rise)");
                                                        valid = true;
                                                    } else if (Util.roundOffTwoDecimal(minTread)
                                                            .compareTo(Util.roundOffTwoDecimal(BigDecimal.valueOf(0.15))) >= 0)
                                                        valid = true;

                                                    String value = typicalFloorValues.get("typicalFloors") != null
                                                            ? (String) typicalFloorValues.get("typicalFloors")
                                                            : " floor " + floor.getNumber();
                                                    if (valid)
                                                        setReportOutputDetailsFloorStairWise(planDetail, RULE114, value,
                                                                String.format(TREAD_DESCRIPTION, fireStair.getNumber()),
                                                                highRise ? EXPECTED_TREAD_HIGHRISE : EXPECTED_TREAD,
                                                                String.valueOf(minTread), Result.Accepted.getResultVal(),
                                                                scrutinyDetail3);
                                                    else
                                                        setReportOutputDetailsFloorStairWise(planDetail, RULE114, value,
                                                                String.format(TREAD_DESCRIPTION, fireStair.getNumber()),
                                                                highRise ? EXPECTED_TREAD_HIGHRISE : EXPECTED_TREAD,
                                                                String.valueOf(minTread), Result.Not_Accepted.getResultVal(),
                                                                scrutinyDetail3);
                                                }
                                            } else {
                                                String layerName = String.format(DxfFileConstants.LAYER_FIRESTAIR_FLOOR,
                                                        block.getNumber(), floor.getNumber(), fireStair.getNumber());
                                                errors.put("FLR_HT_M " + layerName,
                                                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                                                new String[] { HEIGHT_FLOOR_DESCRIPTION + layerName },
                                                                LocaleContextHolder.getLocale()));
                                                planDetail.addErrors(errors);
                                            }
                                        } catch (ArithmeticException e) {
                                            LOG.info("Denominator is zero");
                                        }
                                    else {
                                        errors.put("Flight PolyLine length" + flightLayerName,
                                                FLIGHT_LENGTH_DEFINED_DESCRIPTION + flightLayerName);
                                        planDetail.addErrors(errors);

                                    }

                                    // }
                                } else {
                                    errors.put("Flight PolyLine " + flightLayerName,
                                            FLIGHT_POLYLINE_NOT_DEFINED_DESCRIPTION + flightLayerName);
                                    planDetail.addErrors(errors);
                                }

                            if (!floor.getTerrace() && !floor.getUpperMost() && !belowTopMostFloor) {

                                List<DXFLine> lines = fireStair.getLines();
                                if (lines != null && lines.size() > 0) {

                                    DXFLine line = lines.stream()
                                            .min(Comparator.comparing(DXFLine::getLength)).get();

                                    boolean valid = false;

                                    if (line != null) {
                                        BigDecimal lineLength = Util.roundOffTwoDecimal(BigDecimal.valueOf(line.getLength()));

                                        if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor")) {
                                            BigDecimal minLineLength = Util.roundOffTwoDecimal(BigDecimal.valueOf(0.75));

                                            if (lineLength.compareTo(minLineLength) >= 0)
                                                valid = true;
                                            String value = typicalFloorValues.get("typicalFloors") != null
                                                    ? (String) typicalFloorValues.get("typicalFloors")
                                                    : " floor " + floor.getNumber();

                                            if (valid)
                                                setReportOutputDetailsFloorStairWise(planDetail, RULE114, value,
                                                        String.format(LINE_DESCRIPTION, fireStair.getNumber()), EXPECTED_LINE,
                                                        String.valueOf(lineLength), Result.Accepted.getResultVal(),
                                                        scrutinyDetail6);
                                            else
                                                setReportOutputDetailsFloorStairWise(planDetail, RULE114, value,
                                                        String.format(LINE_DESCRIPTION, fireStair.getNumber()), EXPECTED_LINE,
                                                        String.valueOf(lineLength), Result.Not_Accepted.getResultVal(),
                                                        scrutinyDetail6);
                                        }

                                    }
                                }
                            }
                            if (minFlightWidth.compareTo(BigDecimal.valueOf(1.2)) >= 0
                                    && minTread.compareTo(BigDecimal.valueOf(0.3)) >= 0 && !floor.getTerrace())
                                fireStair.setGeneralStair(true);

                        }
                }

                boolean isAbuting = abutingList.stream().anyMatch(aBoolean -> aBoolean == true);

                if (occupancyType != null)
                    if (occupancyType.equalsIgnoreCase("RESIDENTIAL") || occupancyType.equalsIgnoreCase("Apartment/Flat")) {
                        if (floorSize.compareTo(BigDecimal.valueOf(3)) > 0)
                            if (fireStairCount > 0)
                                setReportOutputDetails(planDetail, RULE42, String.format(DcrConstants.RULE114, block.getNumber()),
                                        "",
                                        DcrConstants.OBJECTDEFINED_DESC, Result.Accepted.getResultVal(), scrutinyDetail4);
                            else if (spiralStairCount == 0)
                                setReportOutputDetails(planDetail, RULE42,
                                        String.format(DcrConstants.RULE114, block.getNumber()),
                                        "Minimum 1 fire stair is required",
                                        DcrConstants.OBJECTNOTDEFINED_DESC, Result.Not_Accepted.getResultVal(),
                                        scrutinyDetail4);
                    } else if (floorSize.compareTo(BigDecimal.valueOf(2)) > 0)
                        if (fireStairCount > 0)
                            setReportOutputDetails(planDetail, RULE42, String.format(DcrConstants.RULE114, block.getNumber()), "",
                                    DcrConstants.OBJECTDEFINED_DESC, Result.Accepted.getResultVal(), scrutinyDetail4);
                        else if (spiralStairCount == 0)
                            setReportOutputDetails(planDetail, RULE42, String.format(DcrConstants.RULE114, block.getNumber()), "",
                                    DcrConstants.OBJECTNOTDEFINED_DESC, Result.Not_Accepted.getResultVal(), scrutinyDetail4);

                if (fireStairCount > 0)
                    if (isAbuting)
                        setReportOutputDetails(planDetail, RULE114, String.format(DcrConstants.RULE114, block.getNumber()),
                                "should abut built up area",
                                "is abutting built up area", Result.Accepted.getResultVal(), scrutinyDetail7);
                    else
                        setReportOutputDetails(planDetail, RULE114, String.format(DcrConstants.RULE114, block.getNumber()),
                                "should abut built up area",
                                "is not abutting built up area", Result.Not_Accepted.getResultVal(), scrutinyDetail7);
            }

        return planDetail;
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

    private void setReportOutputDetailsFloorStairWise(PlanDetail pl, String ruleNo, String floor, String description,
            String expected, String actual, String status, ScrutinyDetail scrutinyDetail) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(FLOOR, floor);
        details.put(DESCRIPTION, description);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

    private void validateDimensions(PlanDetail planDetail, String blockNo, int floorNo, String stairNo,
            List<Measurement> flightPolyLines) {
        int count = 0;
        for (Measurement m : flightPolyLines)
            if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
                count++;
        if (count > 0)
            planDetail.addError(String.format(DxfFileConstants.LAYER_FIRESTAIR_FLIGHT_FLOOR, blockNo, floorNo, stairNo),
                    count + " number of flight polyline not having only 4 points in layer "
                            + String.format(DxfFileConstants.LAYER_FIRESTAIR_FLIGHT_FLOOR, blockNo, floorNo, stairNo));
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("BLK_%_FLR_%_FIRESTAIR_%");
        layers.add("BLK_%_FLR_%_FIRESTAIR_%_FLIGHT");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_AREA);
        parameters.add(BUILDING_HEIGHT);
        parameters.add(FLOOR_LEVEL_CHECK);
        parameters.add(FLOOR_COUNT);
        parameters.add(OCCUPANCY);
        parameters.add(COUNT_CHECK);
        parameters.add(FLOOR_HEIGHT);
        parameters.add(WIDTH);
        parameters.add(HEIGHT);
        return parameters;
    }

}