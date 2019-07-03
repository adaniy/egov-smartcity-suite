package org.egov.edcr.rule;

import static org.egov.edcr.utility.ParametersConstants.BUILDING_HEIGHT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_AREA;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_UNITS;
import static org.egov.edcr.utility.ParametersConstants.HEIGHT;
import static org.egov.edcr.utility.ParametersConstants.WIDTH;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.Lift;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.feature.GeneralRule;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.springframework.stereotype.Service;

@Service
public class LiftService extends GeneralRule implements RuleService {

    private static final String SUBRULE_48_DESC = "Minimum number of lifts for block %s";
    private static final String SUBRULE_48 = "48";
    private static final String REMARKS = "Remarks";
    private static final String SUBRULE_48_DESCRIPTION = "Minimum number of lifts";
    private static final String SUBRULE_40A_3 = "40A(3)";
    private static final String SUBRULE_118 = "118";
    private static final String SUBRULE_118_DESCRIPTION = "Minimum dimension Of lift %s on floor %s";
    private static final String SUBRULE_118_DESC = "Minimum dimension Of lift";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        if (pl != null && !pl.getBlocks().isEmpty())
            for (Block block : pl.getBlocks())
                if (!block.getBuilding().getFloors().isEmpty()) {
                    Integer noOfLifts = 0;
                    for (Floor floor : block.getBuilding().getFloors())
                        if (!floor.getTerrace())
                            if (!floor.getLifts().isEmpty() && floor.getLifts().size() > noOfLifts)
                                noOfLifts = floor.getLifts().size();
                    block.setNumberOfLifts(String.valueOf(noOfLifts));
                }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        for (Block block : pl.getBlocks())
            if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                for (Floor floor : block.getBuilding().getFloors())
                    if (!floor.getTerrace()) {
                        List<Lift> lifts = floor.getLifts();
                        if (lifts != null && !lifts.isEmpty())
                            for (Lift lift : lifts) {
                                List<Measurement> liftPolyLines = lift.getLiftPolyLines();
                                if (liftPolyLines != null && !liftPolyLines.isEmpty())
                                    validateDimensions(pl, block.getNumber(), floor.getNumber(), lift.getNumber().toString(),
                                            liftPolyLines);
                            }
                    }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("BLK_%_FLR_%_LIFT_%");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(FLOOR_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(FLOOR_COUNT);
        parameters.add(WIDTH);
        parameters.add(HEIGHT);
        parameters.add(FLOOR_AREA);
        parameters.add(BUILDING_HEIGHT);
        parameters.add(FLOOR_UNITS);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        if (pl != null && !pl.getBlocks().isEmpty())
            blk: for (Block block : pl.getBlocks()) {
                scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, DESCRIPTION);
                scrutinyDetail.addColumnHeading(3, REQUIRED);
                scrutinyDetail.addColumnHeading(4, PROVIDED);
                scrutinyDetail.addColumnHeading(5, STATUS);
                scrutinyDetail.addColumnHeading(6, REMARKS);
                scrutinyDetail.setKey("Block_" + block.getNumber() + "_" + "Lift - Minimum Required");

                ScrutinyDetail scrutinyDetail1 = new ScrutinyDetail();
                scrutinyDetail1.addColumnHeading(1, RULE_NO);
                scrutinyDetail1.addColumnHeading(2, DESCRIPTION);
                scrutinyDetail1.addColumnHeading(3, REQUIRED);
                scrutinyDetail1.addColumnHeading(4, PROVIDED);
                scrutinyDetail1.addColumnHeading(5, STATUS);
                scrutinyDetail1.addColumnHeading(6, REMARKS);
                scrutinyDetail1.setKey("Block_" + block.getNumber() + "_" + "Lift - Minimum Dimension");

                if (block.getBuilding() != null && !block.getBuilding().getOccupancies().isEmpty()) {
                    if (Util.checkExemptionConditionForBuildingParts(block))
                        continue blk;
                    List<OccupancyType> occupancyTypeList = block.getBuilding().getOccupancies().stream()
                            .map(occupancy -> occupancy.getType()).collect(Collectors.toList());
                    BigDecimal noOfLiftsRqrd;
                    if ((occupancyTypeList.contains(OccupancyType.OCCUPANCY_C) ||
                            occupancyTypeList.contains(OccupancyType.OCCUPANCY_C1)
                            || occupancyTypeList.contains(OccupancyType.OCCUPANCY_C2) ||
                            occupancyTypeList.contains(OccupancyType.OCCUPANCY_C3))
                            && block.getBuilding().getFloorsAboveGround() != null &&
                            block.getBuilding().getFloorsAboveGround().compareTo(BigDecimal.valueOf(3)) > 0) {
                        noOfLiftsRqrd = BigDecimal.valueOf(1);
                        if (block.getBuilding().getTotalFloorArea() != null
                                && block.getBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(4000)) > 0)
                            noOfLiftsRqrd = noOfLiftsRqrd.add(BigDecimal
                                    .valueOf(Math.ceil(block.getBuilding().getTotalFloorArea().subtract(BigDecimal.valueOf(4000))
                                            .divide(BigDecimal.valueOf(2500), 2, RoundingMode.HALF_UP).doubleValue())));
                        boolean valid = false;
                        if (noOfLiftsRqrd.compareTo(BigDecimal.valueOf(Double.valueOf(block.getNumberOfLifts()))) <= 0)
                            valid = true;
                        if (valid) {
                            pl.reportOutput
                                    .add(buildRuleOutputWithSubRule(String.format(SUBRULE_48_DESC, block.getNumber()),
                                            SUBRULE_48 + ", " + SUBRULE_40A_3,
                                            String.format(SUBRULE_48_DESC, block.getNumber()),
                                            String.format(SUBRULE_48_DESC, block.getNumber()),
                                            noOfLiftsRqrd.toString(),
                                            block.getNumberOfLifts(), Result.Accepted,
                                            null));
                            setReportOutputDetails(pl, SUBRULE_48 + ", " + SUBRULE_40A_3, SUBRULE_48_DESCRIPTION,
                                    noOfLiftsRqrd.toString(),
                                    block.getNumberOfLifts(),
                                    Result.Accepted.getResultVal(),
                                    "Medical Occupancy is most restrictive, so number of floors above ground level > 3 is required to have lifts",
                                    scrutinyDetail);
                        } else {
                            pl.reportOutput
                                    .add(buildRuleOutputWithSubRule(String.format(SUBRULE_48_DESC, block.getNumber()),
                                            SUBRULE_48 + ", " + SUBRULE_40A_3,
                                            String.format(SUBRULE_48_DESC, block.getNumber()),
                                            String.format(SUBRULE_48_DESC, block.getNumber()),
                                            noOfLiftsRqrd.toString(),
                                            block.getNumberOfLifts(), Result.Not_Accepted,
                                            null));
                            setReportOutputDetails(pl, SUBRULE_48, SUBRULE_48_DESCRIPTION, noOfLiftsRqrd.toString(),
                                    block.getNumberOfLifts(),
                                    Result.Not_Accepted.getResultVal(),
                                    "Medical Occupancy is most restrictive, so number of floors above ground level > 3 is required to have lifts",
                                    scrutinyDetail);
                        }

                    } else if (!occupancyTypeList.contains(OccupancyType.OCCUPANCY_C) &&
                            !occupancyTypeList.contains(OccupancyType.OCCUPANCY_C1) &&
                            !occupancyTypeList.contains(OccupancyType.OCCUPANCY_C2) &&
                            !occupancyTypeList.contains(OccupancyType.OCCUPANCY_C3)
                            && (occupancyTypeList.contains(OccupancyType.OCCUPANCY_A1) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_A2)
                                    || occupancyTypeList.contains(OccupancyType.OCCUPANCY_A3) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_A4)
                                    || occupancyTypeList.contains(OccupancyType.OCCUPANCY_A5) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_B1)
                                    || occupancyTypeList.contains(OccupancyType.OCCUPANCY_B2) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_B3)
                                    || occupancyTypeList.contains(OccupancyType.OCCUPANCY_D) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_D1) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_D2) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_E)
                                    || occupancyTypeList.contains(OccupancyType.OCCUPANCY_F) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_F1)
                                    || occupancyTypeList.contains(OccupancyType.OCCUPANCY_F2) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_F3)
                                    || occupancyTypeList.contains(OccupancyType.OCCUPANCY_F4) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_G1) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_G2)
                                    || occupancyTypeList.contains(OccupancyType.OCCUPANCY_H) ||
                                    occupancyTypeList.contains(OccupancyType.OCCUPANCY_I1)
                                    || occupancyTypeList.contains(OccupancyType.OCCUPANCY_I2))
                            && block.getBuilding().getFloorsAboveGround() != null &&
                            block.getBuilding().getFloorsAboveGround().compareTo(BigDecimal.valueOf(4)) > 0) {
                        noOfLiftsRqrd = BigDecimal.valueOf(1);
                        if (block.getBuilding().getTotalFloorArea() != null
                                && block.getBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(4000)) > 0)
                            noOfLiftsRqrd = noOfLiftsRqrd.add(BigDecimal
                                    .valueOf(Math.ceil(block.getBuilding().getTotalFloorArea().subtract(BigDecimal.valueOf(4000))
                                            .divide(BigDecimal.valueOf(2500), 2, RoundingMode.HALF_UP).doubleValue())));
                        boolean valid = false;
                        if (noOfLiftsRqrd.compareTo(BigDecimal.valueOf(Double.valueOf(block.getNumberOfLifts()))) <= 0)
                            valid = true;
                        if (valid) {
                            pl.reportOutput
                                    .add(buildRuleOutputWithSubRule(String.format(SUBRULE_48_DESC, block.getNumber()),
                                            SUBRULE_48 + ", " + SUBRULE_40A_3,
                                            String.format(SUBRULE_48_DESC, block.getNumber()),
                                            String.format(SUBRULE_48_DESC, block.getNumber()),
                                            noOfLiftsRqrd.toString(),
                                            block.getNumberOfLifts(), Result.Accepted,
                                            null));
                            setReportOutputDetails(pl, SUBRULE_48 + ", " + SUBRULE_40A_3, SUBRULE_48_DESCRIPTION,
                                    noOfLiftsRqrd.toString(),
                                    block.getNumberOfLifts(),
                                    Result.Accepted.getResultVal(),
                                    "Occupancies other than Medical are provided,so number of floors above ground level > 4 is required to have lifts",
                                    scrutinyDetail);

                        } else {
                            pl.reportOutput
                                    .add(buildRuleOutputWithSubRule(String.format(SUBRULE_48_DESC, block.getNumber()),
                                            SUBRULE_48 + ", " + SUBRULE_40A_3,
                                            String.format(SUBRULE_48_DESC, block.getNumber()),
                                            String.format(SUBRULE_48_DESC, block.getNumber()),
                                            noOfLiftsRqrd.toString(),
                                            block.getNumberOfLifts(), Result.Not_Accepted,
                                            null));
                            setReportOutputDetails(pl, SUBRULE_48 + ", " + SUBRULE_40A_3, SUBRULE_48_DESCRIPTION,
                                    noOfLiftsRqrd.toString(),
                                    block.getNumberOfLifts(),
                                    Result.Not_Accepted.getResultVal(),
                                    "Occupancies other than Medical are provided,so number of floors above ground level > 4 is required to have lifts",
                                    scrutinyDetail);
                        }
                    }
                }

                if (block.getBuilding() != null && block.getBuilding().getBuildingHeight() != null
                        && block.getBuilding().getBuildingHeight().intValue() > 16)
                    if (!block.getBuilding().getFloors().isEmpty()) {
                        Integer floorUnits = 0;
                        for (Floor floor : block.getBuilding().getFloors())
                            if (!floor.getTerrace())
                                floorUnits = floorUnits + floor.getUnits().size();
                        if (floorUnits > 16) {
                            boolean validOutside = false;
                            Map<String, String> liftDimensions = new HashMap<>();
                            flr: for (Floor floor : block.getBuilding().getFloors())
                                if (!floor.getTerrace())
                                    for (Lift lift : floor.getLifts())
                                        if (lift.getLiftPolyLineClosed())
                                            for (Measurement measurement : lift.getLiftPolyLines()) {
                                                measurement.setWidth(BigDecimal
                                                        .valueOf(Math.round(measurement.getWidth().doubleValue() * 100d) / 100d));
                                                measurement.setHeight(BigDecimal.valueOf(
                                                        Math.round(measurement.getHeight().doubleValue() * 100d) / 100d));
                                                if (measurement.getWidth().compareTo(BigDecimal.valueOf(1.1)) >= 0
                                                        && measurement.getHeight().compareTo(BigDecimal.valueOf(2)) >= 0) {
                                                    validOutside = true;
                                                    liftDimensions.put("width", measurement.getWidth().toString());
                                                    liftDimensions.put("length", measurement.getHeight().toString());
                                                    liftDimensions.put("floor", floor.getNumber().toString());
                                                    liftDimensions.put("lift", lift.getNumber().toString());
                                                    break flr;
                                                }
                                            }
                            if (validOutside)
                                setReportOutputDetails(pl, SUBRULE_118,
                                        String.format(SUBRULE_118_DESCRIPTION, liftDimensions.get("lift"),
                                                liftDimensions.get("floor")),
                                        "2.0 m * 1.10 m", liftDimensions.get("length") + " * " + liftDimensions.get("width"),
                                        Result.Accepted.getResultVal(),
                                        "Height of building is greater than 16 m", scrutinyDetail1);
                            else
                                setReportOutputDetails(pl, SUBRULE_118, SUBRULE_118_DESC,
                                        "2.0 m * 1.10 m", "None of the lift has minimum dimensions as provided",
                                        Result.Not_Accepted.getResultVal(),
                                        "Height of building is greater than 16 m", scrutinyDetail1);
                        }
                    }

            }

        return pl;
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDesc, String expected, String actual,
            String status, String remarks, ScrutinyDetail scrutinyDetail) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        details.put(REMARKS, remarks);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

    private void validateDimensions(PlanDetail planDetail, String blockNo, int floorNo, String liftNo,
            List<Measurement> liftPolylines) {
        int count = 0;
        for (Measurement m : liftPolylines)
            if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
                count++;
        if (count > 0)
            planDetail.addError(String.format(DxfFileConstants.LAYER_LIFT_WITH_NO, blockNo, floorNo, liftNo),
                    count + " number of lift polyline not having only 4 points in layer "
                            + String.format(DxfFileConstants.LAYER_LIFT_WITH_NO, blockNo, floorNo, liftNo));
    }

}
