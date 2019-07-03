package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.BLOCK_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.FLOOR_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.LOADING_UNLOADING;
import static org.egov.edcr.constants.DxfFileConstants.MECH_PARKING;
import static org.egov.edcr.constants.DxfFileConstants.PARKING_SLOT;
import static org.egov.edcr.constants.DxfFileConstants.TWO_WHEELER_PARKING;
import static org.egov.edcr.constants.DxfFileConstants.UNITFA;
import static org.egov.edcr.constants.DxfFileConstants.UNITFA_BALCONY;
import static org.egov.edcr.constants.DxfFileConstants.UNITFA_DEDUCT;
import static org.egov.edcr.constants.DxfFileConstants.UNITFA_DINING;
import static org.egov.edcr.constants.DxfFileConstants.UNITFA_HALL;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_B1;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_B2;
import static org.egov.edcr.entity.OccupancyType.OCCUPANCY_B3;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.ParametersConstants.BALCONY_AREA;
import static org.egov.edcr.utility.ParametersConstants.BUILDING_TYPE;
import static org.egov.edcr.utility.ParametersConstants.CARPET_AREA;
import static org.egov.edcr.utility.ParametersConstants.COUNT_CHECK;
import static org.egov.edcr.utility.ParametersConstants.DINNING_AREA;
import static org.egov.edcr.utility.ParametersConstants.DISABLED_PERSON_CAR_PARKING_UNITS;
import static org.egov.edcr.utility.ParametersConstants.DIST_FROM_DA_TO_MAIN_ENTRANCE;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.FLOOR_UNITS;
import static org.egov.edcr.utility.ParametersConstants.GENERAL_CAR_PARKING_UNITS;
import static org.egov.edcr.utility.ParametersConstants.HALL_AREA;
import static org.egov.edcr.utility.ParametersConstants.LOADING_UNLOADING_AREA;
import static org.egov.edcr.utility.ParametersConstants.MECHANICAL_PARKING_COUNT;
import static org.egov.edcr.utility.ParametersConstants.MECHANICAL_PARKING_UNITS;
import static org.egov.edcr.utility.ParametersConstants.PLOT_AREA;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.SLOT_DIMENSION_CHECK;
import static org.egov.edcr.utility.ParametersConstants.SP_DINNING_SEATS;
import static org.egov.edcr.utility.ParametersConstants.TWO_WHEELER_PARKING_AREA;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.FloorUnit;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.ParkingDetails;
import org.egov.edcr.entity.ParkingHelper;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.TypicalFloor;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.Polygon;
import org.egov.edcr.utility.math.Ray;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.springframework.stereotype.Service;

@Service
public class Parking extends GeneralRule implements RuleService {

    private static final String LOADING_UNLOADING_DESC = "Minimum required Loading/Unloading area";
    private static final String MINIMUM_AREA_OF_EACH_DA_PARKING = " Minimum Area of Each DA parking";
    private static final String DA_PARKING_SLOT_AREA = "DA Parking Slot Area";
    private static final String NO_VIOLATION_OF_AREA = "No violation of area in ";
    private static final String MIN_AREA_EACH_CAR_PARKING = " Minimum Area of Each Car parking";
    private static final String PARKING_VIOLATED_MINIMUM_AREA = " parking violated minimum area.";
    private static final String PARKING = " parking ";
    private static final String NUMBERS = " Numbers ";
    private static final String MECHANICAL_PARKING = "Mechanical parking";
    private static final String MAX_ALLOWED_MECH_PARK = "Maximum allowed mechanical parking";
    private static final String TWO_WHEELER_PARK_AREA = "Two Wheeler Parking Area";
    private static final String DA_PARKING = "DA parking";
    private static final String OBJECT_NOT_DEFINED = "msg.error.mandatory.object1.not.defined";
    private static final Logger LOGGER = Logger.getLogger(Parking.class);
    private static final String SUB_RULE_34_1 = "34(1)";
    private static final String SUB_RULE_34_1_DESCRIPTION = "Parking Slots Area";
    private static final String SUB_RULE_34_2 = "34(2)";
    private static final String SUB_RULE_40A__5 = "40A(5)";
    private static final String SUB_RULE_34_2_DESCRIPTION = "Car Parking ";
    private static final String PARKING_MIN_AREA = " 2.70 M x 5.50 M";
    private static final double PARKING_SLOT_WIDTH = 2.7;
    private static final double PARKING_SLOT_HEIGHT = 5.5;
    private static final double DA_PARKING_SLOT_WIDTH = 3.6;
    private static final double DA_PARKING_SLOT_HEIGHT = 5.5;
    private static final String DA_PARKING_MIN_AREA = " 3.60 M x 5.50 M";
    private static final String ZERO_TO_60 = "0-60";
    private static final String SIXTY_TO_150 = "60-150";
    private static final String HUNDRED_FIFTY_TO_250 = "150-250";
    private static final String GREATER_THAN_TWO_HUNDRED_FIFTY = ">250";
    private static final String ZERO_TO_5 = "0-5";
    private static final String FIVE_TO_12 = "5-12";
    private static final String GREATER_THAN_TWELVE = ">12";
    private static final String ZERO_TO_12 = "0-12";
    private static final String TWELVE_TO_20 = "12-20";
    private static final String GREATER_THAN_TWENTY = ">20";
    private static final String ZERO_TO_N = ">0";
    final Ray rayCasting = new Ray(new Point(-1.123456789, -1.987654321, 0d));
    public static final String NO_OF_UNITS = "No of apartment units";
    private static final String SUB_RULE_34_2_VISIT_DESCRIPTION = "Visitor Car Parking ";
    private static final String UNITFA_HALL_NOT_DEFINED = "UNITFA_HALL to calculate required parking";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {

        for (Block block : pl.getBlocks()) {
            for (Floor floor : block.getBuilding().getFloors()) {
                String layerRegEx = BLOCK_NAME_PREFIX + block.getNumber() + "_" + FLOOR_NAME_PREFIX + floor.getNumber()
                        + "_" + UNITFA;
                List<DXFLWPolyline> occupancyUnits = Util.getPolyLinesByLayer(doc, layerRegEx);
                extractByLayer(pl, doc, block, floor, occupancyUnits);
            }

            String hallLayer = BLOCK_NAME_PREFIX + block.getNumber() + "_" + UNITFA_HALL + "_" + "\\d";
            List<String> layerNames = Util.getLayerNamesLike(doc, hallLayer);
            for (String s : layerNames)
                Util.getPolyLinesByLayer(doc, s).forEach(
                        hallPolyline -> block.getHallAreas().add(new Measurement(hallPolyline, true)));

            String dinningLayer = BLOCK_NAME_PREFIX + block.getNumber() + "_" + UNITFA_DINING + "_" + "\\d";
            List<String> layerNames1 = Util.getLayerNamesLike(doc, dinningLayer);
            for (String s : layerNames1)
                Util.getPolyLinesByLayer(doc, s).forEach(
                        dinningPolyline -> block.getDiningSpaces().add(new Measurement(dinningPolyline, true)));
            String balconyLayer = BLOCK_NAME_PREFIX + block.getNumber() + "_" + UNITFA_BALCONY + "_" + "\\d";
            List<String> layerNames2 = Util.getLayerNamesLike(doc, balconyLayer);
            for (String s : layerNames2)
                Util.getPolyLinesByLayer(doc, s).forEach(
                        balconyPolyline -> block.getBalconyAreas().add(new Measurement(balconyPolyline, true)));
        }

        Util.getPolyLinesByLayer(doc, LOADING_UNLOADING).forEach(loadUnloadPolyline -> pl.getParkingDetails()
                .getLoadUnload().add(new Measurement(loadUnloadPolyline, true)));

        Util.getPolyLinesByLayer(doc, MECH_PARKING).forEach(
                mechParkPolyline -> pl.getParkingDetails().getMechParking().add(new Measurement(mechParkPolyline, true)));

        Util.getPolyLinesByLayer(doc, TWO_WHEELER_PARKING).forEach(twoWheelerPolyline -> pl.getParkingDetails()
                .getTwoWheelers().add(new Measurement(twoWheelerPolyline, true)));

        Util.getPolyLinesByLayer(doc, DxfFileConstants.DA_PARKING).forEach(disablePersonParkPolyline -> pl
                .getParkingDetails().getDisabledPersons().add(new Measurement(disablePersonParkPolyline, true)));

        BigDecimal dimension = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.DA_PARKING, pl);
        pl.getParkingDetails().setDistFromDAToMainEntrance(dimension);
        List<DXFLWPolyline> bldParking = Util.getPolyLinesByLayer(doc, DxfFileConstants.PARKING_SLOT);
        for (DXFLWPolyline pline : bldParking)
            pl.getParkingDetails().getCars().add(new Measurement(pline, true));

        for (Block b : pl.getBlocks()) {
            b.getBuilding().sortFloorByName();
            if (!b.getTypicalFloor().isEmpty())
                for (TypicalFloor typical : b.getTypicalFloor()) {
                    Floor modelFloor = b.getBuilding().getFloorNumber(typical.getModelFloorNo());
                    for (Integer no : typical.getRepetitiveFloorNos()) {
                        Floor typicalFloor = b.getBuilding().getFloorNumber(no);
                        typicalFloor.setUnits(modelFloor.getUnits());
                    }
                }
        }

        return pl;

    }

    /**
     *
     */
    private void extractByLayer(PlanDetail pl, DXFDocument doc, Block block, Floor floor,
            List<DXFLWPolyline> dxflwPolylines) {
        int i = 0;
        if (!dxflwPolylines.isEmpty()) {
            List<FloorUnit> floorUnits = new ArrayList<>();
            for (DXFLWPolyline flrUnitPLine : dxflwPolylines) {
                Occupancy occupancy = new Occupancy();
                // this should not be called
                Util.setOccupancyType(flrUnitPLine, occupancy);
                specialCaseCheckForOccupancyType(flrUnitPLine, occupancy);

                if (occupancy.getType() != null) {
                    FloorUnit floorUnit = new FloorUnit();
                    floorUnit.setOccupancy(occupancy);
                    floorUnit.setPolyLine(flrUnitPLine);
                    floorUnit.setArea(Util.getPolyLineArea(flrUnitPLine));
                    i++;
                    Polygon polygon = Util.getPolygon(flrUnitPLine);
                    BigDecimal deduction = BigDecimal.ZERO;
                    String deductLayerName = BLOCK_NAME_PREFIX + block.getNumber() + "_" + FLOOR_NAME_PREFIX
                            + floor.getNumber() + "_" + UNITFA_DEDUCT;
                    for (DXFLWPolyline occupancyDeduct : Util.getPolyLinesByLayer(doc, deductLayerName)) {
                        boolean contains = false;
                        Iterator buildingIterator = occupancyDeduct.getVertexIterator();
                        while (buildingIterator.hasNext()) {
                            DXFVertex dxfVertex = (DXFVertex) buildingIterator.next();
                            Point point = dxfVertex.getPoint();
                            if (rayCasting.contains(point, polygon)) {
                                contains = true;
                                Measurement measurement = new Measurement();
                                measurement.setPolyLine(occupancyDeduct);
                                measurement.setArea(Util.getPolyLineArea(occupancyDeduct));
                                floorUnit.getArea().subtract(Util.getPolyLineArea(occupancyDeduct));
                                floorUnit.getDeductions().add(measurement);
                            }
                        }
                        if (contains) {
                            LOGGER.info("current deduct " + deduction + "  :add deduct for rest unit " + i + " area added "
                                    + Util.getPolyLineArea(occupancyDeduct));
                            deduction = deduction.add(Util.getPolyLineArea(occupancyDeduct));
                        }
                    }

                    floorUnit.setTotalUnitDeduction(deduction);
                    floorUnits.add(floorUnit);
                }
                floor.setUnits(floorUnits);
            }
        }
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {

        Boolean isExempted = checkIsParkingValidationRequired(pl);
        if (!isExempted) {
            HashMap<String, String> errors = new HashMap<>();
            if (pl.getParkingDetails().getCars().isEmpty()) {
                errors.put(DcrConstants.PARKINGSLOT_UNIT,
                        getLocaleMessage(OBJECT_NOT_DEFINED, DcrConstants.PARKINGSLOT_UNIT));
                pl.addErrors(errors);
            }
            if (pl.getParkingDetails().getTwoWheelers().isEmpty()) {
                errors.put(DcrConstants.RULE34,
                        getLocaleMessage(OBJECT_NOT_DEFINED, DcrConstants.TWO_WHEELER_PARKING_SLOT));
                pl.addErrors(errors);
            }
            validateDimensions(pl);
        }

        return pl;
    }

    private void processUnits(PlanDetail pl) {
        int occupancyA4UnitsCount = 0;
        int occupancyA2UnitsCount = 0;
        int noOfRoomsWithAttchdBthrms = 0;
        int noOfRoomsWithoutAttchdBthrms = 0;
        int noOfDineRooms = 0;
        HashMap<String, String> errors = new HashMap<>();

        for (Block block : pl.getBlocks())
            if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                for (Floor floor : block.getBuilding().getFloors())
                    if (floor.getUnits() != null && !floor.getUnits().isEmpty())
                        for (FloorUnit unit : floor.getUnits()) {
                            if (unit.getOccupancy().getType().equals(OccupancyType.OCCUPANCY_A4))
                                occupancyA4UnitsCount++;
                            if (unit.getOccupancy().getType().equals(OccupancyType.OCCUPANCY_A2)
                                    || unit.getOccupancy().getType().equals(OccupancyType.OCCUPANCY_F3)) {
                                occupancyA2UnitsCount++;
                                if (unit.getOccupancy().getWithAttachedBath())
                                    noOfRoomsWithAttchdBthrms++;
                                if (unit.getOccupancy().getWithOutAttachedBath())
                                    noOfRoomsWithoutAttchdBthrms++;
                                if (unit.getOccupancy().getWithDinningSpace())
                                    noOfDineRooms++;

                            }
                        }

        if (pl.getVirtualBuilding().getOccupancies().contains(OccupancyType.OCCUPANCY_A4)) {
            if (occupancyA4UnitsCount == 0)
                setReportOutputDetails(pl, SUB_RULE_34_2, "UNITFA is not defined", String.valueOf(1),
                        String.valueOf(occupancyA4UnitsCount), Result.Not_Accepted.getResultVal());

            if (occupancyA4UnitsCount > 0)
                setReportOutputDetails(pl, SUB_RULE_34_2, NO_OF_UNITS, String.valueOf("-"),
                        String.valueOf(occupancyA4UnitsCount), Result.Accepted.getResultVal());

        }

        if (pl.getVirtualBuilding().getOccupancies().contains(OccupancyType.OCCUPANCY_A2) ||
                pl.getVirtualBuilding().getOccupancies().contains(OccupancyType.OCCUPANCY_F3)) {

            if (occupancyA2UnitsCount == 0)
                setReportOutputDetails(pl, SUB_RULE_34_2, "UNITFA is not defined", String.valueOf(1),
                        String.valueOf(occupancyA2UnitsCount), Result.Not_Accepted.getResultVal());

            if (occupancyA2UnitsCount > 0 && noOfRoomsWithoutAttchdBthrms > 0)
                setReportOutputDetails(pl, SUB_RULE_34_2, "No of rooms without attached bathrooms", String.valueOf("-"),
                        String.valueOf(noOfRoomsWithoutAttchdBthrms), Result.Accepted.getResultVal());

            if (occupancyA2UnitsCount > 0 && noOfRoomsWithAttchdBthrms > 0)
                setReportOutputDetails(pl, SUB_RULE_34_2, "No of rooms with attached bathrooms", String.valueOf("-"),
                        String.valueOf(noOfRoomsWithAttchdBthrms), Result.Accepted.getResultVal());

            if (noOfDineRooms > 0 && (pl.getPlanInformation().getNoOfSeats() == null
                    || pl.getPlanInformation().getNoOfSeats() == 0)) {
                errors.put("SEATS_SP_RESI", "SEATS_SP_RESI not defined in PLAN_INFO");
                pl.addErrors(errors);
            }
        }

    }

    private void validateDimensions(PlanDetail pl) {
        ParkingDetails parkDtls = pl.getParkingDetails();
        if (!parkDtls.getCars().isEmpty()) {
            int count = 0;
            for (Measurement m : parkDtls.getCars())
                if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
                    count++;
            if (count > 0)
                pl.addError(PARKING_SLOT, count + " number of Parking slot polygon not having only 4 points.");
        }

        if (!parkDtls.getDisabledPersons().isEmpty()) {
            int count = 0;
            for (Measurement m : parkDtls.getDisabledPersons())
                if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
                    count++;
            if (count > 0)
                pl.addError(DA_PARKING, count + " number of DA Parking slot polygon not having only 4 points.");
        }
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        /*
         * All blocks is small plot in entire plot and floors above ground less than or equal to three and occupancy type of
         * entire block is either Residential or Commercial then parking process not require.
         */
        Boolean isExempted = checkIsParkingValidationRequired(pl);
        if (!isExempted) {
            scrutinyDetail = new ScrutinyDetail();
            scrutinyDetail.setKey("Common_Parking");
            scrutinyDetail.addColumnHeading(1, RULE_NO);
            scrutinyDetail.addColumnHeading(2, DESCRIPTION);
            scrutinyDetail.addColumnHeading(3, REQUIRED);
            scrutinyDetail.addColumnHeading(4, PROVIDED);
            scrutinyDetail.addColumnHeading(5, STATUS);
            processParking(pl);
        }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add(PARKING_SLOT);
        layers.add(DA_PARKING);
        layers.add("BLK_%_FLR_%_UNITFA");
        layers.add("BLK_%_FLR_%_UNITFA_DEDUCT");
        layers.add("SEATS_SP_RESI");
        layers.add("BLK_%_UNITFA_HALL_%");
        layers.add("BLK_%_UNITFA_BALCONY_%");
        layers.add("BLK_%_UNITFA_DINING");
        layers.add(TWO_WHEELER_PARKING);
        layers.add(LOADING_UNLOADING);
        layers.add(MECH_PARKING);
        layers.add(MECHANICAL_PARKING);
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        parameters.add(BUILDING_TYPE);
        parameters.add(FLOOR_COUNT);
        parameters.add(PLOT_AREA);
        parameters.add(OCCUPANCY);
        parameters.add(SLOT_DIMENSION_CHECK);
        parameters.add(COUNT_CHECK);
        parameters.add(GENERAL_CAR_PARKING_UNITS);
        parameters.add(DISABLED_PERSON_CAR_PARKING_UNITS);
        parameters.add(DIST_FROM_DA_TO_MAIN_ENTRANCE);
        parameters.add(FLOOR_UNITS);
        parameters.add(SP_DINNING_SEATS);
        parameters.add(HALL_AREA);
        parameters.add(BALCONY_AREA);
        parameters.add(DINNING_AREA);
        parameters.add(TWO_WHEELER_PARKING_AREA);
        parameters.add(CARPET_AREA);
        parameters.add(LOADING_UNLOADING_AREA);
        parameters.add(MECHANICAL_PARKING_COUNT);
        parameters.add(MECHANICAL_PARKING_UNITS);
        return parameters;
    }

    public boolean checkIsParkingValidationRequired(PlanDetail pl) {
        boolean exempted = true;
        BigDecimal commercialTotalCarpetArea = BigDecimal.ZERO;

        for (Block blk : pl.getBlocks())
            if (pl.getPlot().getSmallPlot()) {
                if (!Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), blk)) {
                    exempted = false;
                    break;
                }

            } else {
                List<Occupancy> proposedOccupancies = blk.getBuilding().getOccupancies();

                List<OccupancyType> occupancyTypes = proposedOccupancies.stream().map(Occupancy::getType)
                        .collect(Collectors.toList());

                Map<OccupancyType, BigDecimal> occupancyWiseCarpetAreaMap = proposedOccupancies.stream()
                        .collect(Collectors.toMap(Occupancy::getType, Occupancy::getCarpetArea));

                BigDecimal commercialCarpetArea = BigDecimal.ZERO;
                if (occupancyTypes.contains(OccupancyType.OCCUPANCY_F))
                    commercialCarpetArea = occupancyWiseCarpetAreaMap.get(OccupancyType.OCCUPANCY_F);

                BigDecimal floorCount = blk.getBuilding().getFloorsAboveGround();
                if (!occupancyTypes.isEmpty())
                    if (occupancyTypes.size() == 1 && occupancyTypes.contains(OccupancyType.OCCUPANCY_F)) {
                        if (commercialCarpetArea != null
                                && commercialCarpetArea.doubleValue() > 75) {
                            exempted = false;
                            break;
                        } else
                            commercialTotalCarpetArea = commercialTotalCarpetArea.add(commercialCarpetArea);

                    } else if (occupancyTypes.size() <= 2 && floorCount != null && floorCount.intValue() <= 3) {
                        if (occupancyTypes.size() == 2 && occupancyTypes.contains(OccupancyType.OCCUPANCY_A1)
                                && occupancyTypes.contains(OccupancyType.OCCUPANCY_A5)
                                || occupancyTypes.size() == 1 && (occupancyTypes.contains(OccupancyType.OCCUPANCY_A1)
                                        || occupancyTypes.contains(OccupancyType.OCCUPANCY_A5)))
                            exempted = true;
                        else if (occupancyTypes.size() == 2
                                && (occupancyTypes.contains(OccupancyType.OCCUPANCY_A1)
                                        || occupancyTypes.contains(OccupancyType.OCCUPANCY_A5))
                                && occupancyTypes.contains(OccupancyType.OCCUPANCY_F) && commercialCarpetArea != null
                                && commercialCarpetArea.doubleValue() <= 75)
                            exempted = true;
                        else {
                            exempted = false;
                            break;
                        }
                    } else {
                        exempted = false;
                        break;
                    }
            }

        if (commercialTotalCarpetArea.doubleValue() > 0)
            if (!exempted)
                exempted = false;
            else if (commercialTotalCarpetArea.doubleValue() <= 75)
                exempted = true;
            else
                exempted = false;

        return exempted;
    }

    public void processParking(PlanDetail planDetail) {
        Map<String, Integer> unitsCountMap = new ConcurrentHashMap<>();

        Map<String, BigDecimal> unitsAreaMap = new ConcurrentHashMap<>();
        ParkingHelper helper = new ParkingHelper();
        BigDecimal noOfSeats = BigDecimal.ZERO;

        // use proposed occupancies excluding existing single family.
        // INCLUDE EXISTING AND SINGLEFAMILY ALSO

        for (Occupancy occupancy : planDetail.getOccupancies())
            switch (occupancy.getType()) {
            case OCCUPANCY_A1:
            case OCCUPANCY_A4:
                unitsCountMap.put(ZERO_TO_60, 0);
                unitsCountMap.put(SIXTY_TO_150, 0);
                unitsCountMap.put(HUNDRED_FIFTY_TO_250, 0);
                unitsCountMap.put(GREATER_THAN_TWO_HUNDRED_FIFTY, 0);
                for (Block b : planDetail.getBlocks())
                    for (Floor f : b.getBuilding().getFloors())
                        for (FloorUnit unit : f.getUnits())
                            /*
                             * Even though if building type is single family (OCCUPANCY_A1) and no of floors more than 3 excluding
                             * stair room then parking validation is required.
                             **/
                            if (occupancy.getType().equals(unit.getOccupancy().getType()))
                                if (unit.getArea().doubleValue() < 60d)
                                    unitsCountMap.put(ZERO_TO_60, unitsCountMap.get(ZERO_TO_60) + 1);
                                else if (unit.getArea().doubleValue() < 150)
                                    unitsCountMap.put(SIXTY_TO_150, unitsCountMap.get(SIXTY_TO_150) + 1);
                                else if (unit.getArea().doubleValue() < 250d)
                                    unitsCountMap.put(HUNDRED_FIFTY_TO_250, unitsCountMap.get(HUNDRED_FIFTY_TO_250) + 1);
                                else
                                    unitsCountMap.put(GREATER_THAN_TWO_HUNDRED_FIFTY,
                                            unitsCountMap.get(GREATER_THAN_TWO_HUNDRED_FIFTY) + 1);

                helper.a1TotalParking = Math.ceil(unitsCountMap.get(ZERO_TO_60) / 3.0
                        + unitsCountMap.get(SIXTY_TO_150) * 1.0
                        + unitsCountMap.get(HUNDRED_FIFTY_TO_250) * 1.5
                        + unitsCountMap.get(GREATER_THAN_TWO_HUNDRED_FIFTY) * 2.0);
                helper.carParkingForDACal += helper.a1TotalParking;
                helper.visitorParking = Math.ceil(helper.a1TotalParking * 15 / 100);
                helper.totalRequiredCarParking += helper.a1TotalParking + helper.visitorParking;

                break;
            case OCCUPANCY_A2:
            case OCCUPANCY_F3:
                unitsCountMap.put(ZERO_TO_5, 0);
                unitsCountMap.put(FIVE_TO_12, 0);
                unitsCountMap.put(GREATER_THAN_TWELVE, 0);
                unitsCountMap.put(ZERO_TO_12, 0);
                unitsCountMap.put(TWELVE_TO_20, 0);
                unitsCountMap.put(GREATER_THAN_TWENTY, 0);
                unitsAreaMap.put(ZERO_TO_N, BigDecimal.ZERO);

                for (Block b : planDetail.getBlocks())
                    for (Floor f : b.getBuilding().getFloors())
                        for (FloorUnit unit : f.getUnits())
                            if ((unit.getOccupancy().getType().equals(OccupancyType.OCCUPANCY_A2)
                                    || unit.getOccupancy().getType().equals(OccupancyType.OCCUPANCY_F3))
                                    && unit.getOccupancy().getWithOutAttachedBath()) {
                                if (unit.getArea().compareTo(BigDecimal.valueOf(5)) < 0)
                                    unitsCountMap.put(ZERO_TO_5, unitsCountMap.get(ZERO_TO_5) + 1);
                                else if (unit.getArea().compareTo(BigDecimal.valueOf(12)) <= 0)
                                    unitsCountMap.put(FIVE_TO_12, unitsCountMap.get(FIVE_TO_12) + 1);
                                else if (unit.getArea().compareTo(BigDecimal.valueOf(12)) > 0)
                                    unitsCountMap.put(GREATER_THAN_TWELVE, unitsCountMap.get(GREATER_THAN_TWELVE) + 1);
                            } else if ((unit.getOccupancy().getType().equals(OccupancyType.OCCUPANCY_A2)
                                    || unit.getOccupancy().getType().equals(OccupancyType.OCCUPANCY_F3))
                                    && unit.getOccupancy().getWithAttachedBath()) {
                                if (unit.getArea().compareTo(BigDecimal.valueOf(12)) < 0)
                                    unitsCountMap.put(ZERO_TO_12, unitsCountMap.get(ZERO_TO_12) + 1);
                                else if (unit.getArea().compareTo(BigDecimal.valueOf(20)) <= 0)
                                    unitsCountMap.put(TWELVE_TO_20, unitsCountMap.get(TWELVE_TO_20) + 1);
                                else if (unit.getArea().compareTo(BigDecimal.valueOf(20)) > 0)
                                    unitsCountMap.put(GREATER_THAN_TWENTY, unitsCountMap.get(GREATER_THAN_TWENTY) + 1);
                            } else if ((unit.getOccupancy().getType().equals(OccupancyType.OCCUPANCY_A2)
                                    || unit.getOccupancy().getType().equals(OccupancyType.OCCUPANCY_F3))
                                    && unit.getOccupancy().getWithDinningSpace())
                                unitsAreaMap.put(ZERO_TO_N, unitsAreaMap.get(ZERO_TO_N).add(unit.getArea()));
                // For A2 with out attached bathroom
                double a2WOAttach = unitsCountMap.get(ZERO_TO_5) / 9.0
                        + unitsCountMap.get(FIVE_TO_12) / 6.0
                        + unitsCountMap.get(GREATER_THAN_TWELVE) / 3.0;
                helper.totalRequiredCarParking += a2WOAttach;
                helper.carParkingForDACal += a2WOAttach;

                // For A2 with attached bathroom
                double a2WithAttach = unitsCountMap.get(ZERO_TO_12) / 4.0
                        + unitsCountMap.get(TWELVE_TO_20) / 2.5
                        + unitsCountMap.get(GREATER_THAN_TWENTY) / 1.5;
                helper.totalRequiredCarParking += a2WithAttach;
                helper.carParkingForDACal += a2WithAttach;

                // For A2 with dinning area
                double noOfSeatsPlInfo = planDetail.getPlanInformation().getNoOfSeats() == null ? 0
                        : planDetail.getPlanInformation().getNoOfSeats() / 10.0;
                noOfSeats = unitsAreaMap.get(ZERO_TO_N).divide(BigDecimal.valueOf(20), DECIMALDIGITS_MEASUREMENTS,
                        RoundingMode.HALF_UP);
                if (noOfSeatsPlInfo > 0 && noOfSeats.doubleValue() > 0
                        && BigDecimal.valueOf(noOfSeatsPlInfo).compareTo(noOfSeats) > 0) {
                    helper.totalRequiredCarParking += noOfSeatsPlInfo;
                    helper.carParkingForDACal += noOfSeatsPlInfo;
                } else {
                    helper.totalRequiredCarParking += noOfSeats.doubleValue();
                    helper.carParkingForDACal += noOfSeats.doubleValue();
                }
                break;
            case OCCUPANCY_B1:
            case OCCUPANCY_B2:
            case OCCUPANCY_B3:
                BigDecimal carpetAreaForB1 = getTotalCarpetAreaByOccupancy(planDetail, OCCUPANCY_B1);
                if (carpetAreaForB1 != null) {
                    double b1 = carpetAreaForB1.divide(BigDecimal.valueOf(250), DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP)
                            .doubleValue();
                    helper.totalRequiredCarParking += b1;
                    helper.carParkingForDACal += b1;
                }
                BigDecimal carpetAreaForB2 = getTotalCarpetAreaByOccupancy(planDetail, OCCUPANCY_B2);
                if (carpetAreaForB2 != null) {
                    double b2 = carpetAreaForB2.divide(BigDecimal.valueOf(250), DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP)
                            .doubleValue();
                    helper.totalRequiredCarParking += b2;
                    helper.carParkingForDACal += b2;
                }
                BigDecimal carpetAreaForB3 = getTotalCarpetAreaByOccupancy(planDetail, OCCUPANCY_B3);
                if (carpetAreaForB3 != null) {
                    double b3 = carpetAreaForB3.divide(BigDecimal.valueOf(100), DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP)
                            .doubleValue();
                    helper.totalRequiredCarParking += b3;
                    helper.carParkingForDACal += b3;
                }
                break;
            case OCCUPANCY_D:
                BigDecimal hallArea = BigDecimal.ZERO;
                BigDecimal balconyArea = BigDecimal.ZERO;
                BigDecimal diningSpace = BigDecimal.ZERO;
                for (Block b : planDetail.getBlocks()) {
                    for (Measurement measurement : b.getHallAreas())
                        hallArea = hallArea.add(measurement.getArea());
                    for (Measurement measurement : b.getBalconyAreas())
                        balconyArea = balconyArea.add(measurement.getArea());
                    for (Measurement measurement : b.getDiningSpaces())
                        diningSpace = diningSpace.add(measurement.getArea());
                }

                BigDecimal totalArea = hallArea.add(balconyArea);
                if (totalArea.doubleValue() > 0) {
                    if (totalArea.compareTo(diningSpace) > 0) {
                        BigDecimal hall = totalArea.divide(BigDecimal.valueOf(1.5).multiply(BigDecimal.valueOf(15)),
                                DECIMALDIGITS_MEASUREMENTS,
                                RoundingMode.HALF_UP);
                        helper.totalRequiredCarParking += hall.doubleValue();
                        helper.carParkingForDACal += hall.doubleValue();
                    } else {
                        BigDecimal dining = diningSpace
                                .divide(BigDecimal.valueOf(1.5).multiply(BigDecimal.valueOf(15)), DECIMALDIGITS_MEASUREMENTS,
                                        RoundingMode.HALF_UP);
                        helper.totalRequiredCarParking += dining.doubleValue();
                        helper.carParkingForDACal += dining.doubleValue();
                    }
                } else
                    planDetail.addError(UNITFA_HALL,
                            getLocaleMessage(OBJECT_NOT_DEFINED, UNITFA_HALL_NOT_DEFINED));
                break;
            case OCCUPANCY_F:
            case OCCUPANCY_F4:
                BigDecimal noOfCarParking = BigDecimal.ZERO;
                if (occupancy.getCarpetArea().compareTo(BigDecimal.valueOf(75)) > 0)
                    if (occupancy != null && occupancy.getCarpetArea() != null
                            && occupancy.getCarpetArea().compareTo(BigDecimal.valueOf(1000)) <= 0)
                        noOfCarParking = occupancy.getCarpetArea().divide(BigDecimal.valueOf(75), DECIMALDIGITS_MEASUREMENTS,
                                RoundingMode.HALF_UP);
                    else if (occupancy != null && occupancy.getCarpetArea() != null
                            && occupancy.getCarpetArea().compareTo(BigDecimal.valueOf(1000)) > 0)
                        noOfCarParking = BigDecimal.valueOf(1000)
                                .divide(BigDecimal.valueOf(75), DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP)
                                .add(occupancy.getCarpetArea().subtract(BigDecimal.valueOf(1000)).divide(BigDecimal.valueOf(50),
                                        0, RoundingMode.HALF_UP));
                double f = noOfCarParking == null ? 0 : noOfCarParking.doubleValue();
                helper.totalRequiredCarParking += f;
                helper.carParkingForDACal += f;
                break;
            case OCCUPANCY_E:
                BigDecimal noOfCarParking1 = BigDecimal.ZERO;
                if (occupancy != null && occupancy.getCarpetArea() != null
                        && occupancy.getCarpetArea().compareTo(BigDecimal.valueOf(1000)) <= 0)
                    noOfCarParking1 = occupancy.getCarpetArea().divide(BigDecimal.valueOf(75), DECIMALDIGITS_MEASUREMENTS,
                            RoundingMode.HALF_UP);
                else if (occupancy != null && occupancy.getCarpetArea() != null
                        && occupancy.getCarpetArea().compareTo(BigDecimal.valueOf(1000)) > 0)
                    noOfCarParking1 = BigDecimal.valueOf(1000)
                            .divide(BigDecimal.valueOf(75), DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP)
                            .add(occupancy.getCarpetArea().subtract(BigDecimal.valueOf(1000)).divide(BigDecimal.valueOf(50),
                                    DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP));
                double e = noOfCarParking1 == null ? 0 : noOfCarParking1.doubleValue();
                helper.totalRequiredCarParking += e;
                helper.carParkingForDACal += e;
                break;
            }

        double medical = processParkingForMedical(planDetail);
        helper.totalRequiredCarParking += medical;
        helper.carParkingForDACal += medical;
        helper.totalRequiredCarParking += processParkingForIndustrialAndStorage(planDetail);
        helper.totalRequiredCarParking = Math.ceil(helper.totalRequiredCarParking);
        helper.carParkingForDACal = Math.ceil(helper.carParkingForDACal);
        planDetail.setParkingRequired(helper.totalRequiredCarParking);
        if (helper.totalRequiredCarParking > 0) {
            validateDAParking(planDetail, helper);
            checkDimensionForCarParking(planDetail, helper);
            Integer providedMechParking = 0;
            if (!planDetail.getParkingDetails().getMechParking().isEmpty())
                providedMechParking = planDetail.getPlanInformation().getNoOfMechanicalParking();
            Integer totalProvidedParking = planDetail.getParkingDetails().getValidCarParkingSlots()
                    + planDetail.getParkingDetails().getValidDAParkingSlots()
                    + providedMechParking;
            if (helper.totalRequiredCarParking > totalProvidedParking)
                setReportOutputDetails(planDetail, SUB_RULE_34_2, SUB_RULE_34_2_DESCRIPTION,
                        helper.totalRequiredCarParking.intValue() + NUMBERS, String.valueOf(totalProvidedParking) + NUMBERS,
                        Result.Not_Accepted.getResultVal());
            else
                setReportOutputDetails(planDetail, SUB_RULE_34_2, SUB_RULE_34_2_DESCRIPTION,
                        helper.totalRequiredCarParking.intValue() + NUMBERS, String.valueOf(totalProvidedParking) + NUMBERS,
                        Result.Accepted.getResultVal());
            if (helper.visitorParking > 0)
                setReportOutputDetails(planDetail, SUB_RULE_34_2, SUB_RULE_34_2_VISIT_DESCRIPTION,
                        helper.visitorParking.intValue() + NUMBERS, "Included", Result.Accepted.getResultVal());
            processTwoWheelerParking(planDetail, helper);
            processMechanicalParking(planDetail, helper);
        }

        processUnits(planDetail);

        LOGGER.info("******************Require no of Car Parking***************" + helper.totalRequiredCarParking);
    }

    private double processParkingForIndustrialAndStorage(PlanDetail planDetail) {
        List<Occupancy> occupancyList = new ArrayList<>();
        Occupancy f = planDetail.getOccupancies().stream()
                .filter(occupancy -> OccupancyType.OCCUPANCY_F4.equals(occupancy.getType())
                        || OccupancyType.OCCUPANCY_F.equals(occupancy.getType()))
                .findAny().orElse(null);
        Occupancy g1 = planDetail.getOccupancies().stream()
                .filter(occupancy -> OccupancyType.OCCUPANCY_G1.equals(occupancy.getType())).findAny().orElse(null);
        Occupancy g2 = planDetail.getOccupancies().stream()
                .filter(occupancy -> OccupancyType.OCCUPANCY_G2.equals(occupancy.getType())).findAny().orElse(null);
        Occupancy h = planDetail.getOccupancies().stream()
                .filter(occupancy -> OccupancyType.OCCUPANCY_H.equals(occupancy.getType())).findAny().orElse(null);

        if (f != null)
            occupancyList.add(f);

        BigDecimal noOfCarParking = BigDecimal.ZERO;
        if (g1 != null && g1.getCarpetArea() != null) {
            occupancyList.add(g1);
            noOfCarParking = g1.getCarpetArea().divide(BigDecimal.valueOf(200), 0, RoundingMode.UP);
        }
        if (g2 != null && g2.getCarpetArea() != null) {
            noOfCarParking = g2.getCarpetArea().divide(BigDecimal.valueOf(200), 0, RoundingMode.UP);
            occupancyList.add(g2);
        }
        if (h != null && h.getCarpetArea() != null) {
            occupancyList.add(h);
            noOfCarParking = h.getCarpetArea().divide(BigDecimal.valueOf(200), 0, RoundingMode.UP);
        }
        processLoadingAndUnloading(planDetail, occupancyList);
        return noOfCarParking == null ? 0 : noOfCarParking.doubleValue();
    }

    private double processParkingForMedical(PlanDetail planDetail) {
        Occupancy c = planDetail.getOccupancies().stream()
                .filter(occupancy -> OccupancyType.OCCUPANCY_C.equals(occupancy.getType())).findAny().orElse(null);
        Occupancy c1 = planDetail.getOccupancies().stream()
                .filter(occupancy -> OccupancyType.OCCUPANCY_C1.equals(occupancy.getType())).findAny().orElse(null);
        Occupancy c2 = planDetail.getOccupancies().stream()
                .filter(occupancy -> OccupancyType.OCCUPANCY_C2.equals(occupancy.getType())).findAny().orElse(null);
        Occupancy c3 = planDetail.getOccupancies().stream()
                .filter(occupancy -> OccupancyType.OCCUPANCY_C3.equals(occupancy.getType())).findAny().orElse(null);
        BigDecimal totalCarpetArea = BigDecimal.ZERO;
        if (c != null && c.getCarpetArea() != null)
            totalCarpetArea = totalCarpetArea.add(c.getCarpetArea());
        if (c1 != null && c1.getCarpetArea() != null)
            totalCarpetArea = totalCarpetArea.add(c1.getCarpetArea());
        if (c2 != null && c2.getCarpetArea() != null)
            totalCarpetArea = totalCarpetArea.add(c2.getCarpetArea());
        if (c3 != null && c3.getCarpetArea() != null)
            totalCarpetArea = totalCarpetArea.add(c3.getCarpetArea());
        return totalCarpetArea == null ? 0
                : totalCarpetArea.divide(BigDecimal.valueOf(75), DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP).doubleValue();
    }

    private void processLoadingAndUnloading(PlanDetail pl, List<Occupancy> occupancies) {
        double providedArea = 0;
        for (Measurement measurement : pl.getParkingDetails().getLoadUnload())
            providedArea = providedArea + measurement.getArea().doubleValue();
        BigDecimal totalCarpetArea = BigDecimal.ZERO;
        for (Occupancy occupancy : occupancies)
            totalCarpetArea = totalCarpetArea
                    .add(occupancy.getCarpetArea() == null ? BigDecimal.ZERO : occupancy.getCarpetArea());
        if (totalCarpetArea.doubleValue() > 700) {
            double requiredArea = Math.ceil((totalCarpetArea.doubleValue() - 700) / 1000 * 30);
            HashMap<String, String> errors = new HashMap<>();
            if (pl.getParkingDetails().getLoadUnload().isEmpty()) {
                errors.put(DcrConstants.RULE34,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.LOAD_UNLOAD_AREA));
                pl.addErrors(errors);
            } else if (providedArea < requiredArea)
                setReportOutputDetails(pl, SUB_RULE_34_2, LOADING_UNLOADING_DESC, String.valueOf(requiredArea),
                        String.valueOf(providedArea), Result.Not_Accepted.getResultVal());
            else
                setReportOutputDetails(pl, SUB_RULE_34_2, LOADING_UNLOADING_DESC, String.valueOf(requiredArea),
                        String.valueOf(providedArea), Result.Accepted.getResultVal());
        }
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDesc, String expected, String actual,
            String status) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

    private void validateDAParking(PlanDetail pl, ParkingHelper helper) {
        helper.daParking = Math.ceil(helper.carParkingForDACal * 3 / 100);
        checkDimensionForDAParking(pl, helper);
        HashMap<String, String> errors = new HashMap<>();
        Boolean isValid = true;
        if (pl.getParkingDetails().getDisabledPersons().isEmpty()) {
            isValid = false;
            errors.put(SUB_RULE_40A__5, prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.DAPARKING_UNIT));
            pl.addErrors(errors);
        } else if (pl.getParkingDetails().getDistFromDAToMainEntrance() == null
                || pl.getParkingDetails().getDistFromDAToMainEntrance().compareTo(BigDecimal.ZERO) == 0) {
            isValid = false;
            errors.put(SUB_RULE_40A__5,
                    prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.DIST_FROM_DA_TO_ENTRANCE));
            pl.addErrors(errors);
        } else if (pl.getParkingDetails().getDistFromDAToMainEntrance().compareTo(BigDecimal.valueOf(30)) > 0) {
            isValid = false;
            setReportOutputDetails(pl, SUB_RULE_40A__5, DcrConstants.DIST_FROM_DA_TO_ENTRANCE,
                    "Should be less than 30" + DcrConstants.IN_METER,
                    pl.getParkingDetails().getDistFromDAToMainEntrance() + DcrConstants.IN_METER,
                    Result.Not_Accepted.getResultVal());
        } else if (pl.getParkingDetails().getValidDAParkingSlots() < helper.daParking)
            setReportOutputDetails(pl, SUB_RULE_40A__5, DA_PARKING, helper.daParking.intValue() + NUMBERS,
                    pl.getParkingDetails().getValidDAParkingSlots() + NUMBERS, Result.Not_Accepted.getResultVal());
        else
            setReportOutputDetails(pl, SUB_RULE_40A__5, DA_PARKING, helper.daParking.intValue() + NUMBERS,
                    pl.getParkingDetails().getValidDAParkingSlots() + NUMBERS, Result.Accepted.getResultVal());
        if (isValid)
            setReportOutputDetails(pl, SUB_RULE_40A__5, DcrConstants.DIST_FROM_DA_TO_ENTRANCE,
                    "Less than 30" + DcrConstants.IN_METER,
                    pl.getParkingDetails().getDistFromDAToMainEntrance() + DcrConstants.IN_METER, Result.Accepted.getResultVal());
    }

    private void processTwoWheelerParking(PlanDetail pl, ParkingHelper helper) {
        helper.twoWheelerParking = BigDecimal.valueOf(0.25 * helper.totalRequiredCarParking * 2.70 * 5.50)
                .setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
        double providedArea = 0;
        for (Measurement measurement : pl.getParkingDetails().getTwoWheelers())
            providedArea = providedArea + measurement.getArea().doubleValue();
        if (providedArea < helper.twoWheelerParking)
            setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA, helper.twoWheelerParking + " " + DcrConstants.SQMTRS,
                    BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + " " + DcrConstants.SQMTRS,
                    Result.Not_Accepted.getResultVal());
        else
            setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA, helper.twoWheelerParking + " " + DcrConstants.SQMTRS,
                    BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + " " + DcrConstants.SQMTRS,
                    Result.Accepted.getResultVal());
    }

    private double processMechanicalParking(PlanDetail planDetail, ParkingHelper helper) {
        Integer noOfMechParkingFromPlInfo = planDetail.getPlanInformation().getNoOfMechanicalParking();
        Integer providedSlots = planDetail.getParkingDetails().getMechParking().size();
        double maxAllowedMechPark = BigDecimal.valueOf(helper.totalRequiredCarParking / 2).setScale(0, RoundingMode.UP)
                .intValue();
        if (noOfMechParkingFromPlInfo > 0)
            if (noOfMechParkingFromPlInfo > 0 && providedSlots == 0)
                setReportOutputDetails(planDetail, SUB_RULE_34_2, MECHANICAL_PARKING, 1 + NUMBERS, providedSlots + NUMBERS,
                        Result.Not_Accepted.getResultVal());
            else if (noOfMechParkingFromPlInfo > 0 && providedSlots > 0 && noOfMechParkingFromPlInfo > maxAllowedMechPark)
                setReportOutputDetails(planDetail, SUB_RULE_34_2, MAX_ALLOWED_MECH_PARK, maxAllowedMechPark + NUMBERS,
                        noOfMechParkingFromPlInfo + NUMBERS, Result.Not_Accepted.getResultVal());
            else if (noOfMechParkingFromPlInfo > 0 && providedSlots > 0)
                setReportOutputDetails(planDetail, SUB_RULE_34_2, MECHANICAL_PARKING, "", noOfMechParkingFromPlInfo + NUMBERS,
                        Result.Accepted.getResultVal());
        return 0;
    }

    /*
     * private void buildResultForYardValidation(PlanDetail planDetail, BigDecimal parkSlotAreaInFrontYard, BigDecimal
     * maxAllowedArea, String type) { planDetail.reportOutput .add(buildRuleOutputWithSubRule(DcrConstants.RULE34, SUB_RULE_34_1,
     * "Parking space should not exceed 50% of the area of mandatory " + type,
     * "Parking space should not exceed 50% of the area of mandatory " + type, "Maximum allowed area for parking in " + type +" "
     * + maxAllowedArea + DcrConstants.SQMTRS, "Parking provided in more than the allowed area " + parkSlotAreaInFrontYard +
     * DcrConstants.SQMTRS, Result.Not_Accepted, null)); } private BigDecimal validateParkingSlotsAreWithInYard(PlanDetail
     * planDetail, Polygon yardPolygon) { BigDecimal area = BigDecimal.ZERO; for (Measurement parkingSlot :
     * planDetail.getParkingDetails().getCars()) { Iterator parkSlotIterator = parkingSlot.getPolyLine().getVertexIterator();
     * while (parkSlotIterator.hasNext()) { DXFVertex dxfVertex = (DXFVertex) parkSlotIterator.next(); Point point =
     * dxfVertex.getPoint(); if (rayCasting.contains(point, yardPolygon)) { area = area.add(parkingSlot.getArea()); } } } return
     * area; }
     */

    private void checkDimensionForCarParking(PlanDetail planDetail, ParkingHelper helper) {

        /*
         * for (Block block : planDetail.getBlocks()) { for (SetBack setBack : block.getSetBacks()) { if (setBack.getFrontYard()
         * != null && setBack.getFrontYard().getPresentInDxf()) { Polygon frontYardPolygon =
         * Util.getPolygon(setBack.getFrontYard().getPolyLine()); BigDecimal parkSlotAreaInFrontYard =
         * validateParkingSlotsAreWithInYard(planDetail, frontYardPolygon); BigDecimal maxAllowedArea =
         * setBack.getFrontYard().getArea().divide(BigDecimal.valueOf(2), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
         * RoundingMode.HALF_UP); if (parkSlotAreaInFrontYard.compareTo(maxAllowedArea) > 0) {
         * buildResultForYardValidation(planDetail, parkSlotAreaInFrontYard, maxAllowedArea, "front yard space"); } } if
         * (setBack.getRearYard() != null && setBack.getRearYard().getPresentInDxf()) { Polygon rearYardPolygon =
         * Util.getPolygon(setBack.getRearYard().getPolyLine()); BigDecimal parkSlotAreaInRearYard =
         * validateParkingSlotsAreWithInYard(planDetail, rearYardPolygon); BigDecimal maxAllowedArea =
         * setBack.getRearYard().getArea().divide(BigDecimal.valueOf(2), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
         * RoundingMode.HALF_UP); if (parkSlotAreaInRearYard.compareTo(maxAllowedArea) > 0) {
         * buildResultForYardValidation(planDetail, parkSlotAreaInRearYard, maxAllowedArea, "rear yard space"); } } if
         * (setBack.getSideYard1() != null && setBack.getSideYard1().getPresentInDxf()) { Polygon sideYard1Polygon =
         * Util.getPolygon(setBack.getSideYard1().getPolyLine()); BigDecimal parkSlotAreaInSideYard1 =
         * validateParkingSlotsAreWithInYard(planDetail, sideYard1Polygon); BigDecimal maxAllowedArea =
         * setBack.getSideYard1().getArea().divide(BigDecimal.valueOf(2), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
         * RoundingMode.HALF_UP); if (parkSlotAreaInSideYard1.compareTo(maxAllowedArea) > 0) {
         * buildResultForYardValidation(planDetail, parkSlotAreaInSideYard1, maxAllowedArea, "side yard1 space"); } } if
         * (setBack.getSideYard2() != null && setBack.getSideYard2().getPresentInDxf()) { Polygon sideYard2Polygon =
         * Util.getPolygon(setBack.getSideYard2().getPolyLine()); BigDecimal parkSlotAreaInFrontYard =
         * validateParkingSlotsAreWithInYard(planDetail, sideYard2Polygon); BigDecimal maxAllowedArea =
         * setBack.getSideYard2().getArea().divide(BigDecimal.valueOf(2), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
         * RoundingMode.HALF_UP); if (parkSlotAreaInFrontYard.compareTo(maxAllowedArea) > 0) {
         * buildResultForYardValidation(planDetail, parkSlotAreaInFrontYard, maxAllowedArea, "side yard2 space"); } } } }
         */

        int parkingCount = planDetail.getParkingDetails().getCars().size();
        int failedCount = 0;
        for (Measurement slot : planDetail.getParkingDetails().getCars())
            if (slot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_HEIGHT
                    && slot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_WIDTH) {
            } else
                failedCount++;
        planDetail.getParkingDetails().setValidCarParkingSlots(parkingCount - failedCount);
        if (parkingCount > 0)
            if (failedCount > 0) {
                if (helper.totalRequiredCarParking.intValue() == planDetail.getParkingDetails().getValidCarParkingSlots())
                    setReportOutputDetails(planDetail, SUB_RULE_34_1, SUB_RULE_34_1_DESCRIPTION,
                            PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
                            "Out of " + parkingCount + PARKING + failedCount + PARKING_VIOLATED_MINIMUM_AREA,
                            Result.Accepted.getResultVal());
                else
                    setReportOutputDetails(planDetail, SUB_RULE_34_1, SUB_RULE_34_1_DESCRIPTION,
                            PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
                            "Out of " + parkingCount + PARKING + failedCount + PARKING_VIOLATED_MINIMUM_AREA,
                            Result.Not_Accepted.getResultVal());
            } else
                setReportOutputDetails(planDetail, SUB_RULE_34_1, SUB_RULE_34_1_DESCRIPTION,
                        PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING, NO_VIOLATION_OF_AREA + parkingCount + PARKING,
                        Result.Accepted.getResultVal());

    }

    private void checkDimensionForDAParking(PlanDetail planDetail, ParkingHelper helper) {

        int daFailedCount = 0;
        int daParkingCount = planDetail.getParkingDetails().getDisabledPersons().size();
        for (Measurement daParkingSlot : planDetail.getParkingDetails().getDisabledPersons())
            if (daParkingSlot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= DA_PARKING_SLOT_WIDTH
                    && daParkingSlot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= DA_PARKING_SLOT_HEIGHT) {
            } else
                daFailedCount++;
        planDetail.getParkingDetails().setValidDAParkingSlots(daParkingCount - daFailedCount);
        if (daParkingCount > 0)
            if (daFailedCount > 0) {
                if (helper.daParking.intValue() == planDetail.getParkingDetails().getValidDAParkingSlots())
                    setReportOutputDetails(planDetail, SUB_RULE_40A__5, DA_PARKING_SLOT_AREA,
                            DA_PARKING_MIN_AREA + MINIMUM_AREA_OF_EACH_DA_PARKING,
                            NO_VIOLATION_OF_AREA + planDetail.getParkingDetails().getValidDAParkingSlots() + PARKING,
                            Result.Accepted.getResultVal());
                else
                    setReportOutputDetails(planDetail, SUB_RULE_40A__5, DA_PARKING_SLOT_AREA,
                            DA_PARKING_MIN_AREA + MINIMUM_AREA_OF_EACH_DA_PARKING,
                            "Out of " + daParkingCount + PARKING + daFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
                            Result.Not_Accepted.getResultVal());
            } else
                setReportOutputDetails(planDetail, SUB_RULE_40A__5, DA_PARKING_SLOT_AREA,
                        DA_PARKING_MIN_AREA + MINIMUM_AREA_OF_EACH_DA_PARKING, NO_VIOLATION_OF_AREA + daParkingCount + PARKING,
                        Result.Accepted.getResultVal());
    }

    private void specialCaseCheckForOccupancyType(DXFLWPolyline pLine, Occupancy occupancy) {
        if (pLine.getColor() == DxfFileConstants.OCCUPANCY_A2_PARKING_WITHATTACHBATH_COLOR_CODE) {
            occupancy.setWithAttachedBath(true);
            occupancy.setType(OccupancyType.OCCUPANCY_A2);
        } else if (pLine.getColor() == DxfFileConstants.OCCUPANCY_A2_PARKING_WOATTACHBATH_COLOR_CODE) {
            occupancy.setWithOutAttachedBath(true);
            occupancy.setType(OccupancyType.OCCUPANCY_A2);
        } else if (pLine.getColor() == DxfFileConstants.OCCUPANCY_A2_PARKING_WITHDINE_COLOR_CODE) {
            occupancy.setWithDinningSpace(true);
            occupancy.setType(OccupancyType.OCCUPANCY_A2);
        }
    }

    private BigDecimal getTotalCarpetAreaByOccupancy(PlanDetail pl, OccupancyType type) {
        BigDecimal totalArea = BigDecimal.ZERO;
        for (Block b : pl.getBlocks())
            for (Occupancy occupancy : b.getBuilding().getTotalArea())
                if (occupancy.getType().equals(type))
                    totalArea = totalArea.add(occupancy.getCarpetArea());
        return totalArea;
    }
}