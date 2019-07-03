package org.egov.edcr.rule;

import static org.egov.edcr.utility.ParametersConstants.BUILT_UP_AREA;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.RAMP_DEFINED_OR_NOT_ON_EACH_FLOOR;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Ramp;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.feature.GeneralRule;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.springframework.stereotype.Service;

@Service
public class CommonFeature extends GeneralRule implements RuleService {

    private static final String FLOOR = "Floor";
    private static final String SUBRULE_48_3_DESC = "Minimum number of lifts";
    private static final String SUBRULE_48_3 = "48(3)";
    private static final String SUBRULE_40A_3 = "40A(3)";
    private static final String REMARKS = "Remarks";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(FLOOR_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(FLOOR_COUNT);
        parameters.add(BUILT_UP_AREA);
        parameters.add(RAMP_DEFINED_OR_NOT_ON_EACH_FLOOR);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        if (pl != null && !pl.getBlocks().isEmpty())
            blk: for (Block block : pl.getBlocks()) {
                scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, FLOOR);
                scrutinyDetail.addColumnHeading(3, REQUIRED);
                scrutinyDetail.addColumnHeading(4, PROVIDED);
                scrutinyDetail.addColumnHeading(5, STATUS);
                scrutinyDetail.addColumnHeading(6, REMARKS);
                scrutinyDetail.setSubHeading(SUBRULE_48_3_DESC);
                scrutinyDetail.setKey("Block_" + block.getNumber() + "_" + "Ramp/Lift defined on each floor");
                if (block.getBuilding() != null && !block.getBuilding().getOccupancies().isEmpty()
                        && block.getBuilding().getFloorsAboveGround() != null
                        && block.getBuilding().getFloorsAboveGround().compareTo(BigDecimal.valueOf(1)) > 0) {
                    if (Util.checkExemptionConditionForBuildingParts(block))
                        continue blk;
                    if (!block.getBuilding().getFloors().isEmpty())
                        for (Floor floor : block.getBuilding().getFloors())
                            if (!floor.getTerrace()) {
                                boolean isTypicalRepititiveFloor = false;
                                Map<String, Object> typicalFloorValues = Util.getTypicalFloorValues(block, floor,
                                        isTypicalRepititiveFloor);
                                String value = typicalFloorValues.get("typicalFloors") != null
                                        ? (String) typicalFloorValues.get("typicalFloors")
                                        : " floor " + floor.getNumber();
                                List<OccupancyType> occupancyTypeList = block.getBuilding().getOccupancies().stream()
                                        .map(occupancy -> occupancy.getType()).collect(Collectors.toList());
                                occ: for (OccupancyType occupancyType : occupancyTypeList)
                                    if (occupancyType.equals(OccupancyType.OCCUPANCY_A4) &&
                                            block.getBuilding() != null && block.getBuilding().getTotalBuitUpArea() != null &&
                                            block.getBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(2500)) > 0) {
                                        Boolean flagRampFloor = checkRampDefinedOrNot(floor);
                                        if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor"))
                                            if (flagRampFloor.equals(Boolean.FALSE) && !floor.getLifts().isEmpty()) {
                                                processRule48_3_Accepted(block, floor, pl, value);
                                                break occ;
                                            } else if (flagRampFloor.equals(Boolean.FALSE) && floor.getLifts().isEmpty()) {
                                                processRule48_3_NotAccepted(block, floor, pl, value);
                                                break occ;
                                            }
                                    } else if ((occupancyType.equals(OccupancyType.OCCUPANCY_A2)
                                            || occupancyType.equals(OccupancyType.OCCUPANCY_A3) ||
                                            occupancyType.equals(OccupancyType.OCCUPANCY_B1)
                                            || occupancyType.equals(OccupancyType.OCCUPANCY_B2) ||
                                            occupancyType.equals(OccupancyType.OCCUPANCY_B3)
                                            || occupancyType.equals(OccupancyType.OCCUPANCY_C) ||
                                            occupancyType.equals(OccupancyType.OCCUPANCY_C1)
                                            || occupancyType.equals(OccupancyType.OCCUPANCY_C2) ||
                                            occupancyType.equals(OccupancyType.OCCUPANCY_C3)
                                            || occupancyType.equals(OccupancyType.OCCUPANCY_D) ||
                                            occupancyType.equals(OccupancyType.OCCUPANCY_D1) ||
                                            occupancyType.equals(OccupancyType.OCCUPANCY_D2) ||
                                            occupancyType.equals(OccupancyType.OCCUPANCY_E)
                                            || occupancyType.equals(OccupancyType.OCCUPANCY_F)
                                            || occupancyType.equals(OccupancyType.OCCUPANCY_F1) ||
                                            occupancyType.equals(OccupancyType.OCCUPANCY_F2)
                                            || occupancyType.equals(OccupancyType.OCCUPANCY_F3) ||
                                            occupancyType.equals(OccupancyType.OCCUPANCY_F4))
                                            && block.getBuilding() != null && block.getBuilding().getTotalBuitUpArea() != null &&
                                            block.getBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(1000)) > 0) {
                                        Boolean flagRampFloor = checkRampDefinedOrNot(floor);
                                        if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor"))
                                            if (flagRampFloor.equals(Boolean.FALSE) && !floor.getLifts().isEmpty()) {
                                                processRule48_3_Accepted(block, floor, pl, value);
                                                break occ;
                                            } else if (flagRampFloor.equals(Boolean.FALSE) && floor.getLifts().isEmpty()) {
                                                processRule48_3_NotAccepted(block, floor, pl, value);
                                                break occ;
                                            }
                                    }
                            }
                }
            }
        return pl;
    }

    private void processRule48_3_NotAccepted(Block block, Floor floor, PlanDetail pl, String value) {
        pl.reportOutput
                .add(buildRuleOutputWithSubRule(SUBRULE_48_3_DESC + " for block " + block.getNumber() + value,
                        SUBRULE_48_3 + ", " + SUBRULE_40A_3,
                        SUBRULE_48_3_DESC + " for block " + block.getNumber() + value,
                        SUBRULE_48_3_DESC + " for block " + block.getNumber() + value,
                        String.valueOf(1),
                        String.valueOf(0), Result.Not_Accepted,
                        null));
        setReportOutputDetails(pl, SUBRULE_48_3 + ", " + SUBRULE_40A_3, value, String.valueOf(1),
                String.valueOf(0),
                Result.Not_Accepted.getResultVal(), "Lift required as ramp with maximum slope of 0.1 not defined on this floor");
    }

    private void processRule48_3_Accepted(Block block, Floor floor, PlanDetail pl, String value) {
        pl.reportOutput
                .add(buildRuleOutputWithSubRule(SUBRULE_48_3_DESC + " for block " + block.getNumber() + value,
                        SUBRULE_48_3 + ", " + SUBRULE_40A_3,
                        SUBRULE_48_3_DESC + " for block " + block.getNumber() + value,
                        SUBRULE_48_3_DESC + " for block " + block.getNumber() + value,
                        String.valueOf(1),
                        String.valueOf(floor.getLifts().size()), Result.Accepted,
                        null));
        setReportOutputDetails(pl, SUBRULE_48_3 + ", " + SUBRULE_40A_3, value, String.valueOf(1),
                String.valueOf(floor.getLifts().size()),
                Result.Accepted.getResultVal(), "Lift required as ramp with maximum slope of 0.1 not defined on this floor");
    }

    private Boolean checkRampDefinedOrNot(Floor floor) {
        Boolean flagRampFloor = false;
        if (!floor.getRamps().isEmpty())
            for (Ramp ramp : floor.getRamps())
                if (ramp.getSlope() != null && ramp.getSlope().compareTo(BigDecimal.valueOf(0.1)) <= 0) {
                    flagRampFloor = true;
                    break;
                }
        return flagRampFloor;
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String floor, String expected, String actual, String status,
            String remarks) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(FLOOR, floor);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        details.put(REMARKS, remarks);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }
}
