package org.egov.edcr.rule;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.egov.edcr.utility.ParametersConstants.BLOCK_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_HEIGHT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_UNITS;
import static org.egov.edcr.utility.ParametersConstants.HEIGHT;
import static org.egov.edcr.utility.ParametersConstants.PLOT_AREA;
import static org.egov.edcr.utility.ParametersConstants.WIDTH;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.DARamp;
import org.egov.edcr.entity.DARoom;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.FloorUnit;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Ramp;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.TypicalFloor;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.feature.GeneralRule;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RampService extends GeneralRule implements RuleService {

    private static final String SUBRULE_40_A3 = "40A(3)";
    private static final String SUBRULE_40_A7 = "40A(7)";
    private static final String SUBRULE_40 = "40";
    private static final String SUBRULE_40_A1 = "40A(1)";
    private static final String SUBRULE_40_A_7_DESC = "Minimum number of DA Rooms in block %s ";
    private static final String SUBRULE_40_A_3_WIDTH_DESC = "Minimum Width of Ramp %s for block %s ";
    private static final String SUBRULE_40_DESC = "Maximum slope of ramp %s for block %s ";
    private static final String SUBRULE_40_DESCRIPTION = "Maximum slope of ramp %s";

    private static final String SUBRULE_40_A_1_DESC = "DA Ramp";
    private static final String SUBRULE_40_A_3_SLOPE_DESC = "Maximum Slope of DA Ramp %s for block %s";
    private static final String SUBRULE_40_A_3_SLOPE_DESCRIPTION = "Maximum Slope of DA Ramp %s";
    private static final String FLOOR = "Floor";
    private static final String SUBRULE_40_A_3_WIDTH_DESCRIPTION = "Minimum Width of Ramp %s";
    private static final String SUBRULE_40_A_3_SLOPE_MAN_DESC = "Slope of DA Ramp";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && !pl.getBlocks().isEmpty())
            for (Block block : pl.getBlocks())
                if (!block.getCompletelyExisting()) {
                    String rampLayerNameRegex = String.format(DxfFileConstants.LAYER_DA_RAMP, block.getNumber()) + "_+\\d";
                    List<String> rampLayerNames = Util.getLayerNamesLike(doc, rampLayerNameRegex);
                    for (String rampLayerName : rampLayerNames) {
                        List<DXFLWPolyline> polyLines = Util.getPolyLinesByLayer(doc, rampLayerName);
                        String[] layerArray = rampLayerName.split("_", 5);
                        String text = Util.getMtextByLayerName(doc, rampLayerName);
                        BigDecimal slope = BigDecimal.ZERO;
                        if (StringUtils.isNotBlank(text)) {
                            String[] textArray = text.split("=", 2);
                            String slopeText = "";
                            if (textArray.length > 1)
                                slopeText = textArray[1];
                            String[] slopeDividendAndDivisor = {};
                            if (StringUtils.isNotBlank(slopeText)) {
                                slopeText = slopeText.toUpperCase();
                                if (slopeText.contains("IN")) {
                                    slopeDividendAndDivisor = slopeText.split("IN", 2);

                                    if (slopeDividendAndDivisor != null && slopeDividendAndDivisor.length > 1
                                            && slopeDividendAndDivisor[0] != null
                                            && slopeDividendAndDivisor[1] != null) {
                                        slopeDividendAndDivisor[0] = slopeDividendAndDivisor[0].replaceAll("[^\\d.]",
                                                "");
                                        slopeDividendAndDivisor[1] = slopeDividendAndDivisor[1].replaceAll("[^\\d.]",
                                                "");
                                        if (StringUtils.isNotBlank(slopeDividendAndDivisor[0])
                                                && StringUtils.isNotBlank(slopeDividendAndDivisor[1])) {
                                            BigDecimal numerator = getNumericValue(slopeDividendAndDivisor[0], pl,
                                                    "Slope value in " + rampLayerName);
                                            BigDecimal denominator = getNumericValue(slopeDividendAndDivisor[1], pl,
                                                    "Slope value in " + rampLayerName);
                                            if (numerator != null && denominator != null
                                                    && denominator.compareTo(BigDecimal.ZERO) > 0)
                                                slope = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
                                        } else {
                                            errors.put("Slope format defined " + rampLayerName,
                                                    "Slope format defined in layer " + rampLayerName
                                                            + " is not valid (to be defined like ex: 1IN12).");
                                            pl.addErrors(errors);
                                        }
                                    }
                                } else {
                                    errors.put("Slope format defined " + rampLayerName, "Slope format defined in layer "
                                            + rampLayerName + " is not valid (to be defined like ex: 1IN12).");
                                    pl.addErrors(errors);
                                }

                            }
                        }
                        if (!polyLines.isEmpty() && polyLines != null && !layerArray[4].isEmpty() && layerArray[4] != null) {
                            DARamp daRamp = new DARamp();
                            daRamp.setNumber(Integer.valueOf(layerArray[4]));
                            daRamp.setPolylines(polyLines);
                            daRamp.setPresentInDxf(true);
                            daRamp.setSlope(slope);
                            block.addDARamps(daRamp);
                        }

                    }
                    if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
                        outside: for (Floor floor : block.getBuilding().getFloors()) {
                            if (!block.getTypicalFloor().isEmpty())
                                for (TypicalFloor tp : block.getTypicalFloor())
                                    if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                                        for (Floor allFloors : block.getBuilding().getFloors())
                                            if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                                                if (!allFloors.getDaRooms().isEmpty()) {
                                                    floor.setDaRooms(allFloors.getDaRooms());
                                                    continue outside;
                                                }
                            String daRoomLayerName = String.format(DxfFileConstants.LAYER_DA_ROOM, block.getNumber(),
                                    floor.getNumber());
                            List<DXFLWPolyline> polyLinesByLayer = Util.getPolyLinesByLayer(doc, daRoomLayerName);
                            if (!polyLinesByLayer.isEmpty() && polyLinesByLayer != null)
                                for (DXFLWPolyline polyline : polyLinesByLayer) {
                                    DARoom daRoom = new DARoom();
                                    daRoom.setPolyLine(polyline);
                                    daRoom.setPresentInDxf(true);
                                    floor.addDaRoom(daRoom);
                                }
                        }
                        outside: for (Floor floor : block.getBuilding().getFloors()) {
                            if (!block.getTypicalFloor().isEmpty())
                                for (TypicalFloor tp : block.getTypicalFloor())
                                    if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                                        for (Floor allFloors : block.getBuilding().getFloors())
                                            if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                                                if (!allFloors.getRamps().isEmpty()) {
                                                    floor.setRamps(allFloors.getRamps());
                                                    continue outside;
                                                }
                            String rampRegex = String.format(DxfFileConstants.LAYER_RAMP, block.getNumber(), floor.getNumber())
                                    + "_+\\d";
                            List<String> rampLayer = Util.getLayerNamesLike(doc, rampRegex);
                            if (!rampLayer.isEmpty())
                                for (String rmpLayer : rampLayer) {
                                    List<DXFLWPolyline> polylines = Util.getPolyLinesByLayer(doc, rmpLayer);
                                    String[] splitLayer = rmpLayer.split("_", 6);
                                    if (splitLayer[5] != null && !splitLayer[5].isEmpty() && !polylines.isEmpty()) {
                                        Ramp ramp = new Ramp();
                                        ramp.setNumber(Integer.valueOf(splitLayer[5]));
                                        boolean isClosed = polylines.stream().allMatch(dxflwPolyline -> dxflwPolyline.isClosed());
                                        ramp.setRampPolyLineClosed(isClosed);
                                        List<Measurement> rampPolyLine = polylines.stream()
                                                .map(dxflwPolyline -> new Measurement(dxflwPolyline, true))
                                                .collect(Collectors.toList());
                                        ramp.setRampPolyLines(rampPolyLine);
                                        String floorHeight = Util.getMtextByLayerName(doc, rmpLayer, "FLR_HT_M");

                                        if (!isBlank(floorHeight)) {
                                            if (floorHeight.contains("="))
                                                floorHeight = floorHeight.split("=")[1] != null
                                                        ? floorHeight.split("=")[1].replaceAll("[^\\d.]", "")
                                                        : "";
                                            else
                                                floorHeight = floorHeight.replaceAll("[^\\d.]", "");

                                            if (!isBlank(floorHeight)) {
                                                BigDecimal height = BigDecimal.valueOf(Double.parseDouble(floorHeight));
                                                ramp.setFloorHeight(height);
                                            }
                                            floor.addRamps(ramp);
                                        }
                                    }
                                }
                        }
                    }
                }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        for (Block block : pl.getBlocks())
            if (!block.getCompletelyExisting())
                if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                    for (Floor floor : block.getBuilding().getFloors())
                        if (!floor.getTerrace()) {
                            List<Ramp> ramps = floor.getRamps();
                            if (ramps != null && !ramps.isEmpty())
                                for (Ramp ramp : ramps) {
                                    List<Measurement> rampPolyLines = ramp.getRampPolyLines();
                                    if (rampPolyLines != null && !rampPolyLines.isEmpty())
                                        validateDimensions(pl, block.getNumber(), floor.getNumber(), ramp.getNumber().toString(),
                                                rampPolyLines);
                                }
                        }

        // validate necessary
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && !pl.getBlocks().isEmpty())
            blk: for (Block block : pl.getBlocks())
                if (!block.getCompletelyExisting()) {
                    if (block.getBuilding() != null && !block.getBuilding().getOccupancies().isEmpty()) {
                        if (Util.checkExemptionConditionForBuildingParts(block) ||
                                Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block))
                            continue blk;
                        List<OccupancyType> occupancyTypeList = block.getBuilding().getOccupancies().stream()
                                .map(occupancy -> occupancy.getType()).collect(Collectors.toList());
                        for (OccupancyType occupancyType : occupancyTypeList)
                            if (getOccupanciesForRamp(occupancyType))
                                if (block.getDARamps().isEmpty()) {
                                    errors.put(String.format(DcrConstants.RAMP, block.getNumber()),
                                            edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                                    new String[] { String.format(DcrConstants.RAMP, block.getNumber()) },
                                                    LocaleContextHolder.getLocale()));
                                    pl.addErrors(errors);
                                    break;
                                }
                    }
                    if (pl.getPlot() != null && !Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block))
                        if (!block.getDARamps().isEmpty()) {
                            boolean isSlopeDefined = false;
                            for (DARamp daRamp : block.getDARamps())
                                if (daRamp != null && daRamp.getSlope() != null
                                        && daRamp.getSlope().compareTo(BigDecimal.valueOf(0)) > 0)
                                    isSlopeDefined = true;
                            if (!isSlopeDefined) {
                                errors.put(String.format(DcrConstants.RAMP_SLOPE, "", block.getNumber()),
                                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                                new String[] { String.format(DcrConstants.RAMP_SLOPE, "", block.getNumber()) },
                                                LocaleContextHolder.getLocale()));
                                pl.addErrors(errors);
                            }
                        }
                }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("BLK_%_DA_RAMP_%");
        layers.add("BLK_%_FLR_%_DA_ROOM");
        layers.add("BLK_%_FLR_%_RAMP_%");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(BLOCK_LEVEL_CHECK);
        parameters.add(FLOOR_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(FLOOR_COUNT);
        parameters.add(PLOT_AREA);
        parameters.add(FLOOR_UNITS);
        parameters.add(WIDTH);
        parameters.add(HEIGHT);
        parameters.add(FLOOR_HEIGHT);
        return parameters;
    }

    private boolean getOccupanciesForRamp(OccupancyType occupancyType) {
        return occupancyType.equals(OccupancyType.OCCUPANCY_A2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_A3) || occupancyType.equals(OccupancyType.OCCUPANCY_A4) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_B1) || occupancyType.equals(OccupancyType.OCCUPANCY_B2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_B3) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C) || occupancyType.equals(OccupancyType.OCCUPANCY_C1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C2) || occupancyType.equals(OccupancyType.OCCUPANCY_C3) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_D) || occupancyType.equals(OccupancyType.OCCUPANCY_D1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_D2) || occupancyType.equals(OccupancyType.OCCUPANCY_E) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F) || occupancyType.equals(OccupancyType.OCCUPANCY_F1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F4);
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        boolean valid;
        if (pl != null && !pl.getBlocks().isEmpty())
            blk: for (Block block : pl.getBlocks())
                if (!block.getCompletelyExisting()) {

                    scrutinyDetail = new ScrutinyDetail();
                    scrutinyDetail.addColumnHeading(1, RULE_NO);
                    scrutinyDetail.addColumnHeading(2, DESCRIPTION);
                    scrutinyDetail.addColumnHeading(3, REQUIRED);
                    scrutinyDetail.addColumnHeading(4, PROVIDED);
                    scrutinyDetail.addColumnHeading(5, STATUS);
                    scrutinyDetail.setKey("Block_" + block.getNumber() + "_" + "DA Ramp - Defined or not");

                    ScrutinyDetail scrutinyDetail1 = new ScrutinyDetail();
                    scrutinyDetail1.addColumnHeading(1, RULE_NO);
                    scrutinyDetail1.addColumnHeading(2, DESCRIPTION);
                    scrutinyDetail1.addColumnHeading(3, REQUIRED);
                    scrutinyDetail1.addColumnHeading(4, PROVIDED);
                    scrutinyDetail1.addColumnHeading(5, STATUS);
                    scrutinyDetail1.setKey("Block_" + block.getNumber() + "_" + "DA Ramp - Slope");

                    ScrutinyDetail scrutinyDetail2 = new ScrutinyDetail();
                    scrutinyDetail2.addColumnHeading(1, RULE_NO);
                    scrutinyDetail2.addColumnHeading(2, DESCRIPTION);
                    scrutinyDetail2.addColumnHeading(3, REQUIRED);
                    scrutinyDetail2.addColumnHeading(4, PROVIDED);
                    scrutinyDetail2.addColumnHeading(5, STATUS);
                    scrutinyDetail2.setKey("Block_" + block.getNumber() + "_" + "DA Ramp - Maximum Slope");

                    ScrutinyDetail scrutinyDetail3 = new ScrutinyDetail();
                    scrutinyDetail3.addColumnHeading(1, RULE_NO);
                    scrutinyDetail3.addColumnHeading(2, DESCRIPTION);
                    scrutinyDetail3.addColumnHeading(3, REQUIRED);
                    scrutinyDetail3.addColumnHeading(4, PROVIDED);
                    scrutinyDetail3.addColumnHeading(5, STATUS);
                    scrutinyDetail3.setSubHeading("Minimum number of da rooms");
                    scrutinyDetail3.setKey("Block_" + block.getNumber() + "_" + "DA Room");

                    ScrutinyDetail scrutinyDetail4 = new ScrutinyDetail();
                    scrutinyDetail4.addColumnHeading(1, RULE_NO);
                    scrutinyDetail4.addColumnHeading(2, DESCRIPTION);
                    scrutinyDetail4.addColumnHeading(3, FLOOR);
                    scrutinyDetail4.addColumnHeading(4, REQUIRED);
                    scrutinyDetail4.addColumnHeading(5, PROVIDED);
                    scrutinyDetail4.addColumnHeading(6, STATUS);
                    scrutinyDetail4.setKey("Block_" + block.getNumber() + "_" + "Ramp - Minimum Width");

                    ScrutinyDetail scrutinyDetail5 = new ScrutinyDetail();
                    scrutinyDetail5.addColumnHeading(1, RULE_NO);
                    scrutinyDetail5.addColumnHeading(2, DESCRIPTION);
                    scrutinyDetail5.addColumnHeading(3, FLOOR);
                    scrutinyDetail5.addColumnHeading(4, REQUIRED);
                    scrutinyDetail5.addColumnHeading(5, PROVIDED);
                    scrutinyDetail5.addColumnHeading(6, STATUS);
                    scrutinyDetail5.setKey("Block_" + block.getNumber() + "_" + "Ramp - Maximum Slope");

                    if (block.getBuilding() != null && !block.getBuilding().getOccupancies().isEmpty()) {
                        if (Util.checkExemptionConditionForBuildingParts(block) ||
                                Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block))
                            continue blk;
                        List<OccupancyType> occupancyTypeList = block.getBuilding().getOccupancies().stream()
                                .map(occupancy -> occupancy.getType()).collect(Collectors.toList());
                        for (OccupancyType occupancyType : occupancyTypeList)
                            if (getOccupanciesForRamp(occupancyType))
                                if (!block.getDARamps().isEmpty()) {
                                    pl.reportOutput
                                            .add(buildRuleOutputWithSubRule(String.format(SUBRULE_40_A_1_DESC, block.getNumber()),
                                                    SUBRULE_40_A1,
                                                    String.format(SUBRULE_40_A_1_DESC, block.getNumber()),
                                                    String.format(SUBRULE_40_A_1_DESC, block.getNumber()),
                                                    null,
                                                    null,
                                                    Result.Accepted, DcrConstants.OBJECTDEFINED_DESC));
                                    setReportOutputDetails(pl, SUBRULE_40_A1, SUBRULE_40_A_1_DESC, "",
                                            DcrConstants.OBJECTDEFINED_DESC, Result.Accepted.getResultVal(), scrutinyDetail);

                                    break;
                                } else {
                                    pl.reportOutput
                                            .add(buildRuleOutputWithSubRule(String.format(SUBRULE_40_A_1_DESC, block.getNumber()),
                                                    SUBRULE_40_A1,
                                                    String.format(SUBRULE_40_A_1_DESC, block.getNumber()),
                                                    String.format(SUBRULE_40_A_1_DESC, block.getNumber()),
                                                    null,
                                                    null,
                                                    Result.Not_Accepted, DcrConstants.OBJECTNOTDEFINED_DESC));
                                    setReportOutputDetails(pl, SUBRULE_40_A1, SUBRULE_40_A_1_DESC, "",
                                            DcrConstants.OBJECTNOTDEFINED_DESC, Result.Not_Accepted.getResultVal(),
                                            scrutinyDetail);
                                    break;
                                }
                    }
                    if (pl.getPlot() != null && !Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block))
                        if (!block.getDARamps().isEmpty()) {
                            boolean isSlopeDefined = false;
                            for (DARamp daRamp : block.getDARamps())
                                if (daRamp != null && daRamp.getSlope() != null
                                        && daRamp.getSlope().compareTo(BigDecimal.valueOf(0)) > 0)
                                    isSlopeDefined = true;
                            if (isSlopeDefined)
                                setReportOutputDetails(pl, SUBRULE_40_A3, SUBRULE_40_A_3_SLOPE_MAN_DESC, "",
                                        DcrConstants.OBJECTDEFINED_DESC, Result.Accepted.getResultVal(), scrutinyDetail1);
                            else
                                setReportOutputDetails(pl, SUBRULE_40_A3, SUBRULE_40_A_3_SLOPE_MAN_DESC, "",
                                        DcrConstants.OBJECTNOTDEFINED_DESC, Result.Not_Accepted.getResultVal(), scrutinyDetail1);
                            valid = false;
                            if (isSlopeDefined) {
                                Map<String, String> mapOfRampNumberAndSlopeValues = new HashMap<>();
                                BigDecimal expectedSlope = BigDecimal.valueOf(1).divide(BigDecimal.valueOf(12), 2,
                                        RoundingMode.HALF_UP);
                                for (DARamp daRamp : block.getDARamps()) {
                                    BigDecimal slope = daRamp.getSlope();
                                    if (slope != null && slope.compareTo(BigDecimal.valueOf(0)) > 0 && expectedSlope != null)
                                        if (slope.compareTo(expectedSlope) <= 0) {
                                            valid = true;
                                            mapOfRampNumberAndSlopeValues.put("daRampNumber", daRamp.getNumber().toString());
                                            mapOfRampNumberAndSlopeValues.put("slope", slope.toString());
                                            break;
                                        }
                                }
                                if (valid) {
                                    pl.reportOutput
                                            .add(buildRuleOutputWithSubRule(
                                                    String.format(SUBRULE_40_A_3_SLOPE_DESC,
                                                            mapOfRampNumberAndSlopeValues.get("daRampNumber"), block.getNumber()),
                                                    SUBRULE_40_A3,
                                                    String.format(SUBRULE_40_A_3_SLOPE_DESC,
                                                            mapOfRampNumberAndSlopeValues.get("daRampNumber"), block.getNumber()),
                                                    String.format(SUBRULE_40_A_3_SLOPE_DESC,
                                                            mapOfRampNumberAndSlopeValues.get("daRampNumber"), block.getNumber()),
                                                    expectedSlope.toString(),
                                                    mapOfRampNumberAndSlopeValues.get("slope"),
                                                    Result.Accepted, null));
                                    setReportOutputDetails(pl, SUBRULE_40_A3, String.format(SUBRULE_40_A_3_SLOPE_DESCRIPTION,
                                            mapOfRampNumberAndSlopeValues.get("daRampNumber")), expectedSlope.toString(),
                                            mapOfRampNumberAndSlopeValues.get("slope"), Result.Accepted.getResultVal(),
                                            scrutinyDetail2);
                                } else {
                                    pl.reportOutput
                                            .add(buildRuleOutputWithSubRule(
                                                    String.format(SUBRULE_40_A_3_SLOPE_DESC, "", block.getNumber()),
                                                    SUBRULE_40_A3,
                                                    String.format(SUBRULE_40_A_3_SLOPE_DESC, "", block.getNumber()),
                                                    String.format(SUBRULE_40_A_3_SLOPE_DESC, "", block.getNumber()),
                                                    expectedSlope.toString(),
                                                    "Less than 0.08 for all da ramps",
                                                    Result.Not_Accepted, null));
                                    setReportOutputDetails(pl, SUBRULE_40_A3, String.format(SUBRULE_40_A_3_SLOPE_DESCRIPTION, ""),
                                            expectedSlope.toString(),
                                            "Less than 0.08 for all da ramps", Result.Not_Accepted.getResultVal(),
                                            scrutinyDetail2);
                                }
                            }

                        }

                    if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
                        int noOfDaRooms = 0;
                        int noOfFloorUnitsInColorCode23And3 = 0;

                        for (Floor floor : block.getBuilding().getFloors())
                            if (!floor.getTerrace()) {

                                if (!floor.getDaRooms().isEmpty())
                                    noOfDaRooms = noOfDaRooms + floor.getDaRooms().size();
                                if (!floor.getUnits().isEmpty())
                                    for (FloorUnit floorUnit : floor.getUnits())
                                        if (floorUnit.getPolyLine() != null &&
                                                (floorUnit.getPolyLine().getColor() == 23
                                                        || floorUnit.getPolyLine().getColor() == 3))
                                            noOfFloorUnitsInColorCode23And3++;
                            }

                        if (noOfFloorUnitsInColorCode23And3 >= 25) {
                            BigDecimal expectedNoOfDARooms = BigDecimal.valueOf(Double.valueOf(noOfFloorUnitsInColorCode23And3))
                                    .divide(BigDecimal.valueOf(25), 2, RoundingMode.HALF_UP);

                            if (BigDecimal.valueOf(noOfDaRooms).compareTo(expectedNoOfDARooms) >= 0) {
                                pl.reportOutput
                                        .add(buildRuleOutputWithSubRule(String.format(SUBRULE_40_A_7_DESC, block.getNumber()),
                                                SUBRULE_40_A7,
                                                String.format(SUBRULE_40_A_7_DESC, block.getNumber()),
                                                String.format(SUBRULE_40_A_7_DESC, block.getNumber()),
                                                expectedNoOfDARooms.toString() + " (" + noOfFloorUnitsInColorCode23And3
                                                        + " Rooms declared) ",
                                                String.valueOf(noOfDaRooms),
                                                Result.Accepted, null));
                                setReportOutputDetails(pl, SUBRULE_40_A7, String.format(SUBRULE_40_A_7_DESC, block.getNumber()),
                                        expectedNoOfDARooms.toString() + " (" + noOfFloorUnitsInColorCode23And3
                                                + " Rooms declared) ",
                                        String.valueOf(noOfDaRooms), Result.Accepted.getResultVal(), scrutinyDetail3);
                            } else {
                                pl.reportOutput
                                        .add(buildRuleOutputWithSubRule(String.format(SUBRULE_40_A_7_DESC, block.getNumber()),
                                                SUBRULE_40_A7,
                                                String.format(SUBRULE_40_A_7_DESC, block.getNumber()),
                                                String.format(SUBRULE_40_A_7_DESC, block.getNumber()),
                                                expectedNoOfDARooms.toString() + " (" + noOfFloorUnitsInColorCode23And3
                                                        + " Rooms declared) ",
                                                String.valueOf(noOfDaRooms),
                                                Result.Not_Accepted, null));
                                setReportOutputDetails(pl, SUBRULE_40_A7, String.format(SUBRULE_40_A_7_DESC, block.getNumber()),
                                        expectedNoOfDARooms.toString() + " (" + noOfFloorUnitsInColorCode23And3
                                                + " Rooms declared) ",
                                        String.valueOf(noOfDaRooms), Result.Not_Accepted.getResultVal(), scrutinyDetail3);

                            }

                        }

                    }

                    if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                        for (Floor floor : block.getBuilding().getFloors())
                            if (!floor.getTerrace())
                                for (Ramp ramp : floor.getRamps())
                                    if (ramp.getRampPolyLineClosed()) {
                                        List<BigDecimal> rampWidths = new ArrayList<>();
                                        List<BigDecimal> rampLengths = new ArrayList<>();
                                        for (Measurement measurement : ramp.getRampPolyLines()) {
                                            rampWidths.add(measurement.getWidth());
                                            rampLengths.add(measurement.getHeight());
                                        }

                                        /*
                                         * for (DXFLWPolyline polyline : ramp.getPolylines()) { if (polyline.getBounds() != null
                                         * && polyline.getBounds().getWidth() > 0 && polyline.getBounds().getHeight() > 0) { if
                                         * (polyline.getBounds().getHeight() < polyline.getBounds().getWidth()) {
                                         * rampWidths.add(BigDecimal.valueOf(polyline.getBounds().getHeight()));
                                         * rampLengths.add(BigDecimal.valueOf(polyline.getBounds().getWidth())); } else {
                                         * rampWidths.add(BigDecimal.valueOf(polyline.getBounds().getWidth()));
                                         * rampLengths.add(BigDecimal.valueOf(polyline.getBounds().getHeight())); } } }
                                         */
                                        if (!rampWidths.isEmpty()) {
                                            BigDecimal minimumWidth = rampWidths.get(0);
                                            for (BigDecimal width : rampWidths)
                                                if (width.compareTo(minimumWidth) < 0)
                                                    minimumWidth = width;
                                            boolean isTypicalRepititiveFloor = false;
                                            valid = false;
                                            Map<String, Object> typicalFloorValues = Util.getTypicalFloorValues(block, floor,
                                                    isTypicalRepititiveFloor);
                                            if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor")) {
                                                if (minimumWidth.compareTo(BigDecimal.valueOf(1.2)) >= 0)
                                                    valid = true;
                                                String value = typicalFloorValues.get("typicalFloors") != null
                                                        ? (String) typicalFloorValues.get("typicalFloors")
                                                        : " floor " + floor.getNumber();
                                                if (valid) {
                                                    pl.reportOutput
                                                            .add(buildRuleOutputWithSubRule(
                                                                    String.format(SUBRULE_40_A_3_WIDTH_DESC, ramp.getNumber(),
                                                                            block.getNumber()) + value,
                                                                    SUBRULE_40_A3,
                                                                    String.format(SUBRULE_40_A_3_WIDTH_DESC, ramp.getNumber(),
                                                                            block.getNumber()) + value,
                                                                    String.format(SUBRULE_40_A_3_WIDTH_DESC, ramp.getNumber(),
                                                                            block.getNumber()) + value,
                                                                    BigDecimal.valueOf(1.2).toString() + DcrConstants.IN_METER,
                                                                    String.valueOf(Math.round(
                                                                            minimumWidth.doubleValue() * Double.valueOf(100))
                                                                            / Double.valueOf(100)) + DcrConstants.IN_METER,
                                                                    Result.Accepted, null));
                                                    setReportOutputDetailsFloorWiseWithDescription(pl, SUBRULE_40_A3,
                                                            String.format(SUBRULE_40_A_3_WIDTH_DESCRIPTION, ramp.getNumber()),
                                                            value, BigDecimal.valueOf(1.2).toString() + DcrConstants.IN_METER,
                                                            String.valueOf(
                                                                    Math.round(minimumWidth.doubleValue() * Double.valueOf(100))
                                                                            / Double.valueOf(100))
                                                                    +
                                                                    DcrConstants.IN_METER,
                                                            Result.Accepted.getResultVal(), scrutinyDetail4);

                                                } else {
                                                    pl.reportOutput
                                                            .add(buildRuleOutputWithSubRule(
                                                                    String.format(SUBRULE_40_A_3_WIDTH_DESC, ramp.getNumber(),
                                                                            block.getNumber()) + value,
                                                                    SUBRULE_40_A3,
                                                                    String.format(SUBRULE_40_A_3_WIDTH_DESC, ramp.getNumber(),
                                                                            block.getNumber()) + value,
                                                                    String.format(SUBRULE_40_A_3_WIDTH_DESC, ramp.getNumber(),
                                                                            block.getNumber()) + value,
                                                                    BigDecimal.valueOf(1.2).toString() + DcrConstants.IN_METER,
                                                                    String.valueOf(Math.round(
                                                                            minimumWidth.doubleValue() * Double.valueOf(100))
                                                                            / Double.valueOf(100)) + DcrConstants.IN_METER,
                                                                    Result.Not_Accepted, null));
                                                    setReportOutputDetailsFloorWiseWithDescription(pl, SUBRULE_40_A3,
                                                            String.format(SUBRULE_40_A_3_WIDTH_DESCRIPTION, ramp.getNumber()),
                                                            value, BigDecimal.valueOf(1.2).toString() + DcrConstants.IN_METER,
                                                            String.valueOf(
                                                                    Math.round(minimumWidth.doubleValue() * Double.valueOf(100))
                                                                            / Double.valueOf(100))
                                                                    +
                                                                    DcrConstants.IN_METER,
                                                            Result.Not_Accepted.getResultVal(), scrutinyDetail4);
                                                }
                                            }
                                        }
                                        if (pl.getPlot() != null
                                                && !Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block)) {
                                            BigDecimal rampTotalLength = BigDecimal.ZERO;
                                            for (BigDecimal length : rampLengths)
                                                rampTotalLength = rampTotalLength.add(length);
                                            if (rampTotalLength.compareTo(BigDecimal.valueOf(0)) > 0
                                                    && ramp.getFloorHeight() != null) {
                                                boolean isTypicalRepititiveFloor = false;
                                                BigDecimal rampSlope = ramp.getFloorHeight().divide(rampTotalLength, 2,
                                                        RoundingMode.HALF_UP);
                                                ramp.setSlope(rampSlope);
                                                valid = false;
                                                Map<String, Object> typicalFloorValues = Util.getTypicalFloorValues(block, floor,
                                                        isTypicalRepititiveFloor);
                                                if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor")) {
                                                    if (rampSlope.compareTo(BigDecimal.valueOf(0.1)) <= 0)
                                                        valid = true;
                                                    String value = typicalFloorValues.get("typicalFloors") != null
                                                            ? (String) typicalFloorValues.get("typicalFloors")
                                                            : " floor " + floor.getNumber();
                                                    if (valid) {
                                                        pl.reportOutput
                                                                .add(buildRuleOutputWithSubRule(String.format(SUBRULE_40_DESC,
                                                                        ramp.getNumber(), block.getNumber()) + value, SUBRULE_40,
                                                                        String.format(SUBRULE_40_DESC, ramp.getNumber(),
                                                                                block.getNumber()) + value,
                                                                        String.format(SUBRULE_40_DESC, ramp.getNumber(),
                                                                                block.getNumber()) + value,
                                                                        BigDecimal.valueOf(0.1).toString(),
                                                                        rampSlope.toString(),
                                                                        Result.Accepted, null));
                                                        setReportOutputDetailsFloorWiseWithDescription(pl, SUBRULE_40,
                                                                String.format(SUBRULE_40_DESCRIPTION, ramp.getNumber()), value,
                                                                BigDecimal.valueOf(0.1).toString(), rampSlope.toString(),
                                                                Result.Accepted.getResultVal(), scrutinyDetail5);
                                                    } else {
                                                        pl.reportOutput
                                                                .add(buildRuleOutputWithSubRule(String.format(SUBRULE_40_DESC,
                                                                        ramp.getNumber(), block.getNumber()) + value, SUBRULE_40,
                                                                        String.format(SUBRULE_40_DESC, ramp.getNumber(),
                                                                                block.getNumber()) + value,
                                                                        String.format(SUBRULE_40_DESC, ramp.getNumber(),
                                                                                block.getNumber()) + value,
                                                                        BigDecimal.valueOf(0.1).toString(),
                                                                        rampSlope.toString(),
                                                                        Result.Not_Accepted, null));
                                                        setReportOutputDetailsFloorWiseWithDescription(pl, SUBRULE_40,
                                                                String.format(SUBRULE_40_DESCRIPTION, ramp.getNumber()), value,
                                                                BigDecimal.valueOf(0.1).toString(), rampSlope.toString(),
                                                                Result.Not_Accepted.getResultVal(), scrutinyDetail5);
                                                    }
                                                }
                                            }
                                        }
                                    }
                }
        return pl;
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

    private void setReportOutputDetailsFloorWiseWithDescription(PlanDetail pl, String ruleNo, String ruleDesc, String floor,
            String expected, String actual, String status, ScrutinyDetail scrutinyDetail) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(FLOOR, floor);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

    private void validateDimensions(PlanDetail planDetail, String blockNo, int floorNo, String rampNo,
            List<Measurement> rampPolylines) {
        int count = 0;
        for (Measurement m : rampPolylines)
            if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
                count++;
        if (count > 0)
            planDetail.addError(String.format(DxfFileConstants.LAYER_RAMP_WITH_NO, blockNo, floorNo, rampNo),
                    count + " number of ramp polyline not having only 4 points in layer "
                            + String.format(DxfFileConstants.LAYER_RAMP_WITH_NO, blockNo, floorNo, rampNo));
    }
}
