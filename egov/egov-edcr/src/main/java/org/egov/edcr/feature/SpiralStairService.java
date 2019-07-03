package org.egov.edcr.feature;

import static org.egov.edcr.utility.ParametersConstants.BUILDING_HEIGHT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.PLOT_AREA;
import static org.egov.edcr.utility.ParametersConstants.RADIUS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.SpiralStair;
import org.egov.edcr.entity.TypicalFloor;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFDocument;
import org.springframework.stereotype.Service;

@Service
public class SpiralStairService extends GeneralRule implements RuleService {
    private static final Logger LOG = Logger.getLogger(SpiralStairService.class);
    private static final String FLOOR = "Floor";
    private static final String EXPECTED_DIAMETER = "1.50";
    private static final String RULE114_7 = "114(6)";
    private static final String RULE47_1 = "47(1)";
    private static final String DIAMETER_DESCRIPTION = "Minimum diameter for spiral fire stair %s";

    @Override
    public PlanDetail extract(PlanDetail planDetail, DXFDocument doc) {

        List<Block> blocks = planDetail.getBlocks();

        for (Block block : blocks)
            if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                outside: for (Floor floor : block.getBuilding().getFloors()) {
                    if (!block.getTypicalFloor().isEmpty())
                        for (TypicalFloor tp : block.getTypicalFloor())
                            if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                                for (Floor allFloors : block.getBuilding().getFloors())
                                    if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                                        if (!allFloors.getFireStairs().isEmpty()) {
                                            floor.setFireStairs(allFloors.getFireStairs());
                                            continue outside;
                                        }

                    // Layer name convention BLK_n_FLR_i_SPIRAL_FIRE_STAIR
                    String spiralStairLayerName = "BLK_" + block.getNumber() + "_FLR_" + floor.getNumber() + "_SPIRAL_FIRE_STAIR"
                            + "_+\\d";

                    List<String> spiralStairNames = Util.getLayerNamesLike(doc, spiralStairLayerName);
                    List<SpiralStair> spiralStairs = new ArrayList<>();

                    if (!spiralStairNames.isEmpty())
                        for (String spiralStairName : spiralStairNames) {
                            String[] array = spiralStairName.split("_");
                            if (array[7] != null && !array[7].isEmpty()) {
                                SpiralStair spiralStair = new SpiralStair();
                                spiralStair.setNumber(array[7]);

                                // set polylines in BLK_n_FLR_i_SPIRAL_FIRE_STAIR_k
                                List<DXFCircle> spiralFireEscapeStairPolyLines = Util.getPolyCircleByLayer(doc,
                                        String.format(DxfFileConstants.LAYER_FLOOR_SPIRAL_STAIR, block.getNumber(),
                                                floor.getNumber(), spiralStair.getNumber()));
                                spiralStair.setSpiralPolyLines(spiralFireEscapeStairPolyLines);

                                spiralStairs.add(spiralStair);

                            }
                        }

                    floor.setSpiralStairs(spiralStairs);
                }

        return planDetail;
    }

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        new HashMap<>();
        blk: for (Block block : planDetail.getBlocks())
            if (block.getBuilding() != null && !block.getBuilding().getOccupancies().isEmpty()) {
                if (Util.singleFamilyWithLessThanOrEqualToThreeFloor(block))
                    continue blk;

                ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, FLOOR);
                scrutinyDetail.addColumnHeading(3, DESCRIPTION);
                scrutinyDetail.addColumnHeading(4, REQUIRED);
                scrutinyDetail.addColumnHeading(5, PROVIDED);
                scrutinyDetail.addColumnHeading(6, STATUS);
                scrutinyDetail.setKey("Block_" + block.getNumber() + "_" + "Spiral Fire Stair");

                List<Floor> floors = block.getBuilding().getFloors();
                Floor topMostFloor = floors.stream()
                        .filter(floor -> floor.getTerrace() || floor.getUpperMost())
                        .findAny()
                        .orElse(null);

                for (Floor floor : floors) {
                    boolean belowTopMostFloor = topMostFloor != null ? floor.getNumber() == topMostFloor.getNumber() - 1 : false;
                    if (!floor.getTerrace() && !floor.getUpperMost() && !belowTopMostFloor) {
                        boolean isTypicalRepititiveFloor = false;
                        Map<String, Object> typicalFloorValues = Util.getTypicalFloorValues(block, floor,
                                isTypicalRepititiveFloor);

                        List<SpiralStair> spiralStairs = floor.getSpiralStairs();

                        if (spiralStairs.size() != 0) {
                            boolean valid = false;

                            for (SpiralStair spiralStair : spiralStairs) {
                                List<DXFCircle> spiralPolyLines = spiralStair.getSpiralPolyLines();

                                if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor")) {
                                    if (roundOffTwoDecimal(block.getBuilding().getBuildingHeight())
                                            .compareTo(roundOffTwoDecimal(BigDecimal.valueOf(10))) > 0 &&
                                            !spiralPolyLines.isEmpty())
                                        valid = true;
                                    String value = typicalFloorValues.get("typicalFloors") != null
                                            ? (String) typicalFloorValues.get("typicalFloors")
                                            : " floor " + floor.getNumber();

                                    if (valid)
                                        setReportOutputDetailsFloorStairWise(planDetail, RULE47_1, value, spiralStair.getNumber(),
                                                "",
                                                "spiral stair of fire stair not allowed for building with height > 9 for block " +
                                                        block.getNumber() + " " + value,
                                                Result.Not_Accepted.getResultVal(), scrutinyDetail);
                                    else if (!spiralPolyLines.isEmpty()) {
                                        DXFCircle minSpiralStair = spiralPolyLines.stream()
                                                .min(Comparator.comparing(DXFCircle::getRadius)).get();

                                        double minRadius = minSpiralStair.getRadius();

                                        BigDecimal radius = roundOffTwoDecimal(BigDecimal.valueOf(minRadius));
                                        BigDecimal diameter = roundOffTwoDecimal(
                                                radius.multiply(roundOffTwoDecimal(BigDecimal.valueOf(2))));
                                        BigDecimal minDiameter = roundOffTwoDecimal(BigDecimal.valueOf(1.50));

                                        if (diameter.compareTo(minDiameter) >= 0)
                                            setReportOutputDetailsFloorStairWise(planDetail, RULE114_7, value,
                                                    String.format(DIAMETER_DESCRIPTION, spiralStair.getNumber()),
                                                    EXPECTED_DIAMETER, String.valueOf(diameter), Result.Accepted.getResultVal(),
                                                    scrutinyDetail);
                                        else
                                            setReportOutputDetailsFloorStairWise(planDetail, RULE114_7, value,
                                                    String.format(DIAMETER_DESCRIPTION, spiralStair.getNumber()),
                                                    EXPECTED_DIAMETER, String.valueOf(diameter),
                                                    Result.Not_Accepted.getResultVal(), scrutinyDetail);
                                    }

                                }
                            }
                        }
                    }
                }
            }
        return planDetail;
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

    private BigDecimal roundOffTwoDecimal(BigDecimal number) {
        return number.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                DcrConstants.ROUNDMODE_MEASUREMENTS);
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("BLK_n_FLR_i_SPIRAL_FIRE_STAIR");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_AREA);
        parameters.add(BUILDING_HEIGHT);
        parameters.add(FLOOR_COUNT);
        parameters.add(RADIUS);
        return parameters;
    }

}
