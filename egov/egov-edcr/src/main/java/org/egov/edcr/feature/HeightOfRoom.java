package org.egov.edcr.feature;

import static org.egov.edcr.utility.ParametersConstants.COLOR_CODE;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.PLOT_AREA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.Room;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.TypicalFloor;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFMText;
import org.kabeja.dxf.helpers.StyledTextParagraph;
import org.springframework.stereotype.Service;

@Service
public class HeightOfRoom extends GeneralRule implements RuleService {

    private static final String SUBRULE_35_2 = "35(2)";
    private static final String SUBRULE_36 = "36";
    private static final String SUBRULE_55_3 = "55(3)";
    private static final String SUBRULE_55_4 = "55(4)";
    private static final String SUBRULE_55_5 = "55(5)";
    private static final String SUBRULE_55_6 = "55(6)";
    private static final String SUBRULE_55_7 = "55(7)";
    private static final String SUBRULE_55_8 = "55(8)";
    private static final String SUBRULE_55_9 = "55(9)";

    private static final String SUBRULE_35_2_DESC = "Minimum height of head room below mezzanine floor";
    private static final String SUBRULE_36_DESC_NORMAL_ROOMS = "Minimum height of room under occupancy Educational,Medical/Hospital,Office/Business,Mercantile/Commercial,Storage,Hazardous";
    private static final String SUBRULE_36_DESC_AC_ROOMS = "Minimum height of AC room under occupancy Educational,Medical/Hospital,Office/Business,Mercantile/Commercial,Storage,Hazardous";
    private static final String SUBRULE_36_DESC_PARKING_ROOMS = "Minimum height of car and two wheeler parking room";
    private static final String SUBRULE_55_3_DESC_ASSEMBLY_ROOMS = "Minimum height of room under assembly occupancy";
    private static final String SUBRULE_55_3_DESC_ASSEMBLY_AC_ROOMS = "Minimum height of AC room under assembly occupancy";
    private static final String SUBRULE_55_4_DESC = "Minimum height of headroom beneath or above balcony";
    private static final String SUBRULE_55_5_DESC = "Minimum height of headroom in general ac rooms in assembly occupancy";
    private static final String SUBRULE_55_6_DESC = "Minimum height of general ac rooms,store rooms,toilets,lamber and cellar rooms";
    private static final String SUBRULE_55_7_DESC = "Minimum height of work room under occupancy G";
    private static final String SUBRULE_55_8_DESC = "Minimum height of laboratory,entrance hall,canteen,cloak room";
    private static final String SUBRULE_55_9_DESC = "Minimum height of store rooms and toilets in industrial buildings";

    public static final BigDecimal MINIMUM_HEIGHT_2_2 = BigDecimal.valueOf(2.2);
    public static final BigDecimal MINIMUM_HEIGHT_3 = BigDecimal.valueOf(3);
    public static final BigDecimal MINIMUM_HEIGHT_2_4 = BigDecimal.valueOf(2.4);
    public static final BigDecimal MINIMUM_HEIGHT_4 = BigDecimal.valueOf(4);
    public static final BigDecimal MINIMUM_HEIGHT_3_6 = BigDecimal.valueOf(3.6);
    private static final String FLOOR = "Floor";
    private static final String HGHT_OF_ROOM_UNDEFINED = "%s is not defined for block %s floor %s with colour code %s";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        if (pl != null && !pl.getBlocks().isEmpty())
            for (Block block : pl.getBlocks())
                if (!block.getCompletelyExisting())
                    if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                        outside: for (Floor floor : block.getBuilding().getFloors()) {
                            if (!block.getTypicalFloor().isEmpty())
                                for (TypicalFloor tp : block.getTypicalFloor())
                                    if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                                        for (Floor allFloors : block.getBuilding().getFloors())
                                            if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                                                if (!allFloors.getHabitableRooms().isEmpty()) {
                                                    floor.setHabitableRooms(allFloors.getHabitableRooms());
                                                    continue outside;
                                                }
                            String layerName = String.format(DxfFileConstants.LAYER_HGHT_ROOM, block.getNumber(),
                                    floor.getNumber());
                            List<DXFDimension> heightOfRoom = Util.getDimensionsByLayer(doc, layerName);
                            if (heightOfRoom != null && !heightOfRoom.isEmpty()) {
                                List<Room> rooms = extractDistanceWithColourCode(doc, heightOfRoom);
                                if (!rooms.isEmpty())
                                    floor.setHabitableRooms(rooms);
                            }
                        }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("BLK_%_FLR_%_HT_ROOM");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(FLOOR_LEVEL_CHECK);
        parameters.add(OCCUPANCY);
        parameters.add(PLOT_AREA);
        parameters.add(FLOOR_COUNT);
        parameters.add(COLOR_CODE);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && pl.getBlocks() != null)
            blk: for (Block block : pl.getBlocks())
                if (!block.getCompletelyExisting())
                    if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
                        scrutinyDetail = new ScrutinyDetail();
                        scrutinyDetail.addColumnHeading(1, RULE_NO);
                        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
                        scrutinyDetail.addColumnHeading(3, FLOOR);
                        scrutinyDetail.addColumnHeading(4, REQUIRED);
                        scrutinyDetail.addColumnHeading(5, PROVIDED);
                        scrutinyDetail.addColumnHeading(6, STATUS);
                        List<Integer> colorCodesForExemption = getColorCodesListForExemption();
                        if (pl.getPlot() != null)
                            if (Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block))
                                continue blk;
                        scrutinyDetail.setKey("Block_" + block.getNumber() + "_" + "Height Of Room");
                        for (Floor floor : block.getBuilding().getFloors())
                            if (!floor.getTerrace())
                                if (!floor.getHabitableRooms().isEmpty()) {
                                    Map<Integer, Long> requirementCountMap = floor.getHabitableRooms().stream()
                                            .collect(Collectors.groupingBy(Room::getColorCode, Collectors.counting()));
                                    for (Integer colorCode : requirementCountMap.keySet()) {
                                        BigDecimal minimumHeight = BigDecimal.ZERO;
                                        String subRule = null;
                                        String subRuleDesc = null;
                                        List<BigDecimal> distancesList = new ArrayList<>();
                                        for (Room room : floor.getHabitableRooms()) {
                                            if (room.getColorCode() == colorCode) {
                                                if (room.getColorCode() == DxfFileConstants.MEZZANINE_HEAD_ROOM_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_2_2;
                                                    subRule = SUBRULE_35_2;
                                                    subRuleDesc = SUBRULE_35_2_DESC;
                                                } else if (room
                                                        .getColorCode() == DxfFileConstants.NORMAL_ROOM_BCEFHI_OCCUPANCIES_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_3;
                                                    subRule = SUBRULE_36;
                                                    subRuleDesc = SUBRULE_36_DESC_NORMAL_ROOMS;
                                                } else if (room
                                                        .getColorCode() == DxfFileConstants.AC_ROOM_BCEFHI_OCCUPANCIES_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_2_4;
                                                    subRule = SUBRULE_36;
                                                    subRuleDesc = SUBRULE_36_DESC_AC_ROOMS;
                                                } else if (room
                                                        .getColorCode() == DxfFileConstants.CAR_AND_TWO_WHEELER_PARKING_ROOM_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_2_2;
                                                    subRule = SUBRULE_36;
                                                    subRuleDesc = SUBRULE_36_DESC_PARKING_ROOMS;
                                                } else if (room.getColorCode() == DxfFileConstants.ASSEMBLY_ROOM_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_4;
                                                    subRule = SUBRULE_55_3;
                                                    subRuleDesc = SUBRULE_55_3_DESC_ASSEMBLY_ROOMS;
                                                } else if (room.getColorCode() == DxfFileConstants.ASSEMBLY_AC_HALL_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_3;
                                                    subRule = SUBRULE_55_3;
                                                    subRuleDesc = SUBRULE_55_3_DESC_ASSEMBLY_AC_ROOMS;
                                                } else if (room
                                                        .getColorCode() == DxfFileConstants.HEAD_ROOM_BENEATH_OR_ABOVE_BALCONY_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_3;
                                                    subRule = SUBRULE_55_4;
                                                    subRuleDesc = SUBRULE_55_4_DESC;
                                                } else if (room
                                                        .getColorCode() == DxfFileConstants.HEAD_ROOM_IN_GENERAL_AC_ROOM_IN_ASSEMBLY_OCCUPANCY_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_2_4;
                                                    subRule = SUBRULE_55_5;
                                                    subRuleDesc = SUBRULE_55_5_DESC;
                                                } else if (room
                                                        .getColorCode() == DxfFileConstants.GENERALAC_STORE_TOILET_LAMBER_CELLAR_ROOM_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_2_4;
                                                    subRule = SUBRULE_55_6;
                                                    subRuleDesc = SUBRULE_55_6_DESC;
                                                } else if (room
                                                        .getColorCode() == DxfFileConstants.WORK_ROOM_UNDER_OCCUPANCY_G_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_3_6;
                                                    subRule = SUBRULE_55_7;
                                                    subRuleDesc = SUBRULE_55_7_DESC;
                                                } else if (room
                                                        .getColorCode() == DxfFileConstants.LAB_ENTRANCE_HALL_CANTEEN_CLOAK_ROOM_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_3;
                                                    subRule = SUBRULE_55_8;
                                                    subRuleDesc = SUBRULE_55_8_DESC;
                                                } else if (room
                                                        .getColorCode() == DxfFileConstants.STORE_TOILET_ROOM_IN_INDUSTRIES_COLOR_CODE) {
                                                    minimumHeight = MINIMUM_HEIGHT_2_4;
                                                    subRule = SUBRULE_55_9;
                                                    subRuleDesc = SUBRULE_55_9_DESC;
                                                }
                                                distancesList.add(room.getHeight());
                                            }
                                            if (colorCodesForExemption.contains(room.getColorCode())
                                                    && Util.checkExemptionConditionForBuildingParts(block)) {
                                                minimumHeight = BigDecimal.ZERO;
                                                subRule = null;
                                                subRuleDesc = null;
                                            }
                                        }
                                        BigDecimal minimumHght = distancesList.get(0);
                                        for (BigDecimal distance : distancesList)
                                            if (distance.compareTo(minimumHght) < 0)
                                                minimumHght = distance;
                                        boolean valid = false;
                                        boolean isTypicalRepititiveFloor = false;
                                        Map<String, Object> typicalFloorValues = Util.getTypicalFloorValues(block, floor,
                                                isTypicalRepititiveFloor);
                                        if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor")
                                                && minimumHeight.compareTo(BigDecimal.valueOf(0)) > 0 &&
                                                subRule != null && subRuleDesc != null) {
                                            if (minimumHeight.compareTo(minimumHght) <= 0)
                                                valid = true;
                                            String value = typicalFloorValues.get("typicalFloors") != null
                                                    ? (String) typicalFloorValues.get("typicalFloors")
                                                    : " floor " + floor.getNumber();
                                            if (valid) {
                                                pl.reportOutput
                                                        .add(buildRuleOutputWithSubRule(subRuleDesc,
                                                                subRule,
                                                                subRuleDesc + " for block " + block.getNumber() + value,
                                                                subRuleDesc + " for block " + block.getNumber() + value,
                                                                minimumHeight + DcrConstants.IN_METER,
                                                                minimumHght + DcrConstants.IN_METER, Result.Accepted,
                                                                null));
                                                setReportOutputDetails(pl, subRule, subRuleDesc, value,
                                                        minimumHeight + DcrConstants.IN_METER,
                                                        minimumHght + DcrConstants.IN_METER
                                                                + (colorCode > 0
                                                                        ? "\n\nnumber of rooms : "
                                                                                + requirementCountMap.get(colorCode) + " nos"
                                                                        : org.apache.commons.lang.StringUtils.EMPTY),
                                                        Result.Accepted.getResultVal());

                                            } else {
                                                pl.reportOutput
                                                        .add(buildRuleOutputWithSubRule(subRuleDesc,
                                                                subRule,
                                                                subRuleDesc + " for block " + block.getNumber() + value,
                                                                subRuleDesc + " for block " + block.getNumber() + value,
                                                                minimumHeight + DcrConstants.IN_METER,
                                                                minimumHght + DcrConstants.IN_METER, Result.Not_Accepted,
                                                                null));
                                                setReportOutputDetails(pl, subRule, subRuleDesc, value,
                                                        minimumHeight + DcrConstants.IN_METER,
                                                        minimumHght + DcrConstants.IN_METER
                                                                + (colorCode > 0
                                                                        ? "\n\nnumber of rooms : "
                                                                                + requirementCountMap.get(colorCode) + " nos"
                                                                        : org.apache.commons.lang.StringUtils.EMPTY),
                                                        Result.Not_Accepted.getResultVal());
                                            }
                                        }
                                    }
                                    validateRoomHghtMandatory(pl, errors, block, floor, requirementCountMap.keySet());
                                } else {
                                    List<OccupancyType> occupancyTypes = floor.getOccupancies().stream()
                                            .map(occupancy -> occupancy.getType()).collect(Collectors.toList());
                                    if (occupancyTypes != null && occupancyTypes.size() > 0)
                                        if (!(occupancyTypes.contains(OccupancyType.OCCUPANCY_A1)
                                                || occupancyTypes.contains(OccupancyType.OCCUPANCY_A2)
                                                || occupancyTypes.contains(OccupancyType.OCCUPANCY_F3)
                                                || occupancyTypes.contains(OccupancyType.OCCUPANCY_A4))) {
                                            errors.put(
                                                    "HGHT_OF_ROOM for block " + block.getNumber() + " floor " + floor.getNumber(),
                                                    "Height of room is not defined for block " + block.getNumber() + " floor "
                                                            + floor.getNumber());
                                            pl.addErrors(errors);
                                        }
                                }
                    }
        return pl;

    }

    private void validateRoomHghtMandatory(PlanDetail pl, HashMap<String, String> errors, Block block, Floor floor,
            Set<Integer> coloursUniqueSet) {
        boolean isPresent = false;

        boolean isAssembly = floor.getOccupancies().stream()
                .anyMatch(occupancy -> Arrays
                        .asList(OccupancyType.OCCUPANCY_D, OccupancyType.OCCUPANCY_D1, OccupancyType.OCCUPANCY_D2)
                        .contains(occupancy.getType()));

        if (isAssembly) {

            isPresent = coloursUniqueSet.stream()
                    .anyMatch(colorCode -> colorCode == DxfFileConstants.CAR_AND_TWO_WHEELER_PARKING_ROOM_COLOR_CODE
                            || colorCode == DxfFileConstants.ASSEMBLY_AC_HALL_COLOR_CODE
                            || colorCode == DxfFileConstants.ASSEMBLY_ROOM_COLOR_CODE);

            if (!isPresent) {
                errors.put(
                        "assembly room height" + block.getNumber() + " floor " + floor.getNumber(),
                        String.format(HGHT_OF_ROOM_UNDEFINED, "Assembly Ac Hall or parking floor or Assembly room height",
                                block.getNumber(), floor.getNumber(),
                                DxfFileConstants.ASSEMBLY_AC_HALL_COLOR_CODE + " or "
                                        + DxfFileConstants.CAR_AND_TWO_WHEELER_PARKING_ROOM_COLOR_CODE + " or "
                                        + DxfFileConstants.ASSEMBLY_ROOM_COLOR_CODE));
                pl.addErrors(errors);
            }
        }

        if (floor.getMezzanineFloor() != null && floor.getMezzanineFloor().size() > 0) {
            isPresent = coloursUniqueSet.stream()
                    .anyMatch(colorCode -> colorCode == DxfFileConstants.MEZZANINE_HEAD_ROOM_COLOR_CODE);

            if (!isPresent) {
                errors.put(
                        DxfFileConstants.MEZZANINE_HEAD_ROOM_COLOR_CODE + block.getNumber() + " floor " + floor.getNumber(),
                        String.format(HGHT_OF_ROOM_UNDEFINED, "Mezzanine floor room height", block.getNumber(), floor.getNumber(),
                                DxfFileConstants.MEZZANINE_HEAD_ROOM_COLOR_CODE));
                pl.addErrors(errors);
            }
        }

        boolean isIndustrial = floor.getOccupancies().stream().anyMatch(occupancy -> Arrays
                .asList(OccupancyType.OCCUPANCY_G1, OccupancyType.OCCUPANCY_G2).contains(occupancy.getType()));
        if (isIndustrial) {
            isPresent = coloursUniqueSet.stream()
                    .anyMatch(colorCode -> colorCode == DxfFileConstants.WORK_ROOM_UNDER_OCCUPANCY_G_COLOR_CODE);

            if (!isPresent) {
                errors.put(
                        DxfFileConstants.WORK_ROOM_UNDER_OCCUPANCY_G_COLOR_CODE + block.getNumber() + " floor "
                                + floor.getNumber(),
                        String.format(HGHT_OF_ROOM_UNDEFINED, "Work room height", block.getNumber(), floor.getNumber(),
                                DxfFileConstants.WORK_ROOM_UNDER_OCCUPANCY_G_COLOR_CODE));
                pl.addErrors(errors);
            }
        }

        // For other occupancies, atleast one room required in the plan.
        boolean isBCDEFHIOccupancies = floor.getOccupancies().stream()
                .anyMatch(occupancy -> Arrays
                        .asList(OccupancyType.OCCUPANCY_A2, OccupancyType.OCCUPANCY_B1, OccupancyType.OCCUPANCY_B2,
                                OccupancyType.OCCUPANCY_B3, OccupancyType.OCCUPANCY_C, OccupancyType.OCCUPANCY_C1,
                                OccupancyType.OCCUPANCY_C2, OccupancyType.OCCUPANCY_C3, OccupancyType.OCCUPANCY_E,
                                OccupancyType.OCCUPANCY_F, OccupancyType.OCCUPANCY_F1, OccupancyType.OCCUPANCY_F2,
                                OccupancyType.OCCUPANCY_F3, OccupancyType.OCCUPANCY_F4, OccupancyType.OCCUPANCY_H,
                                OccupancyType.OCCUPANCY_I1, OccupancyType.OCCUPANCY_I2)
                        .contains(occupancy.getType()));

        if (isBCDEFHIOccupancies) {
            isPresent = coloursUniqueSet.stream()
                    .anyMatch(colorCode -> colorCode == DxfFileConstants.CAR_AND_TWO_WHEELER_PARKING_ROOM_COLOR_CODE
                            || colorCode == DxfFileConstants.NORMAL_ROOM_BCEFHI_OCCUPANCIES_COLOR_CODE
                            || colorCode == DxfFileConstants.AC_ROOM_BCEFHI_OCCUPANCIES_COLOR_CODE);
            if (!isPresent) {
                errors.put(
                        "normal or ac rooms for other occupancies of " + block.getNumber() + " floor " + floor.getNumber(),
                        String.format(HGHT_OF_ROOM_UNDEFINED, "Normal room or ac room height or parking floor", block.getNumber(),
                                floor.getNumber(),
                                DxfFileConstants.NORMAL_ROOM_BCEFHI_OCCUPANCIES_COLOR_CODE + " or "
                                        + DxfFileConstants.AC_ROOM_BCEFHI_OCCUPANCIES_COLOR_CODE + " or "
                                        + DxfFileConstants.CAR_AND_TWO_WHEELER_PARKING_ROOM_COLOR_CODE));
                pl.addErrors(errors);

            }
        }
    }

    private List<Integer> getColorCodesListForExemption() {
        List<Integer> colorCodesForExemption = new ArrayList<>();
        colorCodesForExemption.add(DxfFileConstants.MEZZANINE_HEAD_ROOM_COLOR_CODE);
        colorCodesForExemption.add(DxfFileConstants.NORMAL_ROOM_BCEFHI_OCCUPANCIES_COLOR_CODE);
        colorCodesForExemption.add(DxfFileConstants.AC_ROOM_BCEFHI_OCCUPANCIES_COLOR_CODE);
        colorCodesForExemption.add(DxfFileConstants.CAR_AND_TWO_WHEELER_PARKING_ROOM_COLOR_CODE);
        return colorCodesForExemption;
    }

    private List<Room> extractDistanceWithColourCode(DXFDocument doc,
            List<DXFDimension> shortestDistanceCentralLineRoadDimension) {
        List<Room> rooms = new ArrayList<>();

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
                        String text2 = "";

                        Iterator styledParagraphIterator = text.getTextDocument().getStyledParagraphIterator();
                        while (styledParagraphIterator.hasNext()) {
                            StyledTextParagraph styledTextParagraph = (StyledTextParagraph) styledParagraphIterator.next();
                            text2 = styledTextParagraph.getText();
                            if (text2.contains(";"))
                                text2 = text2.split(";")[1];
                            else
                                text2 = text2.replaceAll("[^\\d`.]", "");
                        }

                        if (!text2.isEmpty()) {
                            value = BigDecimal.valueOf(Double.parseDouble(text2));
                            Room room = new Room();
                            room.setHeight(value);
                            room.setColorCode(line.getColor());
                            rooms.add(room);
                        }

                    }
                }

            }
        return rooms;
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDesc, String floor, String expected,
            String actual, String status) {
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

}