package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.BLOCK_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.FLOOR_NAME_PREFIX;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.RECREATIONSPACE_DESC;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;
import static org.egov.edcr.utility.ParametersConstants.BLOCK_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_AREA;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_UNITS;
import static org.egov.edcr.utility.ParametersConstants.RECREATIONAL_AREA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.FloorUnit;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.stereotype.Service;

@Service
public class RecreationalSpace extends GeneralRule implements RuleService {

    public static final String SUB_RULE_50_DESC = "Recreational space for Residential Apartment ";
    public static final String SUB_RULE_50_DESC_CELLER = " Ground floor Recreational space ";

    public static final String SUB_RULE_50 = "50";
    public static final String SUB_RULE_50_2 = "50(2)";
    public static final String RECREATION = "RECREATION";
    public static final int TOTALNUMBEROFUNITS = 12;
    public static final BigDecimal THREE = BigDecimal.valueOf(3);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        for (Block block : pl.getBlocks())
            for (Floor floor : block.getBuilding().getFloors()) {
                String layerRegEx = BLOCK_NAME_PREFIX + block.getNumber() + "_" + FLOOR_NAME_PREFIX + floor.getNumber()
                        + "_" + RECREATION;
                for (DXFLWPolyline pline : Util.getPolyLinesByLayer(doc, layerRegEx))
                    for (Occupancy existingOcc : floor.getOccupancies())
                        if (OccupancyType.OCCUPANCY_A4.equals(existingOcc.getType()))
                            // only for apartment occupancies.
                            existingOcc.getRecreationalSpace().add(new Measurement(pline, false));
            }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {

        HashMap<String, String> errors = new HashMap<>();

        for (Block block : pl.getBlocks()) {
            BigDecimal totalRecreationArea = BigDecimal.ZERO;
            int numberOfUnitsInBlock = 0;

            if (block.getBuilding() != null)
                if (block.getBuilding().getFloorsAboveGround() != null
                        && block.getBuilding().getFloorsAboveGround().compareTo(THREE) > 0) {
                    if (!block.getBuilding().getOccupancies().isEmpty()
                            && checkOccupancyPresent(block.getBuilding().getOccupancies(), OccupancyType.OCCUPANCY_A4))
                        for (Floor floor : block.getBuilding().getFloors()) {
                            for (FloorUnit unit : floor.getUnits())
                                if (OccupancyType.OCCUPANCY_A4.equals(unit.getOccupancy().getType()))
                                    numberOfUnitsInBlock++;
                            for (Occupancy occ : floor.getOccupancies())
                                if (OccupancyType.OCCUPANCY_A4.equals(occ.getType()))
                                    for (Measurement measure : occ.getRecreationalSpace())
                                        totalRecreationArea = totalRecreationArea.add(measure.getArea());
                        }
                    if (numberOfUnitsInBlock > TOTALNUMBEROFUNITS && totalRecreationArea.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.put(RECREATIONSPACE_DESC,
                                prepareMessage(OBJECTNOTDEFINED, RECREATIONSPACE_DESC) + " for block " + block.getNumber());
                        pl.addErrors(errors);
                    }
                }
        }

        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("BLK_%_FLR_%_RECREATION");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(BLOCK_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(FLOOR_COUNT);
        parameters.add(FLOOR_UNITS);
        parameters.add(FLOOR_AREA);
        parameters.add(RECREATIONAL_AREA);
        return parameters;
    }

    private boolean checkOccupancyPresent(List<Occupancy> occupanc, OccupancyType occupancyType) {
        for (Occupancy occ : occupanc)
            if (occ.getType().equals(occupancyType))
                return true;
        return false;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        for (Block block : pl.getBlocks()) {

            scrutinyDetail = new ScrutinyDetail();
            scrutinyDetail.addColumnHeading(1, RULE_NO);
            scrutinyDetail.addColumnHeading(2, FIELDVERIFIED);
            scrutinyDetail.addColumnHeading(3, REQUIRED);
            scrutinyDetail.addColumnHeading(4, PROVIDED);
            scrutinyDetail.addColumnHeading(5, STATUS);

            scrutinyDetail.setKey("Block_" + block.getName() + "_" + "Recreational space");

            BigDecimal totalRecreationArea = BigDecimal.ZERO;
            BigDecimal totalRecreationAreaInGroundFloor = BigDecimal.ZERO;
            BigDecimal totalFloorArea = BigDecimal.ZERO;
            int numberOfUnitsInBlock = 0;

            if (block.getBuilding() != null && block.getBuilding().getFloorsAboveGround() != null
                    && block.getBuilding().getFloorsAboveGround().compareTo(THREE) > 0) {
                if (!block.getBuilding().getOccupancies().isEmpty()
                        && checkOccupancyPresent(block.getBuilding().getOccupancies(), OccupancyType.OCCUPANCY_A4))
                    for (Floor floor : block.getBuilding().getFloors()) {
                        for (FloorUnit unit : floor.getUnits())
                            if (OccupancyType.OCCUPANCY_A4.equals(unit.getOccupancy().getType()))
                                numberOfUnitsInBlock++;
                        for (Occupancy occ : floor.getOccupancies())
                            if (OccupancyType.OCCUPANCY_A4.equals(occ.getType())) {

                                if (occ.getFloorArea() != null)
                                    totalFloorArea = totalFloorArea.add(occ.getFloorArea());
                                for (Measurement measure : occ.getRecreationalSpace()) {
                                    if (floor.getNumber() == 0)
                                        totalRecreationAreaInGroundFloor = totalRecreationAreaInGroundFloor
                                                .add(measure.getArea());
                                    totalRecreationArea = totalRecreationArea.add(measure.getArea());

                                }
                            }
                    }
                if (numberOfUnitsInBlock > TOTALNUMBEROFUNITS && totalRecreationArea.compareTo(BigDecimal.ZERO) > 0) {
                    double requiredArea = totalFloorArea.doubleValue() * 6 / 100;
                    BigDecimal totalRecArea = totalRecreationArea.divide(totalFloorArea, 3, ROUNDMODE_MEASUREMENTS)
                            .multiply(BigDecimal.valueOf(100)).setScale(3, ROUNDMODE_MEASUREMENTS);

                    Map<String, String> details = new HashMap<>();
                    details.put(RULE_NO, SUB_RULE_50_2);
                    details.put(FIELDVERIFIED, "Minimum 6% recreational space should be provided of total floor area");
                    details.put(REQUIRED, "6%");
                    details.put(PROVIDED, totalRecArea + "%");

                    if (totalRecreationArea.doubleValue() > requiredArea)
                        details.put(STATUS, Result.Accepted.getResultVal());
                    else
                        details.put(STATUS, Result.Not_Accepted.getResultVal());
                    scrutinyDetail.getDetail().add(details);

                    // requiredArea = (totalFloorArea.doubleValue() * 35) / 100;

                    Map<String, String> details1 = new HashMap<>();
                    details1.put(RULE_NO, SUB_RULE_50_2);
                    details1.put(FIELDVERIFIED, "Minimum 35% recreational space should be provided in ground floor");
                    details1.put(REQUIRED, "35%");
                    BigDecimal totalRecArea1 = totalRecreationAreaInGroundFloor
                            .divide(BigDecimal.valueOf(requiredArea), 5, ROUNDMODE_MEASUREMENTS).multiply(BigDecimal.valueOf(100))
                            .setScale(2, ROUNDMODE_MEASUREMENTS);
                    details1.put(PROVIDED, totalRecArea1 + "%");

                    if (totalRecArea1.doubleValue() >= 35)
                        details1.put(STATUS, Result.Accepted.getResultVal());
                    else
                        details1.put(STATUS, Result.Not_Accepted.getResultVal());
                    scrutinyDetail.getDetail().add(details1);
                    pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

                }
            }
        }

        return pl;
    }

}
