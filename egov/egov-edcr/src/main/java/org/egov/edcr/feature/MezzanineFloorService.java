package org.egov.edcr.feature;

import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.MEZZANINE_HALL;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;
import static org.egov.edcr.utility.ParametersConstants.CARPET_AREA_ASSEMBLY_BLDG_BALCONY;
import static org.egov.edcr.utility.ParametersConstants.CARPET_AREA_ASSEMBLY_BLDG_HALL;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.HALL_FLOOR_AREA;
import static org.egov.edcr.utility.ParametersConstants.MEZZANINE_FLOOR_AREA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.Hall;
import org.egov.edcr.entity.MezzanineFloor;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MezzanineFloorService extends GeneralRule implements RuleService {
    private static final String SUBRULE_35_1 = "35(1)";
    private static final String SUBRULE_35_1_DESC = "Maximum area of mezzanine floor";
    public static final String SUB_RULE_55_7_DESC = "Maximum allowed area of balcony";
    public static final String SUB_RULE_55_7 = "55(7)";
    private static final String FLOOR = "Floor";
    public static final String HALL_NUMBER = "Hall Number";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // extract mezzanine floor details
        /*
         * if (!pl.getBlocks().isEmpty()) { for (Block block : pl.getBlocks()) { if (block.getBuilding() != null &&
         * !block.getBuilding().getFloors().isEmpty()) { outside: for (Floor floor : block.getBuilding().getFloors()) { if
         * (!block.getTypicalFloor().isEmpty()) { for (TypicalFloor tp : block.getTypicalFloor()) { if
         * (tp.getRepetitiveFloorNos().contains(floor.getNumber())) { for (Floor allFloors : block.getBuilding().getFloors()) { if
         * (allFloors.getNumber().equals(tp.getModelFloorNo())) { if (!allFloors.getMezzanineFloor().isEmpty() ||
         * !allFloors.getHalls().isEmpty()) { floor.setMezzanineFloor(allFloors.getMezzanineFloor());
         * floor.setHalls(allFloors.getHalls()); continue outside; } } } } } } // extract mezzanine data String
         * mezzanineLayerNameRegExp = "BLK_" + block.getNumber() + "_FLR_" + floor.getNumber() + "_M" + "_+\\d" + "_BLT_UP_AREA";
         * List<String> mezzanineLayerNames = Util.getLayerNamesLike(doc, mezzanineLayerNameRegExp); List<MezzanineFloor>
         * mezzanineFloorList = new ArrayList<>(); if (!mezzanineLayerNames.isEmpty()) { for (String mezzanine :
         * mezzanineLayerNames) { String[] array = mezzanine.split("_"); if (array[5] != null && !array[5].isEmpty()) {
         * MezzanineFloor mezzanineFloor = new MezzanineFloor(); mezzanineFloor.setNumber(array[5]); List<DXFLWPolyline>
         * mezzaninePolyLines = Util.getPolyLinesByLayer(doc, String.format(DxfFileConstants. LAYER_MEZZANINE_FLOOR_BLT_UP_AREA,
         * block.getNumber(), floor.getNumber(), mezzanineFloor.getNumber())); BigDecimal builtUpArea = BigDecimal.ZERO;
         * OccupancyType occupancyType = null; if (!mezzaninePolyLines.isEmpty()) { for (DXFLWPolyline polyline :
         * mezzaninePolyLines) { BigDecimal polyLineBuiltUpArea = Util.getPolyLineArea(polyline); builtUpArea =
         * builtUpArea.add(polyLineBuiltUpArea == null ? BigDecimal.ZERO : polyLineBuiltUpArea); occupancyType =
         * Util.findOccupancyType(polyline); } } mezzanineFloor.setBuiltUpArea(builtUpArea);
         * mezzanineFloor.setOccupancyType(occupancyType); List<DXFLWPolyline> mezzanineDeductPolyLines =
         * Util.getPolyLinesByLayer(doc, String.format(DxfFileConstants. LAYER_MEZZANINE_FLOOR_DEDUCTION, block.getNumber(),
         * floor.getNumber(), mezzanineFloor.getNumber())); BigDecimal builtUpAreaDeduct = BigDecimal.ZERO; if
         * (!mezzanineDeductPolyLines.isEmpty()) { for (DXFLWPolyline polyLine : mezzanineDeductPolyLines) { BigDecimal
         * polyLineDeduct = Util.getPolyLineArea(polyLine); builtUpAreaDeduct = builtUpAreaDeduct.add(polyLineDeduct == null ?
         * BigDecimal.ZERO : polyLineDeduct); } } mezzanineFloor.setDeductions(builtUpAreaDeduct);
         * mezzanineFloorList.add(mezzanineFloor); } } } floor.setMezzanineFloor(mezzanineFloorList); // extract Hall data String
         * hallLayerNameRegExp = "BLK_" + block.getNumber() + "_FLR_" + floor.getNumber() + "_HALL" + "_+\\d" + "_BLT_UP_AREA";
         * List<String> hallLayerNames = Util.getLayerNamesLike(doc, hallLayerNameRegExp); List<Hall> hallsList = new
         * ArrayList<>(); if (!hallLayerNames.isEmpty()) { for (String hl : hallLayerNames) { String[] array = hl.split("_"); if
         * (array[5] != null && !array[5].isEmpty()) { Hall hall = new Hall(); hall.setNumber(array[5]); List<DXFLWPolyline>
         * hallPolyLines = Util.getPolyLinesByLayer(doc, String.format(DxfFileConstants. LAYER_MEZZANINE_HALL_BLT_UP_AREA,
         * block.getNumber(), floor.getNumber(), hall.getNumber())); BigDecimal builtUpArea = BigDecimal.ZERO; if
         * (!hallPolyLines.isEmpty()) { for (DXFLWPolyline polyline : hallPolyLines) { BigDecimal polyLineBuiltUpArea =
         * Util.getPolyLineArea(polyline); builtUpArea = builtUpArea.add(polyLineBuiltUpArea == null ? BigDecimal.ZERO :
         * polyLineBuiltUpArea); } } hall.setBuiltUpArea(builtUpArea); List<DXFLWPolyline> hallDeductPolyLines =
         * Util.getPolyLinesByLayer(doc, String.format(DxfFileConstants. LAYER_MEZZANINE_HALL_DEDUCTION, block.getNumber(),
         * floor.getNumber(), hall.getNumber())); BigDecimal builtUpAreaDeduct = BigDecimal.ZERO; if
         * (!hallDeductPolyLines.isEmpty()) { for (DXFLWPolyline polyLine : hallDeductPolyLines) { BigDecimal polyLineDeduct =
         * Util.getPolyLineArea(polyLine); builtUpAreaDeduct = builtUpAreaDeduct.add(polyLineDeduct == null ? BigDecimal.ZERO :
         * polyLineDeduct); } } hall.setDeductions(builtUpAreaDeduct); hallsList.add(hall); } } } floor.setHalls(hallsList); } } }
         * }
         */
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && !pl.getBlocks().isEmpty())
            blk: for (Block block : pl.getBlocks())
                if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
                    if (Util.checkExemptionConditionForBuildingParts(block))
                        continue blk;
                    for (Floor floor : block.getBuilding().getFloors())
                        if (!floor.getMezzanineFloor().isEmpty())
                            for (MezzanineFloor mezzanineFloor : floor.getMezzanineFloor())
                                if (mezzanineFloor != null && mezzanineFloor.getNumber() != null) {
                                    boolean mezzanineHallFound = false;
                                    for (Hall hall : floor.getHalls())
                                        if (hall.getNumber().equals(mezzanineFloor.getNumber())) {
                                            mezzanineHallFound = true;
                                            break;
                                        }
                                    if (!mezzanineHallFound) {
                                        errors.put(
                                                String.format(MEZZANINE_HALL, mezzanineFloor.getNumber(), floor.getNumber(),
                                                        block.getNumber()),
                                                edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                                        new String[] { String.format(MEZZANINE_HALL, mezzanineFloor.getNumber(),
                                                                floor.getNumber(), block.getNumber()) },
                                                        LocaleContextHolder.getLocale()));
                                        pl.addErrors(errors);
                                    }

                                    if (mezzanineFloor.getOccupancyType() == null) {
                                        String mezzanineLayer = String.format(DxfFileConstants.LAYER_MEZZANINE_FLOOR_BLT_UP_AREA,
                                                block.getNumber(), floor.getNumber(), mezzanineFloor.getNumber());
                                        errors.put(mezzanineLayer,
                                                "Color code not defined properly for mezzanine floor " + mezzanineLayer);
                                        pl.addErrors(errors);
                                    }
                                }
                }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("BLK_%_FLR_%_M_%_BLT_UP_AREA");
        layers.add("BLK_%_FLR_%_M_%_BLT_UP_AREA_DEDUCT");
        layers.add("BLK_%_FLR_%_HALL_%_BLT_UP_AREA");
        layers.add("BLK_%_FLR_%_HALL_%_BLT_UP_AREA_DEDUCT");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(FLOOR_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(FLOOR_COUNT);
        parameters.add(MEZZANINE_FLOOR_AREA);
        parameters.add(HALL_FLOOR_AREA);
        parameters.add(CARPET_AREA_ASSEMBLY_BLDG_BALCONY);
        parameters.add(CARPET_AREA_ASSEMBLY_BLDG_HALL);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        String subRule = SUBRULE_35_1;
        if (pl != null && !pl.getBlocks().isEmpty())
            blk: for (Block block : pl.getBlocks()) {
                scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, DESCRIPTION);
                scrutinyDetail.addColumnHeading(3, FLOOR);
                scrutinyDetail.addColumnHeading(4, REQUIRED);
                scrutinyDetail.addColumnHeading(5, PROVIDED);
                scrutinyDetail.addColumnHeading(6, STATUS);
                scrutinyDetail.setKey("Block_" + block.getNumber() + "_" + "Mezzanine Floor");
                if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
                    if (Util.checkExemptionConditionForBuildingParts(block))
                        continue blk;
                    for (Floor floor : block.getBuilding().getFloors())
                        for (MezzanineFloor mezzanineFloor : floor.getMezzanineFloor())
                            for (Hall hall : floor.getHalls())
                                if (mezzanineFloor.getNumber().equals(hall.getNumber())) {
                                    Boolean valid = false;
                                    boolean isTypicalRepititiveFloor = false;
                                    BigDecimal mezzanineFloorArea = mezzanineFloor.getBuiltUpArea() == null ? BigDecimal.ZERO
                                            : mezzanineFloor.getBuiltUpArea()
                                                    .subtract(mezzanineFloor.getDeductions() == null ? BigDecimal.ZERO
                                                            : mezzanineFloor.getDeductions());
                                    BigDecimal hallFloorArea = hall.getBuiltUpArea() == null ? BigDecimal.ZERO
                                            : hall.getBuiltUpArea().subtract(
                                                    hall.getDeductions() == null ? BigDecimal.ZERO : hall.getDeductions());
                                    BigDecimal oneThirdHallFloorArea = hallFloorArea.divide(BigDecimal.valueOf(3),
                                            DECIMALDIGITS_MEASUREMENTS,
                                            ROUNDMODE_MEASUREMENTS);
                                    Map<String, Object> typicalFloorValues = Util.getTypicalFloorValues(block, floor,
                                            isTypicalRepititiveFloor);
                                    if (!(Boolean) typicalFloorValues.get("isTypicalRepititiveFloor")) {
                                        if (mezzanineFloorArea.compareTo(oneThirdHallFloorArea) <= 0)
                                            valid = true;
                                        String value = typicalFloorValues.get("typicalFloors") != null
                                                ? (String) typicalFloorValues.get("typicalFloors")
                                                : " floor " + floor.getNumber();
                                        if (valid) {
                                            pl.reportOutput
                                                    .add(buildRuleOutputWithSubRule(
                                                            SUBRULE_35_1_DESC + " " + mezzanineFloor.getNumber() + " on floor "
                                                                    + floor.getNumber() + " , block " + block.getNumber(),
                                                            subRule,
                                                            SUBRULE_35_1_DESC + " " + mezzanineFloor.getNumber() + " on " + value
                                                                    + " , block " + block.getNumber(),
                                                            SUBRULE_35_1_DESC + " " + mezzanineFloor.getNumber() + " on " + value
                                                                    + " , block " + block.getNumber(),
                                                            oneThirdHallFloorArea + DcrConstants.IN_METER_SQR,
                                                            mezzanineFloorArea +
                                                                    DcrConstants.IN_METER_SQR,
                                                            Result.Accepted,
                                                            null));
                                            setReportOutputDetails(pl, subRule,
                                                    SUBRULE_35_1_DESC + " " + mezzanineFloor.getNumber(), value,
                                                    oneThirdHallFloorArea + DcrConstants.IN_METER_SQR, mezzanineFloorArea +
                                                            DcrConstants.IN_METER_SQR,
                                                    Result.Accepted.getResultVal());
                                        } else {
                                            pl.reportOutput
                                                    .add(buildRuleOutputWithSubRule(
                                                            SUBRULE_35_1_DESC + " " + mezzanineFloor.getNumber() + " on floor "
                                                                    + floor.getNumber() + " , block " + block.getNumber(),
                                                            subRule,
                                                            SUBRULE_35_1_DESC + " " + mezzanineFloor.getNumber() + " on " + value
                                                                    + " , block " + block.getNumber(),
                                                            SUBRULE_35_1_DESC + " " + mezzanineFloor.getNumber() + " on " + value
                                                                    + " , block " + block.getNumber(),
                                                            oneThirdHallFloorArea + DcrConstants.IN_METER_SQR,
                                                            mezzanineFloorArea +
                                                                    DcrConstants.IN_METER_SQR,
                                                            Result.Not_Accepted,
                                                            null));
                                            setReportOutputDetails(pl, subRule,
                                                    SUBRULE_35_1_DESC + " " + mezzanineFloor.getNumber(), value,
                                                    oneThirdHallFloorArea + DcrConstants.IN_METER_SQR, mezzanineFloorArea +
                                                            DcrConstants.IN_METER_SQR,
                                                    Result.Not_Accepted.getResultVal());

                                        }
                                    }
                                }
                }
            }
        processAssembly(pl);
        return pl;
    }

    public void processAssembly(PlanDetail pl) {
        for (Block b : pl.getBlocks())
            if (!b.getHallAreas().isEmpty() && !b.getBalconyAreas().isEmpty()) {
                scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, DESCRIPTION);
                scrutinyDetail.addColumnHeading(3, HALL_NUMBER);
                scrutinyDetail.addColumnHeading(4, REQUIRED);
                scrutinyDetail.addColumnHeading(5, PROVIDED);
                scrutinyDetail.addColumnHeading(6, STATUS);
                scrutinyDetail.setKey("Block_" + b.getNumber() + "_" + "Maximum area of balcony");

                for (Measurement m : b.getHallAreas()) {
                    BigDecimal balconyArea = BigDecimal.ZERO;
                    BigDecimal hallArea = m.getArea();
                    String hallNo = m.getPolyLine().getLayerName().substring(m.getPolyLine().getLayerName().length() - 1);
                    for (Measurement balcony : b.getBalconyAreas()) {
                        String balconyNo = balcony.getPolyLine().getLayerName()
                                .substring(balcony.getPolyLine().getLayerName().length() - 1);
                        if (hallNo.equalsIgnoreCase(balconyNo))
                            balconyArea = balconyArea.add(balcony.getArea());
                    }
                    double maxAllowedArea = hallArea.doubleValue() * 25 / 100;
                    if (balconyArea.doubleValue() > 0) {
                        Map<String, String> details = new HashMap<>();
                        details.put(RULE_NO, SUB_RULE_55_7);
                        details.put(DESCRIPTION, SUB_RULE_55_7_DESC);
                        details.put(HALL_NUMBER, hallNo);
                        details.put(REQUIRED, "<= " + String.valueOf(maxAllowedArea));
                        details.put(PROVIDED, String.valueOf(balconyArea));
                        details.put(STATUS, Result.Not_Accepted.getResultVal());

                        if (balconyArea.doubleValue() > maxAllowedArea)
                            details.put(STATUS, Result.Not_Accepted.getResultVal());
                        else
                            details.put(STATUS, Result.Accepted.getResultVal());
                        scrutinyDetail.getDetail().add(details);
                        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
                    }
                }
            }
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