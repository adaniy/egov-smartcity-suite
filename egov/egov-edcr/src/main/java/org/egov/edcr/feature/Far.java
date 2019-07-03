package org.egov.edcr.feature;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.egov.edcr.constants.DxfFileConstants.BLOCK_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.BUILT_UP_AREA;
import static org.egov.edcr.constants.DxfFileConstants.EXISTING_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.FLOOR_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.LAYER_EXISTING_BLT_UP_AREA_DEDUCT;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.PLOT_AREA;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.FireStair;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.GeneralStair;
import org.egov.edcr.entity.Hall;
import org.egov.edcr.entity.Lift;
import org.egov.edcr.entity.MezzanineFloor;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.TypicalFloor;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.PrintUtil;
import org.egov.edcr.utility.Util;
import org.jfree.util.Log;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLine;
import org.springframework.stereotype.Service;

@Service
public class Far extends GeneralRule implements RuleService {

    private static final Logger LOG = Logger.getLogger(Far.class);

    private static final String RULE_NAME_KEY = "far.rulename";
    private static final String RULE_DESCRIPTION_KEY = "far.description";
    private static final String RULE_EXPECTED_KEY = "far.expected";
    private static final String RULE_ACTUAL_KEY = "far.actual";
    private static final String RULE_DESCRIPTION_KEY_WEIGHTED = "weighted.far.description";
    private static final String VALIDATION_WRONG_COLORCODE_FLOORAREA = "msg.error.wrong.colourcode.floorarea";
    private static final String OCCUPANCY = "Occupancy";

    private static final BigDecimal onePointFive = BigDecimal.valueOf(1.5);
    private static final BigDecimal two = BigDecimal.valueOf(2.0);
    private static final BigDecimal twoPointFive = BigDecimal.valueOf(2.5);
    private static final BigDecimal three = BigDecimal.valueOf(3.0);
    private static final BigDecimal threePointFive = BigDecimal.valueOf(3.5);
    private static final BigDecimal four = BigDecimal.valueOf(4.0);
    private static final String VALIDATION_NEGATIVE_FLOOR_AREA = "msg.error.negative.floorarea.occupancy.floor";
    private static final String VALIDATION_NEGATIVE_EXISTING_FLOOR_AREA = "msg.error.negative.existing.floorarea.occupancy.floor";
    private static final String VALIDATION_NEGATIVE_BUILTUP_AREA = "msg.error.negative.builtuparea.occupancy.floor";
    private static final String VALIDATION_NEGATIVE_EXISTING_BUILTUP_AREA = "msg.error.negative.existing.builtuparea.occupancy.floor";
    public static final String RULE_31_1 = "31(1)";

    String farDeductByFloor = BLOCK_NAME_PREFIX + "%s" + "_" + FLOOR_NAME_PREFIX + "%s" + "_"
            + DxfFileConstants.BUILT_UP_AREA_DEDUCT;

    /**
     * @param doc
     * @param pl
     * @return 1) Floor area = (sum of areas of all polygon in Building_exterior_wall layer) - (sum of all polygons in FAR_deduct
     * layer) Color is not available here when color availble change to getPolyLinesByLayerAndColor Api if required
     */

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        LOG.info(" Extract BUILT_UP_AREA");
        for (Block block : pl.getBlocks()) {
            /*
             * String singleFamily = "B_" + block.getNumber() + "_" + DxfFileConstants.SINGLE_FAMILY_BLDG; if
             * (pl.getPlanInformation().getSingleFamilyBuilding() != null) { Boolean value =
             * pl.getPlanInformation().getSingleFamilyBuilding(); if (value) block.setSingleFamilyBuilding(true); else
             * block.setSingleFamilyBuilding(false); }
             */

            LOG.error(" Working on Block  " + block.getNumber());
            List<String> typicals = new ArrayList<>();
            List<DXFLWPolyline> polyLinesByLayer;
            String layerRegEx = BLOCK_NAME_PREFIX + block.getNumber() + "_" + FLOOR_NAME_PREFIX + "-?\\d+_"
                    + BUILT_UP_AREA;
            List<String> layerNames = Util.getLayerNamesLike(doc, layerRegEx);
            int floorNo;
            Floor floor;
            for (String s : layerNames) {
                String typical = "";
                LOG.error("Working on Block  " + block.getNumber() + " For layer Name " + s);
                polyLinesByLayer = Util.getPolyLinesByLayer(doc, s);
                if (polyLinesByLayer.isEmpty())
                    continue;
                String typicalStr = Util.getMtextByLayerName2(doc, s, "TYPICAL_FLOOR_PLAN");

                if (typicalStr != null) {
                    LOG.error("Typical found in  " + block.getNumber() + " in layer" + s + "with Details " + typicalStr);
                    if (typical.isEmpty()) {
                        typical = typicalStr;
                        typicals.add(typical);
                    } else {
                        LOG.info("multiple typical floors defined in block " + block.getNumber() + " in layer" + s);
                        pl.addError("multiple typical floors defined",
                                "multiple typical floors defined. Considering First one");
                    }
                }

                floorNo = Integer.valueOf(s.split("_")[3]);
                if (block.getBuilding().getFloorNumber(floorNo) == null) {
                    floor = new Floor();
                    floor.setNumber(floorNo);
                    extractFloorHeight(doc, block, floor);
                } else
                    floor = block.getBuilding().getFloorNumber(floorNo);
                // find builtup area
                for (DXFLWPolyline pline : polyLinesByLayer) {

                    BigDecimal occupancyArea = Util.getPolyLineArea(pline);
                    LOG.error(" occupancyArea *************** " + occupancyArea);
                    Occupancy occupancy = new Occupancy();
                    occupancy.setPolyLine(pline);
                    occupancy.setBuiltUpArea(occupancyArea == null ? BigDecimal.ZERO : occupancyArea);
                    occupancy.setExistingBuiltUpArea(BigDecimal.ZERO);
                    occupancy.setType(Util.findOccupancyType(pline));
                    LOG.error(" occupancy type " + occupancy.getType());
                    if (occupancy.getType() == null)
                        pl.addError(VALIDATION_WRONG_COLORCODE_FLOORAREA,
                                getLocaleMessage(VALIDATION_WRONG_COLORCODE_FLOORAREA, String.valueOf(pline.getColor()), s));
                    else
                        floor.addBuiltUpArea(occupancy);
                }
                if (block.getBuilding().getFloorNumber(floorNo) == null)
                    block.getBuilding().getFloors().add(floor);
                // find deductions
                String deductLayerName = String.format(farDeductByFloor, block.getNumber(), floor.getNumber());

                LOG.error("Working on Block deduction  " + deductLayerName);

                List<DXFLWPolyline> bldDeduct = Util.getPolyLinesByLayer(doc, deductLayerName);
                for (DXFLWPolyline pline : bldDeduct) {
                    BigDecimal deductionArea = Util.getPolyLineArea(pline);
                    LOG.error(" deductionArea *************** " + deductionArea);

                    Occupancy occupancy = new Occupancy();
                    occupancy.setDeduction(deductionArea == null ? BigDecimal.ZERO : deductionArea);
                    occupancy.setExistingDeduction(BigDecimal.ZERO);
                    occupancy.setType(Util.findOccupancyType(pline));
                    LOG.error(" occupancy type deduction " + occupancy.getType());

                    if (occupancy.getType() == null)
                        pl.addError(VALIDATION_WRONG_COLORCODE_FLOORAREA,
                                getLocaleMessage(VALIDATION_WRONG_COLORCODE_FLOORAREA, String.valueOf(pline.getColor()),
                                        deductLayerName));
                    else
                        floor.addDeductionArea(occupancy);
                }
            }
            if (!typicals.isEmpty()) {
                LOG.info("Adding typical:" + block.getNumber());
                List<TypicalFloor> typicalFloors = new ArrayList<>();
                for (String typical : typicals) {
                    TypicalFloor tpf = new TypicalFloor(typical);
                    typicalFloors.add(tpf);
                }
                block.setTypicalFloor(typicalFloors);
            }
        }

        // set Floor wise poly line for terrace check.
        for (Block block : pl.getBlocks())
            if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
                // High Rise Building
                boolean highRise = block.getBuilding().getBuildingHeight().setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                        DcrConstants.ROUNDMODE_MEASUREMENTS)
                        .compareTo(BigDecimal.valueOf(16).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                                DcrConstants.ROUNDMODE_MEASUREMENTS)) > 0;

                for (Floor floor : block.getBuilding().getFloors()) {
                    // set polylines in BLK_n_FLR_i_BLT_UP_AREA (built up area)
                    List<DXFLWPolyline> floorBuiltUpPolyLines = Util.getPolyLinesByLayer(doc,
                            String.format(DxfFileConstants.LAYER_FLOOR_BLT_UP, block.getNumber(), floor.getNumber()));
                    floor.setBuiltUpAreaPolyLine(floorBuiltUpPolyLines);

                    addFireStairs(doc, block, floor);
                    addGeneralStairs(doc, block, floor, highRise);
                    setLifts(doc, block, floor);
                    addMezzanineFloor(doc, block, floor);
                }
            }

        for (Block b : pl.getBlocks()) {
            b.getBuilding().sortFloorByName();
            if (!b.getTypicalFloor().isEmpty())
                for (TypicalFloor typical : b.getTypicalFloor()) {
                    Floor modelFloor = b.getBuilding().getFloorNumber(typical.getModelFloorNo());
                    for (Integer no : typical.getRepetitiveFloorNos())
                        try {
                            Floor newFloor = (Floor) modelFloor.clone();
                            newFloor.setNumber(no);
                            b.getBuilding().getFloors().add(newFloor);
                        } catch (Exception e) {

                        }
                }
        }

        // get Existing Builtup area
        for (Block block : pl.getBlocks()) {
            String layerRegExForExistingPlan = BLOCK_NAME_PREFIX + block.getNumber() + "_" + FLOOR_NAME_PREFIX + "-?\\d+_"
                    + BUILT_UP_AREA + EXISTING_PREFIX;
            List<String> layerNamesList = Util.getLayerNamesLike(doc, layerRegExForExistingPlan);
            Floor floor;
            for (String layer : layerNamesList) {
                List<DXFLWPolyline> polylines = Util.getPolyLinesByLayer(doc, layer);
                int floorNo = Integer.valueOf(layer.split("_")[3]);
                if (block.getBuilding().getFloorNumber(floorNo) == null) {
                    floor = new Floor();
                    floor.setNumber(floorNo);
                    extractFloorHeight(doc, block, floor);
                } else
                    floor = block.getBuilding().getFloorNumber(floorNo);
                for (DXFLWPolyline pline : polylines) {
                    BigDecimal occupancyArea = Util.getPolyLineArea(pline);
                    Occupancy occupancy = new Occupancy();
                    occupancy.setPolyLine(pline);
                    occupancy.setBuiltUpArea(occupancyArea == null ? BigDecimal.ZERO : occupancyArea);
                    occupancy.setExistingBuiltUpArea(occupancyArea == null ? BigDecimal.ZERO : occupancyArea);
                    occupancy.setType(Util.findOccupancyType(pline));
                    if (occupancy.getType() == null)
                        pl.addError(VALIDATION_WRONG_COLORCODE_FLOORAREA,
                                getLocaleMessage(VALIDATION_WRONG_COLORCODE_FLOORAREA, String.valueOf(pline.getColor()), layer));
                    else
                        floor.addBuiltUpArea(occupancy);

                }
                if (block.getBuilding().getFloorNumber(floorNo) == null)
                    block.getBuilding().getFloors().add(floor);
                // existing deduction
                String deductLayerName = String.format(LAYER_EXISTING_BLT_UP_AREA_DEDUCT, block.getNumber(), floor.getNumber());
                List<DXFLWPolyline> bldDeduct = Util.getPolyLinesByLayer(doc, deductLayerName);
                for (DXFLWPolyline pline : bldDeduct) {
                    BigDecimal deductionArea = Util.getPolyLineArea(pline);
                    Occupancy occupancy = new Occupancy();
                    occupancy.setDeduction(deductionArea == null ? BigDecimal.ZERO : deductionArea);
                    occupancy.setExistingDeduction(deductionArea == null ? BigDecimal.ZERO : deductionArea);
                    occupancy.setType(Util.findOccupancyType(pline));
                    if (occupancy.getType() == null)
                        pl.addError(VALIDATION_WRONG_COLORCODE_FLOORAREA,
                                getLocaleMessage(VALIDATION_WRONG_COLORCODE_FLOORAREA, String.valueOf(pline.getColor()),
                                        deductLayerName));
                    else
                        floor.addDeductionArea(occupancy);
                }
            }
        }
        for (Block block : pl.getBlocks()) {
            Building building = block.getBuilding();
            if (building != null)
                if (building.getFloors().size() > 0) {
                    Floor floor = building.getFloors().stream().max(Comparator.comparing(Floor::getNumber)).get();
                    if (floor != null) {
                        floor.setTerrace(checkTerrace(floor));
                        floor.setUpperMost(Boolean.TRUE);
                    }
                }
        }
        return pl;
    }

    private void extractFloorHeight(DXFDocument doc, Block block, Floor floor) {
        String floorHeightLayerName = DxfFileConstants.BLOCK_NAME_PREFIX +
                block.getNumber() + "_" + DxfFileConstants.FLOOR_NAME_PREFIX +
                floor.getNumber() + "_" + DxfFileConstants.FLOOR_HEIGHT_PREFIX;
        List<BigDecimal> flrHeights = Util.getListOfDimensionValueByLayer(doc, floorHeightLayerName);
        floor.setFloorHeights(flrHeights);
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        if (pl.getPlot().getArea() == null || pl.getPlot().getArea().doubleValue() == 0)
            pl.addError(PLOT_AREA, getLocaleMessage(OBJECTNOTDEFINED, PLOT_AREA));

        return pl;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {

        int errors = pl.getErrors().size();
        validate(pl);
        int validatedErrors = pl.getErrors().size();
        if (validatedErrors > errors)
            return pl;
        BigDecimal totalExistingBuiltUpArea = BigDecimal.ZERO;
        BigDecimal totalExistingFloorArea = BigDecimal.ZERO;
        BigDecimal totalBuiltUpArea = BigDecimal.ZERO;
        BigDecimal totalFloorArea = BigDecimal.ZERO;
        EnumSet<OccupancyType> distinctOccupancyTypes = EnumSet.noneOf(OccupancyType.class);
        for (Block blk : pl.getBlocks()) {
            BigDecimal flrArea = BigDecimal.ZERO;
            BigDecimal bltUpArea = BigDecimal.ZERO;
            BigDecimal existingFlrArea = BigDecimal.ZERO;
            BigDecimal existingBltUpArea = BigDecimal.ZERO;
            Building building = blk.getBuilding();
            for (Floor flr : building.getFloors())
                for (Occupancy occupancy : flr.getOccupancies()) {
                    validate2(pl, blk, flr, occupancy);

                    occupancy.setCarpetArea(occupancy.getFloorArea().multiply(BigDecimal.valueOf(0.80)));

                    occupancy.setExistingCarpetArea(occupancy.getExistingFloorArea().multiply(BigDecimal.valueOf(0.80)));

                    bltUpArea = bltUpArea
                            .add(occupancy.getBuiltUpArea() == null ? BigDecimal.valueOf(0) : occupancy.getBuiltUpArea());

                    flrArea = flrArea.add(occupancy.getFloorArea());

                    existingFlrArea = existingFlrArea.add(occupancy.getExistingFloorArea());

                    existingBltUpArea = existingBltUpArea.add(occupancy.getExistingBuiltUpArea() == null ? BigDecimal.valueOf(0)
                            : occupancy.getExistingBuiltUpArea());
                }
            building.setTotalFloorArea(flrArea);

            building.setTotalBuitUpArea(bltUpArea);

            building.setTotalExistingBuiltUpArea(existingBltUpArea);

            building.setTotalExistingFloorArea(existingFlrArea);

            // check block is completely existing building or not.
            if (existingBltUpArea.compareTo(bltUpArea) == 0)
                blk.setCompletelyExisting(Boolean.TRUE);

            totalFloorArea = totalFloorArea.add(flrArea);
            totalBuiltUpArea = totalBuiltUpArea.add(bltUpArea);
            totalExistingBuiltUpArea = totalExistingBuiltUpArea.add(existingBltUpArea);
            totalExistingFloorArea = totalExistingFloorArea.add(existingFlrArea);

            /*
             * BigDecimal far = flrArea.divide(pl.getPlot().getArea(), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
             * DcrConstants.ROUNDMODE_MEASUREMENTS); blk.getBuilding().setFar(far);
             */
            // occupancy conversion logic

            // Find Occupancies by block and add
            Set<OccupancyType> occupancyByBlock = new HashSet<>();
            for (Floor flr : building.getFloors()) {
                for (Occupancy occupancy : flr.getOccupancies())
                    occupancyByBlock.add(occupancy.getType());

                for (MezzanineFloor mezz : flr.getMezzanineFloor())
                    if (mezz.getOccupancyType() != null && mezz.getBuiltUpArea() != null)
                        occupancyByBlock.add(mezz.getOccupancyType());
            }

            List<Map<String, Object>> listOfMapOfAllDtls = new ArrayList<>();
            List<OccupancyType> listOfOccupancyTypes = new ArrayList<>();

            for (OccupancyType occupancyType : occupancyByBlock) {

                Map<String, Object> allDtlsMap = new HashMap<>();
                BigDecimal blockWiseFloorArea = BigDecimal.ZERO;
                BigDecimal blockWiseBuiltupArea = BigDecimal.ZERO;
                BigDecimal blockWiseExistingFloorArea = BigDecimal.ZERO;
                BigDecimal blockWiseExistingBuiltupArea = BigDecimal.ZERO;
                BigDecimal blockWiseMezzanineFloorArea = BigDecimal.ZERO;
                BigDecimal blockWiseMezzanineBuiltupArea = BigDecimal.ZERO;
                for (Floor flr : blk.getBuilding().getFloors()) {
                    for (Occupancy occupancy : flr.getOccupancies())
                        if (occupancy.getType().equals(occupancyType)) {
                            blockWiseFloorArea = blockWiseFloorArea
                                    .add(occupancy.getFloorArea());

                            blockWiseBuiltupArea = blockWiseBuiltupArea
                                    .add(occupancy.getBuiltUpArea() == null ? BigDecimal.valueOf(0) : occupancy.getBuiltUpArea());
                            blockWiseExistingFloorArea = blockWiseExistingFloorArea

                                    .add(occupancy.getExistingFloorArea());

                            blockWiseExistingBuiltupArea = blockWiseExistingBuiltupArea
                                    .add(occupancy.getExistingBuiltUpArea() == null ? BigDecimal.valueOf(0)
                                            : occupancy.getExistingBuiltUpArea());
                        }
                    for (MezzanineFloor mezz : flr.getMezzanineFloor())
                        if (mezz.getOccupancyType() != null && mezz.getBuiltUpArea() != null
                                && mezz.getOccupancyType().equals(occupancyType)) {
                            blockWiseMezzanineFloorArea = blockWiseMezzanineFloorArea.add(mezz.getFloorArea());
                            blockWiseMezzanineBuiltupArea = blockWiseMezzanineBuiltupArea.add(mezz.getBuiltUpArea());
                        }
                }
                if (blockWiseBuiltupArea.doubleValue() > 0) {
                    Occupancy occupancy = new Occupancy();
                    occupancy.setBuiltUpArea(blockWiseBuiltupArea);
                    occupancy.setFloorArea(blockWiseFloorArea);
                    occupancy.setExistingFloorArea(blockWiseExistingFloorArea);
                    occupancy.setExistingBuiltUpArea(blockWiseExistingBuiltupArea);
                    occupancy.setCarpetArea(blockWiseFloorArea.multiply(BigDecimal.valueOf(.80)));
                    occupancy.setType(occupancyType);
                    building.getTotalArea().add(occupancy);
                }

                if (blockWiseMezzanineBuiltupArea.doubleValue() > 0) {
                    Occupancy mezzanineOccupancy = new Occupancy();
                    mezzanineOccupancy.setBuiltUpArea(blockWiseMezzanineBuiltupArea);
                    mezzanineOccupancy.setFloorArea(blockWiseMezzanineFloorArea);
                    mezzanineOccupancy.setCarpetArea(blockWiseMezzanineFloorArea.multiply(BigDecimal.valueOf(.80)));
                    mezzanineOccupancy.setType(occupancyType);
                    building.getMezzanineOccupancies().add(mezzanineOccupancy);
                    building.getTotalArea().add(mezzanineOccupancy);
                    blockWiseBuiltupArea = blockWiseBuiltupArea.add(blockWiseMezzanineBuiltupArea);
                    blockWiseFloorArea = blockWiseFloorArea.add(blockWiseMezzanineFloorArea);

                }
                OccupancyType occupancyTypeAsPerFloorArea = Util.getOccupancyAsPerFloorArea(occupancyType,
                        blockWiseFloorArea);

                allDtlsMap.put("occupancy", occupancyTypeAsPerFloorArea);
                allDtlsMap.put("totalFloorArea", blockWiseFloorArea);
                allDtlsMap.put("totalBuiltUpArea", blockWiseBuiltupArea);
                allDtlsMap.put("existingFloorArea", blockWiseExistingFloorArea);
                allDtlsMap.put("existingBuiltUpArea", blockWiseExistingBuiltupArea);

                listOfOccupancyTypes.add(occupancyTypeAsPerFloorArea);

                listOfMapOfAllDtls.add(allDtlsMap);
            }
            Set<OccupancyType> setOfOccupancyTypes = new HashSet<>(listOfOccupancyTypes);

            List<Occupancy> listOfOccupanciesOfAParticularblock = new ArrayList<>();
            // for each distinct converted occupancy types
            for (OccupancyType occupancyType : setOfOccupancyTypes)
                if (occupancyType != null) {
                    Occupancy occupancy = new Occupancy();
                    BigDecimal totalFlrArea = BigDecimal.ZERO;
                    BigDecimal totalBltUpArea = BigDecimal.ZERO;
                    BigDecimal totalExistingFlrArea = BigDecimal.ZERO;
                    BigDecimal totalExistingBltUpArea = BigDecimal.ZERO;

                    for (Map<String, Object> dtlsMap : listOfMapOfAllDtls)
                        if (occupancyType.equals(dtlsMap.get("occupancy"))) {
                            totalFlrArea = totalFlrArea.add((BigDecimal) dtlsMap.get("totalFloorArea"));
                            totalBltUpArea = totalBltUpArea.add((BigDecimal) dtlsMap.get("totalBuiltUpArea"));

                            totalExistingBltUpArea = totalExistingBltUpArea.add((BigDecimal) dtlsMap.get("existingBuiltUpArea"));
                            totalExistingFlrArea = totalExistingFlrArea.add((BigDecimal) dtlsMap.get("existingFloorArea"));

                        }
                    occupancy.setType(occupancyType);
                    occupancy.setBuiltUpArea(totalBltUpArea);
                    occupancy.setFloorArea(totalFlrArea);
                    occupancy.setExistingBuiltUpArea(totalExistingBltUpArea);
                    occupancy.setExistingFloorArea(totalExistingFlrArea);
                    occupancy.setExistingCarpetArea(totalExistingFlrArea.multiply(BigDecimal.valueOf(0.80)));
                    occupancy.setCarpetArea(totalFlrArea.multiply(BigDecimal.valueOf(0.80)));

                    listOfOccupanciesOfAParticularblock.add(occupancy);
                }
            blk.getBuilding().setOccupancies(listOfOccupanciesOfAParticularblock);

            if (!listOfOccupanciesOfAParticularblock.isEmpty()) {
                // listOfOccupanciesOfAParticularblock already converted. In case of professional building type, converted into A1
                // type
                boolean singleFamilyBuildingTypeOccupancyPresent = false;
                boolean otherThanSingleFamilyOccupancyTypePresent = false;

                for (Occupancy occupancy : listOfOccupanciesOfAParticularblock)
                    if (occupancy.getType().equals(OccupancyType.OCCUPANCY_A1))
                        singleFamilyBuildingTypeOccupancyPresent = true;
                    else {
                        otherThanSingleFamilyOccupancyTypePresent = true;
                        break;
                    }
                if (!otherThanSingleFamilyOccupancyTypePresent && singleFamilyBuildingTypeOccupancyPresent)
                    blk.setSingleFamilyBuilding(true);
                else
                    blk.setSingleFamilyBuilding(false);

                int allResidentialOccTypes = 0;
                int allResidentialOrCommercialOccTypes = 0;

                for (Occupancy occupancy : listOfOccupanciesOfAParticularblock)
                    if (occupancy.getType() != null) {
                        // setting residentialBuilding
                        int residentialOccupancyType = 0;
                        if (occupancy.getType().equals(OccupancyType.OCCUPANCY_A1)
                                || occupancy.getType().equals(OccupancyType.OCCUPANCY_A4))
                            residentialOccupancyType = 1;
                        if (residentialOccupancyType == 0) {
                            allResidentialOccTypes = 0;
                            break;
                        } else
                            allResidentialOccTypes = 1;
                    }
                if (allResidentialOccTypes == 1)
                    blk.setResidentialBuilding(true);
                else
                    blk.setResidentialBuilding(false);

                for (Occupancy occupancy : listOfOccupanciesOfAParticularblock)
                    if (occupancy.getType() != null) {
                        // setting residentialOrCommercial Occupancy Type
                        int residentialOrCommercialOccupancyType = 0;
                        if (occupancy.getType().equals(OccupancyType.OCCUPANCY_A1)
                                || occupancy.getType().equals(OccupancyType.OCCUPANCY_A4) ||
                                occupancy.getType().equals(OccupancyType.OCCUPANCY_F)
                                || occupancy.getType().equals(OccupancyType.OCCUPANCY_F1) ||
                                occupancy.getType().equals(OccupancyType.OCCUPANCY_F2)
                                || occupancy.getType().equals(OccupancyType.OCCUPANCY_F3)
                                || occupancy.getType().equals(OccupancyType.OCCUPANCY_F4))
                            residentialOrCommercialOccupancyType = 1;
                        if (residentialOrCommercialOccupancyType == 0) {
                            allResidentialOrCommercialOccTypes = 0;
                            break;
                        } else
                            allResidentialOrCommercialOccTypes = 1;
                    }
                if (allResidentialOrCommercialOccTypes == 1)
                    blk.setResidentialOrCommercialBuilding(true);
                else
                    blk.setResidentialOrCommercialBuilding(false);
            }

            if (blk.getBuilding().getFloors() != null && !blk.getBuilding().getFloors().isEmpty()) {
                BigDecimal noOfFloorsAboveGround = BigDecimal.ZERO;
                for (Floor floor : blk.getBuilding().getFloors())
                    if (floor.getNumber() != null && floor.getNumber() >= 0) {
                        BigDecimal occupancyTotalBuiltUpArea = floor.getOccupancies().stream()
                                .map(Occupancy::getBuiltUpArea)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal occupancyTotalExistingBuiltUpArea = floor.getOccupancies().stream()
                                .map(Occupancy::getExistingBuiltUpArea)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal proposedBuiltUpArea = occupancyTotalExistingBuiltUpArea.compareTo(BigDecimal.ZERO) > 0
                                ? occupancyTotalBuiltUpArea.subtract(occupancyTotalExistingBuiltUpArea)
                                : occupancyTotalBuiltUpArea;

                        if (proposedBuiltUpArea.compareTo(BigDecimal.ZERO) > 0)
                            noOfFloorsAboveGround = noOfFloorsAboveGround.add(BigDecimal.valueOf(1));
                    }

                boolean hasTerrace = blk.getBuilding().getFloors().stream()
                        .anyMatch(floor -> floor.getTerrace().equals(Boolean.TRUE));

                noOfFloorsAboveGround = hasTerrace ? noOfFloorsAboveGround.subtract(BigDecimal.ONE) : noOfFloorsAboveGround;

                blk.getBuilding().setMaxFloor(noOfFloorsAboveGround);
                blk.getBuilding().setFloorsAboveGround(noOfFloorsAboveGround);
                blk.getBuilding().setTotalFloors(BigDecimal.valueOf(blk.getBuilding().getFloors().size()));
            }

        }

        List<OccupancyType> plotDeclaredAndConvertedOccupancies = new ArrayList<>();
        Set<OccupancyType> plotDeclaredOccupancies = new HashSet<>();
        Set<OccupancyType> floorDeclaredOccupancies = new HashSet<>();
        Set<OccupancyType> mezzaninefloorOccupancies = new HashSet<>();
        for (Block block : pl.getBlocks()) {
            for (Occupancy occupancy : block.getBuilding().getOccupancies())
                if (occupancy.getType() != null) {
                    plotDeclaredAndConvertedOccupancies.add(occupancy.getType());
                    floorDeclaredOccupancies.add(occupancy.getType());
                }
            for (Occupancy occupancy : block.getBuilding().getTotalArea())
                if (occupancy.getType() != null)
                    plotDeclaredOccupancies.add(occupancy.getType());
            for (Occupancy occupancy : block.getBuilding().getMezzanineOccupancies())
                if (occupancy.getType() != null)
                    mezzaninefloorOccupancies.add(occupancy.getType());
        }

        // Sum of areas by declared occupancy type wise
        List<Occupancy> plotDeclaredOccupancyWiseArea = new ArrayList<>();
        List<Occupancy> plotDeclaredMezzaineOccupancyWiseArea = new ArrayList<>();
        for (OccupancyType occupancyType : plotDeclaredOccupancies)
            if (occupancyType != null) {
                BigDecimal totalFloorAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalBuiltUpAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalCarpetAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalExistBuiltUpAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalExistFloorAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalExistCarpetAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal mezzanineTotalFloorAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal mezzanineTotalBuiltUpAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal mezzanineTotalCarpetAreaForAllBlks = BigDecimal.ZERO;
                Occupancy occupancy = new Occupancy();
                for (Block block : pl.getBlocks()) {
                    for (Occupancy bldgOccupancy : block.getBuilding().getTotalArea())
                        if (occupancyType.equals(bldgOccupancy.getType())) {
                            totalFloorAreaForAllBlks = totalFloorAreaForAllBlks.add(bldgOccupancy.getFloorArea());
                            totalBuiltUpAreaForAllBlks = totalBuiltUpAreaForAllBlks.add(bldgOccupancy.getBuiltUpArea());
                            totalCarpetAreaForAllBlks = totalCarpetAreaForAllBlks.add(bldgOccupancy.getCarpetArea());
                            totalExistBuiltUpAreaForAllBlks = totalExistBuiltUpAreaForAllBlks
                                    .add(bldgOccupancy.getExistingBuiltUpArea());
                            totalExistFloorAreaForAllBlks = totalExistFloorAreaForAllBlks
                                    .add(bldgOccupancy.getExistingFloorArea());
                            totalExistCarpetAreaForAllBlks = totalExistCarpetAreaForAllBlks
                                    .add(bldgOccupancy.getExistingCarpetArea());
                        }
                    for (Occupancy mezz : block.getBuilding().getMezzanineOccupancies())
                        if (occupancyType.equals(mezz.getType())) {
                            mezzanineTotalFloorAreaForAllBlks = mezzanineTotalFloorAreaForAllBlks.add(mezz.getFloorArea());
                            mezzanineTotalBuiltUpAreaForAllBlks = mezzanineTotalBuiltUpAreaForAllBlks.add(mezz.getBuiltUpArea());
                            mezzanineTotalCarpetAreaForAllBlks = mezzanineTotalCarpetAreaForAllBlks.add(mezz.getCarpetArea());
                        }
                }
                occupancy.setType(occupancyType);
                occupancy.setBuiltUpArea(totalBuiltUpAreaForAllBlks);
                occupancy.setCarpetArea(totalCarpetAreaForAllBlks);
                occupancy.setFloorArea(totalFloorAreaForAllBlks);
                occupancy.setExistingBuiltUpArea(totalExistBuiltUpAreaForAllBlks);
                occupancy.setExistingFloorArea(totalExistFloorAreaForAllBlks);
                occupancy.setExistingCarpetArea(totalExistCarpetAreaForAllBlks);
                plotDeclaredOccupancyWiseArea.add(occupancy);
                if (mezzanineTotalBuiltUpAreaForAllBlks.doubleValue() > 0) {
                    Occupancy mezzOcc = new Occupancy();
                    mezzOcc.setType(occupancyType);
                    mezzOcc.setBuiltUpArea(totalBuiltUpAreaForAllBlks);
                    mezzOcc.setCarpetArea(totalCarpetAreaForAllBlks);
                    mezzOcc.setFloorArea(totalFloorAreaForAllBlks);
                    mezzOcc.setExistingBuiltUpArea(totalExistBuiltUpAreaForAllBlks);
                    mezzOcc.setExistingFloorArea(totalExistFloorAreaForAllBlks);
                    mezzOcc.setExistingCarpetArea(totalExistCarpetAreaForAllBlks);
                    plotDeclaredMezzaineOccupancyWiseArea.add(mezzOcc);
                }
            }
        pl.setDeclaredOccupancies(plotDeclaredOccupancyWiseArea);
        pl.setMezzanineOccupancies(plotDeclaredMezzaineOccupancyWiseArea);
        Set<OccupancyType> setOfDistinctOccupancies = new HashSet<>(plotDeclaredAndConvertedOccupancies);

        distinctOccupancyTypes.addAll(setOfDistinctOccupancies);
        // Sum area by declared and converted occupancy type wise
        List<Occupancy> plotDeclaredAndConvertedOccupancyWiseArea = new ArrayList<>();
        for (OccupancyType occupancyType : setOfDistinctOccupancies)
            if (occupancyType != null) {
                BigDecimal totalFloorAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalBuiltUpAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalCarpetAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalExistBuiltUpAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalExistFloorAreaForAllBlks = BigDecimal.ZERO;
                BigDecimal totalExistCarpetAreaForAllBlks = BigDecimal.ZERO;
                Occupancy occupancy = new Occupancy();
                for (Block block : pl.getBlocks())
                    for (Occupancy buildingOccupancy : block.getBuilding().getOccupancies())
                        if (occupancyType.equals(buildingOccupancy.getType())) {
                            totalFloorAreaForAllBlks = totalFloorAreaForAllBlks.add(buildingOccupancy.getFloorArea());
                            totalBuiltUpAreaForAllBlks = totalBuiltUpAreaForAllBlks.add(buildingOccupancy.getBuiltUpArea());
                            totalCarpetAreaForAllBlks = totalCarpetAreaForAllBlks.add(buildingOccupancy.getCarpetArea());
                            totalExistBuiltUpAreaForAllBlks = totalExistBuiltUpAreaForAllBlks
                                    .add(buildingOccupancy.getExistingBuiltUpArea());
                            totalExistFloorAreaForAllBlks = totalExistFloorAreaForAllBlks
                                    .add(buildingOccupancy.getExistingFloorArea());
                            totalExistCarpetAreaForAllBlks = totalExistCarpetAreaForAllBlks
                                    .add(buildingOccupancy.getExistingCarpetArea());
                        }
                occupancy.setType(occupancyType);
                occupancy.setBuiltUpArea(totalBuiltUpAreaForAllBlks);
                occupancy.setCarpetArea(totalCarpetAreaForAllBlks);
                occupancy.setFloorArea(totalFloorAreaForAllBlks);
                occupancy.setExistingBuiltUpArea(totalExistBuiltUpAreaForAllBlks);
                occupancy.setExistingFloorArea(totalExistFloorAreaForAllBlks);
                occupancy.setExistingCarpetArea(totalExistCarpetAreaForAllBlks);
                plotDeclaredAndConvertedOccupancyWiseArea.add(occupancy);
            }
        pl.setOccupancies(plotDeclaredAndConvertedOccupancyWiseArea);
        pl.getVirtualBuilding().setTotalFloorArea(totalFloorArea);
        pl.getVirtualBuilding().setTotalCarpetArea(totalFloorArea.multiply(BigDecimal.valueOf(0.80)));
        pl.getVirtualBuilding().setTotalExistingBuiltUpArea(totalExistingBuiltUpArea);
        pl.getVirtualBuilding().setTotalExistingFloorArea(totalExistingFloorArea);
        pl.getVirtualBuilding().setTotalExistingCarpetArea(totalExistingFloorArea.multiply(BigDecimal.valueOf(0.80)));
        pl.getVirtualBuilding().setOccupancies(distinctOccupancyTypes);
        pl.getVirtualBuilding().setTotalBuitUpArea(totalBuiltUpArea);
        pl.getVirtualBuilding().setMostRestrictiveFar(getMostRestrictiveFar(distinctOccupancyTypes));

        addTotalMezzanineArea(pl);

        if (!distinctOccupancyTypes.isEmpty()) {
            int allResidentialOccTypesForPlan = 0;
            for (OccupancyType occupancy : distinctOccupancyTypes) {
                LOG.info("occupancy :" + occupancy);
                // setting residentialBuilding
                int residentialOccupancyType = 0;
                if (occupancy.equals(OccupancyType.OCCUPANCY_A1) || occupancy.equals(OccupancyType.OCCUPANCY_A4))
                    residentialOccupancyType = 1;
                if (residentialOccupancyType == 0) {
                    allResidentialOccTypesForPlan = 0;
                    break;
                } else
                    allResidentialOccTypesForPlan = 1;
            }
            if (allResidentialOccTypesForPlan == 1)
                pl.getVirtualBuilding().setResidentialBuilding(true);
            else
                pl.getVirtualBuilding().setResidentialBuilding(false);
            int allResidentialOrCommercialOccTypesForPlan = 0;
            for (OccupancyType occupancyType : distinctOccupancyTypes) {
                int residentialOrCommercialOccupancyTypeForPlan = 0;
                if (occupancyType.equals(OccupancyType.OCCUPANCY_A1) || occupancyType.equals(OccupancyType.OCCUPANCY_A4) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_F) || occupancyType.equals(OccupancyType.OCCUPANCY_F1) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_F2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_F4))
                    residentialOrCommercialOccupancyTypeForPlan = 1;
                if (residentialOrCommercialOccupancyTypeForPlan == 0) {
                    allResidentialOrCommercialOccTypesForPlan = 0;
                    break;
                } else
                    allResidentialOrCommercialOccTypesForPlan = 1;
            }
            if (allResidentialOrCommercialOccTypesForPlan == 1)
                pl.getVirtualBuilding().setResidentialOrCommercialBuilding(true);
            else
                pl.getVirtualBuilding().setResidentialOrCommercialBuilding(false);
        }
        if (Log.isInfoEnabled())
            for (Block b : pl.getBlocks()) {
                // PrintUtil.print(b.getBuilding().getFloors());
            }
        OccupancyType mostRestrictiveOccupancy = pl.getVirtualBuilding().getMostRestrictiveFar();
        BigDecimal far = BigDecimal.ZERO;
        if (pl.getPlot().getArea().doubleValue() > 0)
            far = pl.getVirtualBuilding().getTotalFloorArea().divide(pl.getPlot().getArea(), DECIMALDIGITS_MEASUREMENTS,
                    ROUNDMODE_MEASUREMENTS);

        pl.setFar(far);
        if (!Util.isSmallPlot(pl))
            calculateFar(pl, mostRestrictiveOccupancy, far);
        PrintUtil.print(pl, "Block");
        return pl;
    }

    private void calculateFar(PlanDetail pl, OccupancyType mostRestrictiveOccupancy, BigDecimal far) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey("Common_FAR");
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, OCCUPANCY);
        scrutinyDetail.addColumnHeading(4, REQUIRED);
        scrutinyDetail.addColumnHeading(5, PROVIDED);
        scrutinyDetail.addColumnHeading(6, STATUS);

        BigDecimal upperWeightedFar = BigDecimal.ZERO;
        BigDecimal loweWeightedFar = BigDecimal.ZERO;
        BigDecimal weightedAreaWOAddnlFee = BigDecimal.ZERO;
        BigDecimal weightedAreaWithAddnlFee = BigDecimal.ZERO;
        upperWeightedFar.setScale(DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);
        loweWeightedFar.setScale(DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);
        weightedAreaWOAddnlFee.setScale(DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);

        if (pl.getPlot().getArea().doubleValue() >= 5000) {

            for (Occupancy occ : pl.getOccupancies()) {
                weightedAreaWOAddnlFee = weightedAreaWOAddnlFee
                        .add(occ.getFloorArea().multiply(getPermissibleFar(occ.getType())));
                weightedAreaWithAddnlFee = weightedAreaWithAddnlFee
                        .add(occ.getFloorArea().multiply(getMaxPermissibleFar(occ.getType())));
            }
            if (pl.getVirtualBuilding().getTotalFloorArea() != null
                    && pl.getVirtualBuilding().getTotalFloorArea().doubleValue() > 0) {
                loweWeightedFar = weightedAreaWOAddnlFee.divide(pl.getVirtualBuilding().getTotalFloorArea(), 2,
                        ROUNDMODE_MEASUREMENTS);
                upperWeightedFar = weightedAreaWithAddnlFee.divide(pl.getVirtualBuilding().getTotalFloorArea(), 2,
                        ROUNDMODE_MEASUREMENTS);
            }

            processFar(pl, "-", pl.getFar(), upperWeightedFar, loweWeightedFar, scrutinyDetail, RULE_DESCRIPTION_KEY_WEIGHTED);

        } else if (mostRestrictiveOccupancy != null)
            switch (mostRestrictiveOccupancy) {
            case OCCUPANCY_A1:
            case OCCUPANCY_A4:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), four, three, scrutinyDetail,
                        null);
                break;
            case OCCUPANCY_A2:
            case OCCUPANCY_F3:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), four, twoPointFive,
                        scrutinyDetail, null);
                break;
            // case OCCUPANCY_B:
            case OCCUPANCY_B1:
            case OCCUPANCY_B2:
            case OCCUPANCY_B3:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), three, twoPointFive,
                        scrutinyDetail, null);
                break;
            case OCCUPANCY_C:
            case OCCUPANCY_C1:
            case OCCUPANCY_C2:
            case OCCUPANCY_C3:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), threePointFive, twoPointFive,
                        scrutinyDetail, null);
                break;
            case OCCUPANCY_D:
            case OCCUPANCY_D1:
            case OCCUPANCY_D2:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), twoPointFive, onePointFive,
                        scrutinyDetail, null);
                break;
            case OCCUPANCY_E:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), four, three, scrutinyDetail,
                        null);
                break;
            case OCCUPANCY_F:
            case OCCUPANCY_F4:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), four, three, scrutinyDetail,
                        null);
                break;
            case OCCUPANCY_G1:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), twoPointFive, twoPointFive,
                        scrutinyDetail, null);
                break;
            case OCCUPANCY_G2:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), four, threePointFive,
                        scrutinyDetail, null);
                break;
            case OCCUPANCY_H:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), four, three, scrutinyDetail,
                        null);
                break;
            case OCCUPANCY_I1:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), two, two, scrutinyDetail, null);
                break;
            case OCCUPANCY_I2:
                processFar(pl, mostRestrictiveOccupancy.getOccupancyTypeVal(), pl.getFar(), onePointFive, onePointFive,
                        scrutinyDetail, null);
                break;
            default:
                break;

            }
    }

    private void validate2(PlanDetail pl, Block blk, Floor flr, Occupancy occupancy) {
        if (occupancy.getBuiltUpArea() != null && occupancy.getBuiltUpArea().compareTo(BigDecimal.valueOf(0)) < 0)
            pl.addError(VALIDATION_NEGATIVE_BUILTUP_AREA,
                    getLocaleMessage(VALIDATION_NEGATIVE_BUILTUP_AREA, blk.getNumber(), flr.getNumber().toString(),
                            occupancy.getType().getOccupancyTypeVal()));
        if (occupancy.getExistingBuiltUpArea() != null
                && occupancy.getExistingBuiltUpArea().compareTo(BigDecimal.valueOf(0)) < 0)
            pl.addError(VALIDATION_NEGATIVE_EXISTING_BUILTUP_AREA,
                    getLocaleMessage(VALIDATION_NEGATIVE_EXISTING_BUILTUP_AREA, blk.getNumber(), flr.getNumber().toString(),
                            occupancy.getType().getOccupancyTypeVal()));
        occupancy.setFloorArea(
                (occupancy.getBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getBuiltUpArea())
                        .subtract(occupancy.getDeduction() == null ? BigDecimal.ZERO
                                : occupancy.getDeduction()));
        if (occupancy.getFloorArea() != null && occupancy.getFloorArea().compareTo(BigDecimal.valueOf(0)) < 0)
            pl.addError(VALIDATION_NEGATIVE_FLOOR_AREA,
                    getLocaleMessage(VALIDATION_NEGATIVE_FLOOR_AREA, blk.getNumber(), flr.getNumber().toString(),
                            occupancy.getType().getOccupancyTypeVal()));
        occupancy.setExistingFloorArea(
                (occupancy.getExistingBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getExistingBuiltUpArea()).subtract(
                        occupancy.getExistingDeduction() == null ? BigDecimal.ZERO : occupancy.getExistingDeduction()));
        if (occupancy.getExistingFloorArea() != null && occupancy.getExistingFloorArea().compareTo(BigDecimal.valueOf(0)) < 0)
            pl.addError(VALIDATION_NEGATIVE_EXISTING_FLOOR_AREA,
                    getLocaleMessage(VALIDATION_NEGATIVE_EXISTING_FLOOR_AREA, blk.getNumber(), flr.getNumber().toString(),
                            occupancy.getType().getOccupancyTypeVal()));
    }

    private void processFar(PlanDetail pl, String occupancyType, BigDecimal far, BigDecimal upperLimit,
            BigDecimal additionFeeLimit, ScrutinyDetail scrutinyDetail, String desc) {

        if (far.doubleValue() <= upperLimit.doubleValue()) {

            if (far.doubleValue() > additionFeeLimit.doubleValue()) {
                BigDecimal additonalFee = pl.getPlot().getArea().multiply(new BigDecimal(5000))
                        .multiply(far.subtract(additionFeeLimit));

                String actualResult = getLocaleMessage(RULE_ACTUAL_KEY, far.toString(), additonalFee.toString());
                String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, upperLimit.toString(), far.toString(),
                        additionFeeLimit.toString(), pl.getPlot().getArea().toString());
                if (desc == null)
                    desc = getLocaleMessage(RULE_DESCRIPTION_KEY, upperLimit.toString(),
                            additionFeeLimit.toString());
                else
                    desc = getLocaleMessage(desc, upperLimit.toString(),
                            additionFeeLimit.toString());
                desc = desc + "Kozhikode";
                Map<String, String> details = new HashMap<>();
                details.put(RULE_NO, RULE_31_1);
                details.put(DESCRIPTION, desc);
                details.put(OCCUPANCY, occupancyType);
                details.put(REQUIRED, expectedResult);
                details.put(PROVIDED, actualResult);
                details.put(STATUS, Result.Verify.getResultVal());
                scrutinyDetail.getDetail().add(details);
                pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
                // pl.reportOutput.add(buildResult(desc, actualResult, expectedResult, Result.Verify));
            } else {
                if (desc == null)
                    desc = getLocaleMessage(RULE_DESCRIPTION_KEY, upperLimit.toString(),
                            additionFeeLimit.toString());
                else
                    desc = getLocaleMessage(desc, upperLimit.toString(),
                            additionFeeLimit.toString());
                String actualResult = far.toString();
                String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, upperLimit.toString(), far.toString(),
                        additionFeeLimit.toString(), pl.getPlot().getArea().toString());

                Map<String, String> details = new HashMap<>();
                details.put(RULE_NO, RULE_31_1);
                details.put(DESCRIPTION, desc);
                details.put(OCCUPANCY, occupancyType);
                details.put(REQUIRED, expectedResult);
                details.put(PROVIDED, actualResult);
                details.put(STATUS, Result.Accepted.getResultVal());
                scrutinyDetail.getDetail().add(details);
                pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
                pl.reportOutput.add(buildResult(desc, actualResult, expectedResult, Result.Accepted));
            }
        } else {
            if (desc == null)
                desc = getLocaleMessage(RULE_DESCRIPTION_KEY, upperLimit.toString(), additionFeeLimit.toString());
            else
                desc = getLocaleMessage(desc, upperLimit.toString(), additionFeeLimit.toString());
            String actualResult = far.toString();
            String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, far.toString(), BigDecimal.ZERO.toString());

            Map<String, String> details = new HashMap<>();
            details.put(RULE_NO, RULE_31_1);
            details.put(DESCRIPTION, desc);
            details.put(OCCUPANCY, occupancyType);
            details.put(REQUIRED, expectedResult);
            details.put(PROVIDED, actualResult);
            details.put(STATUS, Result.Not_Accepted.getResultVal());
            scrutinyDetail.getDetail().add(details);
            pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
            pl.reportOutput.add(buildResult(desc, actualResult, expectedResult, Result.Not_Accepted));

        }

    }

    private RuleOutput buildResult(String desc, String actualResult, String expectedResult, Result result) {
        RuleOutput ruleOutput = new RuleOutput();
        ruleOutput.key = getLocaleMessage(RULE_NAME_KEY);
        ruleOutput.ruleDescription = desc;
        SubRuleOutput subRuleOutput = new SubRuleOutput();
        RuleReportOutput ruleReportOutput = new RuleReportOutput();
        subRuleOutput.setKey(getLocaleMessage(RULE_NAME_KEY));
        ruleReportOutput.setActualResult(actualResult);
        ruleReportOutput.setExpectedResult(expectedResult);
        ruleReportOutput.setFieldVerified(ruleOutput.key);
        ruleReportOutput.setStatus(result.name());
        subRuleOutput.setRuleDescription(desc);
        subRuleOutput.add(ruleReportOutput);
        ruleOutput.subRuleOutputs.add(subRuleOutput);
        return ruleOutput;

    }

    protected OccupancyType getMostRestrictiveFar(EnumSet<OccupancyType> distinctOccupancyTypes) {
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I2))
            return OccupancyType.OCCUPANCY_I2;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I1))
            return OccupancyType.OCCUPANCY_I1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G1))
            return OccupancyType.OCCUPANCY_G1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D))
            return OccupancyType.OCCUPANCY_D;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B1))
            return OccupancyType.OCCUPANCY_B1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B2))
            return OccupancyType.OCCUPANCY_B2;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B3))
            return OccupancyType.OCCUPANCY_B3;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_C))
            return OccupancyType.OCCUPANCY_C;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D1))
            return OccupancyType.OCCUPANCY_D1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_E))
            return OccupancyType.OCCUPANCY_E;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F))
            return OccupancyType.OCCUPANCY_F;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F4))
            return OccupancyType.OCCUPANCY_F4;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A1))
            return OccupancyType.OCCUPANCY_A1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A4))
            return OccupancyType.OCCUPANCY_A4;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A2))
            return OccupancyType.OCCUPANCY_A2;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F3))
            return OccupancyType.OCCUPANCY_F3;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G2))
            return OccupancyType.OCCUPANCY_G2;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_H))
            return OccupancyType.OCCUPANCY_H;
        else
            return null;
    }

    private BigDecimal getPermissibleFar(OccupancyType occupancyType) {
        BigDecimal permissibleFar = BigDecimal.ZERO;
        switch (occupancyType) {
        case OCCUPANCY_A1:
        case OCCUPANCY_A4:
            permissibleFar = three;
            break;
        case OCCUPANCY_A2:
        case OCCUPANCY_F3:
            permissibleFar = twoPointFive;
            break;
        case OCCUPANCY_B1:
        case OCCUPANCY_B2:
        case OCCUPANCY_B3:
            permissibleFar = twoPointFive;
            break;
        case OCCUPANCY_C:
            permissibleFar = twoPointFive;
            break;
        case OCCUPANCY_D:
        case OCCUPANCY_D1:
            permissibleFar = onePointFive;
            break;
        case OCCUPANCY_E:
            permissibleFar = three;
            break;
        case OCCUPANCY_F:
        case OCCUPANCY_F4:
            permissibleFar = three;
            break;
        case OCCUPANCY_G1:
            permissibleFar = twoPointFive;
            break;
        case OCCUPANCY_G2:
            permissibleFar = threePointFive;
            break;
        case OCCUPANCY_H:
            permissibleFar = three;
            break;
        case OCCUPANCY_I1:
            permissibleFar = two;
            break;
        case OCCUPANCY_I2:
            permissibleFar = onePointFive;
            break;
        default:
            break;

        }
        return permissibleFar;
    }

    private BigDecimal getMaxPermissibleFar(OccupancyType occupancyType) {
        BigDecimal permissibleFar = BigDecimal.ZERO;
        switch (occupancyType) {
        case OCCUPANCY_A1:
        case OCCUPANCY_A4:
            permissibleFar = four;
            break;
        case OCCUPANCY_A2:
        case OCCUPANCY_F3:
            permissibleFar = four;
            break;
        case OCCUPANCY_B1:
        case OCCUPANCY_B2:
        case OCCUPANCY_B3:
            permissibleFar = three;
            break;
        case OCCUPANCY_C:
        case OCCUPANCY_C1:
        case OCCUPANCY_C2:
        case OCCUPANCY_C3:

            permissibleFar = threePointFive;
            break;
        case OCCUPANCY_D:
        case OCCUPANCY_D1:
        case OCCUPANCY_D2:
            permissibleFar = twoPointFive;
            break;
        case OCCUPANCY_E:
            permissibleFar = four;
            break;
        case OCCUPANCY_F:
        case OCCUPANCY_F4:
            permissibleFar = four;
            break;
        case OCCUPANCY_G1:
            permissibleFar = twoPointFive;
            break;
        case OCCUPANCY_G2:
            permissibleFar = four;
            break;
        case OCCUPANCY_H:
            permissibleFar = four;
            break;
        case OCCUPANCY_I1:
            permissibleFar = two;
            break;
        case OCCUPANCY_I2:
            permissibleFar = onePointFive;
            break;
        default:
            break;

        }
        return permissibleFar;
    }

    private void addFireStairs(DXFDocument doc, Block block, Floor floor) {
        if (!block.getTypicalFloor().isEmpty())
            for (TypicalFloor tp : block.getTypicalFloor())
                if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                    for (Floor allFloors : block.getBuilding().getFloors())
                        if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                            if (!allFloors.getFireStairs().isEmpty()) {
                                floor.setFireStairs(allFloors.getFireStairs());
                                return;
                            }

        // Layer name convention BLK_n_FLR_i_FIRESTAIR_k
        String fireEscapeStairNamePattern = "BLK_" + block.getNumber() + "_FLR_" + floor.getNumber() + "_FIRESTAIR" + "_+\\d";

        List<String> fireEscapeStairNames = Util.getLayerNamesLike(doc, fireEscapeStairNamePattern);

        if (!fireEscapeStairNames.isEmpty())
            for (String fireEscapeStairName : fireEscapeStairNames) {
                String[] stairName = fireEscapeStairName.split("_");
                if (stairName.length == 6 && stairName[5] != null && !stairName[5].isEmpty()) {
                    // set polylines in BLK_n_FLR_i_FIRESTAIR_k
                    List<DXFLWPolyline> fireStairPolyLines = Util.getPolyLinesByLayer(doc, String
                            .format(DxfFileConstants.LAYER_FIRESTAIR_FLOOR, block.getNumber(), floor.getNumber(), stairName[5]));

                    if (fireStairPolyLines != null && !fireStairPolyLines.isEmpty()) {
                        FireStair fireStair = new FireStair();
                        fireStair.setNumber(stairName[5]);
                        fireStair.setStairPolylines(fireStairPolyLines);

                        // set floor height
                        String floorHeight = Util.getMtextByLayerName(doc, fireEscapeStairName, "FLR_HT_M");

                        if (!isBlank(floorHeight)) {
                            if (floorHeight.contains("="))
                                floorHeight = floorHeight.split("=")[1] != null
                                        ? floorHeight.split("=")[1].replaceAll("[^\\d.]", "")
                                        : "";
                            else
                                floorHeight = floorHeight.replaceAll("[^\\d.]", "");

                            if (!isBlank(floorHeight)) {
                                BigDecimal height = BigDecimal.valueOf(Double.parseDouble(floorHeight));

                                BigDecimal noOfRises = height.divide(BigDecimal.valueOf(0.19),
                                        DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                                        DcrConstants.ROUNDMODE_MEASUREMENTS);

                                if (noOfRises != null) {
                                    double ceil = Math.ceil(noOfRises.doubleValue());
                                    noOfRises = BigDecimal.valueOf(ceil);
                                }

                                fireStair.setNoOfRises(noOfRises);
                            }

                        }

                        // set polylines in BLK_n_FLR_i_FIRESTAIR_k_FLIGHT

                        String flightLayerName = String.format(DxfFileConstants.LAYER_FIRESTAIR_FLIGHT_FLOOR, block.getNumber(),
                                floor.getNumber(), stairName[5]);
                        List<DXFLWPolyline> fireStairFlightPolyLines = Util.getPolyLinesByLayer(doc, flightLayerName);

                        List<BigDecimal> fireStairFlightLengths = Util.getListOfDimensionByColourCode(doc, flightLayerName,
                                DxfFileConstants.STAIR_FLIGHT_LENGTH_COLOR);

                        fireStair.setLengthOfFlights(fireStairFlightLengths);

                        List<BigDecimal> fireStairFlightWidths = Util.getListOfDimensionByColourCode(doc, flightLayerName,
                                DxfFileConstants.STAIR_FLIGHT_WIDTH_COLOR);

                        fireStair.setWidthOfFlights(fireStairFlightWidths);

                        boolean isClosed = fireStairFlightPolyLines.stream().allMatch(dxflwPolyline -> dxflwPolyline.isClosed());

                        fireStair.setFlightPolyLineClosed(isClosed);

                        List<Measurement> flightPolyLines = fireStairFlightPolyLines.stream()
                                .map(flightPolyLine -> new Measurement(flightPolyLine, true))
                                .collect(Collectors.toList());

                        fireStair.setFlightPolyLines(flightPolyLines);

                        // set lines in BLK_n_FLR_i_FIRESTAIR_k
                        List<DXFLine> fireStairLines = Util.getLinesByLayer(doc, flightLayerName);
                        fireStair.setLines(fireStairLines);

                        floor.addFireStair(fireStair);
                    }
                }
            }
    }

    private void addGeneralStairs(DXFDocument doc, Block block, Floor floor, boolean highRise) {
        if (!block.getTypicalFloor().isEmpty())
            for (TypicalFloor tp : block.getTypicalFloor())
                if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                    for (Floor allFloors : block.getBuilding().getFloors())
                        if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                            if (!allFloors.getGeneralStairs().isEmpty()) {
                                floor.setGeneralStairs(allFloors.getGeneralStairs());
                                return;
                            }

        // Layer name convention BLK_n_FLR_i_STAIR_k
        String generalStairNamePattern = "BLK_" + block.getNumber() + "_FLR_" + floor.getNumber() + "_STAIR" + "_+\\d";

        List<String> generalStairNames = Util.getLayerNamesLike(doc, generalStairNamePattern);

        if (!generalStairNames.isEmpty())
            for (String generalStairName : generalStairNames) {
                String[] stairName = generalStairName.split("_");
                if (stairName.length == 6 && stairName[5] != null && !stairName[5].isEmpty()) {

                    // set polylines in BLK_n_FLR_i_STAIR_k
                    List<DXFLWPolyline> generalStairPolyLines = Util.getPolyLinesByLayer(doc, String
                            .format(DxfFileConstants.LAYER_STAIR_FLOOR, block.getNumber(), floor.getNumber(), stairName[5]));

                    if (generalStairPolyLines != null && !generalStairPolyLines.isEmpty()) {
                        GeneralStair generalStair = new GeneralStair();
                        generalStair.setNumber(stairName[5]);
                        generalStair.setStairPolylines(generalStairPolyLines);

                        // set floor height
                        String floorHeight = Util.getMtextByLayerName(doc, generalStairName, "FLR_HT_M");

                        if (!isBlank(floorHeight)) {
                            if (floorHeight.contains("="))
                                floorHeight = floorHeight.split("=")[1] != null
                                        ? floorHeight.split("=")[1].replaceAll("[^\\d.]", "")
                                        : "";
                            else
                                floorHeight = floorHeight.replaceAll("[^\\d.]", "");

                            if (!isBlank(floorHeight)) {
                                BigDecimal height = BigDecimal.valueOf(Double.parseDouble(floorHeight));

                                BigDecimal noOfRises = height.divide(
                                        highRise ? BigDecimal.valueOf(0.19) : BigDecimal.valueOf(0.15),
                                        DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                                        DcrConstants.ROUNDMODE_MEASUREMENTS);

                                generalStair.setNoOfRises(noOfRises);
                            }

                        }

                        // set polylines in BLK_n_FLR_i_STAIR_k_FLIGHT
                        String flightLayerName = String.format(DxfFileConstants.LAYER_STAIR_FLIGHT_FLOOR, block.getNumber(),
                                floor.getNumber(), generalStair.getNumber());

                        List<DXFLWPolyline> generalStairFlightPolyLines = Util.getPolyLinesByLayer(doc, flightLayerName);

                        List<Measurement> flightPolyLines = generalStairFlightPolyLines.stream()
                                .map(flightPolyLine -> new Measurement(flightPolyLine, true))
                                .collect(Collectors.toList());

                        List<BigDecimal> generalStairFlightLengths = Util.getListOfDimensionByColourCode(doc, flightLayerName,
                                DxfFileConstants.STAIR_FLIGHT_LENGTH_COLOR);

                        generalStair.setLengthOfFlights(generalStairFlightLengths);

                        List<BigDecimal> generalStairWidthLengths = Util.getListOfDimensionByColourCode(doc, flightLayerName,
                                DxfFileConstants.STAIR_FLIGHT_WIDTH_COLOR);

                        generalStair.setWidthOfFlights(generalStairWidthLengths);

                        boolean isClosed = generalStairFlightPolyLines.stream()
                                .allMatch(dxflwPolyline -> dxflwPolyline.isClosed());

                        generalStair.setFlightPolyLineClosed(isClosed);

                        generalStair.setFlightPolyLines(flightPolyLines);

                        // set lines in BLK_n_FLR_i_STAIR_k
                        List<DXFLine> generalStairLines = Util.getLinesByLayer(doc, flightLayerName);
                        generalStair.setLines(generalStairLines);

                        floor.addGeneralStair(generalStair);

                    }
                }
            }
    }

    private void setLifts(DXFDocument doc, Block block, Floor floor) {
        if (!block.getTypicalFloor().isEmpty())
            for (TypicalFloor tp : block.getTypicalFloor())
                if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                    for (Floor allFloors : block.getBuilding().getFloors())
                        if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                            if (!allFloors.getLifts().isEmpty()) {
                                floor.setLifts(allFloors.getLifts());
                                return;
                            }
        String liftRegex = String.format(DxfFileConstants.LAYER_LIFT, block.getNumber(), floor.getNumber()) + "_+\\d";
        List<String> liftLayer = Util.getLayerNamesLike(doc, liftRegex);
        if (!liftLayer.isEmpty())
            for (String lftLayer : liftLayer) {
                List<DXFLWPolyline> polylines = Util.getPolyLinesByLayer(doc, lftLayer);
                String[] splitLayer = lftLayer.split("_", 6);
                if (splitLayer.length == 6 && splitLayer[5] != null && !splitLayer[5].isEmpty() && !polylines.isEmpty()) {
                    Lift lift = new Lift();
                    lift.setNumber(Integer.valueOf(splitLayer[5]));
                    boolean isClosed = polylines.stream().allMatch(dxflwPolyline -> dxflwPolyline.isClosed());
                    lift.setLiftPolyLineClosed(isClosed);
                    List<Measurement> liftPolyLine = polylines.stream()
                            .map(dxflwPolyline -> new Measurement(dxflwPolyline, true))
                            .collect(Collectors.toList());
                    lift.setLiftPolyLines(liftPolyLine);
                    lift.setPolylines(polylines);
                    floor.addLifts(lift);
                }
            }
    }

    private Boolean checkTerrace(Floor floor) {

        BigDecimal totalStairArea = BigDecimal.ZERO;
        BigDecimal fireStairArea = BigDecimal.ZERO;
        BigDecimal generalStairArea = BigDecimal.ZERO;
        BigDecimal liftArea = BigDecimal.ZERO;
        BigDecimal builtUpArea = BigDecimal.ZERO;

        List<FireStair> fireStairs = floor.getFireStairs();
        if (fireStairs != null && !fireStairs.isEmpty())
            for (FireStair fireStair : fireStairs) {
                List<DXFLWPolyline> stairPolylines = fireStair.getStairPolylines();
                if (stairPolylines != null && !stairPolylines.isEmpty())
                    for (DXFLWPolyline stairPolyLine : stairPolylines) {
                        BigDecimal stairArea = Util.getPolyLineArea(stairPolyLine);
                        fireStairArea = fireStairArea.add(stairArea).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                                DcrConstants.ROUNDMODE_MEASUREMENTS);
                    }
            }

        List<GeneralStair> generalStairs = floor.getGeneralStairs();
        if (generalStairs != null && !generalStairs.isEmpty())
            for (GeneralStair generalStair : generalStairs) {
                List<DXFLWPolyline> stairPolylines = generalStair.getStairPolylines();
                if (stairPolylines != null && !stairPolylines.isEmpty())
                    for (DXFLWPolyline stairPolyLine : stairPolylines) {
                        BigDecimal stairArea = Util.getPolyLineArea(stairPolyLine);
                        generalStairArea = generalStairArea.add(stairArea).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                                DcrConstants.ROUNDMODE_MEASUREMENTS);
                    }
            }

        totalStairArea = fireStairArea.add(generalStairArea);

        List<Lift> lifts = floor.getLifts();

        if (lifts != null && !lifts.isEmpty())
            for (Lift lift : lifts) {
                List<DXFLWPolyline> polylines = lift.getPolylines();

                if (polylines != null && !polylines.isEmpty())
                    for (DXFLWPolyline dxflwPolyline : polylines) {
                        BigDecimal polyLineArea = Util.getPolyLineArea(dxflwPolyline);
                        liftArea = liftArea.add(polyLineArea).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                                DcrConstants.ROUNDMODE_MEASUREMENTS);
                    }

            }

        totalStairArea = totalStairArea.add(liftArea);

        List<DXFLWPolyline> builtUpAreaPolyLines = floor.getBuiltUpAreaPolyLine();
        if (builtUpAreaPolyLines != null && !builtUpAreaPolyLines.isEmpty())
            for (DXFLWPolyline builtUpAreaPolyLine : builtUpAreaPolyLines) {
                BigDecimal polyLineArea = Util.getPolyLineArea(builtUpAreaPolyLine);
                builtUpArea = builtUpArea.add(polyLineArea).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                        DcrConstants.ROUNDMODE_MEASUREMENTS);
            }

        return builtUpArea.doubleValue() > 0 && totalStairArea.doubleValue() > 0 && builtUpArea.compareTo(totalStairArea) <= 0
                ? Boolean.TRUE
                : Boolean.FALSE;

    }

    private void addMezzanineFloor(DXFDocument doc, Block block, Floor floor) {

        if (!block.getTypicalFloor().isEmpty())
            for (TypicalFloor tp : block.getTypicalFloor())
                if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                    for (Floor allFloors : block.getBuilding().getFloors())
                        if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                            if (!allFloors.getMezzanineFloor().isEmpty() || !allFloors.getHalls().isEmpty()) {
                                floor.setMezzanineFloor(allFloors.getMezzanineFloor());
                                floor.setHalls(allFloors.getHalls());
                                return;
                            }
        // extract mezzanine data
        String mezzanineLayerNameRegExp = "BLK_" + block.getNumber() + "_FLR_" + floor.getNumber() + "_M" + "_+\\d"
                + "_BLT_UP_AREA";
        List<String> mezzanineLayerNames = Util.getLayerNamesLike(doc, mezzanineLayerNameRegExp);
        List<MezzanineFloor> mezzanineFloorList = new ArrayList<>();
        if (!mezzanineLayerNames.isEmpty())
            for (String mezzanine : mezzanineLayerNames) {
                String[] array = mezzanine.split("_");
                if (array[5] != null && !array[5].isEmpty()) {
                    List<DXFLWPolyline> mezzaninePolyLines = Util.getPolyLinesByLayer(doc, String.format(
                            DxfFileConstants.LAYER_MEZZANINE_FLOOR_BLT_UP_AREA, block.getNumber(), floor.getNumber(), array[5]));
                    List<DXFLWPolyline> mezzanineDeductPolyLines = Util.getPolyLinesByLayer(doc, String.format(
                            DxfFileConstants.LAYER_MEZZANINE_FLOOR_DEDUCTION, block.getNumber(), floor.getNumber(), array[5]));
                    BigDecimal builtUpAreaDeduct = BigDecimal.ZERO;

                    BigDecimal builtUpArea = BigDecimal.ZERO;
                    OccupancyType occupancyType = null;
                    if (!mezzaninePolyLines.isEmpty() || !mezzanineDeductPolyLines.isEmpty()) {
                        MezzanineFloor mezzanineFloor = new MezzanineFloor();
                        mezzanineFloor.setNumber(array[5]);
                        for (DXFLWPolyline polyline : mezzaninePolyLines) {
                            BigDecimal polyLineBuiltUpArea = Util.getPolyLineArea(polyline);
                            builtUpArea = builtUpArea.add(polyLineBuiltUpArea == null ? BigDecimal.ZERO : polyLineBuiltUpArea);
                            occupancyType = Util.findOccupancyType(polyline);
                        }

                        if (!mezzanineDeductPolyLines.isEmpty())
                            for (DXFLWPolyline polyLine : mezzanineDeductPolyLines) {
                                BigDecimal polyLineDeduct = Util.getPolyLineArea(polyLine);
                                builtUpAreaDeduct = builtUpAreaDeduct
                                        .add(polyLineDeduct == null ? BigDecimal.ZERO : polyLineDeduct);
                            }

                        BigDecimal floorArea = builtUpArea.subtract(builtUpAreaDeduct);
                        mezzanineFloor.setFloorArea(floorArea);
                        mezzanineFloor.setCarpetArea(BigDecimal.valueOf(0.8).multiply(floorArea));
                        mezzanineFloor.setBuiltUpArea(builtUpArea);
                        mezzanineFloor.setOccupancyType(occupancyType);
                        mezzanineFloor.setDeductions(builtUpAreaDeduct);

                        mezzanineFloorList.add(mezzanineFloor);
                    }

                }
            }
        floor.setMezzanineFloor(mezzanineFloorList);

        // extract Hall data
        String hallLayerNameRegExp = "BLK_" + block.getNumber() + "_FLR_" + floor.getNumber() + "_HALL" + "_+\\d"
                + "_BLT_UP_AREA";
        List<String> hallLayerNames = Util.getLayerNamesLike(doc, hallLayerNameRegExp);
        List<Hall> hallsList = new ArrayList<>();

        if (!hallLayerNames.isEmpty())
            for (String hl : hallLayerNames) {
                String[] array = hl.split("_");
                if (array[5] != null && !array[5].isEmpty()) {
                    Hall hall = new Hall();
                    hall.setNumber(array[5]);
                    List<DXFLWPolyline> hallPolyLines = Util.getPolyLinesByLayer(doc,
                            String.format(DxfFileConstants.LAYER_MEZZANINE_HALL_BLT_UP_AREA, block.getNumber(), floor.getNumber(),
                                    hall.getNumber()));
                    BigDecimal builtUpArea = BigDecimal.ZERO;
                    if (!hallPolyLines.isEmpty())
                        for (DXFLWPolyline polyline : hallPolyLines) {
                            BigDecimal polyLineBuiltUpArea = Util.getPolyLineArea(polyline);
                            builtUpArea = builtUpArea.add(polyLineBuiltUpArea == null ? BigDecimal.ZERO : polyLineBuiltUpArea);
                        }
                    hall.setBuiltUpArea(builtUpArea);
                    List<DXFLWPolyline> hallDeductPolyLines = Util.getPolyLinesByLayer(doc,
                            String.format(DxfFileConstants.LAYER_MEZZANINE_HALL_DEDUCTION, block.getNumber(), floor.getNumber(),
                                    hall.getNumber()));
                    BigDecimal builtUpAreaDeduct = BigDecimal.ZERO;
                    if (!hallDeductPolyLines.isEmpty())
                        for (DXFLWPolyline polyLine : hallDeductPolyLines) {
                            BigDecimal polyLineDeduct = Util.getPolyLineArea(polyLine);
                            builtUpAreaDeduct = builtUpAreaDeduct.add(polyLineDeduct == null ? BigDecimal.ZERO : polyLineDeduct);
                        }
                    hall.setDeductions(builtUpAreaDeduct);
                    hallsList.add(hall);
                }
            }
        floor.setHalls(hallsList);

    }

    private void addTotalMezzanineArea(PlanDetail planDetail) {
        BigDecimal totalBltUpArea = BigDecimal.ZERO;
        BigDecimal totalFlrArea = BigDecimal.ZERO;
        BigDecimal totalCrptArea = BigDecimal.ZERO;
        List<Block> blocks = planDetail.getBlocks();
        if (!blocks.isEmpty())
            for (Block block : blocks) {
                Building building = block.getBuilding();
                if (building != null) {
                    BigDecimal totalBlkBltUpArea = BigDecimal.ZERO;
                    BigDecimal totalBlkFlrArea = BigDecimal.ZERO;
                    BigDecimal totalBlkCrptArea = BigDecimal.ZERO;
                    List<Floor> floors = building.getFloors();
                    if (!floors.isEmpty())
                        for (Floor floor : floors) {
                            List<MezzanineFloor> mezzanineFloors = floor.getMezzanineFloor();

                            if (!mezzanineFloors.isEmpty())
                                for (MezzanineFloor mezzanineFloor : mezzanineFloors) {
                                    totalBlkBltUpArea = totalBlkBltUpArea.add(mezzanineFloor.getBuiltUpArea());
                                    totalBltUpArea = totalBltUpArea.add(mezzanineFloor.getBuiltUpArea());
                                    BigDecimal floorArea = mezzanineFloor.getBuiltUpArea()
                                            .subtract(mezzanineFloor.getDeductions());
                                    totalFlrArea = totalFlrArea.add(floorArea);
                                    totalBlkFlrArea = totalBlkFlrArea.add(floorArea);
                                    totalCrptArea = totalCrptArea.add(BigDecimal.valueOf(0.8).multiply(floorArea));
                                    totalBlkCrptArea = totalBlkCrptArea.add(BigDecimal.valueOf(0.8).multiply(floorArea));
                                }
                        }
                    totalBlkBltUpArea = building.getTotalBuitUpArea().add(totalBlkBltUpArea);
                    totalBlkFlrArea = building.getTotalFloorArea().add(totalBlkFlrArea);
                    building.setTotalBuitUpArea(totalBlkBltUpArea);
                    building.setTotalFloorArea(totalBlkFlrArea);
                }
            }

        if (planDetail.getVirtualBuilding() != null) {
            totalBltUpArea = planDetail.getVirtualBuilding().getTotalBuitUpArea().add(totalBltUpArea);
            totalFlrArea = planDetail.getVirtualBuilding().getTotalFloorArea().add(totalFlrArea);
            totalCrptArea = planDetail.getVirtualBuilding().getTotalCarpetArea().add(totalCrptArea);
            planDetail.getVirtualBuilding().setTotalBuitUpArea(totalBltUpArea);
            planDetail.getVirtualBuilding().setTotalFloorArea(totalFlrArea);
            planDetail.getVirtualBuilding().setTotalCarpetArea(totalCrptArea);
        }
    }

}
