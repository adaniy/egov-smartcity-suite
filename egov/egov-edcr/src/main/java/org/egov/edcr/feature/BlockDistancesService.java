package org.egov.edcr.feature;

import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;
import static org.egov.edcr.utility.ParametersConstants.BLOCK_LEVEL_CHECK;
import static org.egov.edcr.utility.ParametersConstants.BUILDING_HEIGHT;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.utility.BlockDistances;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BlockDistancesService extends GeneralRule implements RuleService {
    public static final String SUBRULE_54_3 = "54-(3)";
    public static final String SUBRULE_55_2 = "55-(2)";
    public static final String SUBRULE_57_4 = "57-(4)";
    public static final String SUBRULE_58_3_A = "58-(3-a)";
    public static final String SUBRULE_59_3 = "59-(3)";
    public static final String SUBRULE_117_3 = "117-(3)";
    public static final BigDecimal DIS_7_5 = BigDecimal.valueOf(7.5);
    public static final String BLK_NUMBER = "blkNumber";
    public static final String SUBRULE = "subrule";
    public static final String MIN_DISTANCE = "minimumDistance";
    public static final String OCCUPANCY = "occupancy";
    private static final String SUBRULE_24_2 = "24-(2)";
    private static final String SUB_RULE_DESCRIPTION = "Minimum distance between blocks %s and %s for occupancy %s";
    private static final String DIS_BTW_BLOCKS_DESCRIPTION = "Distance between block";
    private static final double VALUE_0_5 = 0.5;
    private static final BigDecimal DIS_THREE = BigDecimal.valueOf(3);
    private static final BigDecimal DIS_1_5 = BigDecimal.valueOf(1.5);
    private static final BigDecimal DIS_5 = BigDecimal.valueOf(5);
    private static final BigDecimal DIS_2 = BigDecimal.valueOf(2);
    private static final String SUB_RULE_DES = "Minimum distance between blocks %s and %s";
    private static final String OCCPNCY = "Occupancy";

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        processDistanceBetweenBlocks(pl);
        return pl;
    }

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        for (Block b : pl.getBlocks()) {
            List<BlockDistances> disBetweenBlocksList = new ArrayList<>();
            for (Block block : pl.getBlocks())
                if (!b.getNumber().equals(block.getNumber())) {
                    String layerName = String.format(DxfFileConstants.LAYER_NAME_DIST_BETWEEN_BLOCKS, b.getNumber(),
                            block.getNumber());
                    List<BigDecimal> disBtwnBlcks = Util.getListOfDimensionValueByLayer(doc, layerName);
                    if (!disBtwnBlcks.isEmpty()) {
                        BlockDistances disBtwnBlocks = new BlockDistances();
                        disBtwnBlocks.setBlockNumber(block.getNumber());
                        disBtwnBlocks.setDistances(disBtwnBlcks);
                        disBetweenBlocksList.add(disBtwnBlocks);
                    }
                }
            b.setDisBetweenBlocks(disBetweenBlocksList);
        }
        return pl;
    }

    public void validateDistanceBetweenBlocks(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        List<String> sourceBlockNumbers = new ArrayList<>();
        // iterating blocks one by one
        for (Block sourceBlock : pl.getBlocks()) {
            // validation for building height and occupancies present in diagram or not
            if (sourceBlock.getBuilding() != null)
                /*
                 * if (sourceBlock.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) == 0) {
                 * errors.put(String.format(DcrConstants.BLOCK_BUILDING_HEIGHT, sourceBlock.getNumber()),
                 * edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED, new
                 * String[]{String.format(DcrConstants.BLOCK_BUILDING_HEIGHT, sourceBlock.getNumber())},
                 * LocaleContextHolder.getLocale())); pl.addErrors(errors); }
                 */
                if (sourceBlock.getBuilding().getOccupancies().isEmpty()) {
                    errors.put(String.format(DcrConstants.BLOCK_BUILDING_OCCUPANCY, sourceBlock.getNumber()),
                            edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                    new String[] {
                                            String.format(DcrConstants.BLOCK_BUILDING_OCCUPANCY, sourceBlock.getNumber()) },
                                    LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);
                }
            // eg if i have three blocks b1 , b2 ,b3 in first iteration and b1 is source, b1-> b1 is not validated as b1 is
            // present in list sourceBlockNumbers.
            // b1->b2 is validated and b2 -> b1 is validated.if no one is present error message is thrown.
            // b1->b3 is validated and b3 -> b1 is validated .if no one is present error message is thrown.
            // in second iteration, when b2 is source b2-> b2 is not validated as b2 is present in list sourceBlockNumbers.
            // b2-> b1 is not validated as b2 is present in list sourceBlockNumbers.
            // b2->b3 is validated and b3 -> b2 is validated .if no one is present error message is thrown.
            // in third iteration , when b3 is source b3->b3,b3->b1,b3->b2 all are not validated as b1,b2,b3 all are present in
            // list sourceBlockNumbers.
            sourceBlockNumbers.add(sourceBlock.getNumber());
            for (Block destinationBlock : pl.getBlocks())
                if (!sourceBlockNumbers.contains(destinationBlock.getNumber())) {
                    // distance from source to destination block present or not
                    List<BigDecimal> distanceBetBlocks = new ArrayList<>();
                    List<BigDecimal> distanceBtwBlocks = new ArrayList<>();
                    if (!sourceBlock.getDistanceBetweenBlocks().isEmpty())
                        for (BlockDistances distanceBetweenBlock : sourceBlock.getDistanceBetweenBlocks())
                            if (distanceBetweenBlock.getBlockNumber().equals(destinationBlock.getNumber()))
                                distanceBetBlocks = distanceBetweenBlock.getDistances();
                    // distance from destination to source block present or not
                    if (!destinationBlock.getDistanceBetweenBlocks().isEmpty())
                        for (BlockDistances distanceBetweenBlock : destinationBlock.getDistanceBetweenBlocks())
                            if (distanceBetweenBlock.getBlockNumber().equals(sourceBlock.getNumber()))
                                distanceBtwBlocks = distanceBetweenBlock.getDistances();
                    // throw error if no distance is found from source to destination and destination to source blocks
                    if (distanceBetBlocks.isEmpty() && distanceBtwBlocks.isEmpty()) {
                        errors.put(String.format(DcrConstants.BLOCKS_DISTANCE, sourceBlock.getNumber(),
                                destinationBlock.getNumber()),
                                edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                        new String[] { String.format(DcrConstants.BLOCKS_DISTANCE, sourceBlock.getNumber(),
                                                destinationBlock.getNumber()) },
                                        LocaleContextHolder.getLocale()));
                        pl.addErrors(errors);

                    }
                }
        }
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("DIST_BETWEEN_BLK_%_BLK_%");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(BLOCK_LEVEL_CHECK);
        parameters.add(BUILDING_HEIGHT);
        parameters.add(ParametersConstants.OCCUPANCY);
        return parameters;
    }

    public void processDistanceBetweenBlocks(PlanDetail pl) {
        if (pl.getBlocks().isEmpty())
            return;
        validateDistanceBetweenBlocks(pl);
        List<Map<String, Object>> listOfMapOfLeadingOccupancyDtls = new ArrayList<>();
        String rule = DIS_BTW_BLOCKS_DESCRIPTION;
        String subRule = null;
        for (Block block : pl.getBlocks())
            if (block.getBuilding() != null && !block.getBuilding().getOccupancies().isEmpty()) {
                // creating list of map of subrule, minimum distance and occupancy for every occupancy for a block
                List<Map<String, Object>> listOfAllDetailsMap = new ArrayList<>();
                // iterating building level occupancies.
                for (Occupancy occupancy : block.getBuilding().getOccupancies()) {
                    // creating map of subrule, minimum distance and occupancy for current occupancy for a block
                    Map<String, Object> mapOfAllDetails = new ConcurrentHashMap<>();
                    BigDecimal minimumDistanceToOtherBuildings = BigDecimal.ZERO;
                    if (occupancy.getType().equals(OccupancyType.OCCUPANCY_A1) || occupancy.getType().equals(
                            OccupancyType.OCCUPANCY_A2)
                            || occupancy.getType().equals(
                                    OccupancyType.OCCUPANCY_A3)
                            ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_A4) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_A5) || occupancy.getType().equals(
                                    OccupancyType.OCCUPANCY_F)
                            || occupancy.getType().equals(
                                    OccupancyType.OCCUPANCY_F1)
                            || occupancy.getType().equals(
                                    OccupancyType.OCCUPANCY_F2)
                            || occupancy.getType().equals(
                                    OccupancyType.OCCUPANCY_F3)
                            || occupancy.getType().equals(
                                    OccupancyType.OCCUPANCY_F4)) {
                        if (getHeightLessThanTenCondition(block)) {
                            subRule = SUBRULE_24_2;
                            minimumDistanceToOtherBuildings = DIS_2;
                        } else if (getHeightGreaterThanTenAndLessThanSixteenCondition(block)) {
                            subRule = SUBRULE_24_2;
                            minimumDistanceToOtherBuildings = DIS_THREE;
                        } else {
                            subRule = SUBRULE_117_3;
                            minimumDistanceToOtherBuildings = DIS_5;
                        }
                    } else if (occupancy.getType().equals(OccupancyType.OCCUPANCY_B1) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_B2) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_B3) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_C) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_C1) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_C2) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_C3) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_E)) {
                        if (getHeightLessThanTenCondition(block)) {
                            subRule = SUBRULE_54_3;
                            minimumDistanceToOtherBuildings = DIS_1_5;
                        } else if (getHeightGreaterThanTenAndLessThanSixteenCondition(block)) {
                            subRule = SUBRULE_54_3;
                            minimumDistanceToOtherBuildings = DIS_THREE;
                        } else {
                            subRule = SUBRULE_117_3;
                            minimumDistanceToOtherBuildings = DIS_5;

                        }
                    } else if (occupancy.getType().equals(OccupancyType.OCCUPANCY_D) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_D1) ||
                            occupancy.getType().equals(OccupancyType.OCCUPANCY_D2)) {
                        if (getHeightLessThanTenCondition(block)) {
                            subRule = SUBRULE_55_2;
                            minimumDistanceToOtherBuildings = DIS_1_5;
                        } else if (getHeightGreaterThanTenAndLessThanSixteenCondition(block)) {
                            subRule = SUBRULE_55_2;
                            minimumDistanceToOtherBuildings = DIS_THREE;
                        } else {
                            subRule = SUBRULE_117_3;
                            minimumDistanceToOtherBuildings = DIS_5;
                        }
                    } else if (occupancy.getType().equals(OccupancyType.OCCUPANCY_G1) || occupancy.getType().equals(
                            OccupancyType.OCCUPANCY_G2)) {
                        if (getHeightLessThanTenCondition(block)) {
                            subRule = SUBRULE_57_4;
                            minimumDistanceToOtherBuildings = DIS_THREE;
                        } else if (getHeightGreaterThanTenAndLessThanSixteenCondition(block)) {
                            BigDecimal distanceIncrementBasedOnHeight = BigDecimal.valueOf(
                                    VALUE_0_5).multiply(
                                            BigDecimal
                                                    .valueOf(Math.ceil(block.getBuilding()
                                                            .getBuildingHeight().subtract(BigDecimal.TEN)
                                                            .divide(DIS_THREE, DECIMALDIGITS_MEASUREMENTS,
                                                                    ROUNDMODE_MEASUREMENTS)
                                                            .doubleValue())));
                            subRule = SUBRULE_57_4;
                            minimumDistanceToOtherBuildings = DIS_THREE.add(
                                    distanceIncrementBasedOnHeight);
                        } else {
                            subRule = SUBRULE_117_3;
                            minimumDistanceToOtherBuildings = DIS_5;
                        }
                    } else if (occupancy.getType().equals(OccupancyType.OCCUPANCY_H)) {
                        if (getHeightLessThanTenCondition(block)) {
                            subRule = SUBRULE_58_3_A;
                            minimumDistanceToOtherBuildings = DIS_1_5;
                        } else if (getHeightGreaterThanTenAndLessThanSixteenCondition(block)) {
                            BigDecimal distanceIncrementBasedOnHeight = BigDecimal.valueOf(
                                    VALUE_0_5).multiply(
                                            BigDecimal
                                                    .valueOf(Math.ceil(block.getBuilding()
                                                            .getBuildingHeight().subtract(BigDecimal.TEN)
                                                            .divide(DIS_THREE, DECIMALDIGITS_MEASUREMENTS,
                                                                    ROUNDMODE_MEASUREMENTS)
                                                            .doubleValue())));
                            subRule = SUBRULE_58_3_A;
                            minimumDistanceToOtherBuildings = DIS_1_5.add(distanceIncrementBasedOnHeight);
                        } else {
                            subRule = SUBRULE_117_3;
                            minimumDistanceToOtherBuildings = DIS_5;
                        }
                    } else if (occupancy.getType().equals(OccupancyType.OCCUPANCY_I1)) {
                        if (block.getBuilding().getBuildingHeight().compareTo(BigDecimal.valueOf(16)) < 0) {
                            subRule = SUBRULE_59_3;
                            minimumDistanceToOtherBuildings = DIS_THREE;
                        } else {
                            subRule = SUBRULE_117_3;
                            minimumDistanceToOtherBuildings = DIS_5;
                        }
                    } else if (occupancy.getType().equals(OccupancyType.OCCUPANCY_I2)) {
                        minimumDistanceToOtherBuildings = DIS_7_5;

                        if (block.getBuilding().getBuildingHeight().compareTo(BigDecimal.valueOf(16)) < 0)
                            subRule = SUBRULE_59_3;
                        else
                            subRule = SUBRULE_117_3;
                    }
                    mapOfAllDetails.put(SUBRULE, subRule);
                    mapOfAllDetails.put(MIN_DISTANCE, minimumDistanceToOtherBuildings);
                    mapOfAllDetails.put(OCCUPANCY, occupancy.getType().getOccupancyTypeVal());
                    listOfAllDetailsMap.add(mapOfAllDetails);
                }
                if (!listOfAllDetailsMap.isEmpty()) {
                    // iterate list of map for a particular block to get maximum of all minimum distances present in map.
                    // create a map for occupancy,subrule,minimum distance and blocknumber for maximum of all minimum distance
                    // values
                    Map<String, Object> maxOfMinDistanceMap = listOfAllDetailsMap.get(0);// map to hold that entry of map which
                                                                                         // has maximum minimum distance, first
                                                                                         // time holding 0 th entry from map.
                    for (Map<String, Object> mapOfAllDtls : listOfAllDetailsMap) {
                        // if minimum distance is same in mapOfAllDtls entry and maxOfMinDistanceMap entry
                        if (((BigDecimal) mapOfAllDtls.get(MIN_DISTANCE))
                                .compareTo((BigDecimal) maxOfMinDistanceMap.get(MIN_DISTANCE)) == 0) {
                            // if subrules are same for any number of same minimum distances in map , show it only once,
                            // duplicates are not shown.
                            // if subrules are different for any number of same minimum distances in map, show all subrules by
                            // comma separated.
                            if (!mapOfAllDtls.get(SUBRULE).equals(maxOfMinDistanceMap.get(SUBRULE))) {
                                SortedSet<String> uniqueSubrules = new TreeSet<>();
                                String[] subRuleString = (mapOfAllDtls.get(SUBRULE) + " , " + maxOfMinDistanceMap.get(SUBRULE))
                                        .split(" , ");
                                for (String str : subRuleString)
                                    uniqueSubrules.add(str);
                                String subRuleStr = removeDuplicates(uniqueSubrules);
                                maxOfMinDistanceMap.put(SUBRULE, subRuleStr);
                            }
                            // if occupancy are same for any number of same minimum distances in map , show it only once,
                            // duplicates are not shown.
                            // if occupancy are different for any number of same minimum distances in map, show all occupancies by
                            // comma separated.
                            if (!mapOfAllDtls.get(OCCUPANCY).equals(maxOfMinDistanceMap.get(OCCUPANCY))) {
                                SortedSet<String> uniqueOccupancies = new TreeSet<>();
                                String[] occupancyString = (mapOfAllDtls.get(OCCUPANCY) + " , "
                                        + maxOfMinDistanceMap.get(OCCUPANCY)).split(" , ");
                                for (String str : occupancyString)
                                    uniqueOccupancies.add(str);
                                String occupancyStr = removeDuplicates(uniqueOccupancies);

                                maxOfMinDistanceMap.put(OCCUPANCY, occupancyStr);
                            }
                            maxOfMinDistanceMap.put(BLK_NUMBER, block.getNumber());
                            continue;
                        }
                        // if minimum distance is greater in mapOfAllDtls entry than maxOfMinDistanceMap entry
                        if (((BigDecimal) maxOfMinDistanceMap.get(MIN_DISTANCE))
                                .compareTo((BigDecimal) mapOfAllDtls.get(MIN_DISTANCE)) < 0)
                            maxOfMinDistanceMap.putAll(mapOfAllDtls);
                        maxOfMinDistanceMap.put(BLK_NUMBER, block.getNumber());
                    }
                    listOfMapOfLeadingOccupancyDtls.add(maxOfMinDistanceMap);
                }
            }
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey("Common_Distance Between Blocks");
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, OCCPNCY);
        scrutinyDetail.addColumnHeading(4, REQUIRED);
        scrutinyDetail.addColumnHeading(5, PROVIDED);
        scrutinyDetail.addColumnHeading(6, STATUS);
        for (Block b : pl.getBlocks())
            for (Block block : pl.getBlocks())
                if (b.getNumber() != block.getNumber())
                    if (!block.getBuilding().getOccupancies().isEmpty() && !b.getBuilding().getOccupancies().isEmpty())
                        if (!b.getDistanceBetweenBlocks().isEmpty())
                            for (BlockDistances distanceBetweenBlock : b.getDistanceBetweenBlocks())
                                // if b is source block , checking that its destination block number is same as block
                                if (distanceBetweenBlock.getBlockNumber().equals(block.getNumber())) {
                                    Map<String, Object> mapForSourceBlkDetails = new ConcurrentHashMap<>();
                                    Map<String, Object> mapForDesBlkDetails = new ConcurrentHashMap<>();
                                    for (Map<String, Object> mapOfLeadingDtlsForBlock : listOfMapOfLeadingOccupancyDtls) {
                                        if (b.getNumber().equals(mapOfLeadingDtlsForBlock.get(BLK_NUMBER)))
                                            mapForSourceBlkDetails.putAll(mapOfLeadingDtlsForBlock);
                                        if (block.getNumber().equals(mapOfLeadingDtlsForBlock.get(BLK_NUMBER)))
                                            mapForDesBlkDetails.putAll(mapOfLeadingDtlsForBlock);
                                    }
                                    Map<String, Object> mapForLeadingBlkDetails = new HashMap<>();
                                    // calculating maximum of minimum distance among source and destination blocks
                                    // create a map and assign either source or destination map to it depending upon whose minimum
                                    // distance is greater than other
                                    if (((BigDecimal) mapForSourceBlkDetails.get(MIN_DISTANCE))
                                            .compareTo((BigDecimal) mapForDesBlkDetails.get(MIN_DISTANCE)) > 0)
                                        mapForLeadingBlkDetails = mapForSourceBlkDetails;
                                    else if (((BigDecimal) mapForSourceBlkDetails.get(MIN_DISTANCE))
                                            .compareTo((BigDecimal) mapForDesBlkDetails.get(MIN_DISTANCE)) < 0)
                                        mapForLeadingBlkDetails = mapForDesBlkDetails;
                                    else {
                                        // if minimum distance is same for souce and destination maps
                                        // combining subrules by comma if they are different , but if they are same not taking
                                        // duplicates
                                        if (!mapForSourceBlkDetails.get(SUBRULE).equals(mapForDesBlkDetails.get(SUBRULE))) {
                                            SortedSet<String> uniqueSubrules = new TreeSet<>();
                                            String[] subRuleString = (mapForSourceBlkDetails.get(SUBRULE) + " , "
                                                    + mapForDesBlkDetails.get(SUBRULE)).split(" , ");
                                            for (String str : subRuleString)
                                                uniqueSubrules.add(str);
                                            String subRuleStr = removeDuplicates(uniqueSubrules);
                                            mapForLeadingBlkDetails.put(SUBRULE, subRuleStr);
                                        } else
                                            mapForLeadingBlkDetails.put(SUBRULE, mapForSourceBlkDetails.get(SUBRULE));
                                        mapForLeadingBlkDetails.put(MIN_DISTANCE, mapForDesBlkDetails.get(MIN_DISTANCE));
                                        // combining occupancies by comma if they are different , but if they are same not taking
                                        // duplicates
                                        if (!mapForSourceBlkDetails.get(OCCUPANCY).equals(mapForDesBlkDetails.get(OCCUPANCY))) {
                                            SortedSet<String> uniqueOccupancies = new TreeSet<>();
                                            String[] occupancyString = (mapForSourceBlkDetails.get(OCCUPANCY) + " , "
                                                    + mapForDesBlkDetails.get(OCCUPANCY)).split(" , ");
                                            for (String str : occupancyString)
                                                uniqueOccupancies.add(str);
                                            String occupancyStr = removeDuplicates(uniqueOccupancies);
                                            mapForLeadingBlkDetails.put(OCCUPANCY, occupancyStr);
                                        } else
                                            mapForLeadingBlkDetails.put(OCCUPANCY, mapForSourceBlkDetails.get(OCCUPANCY));
                                    }
                                    BigDecimal minimumDistance;
                                    boolean valid = false;
                                    // calculate minimum of provided distances between source and destination
                                    if (!distanceBetweenBlock.getDistances().isEmpty()) {
                                        minimumDistance = distanceBetweenBlock.getDistances().get(0);
                                        for (BigDecimal distance : distanceBetweenBlock.getDistances())
                                            if (distance.compareTo(minimumDistance) < 0)
                                                minimumDistance = distance;
                                        valid = validateMinimumDistance(minimumDistance,
                                                (BigDecimal) mapForLeadingBlkDetails.get(MIN_DISTANCE), valid);
                                        if (valid) {
                                            pl.reportOutput
                                                    .add(buildRuleOutputWithSubRule(rule,
                                                            (String) mapForLeadingBlkDetails.get(SUBRULE),
                                                            String.format(SUB_RULE_DESCRIPTION, b.getNumber(),
                                                                    block.getNumber(),
                                                                    mapForLeadingBlkDetails.get(OCCUPANCY).toString()),
                                                            DIS_BTW_BLOCKS_DESCRIPTION + " " + b.getNumber() + " and " +
                                                                    block.getNumber() + " for occupancy "
                                                                    + mapForLeadingBlkDetails.get(OCCUPANCY).toString(),
                                                            mapForLeadingBlkDetails.get(MIN_DISTANCE) + DcrConstants.IN_METER,
                                                            minimumDistance +
                                                                    DcrConstants.IN_METER,
                                                            Result.Accepted,
                                                            null));
                                            setReportOutputDetails(pl, (String) mapForLeadingBlkDetails.get(SUBRULE),
                                                    String.format(SUB_RULE_DES, b.getNumber(),
                                                            block.getNumber()),
                                                    mapForLeadingBlkDetails.get(OCCUPANCY).toString(),
                                                    mapForLeadingBlkDetails.get(MIN_DISTANCE).toString() + DcrConstants.IN_METER,
                                                    minimumDistance.toString() + DcrConstants.IN_METER,
                                                    Result.Accepted.getResultVal());
                                        } else {
                                            pl.reportOutput
                                                    .add(buildRuleOutputWithSubRule(rule,
                                                            (String) mapForLeadingBlkDetails.get(SUBRULE),
                                                            String.format(SUB_RULE_DESCRIPTION, b.getNumber(),
                                                                    block.getNumber(),
                                                                    mapForLeadingBlkDetails.get(OCCUPANCY).toString()),
                                                            DIS_BTW_BLOCKS_DESCRIPTION + " " + b.getNumber() + " and " +
                                                                    block.getNumber() + " for occupancy "
                                                                    + mapForLeadingBlkDetails.get(OCCUPANCY).toString(),
                                                            mapForLeadingBlkDetails.get(MIN_DISTANCE) + DcrConstants.IN_METER,
                                                            minimumDistance +
                                                                    DcrConstants.IN_METER,
                                                            Result.Not_Accepted,
                                                            null));
                                            setReportOutputDetails(pl, (String) mapForLeadingBlkDetails.get(SUBRULE),
                                                    String.format(SUB_RULE_DES, b.getNumber(),
                                                            block.getNumber()),
                                                    mapForLeadingBlkDetails.get(OCCUPANCY).toString(),
                                                    mapForLeadingBlkDetails.get(MIN_DISTANCE).toString() + DcrConstants.IN_METER,
                                                    minimumDistance.toString() + DcrConstants.IN_METER,
                                                    Result.Not_Accepted.getResultVal());
                                        }
                                    }
                                }
    }

    private String removeDuplicates(SortedSet<String> uniqueData) {
        StringBuffer str = new StringBuffer();
        List<String> unqList = new ArrayList<>(uniqueData);
        for (String unique : unqList) {
            str.append(unique);
            if (!unique.equals(unqList.get(unqList.size() - 1)))
                str.append(" , ");
        }
        return str.toString();
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDesc, String occupancy, String expected,
            String actual, String status) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(OCCPNCY, occupancy);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

    private boolean getHeightGreaterThanTenAndLessThanSixteenCondition(Block block) {
        return block.getBuilding().getBuildingHeight().compareTo(BigDecimal.valueOf(10)) > 0
                && block.getBuilding().getBuildingHeight().compareTo(BigDecimal.valueOf(16)) < 0;
    }

    private boolean getHeightLessThanTenCondition(Block block) {
        return block.getBuilding().getBuildingHeight().compareTo(BigDecimal.valueOf(10)) <= 0;
    }

    private Boolean validateMinimumDistance(BigDecimal actualDistance, BigDecimal minimumDistance, Boolean valid) {
        if (actualDistance.compareTo(minimumDistance) >= 0)
            valid = true;
        return valid;
    }
}
