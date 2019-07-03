package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.BLOCK_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.BUILDING_FOOT_PRINT;
import static org.egov.edcr.constants.DxfFileConstants.DEPTH_CUTTING;
import static org.egov.edcr.constants.DxfFileConstants.GOVERNMENT_AIDED;
import static org.egov.edcr.constants.DxfFileConstants.HEIGHT_OF_BUILDING;
import static org.egov.edcr.constants.DxfFileConstants.LEVEL_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.MECHANICAL_PARKING;
import static org.egov.edcr.constants.DxfFileConstants.NO_OF_WORKERS;
import static org.egov.edcr.constants.DxfFileConstants.OPENING_ABOVE_2_1_ON_REAR_LESS_1M;
import static org.egov.edcr.constants.DxfFileConstants.OPENING_ABOVE_2_1_ON_SIDE_LESS_1M;
import static org.egov.edcr.constants.DxfFileConstants.PLOT_BOUNDARY;
import static org.egov.edcr.constants.DxfFileConstants.POWER_USED_HP;
import static org.egov.edcr.utility.DcrConstants.MORETHANONEPOLYLINEDEFINED;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.entity.Plot;
import org.egov.edcr.entity.VirtualBuilding;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.utility.SetBack;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.stereotype.Service;

@Service
public class PlanInfoFeature extends GeneralRule implements RuleService {
    public static final String MSG_ERROR_MANDATORY = "msg.error.mandatory.object.not.defined";
    private String digitsRegex = "[^\\d.]";

    private static final BigDecimal ONEHUDREDTWENTYFIVE = BigDecimal.valueOf(125);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        pl.setPlanInformation(extractPlanInfo(pl, doc));

        VirtualBuilding virtualBuilding = new VirtualBuilding();
        Building building = new Building();

        pl.setVirtualBuilding(virtualBuilding);
        pl.setBuilding(building);

        extractPlotDetails(pl, doc);

        extractBuildingFootprint(pl, doc);
        return pl;

    }

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        return planDetail;

    }

    @Override
    public List<String> getLayerNames() {
        return null;
    }

    @Override
    public List<String> getParameters() {
        return null;
    }

    private void extractPlotDetails(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> plotBoundaries = Util.getPolyLinesByLayer(doc, PLOT_BOUNDARY);
        if (!plotBoundaries.isEmpty()) {
            DXFLWPolyline plotBndryPolyLine = plotBoundaries.get(0);
            pl.getPlot().setPolyLine(plotBndryPolyLine);
            pl.getPlot().setPlotBndryArea(Util.getPolyLineArea(plotBndryPolyLine));
        } else
            pl.addError(PLOT_BOUNDARY, getLocaleMessage(OBJECTNOTDEFINED, PLOT_BOUNDARY));
    }

    private void extractBuildingFootprint(PlanDetail pl, DXFDocument doc) {

        List<DXFLWPolyline> polyLinesByLayer;
        String buildingFootPrint = BLOCK_NAME_PREFIX + "\\d+_" + LEVEL_NAME_PREFIX + "\\d+_" + BUILDING_FOOT_PRINT;
        List<String> layerNames = Util.getLayerNamesLike(doc, buildingFootPrint);
        for (String s : layerNames) {
            polyLinesByLayer = Util.getPolyLinesByLayer(doc, s);
            if (polyLinesByLayer.size() > 1) {
                HashMap<String, String> errors = new HashMap<>();
                errors.put(s, prepareMessage(MORETHANONEPOLYLINEDEFINED, s));
                pl.addErrors(errors);
            }
            if (!polyLinesByLayer.isEmpty())
                if (pl.getBlockByName(s.split("_")[1]) == null) {
                    Block block = new Block();
                    block.setName(s.split("_")[1]);
                    block.setNumber(String.valueOf(s.split("_")[1]));
                    SetBack setBack = new SetBack();
                    setBack.setLevel(Integer.valueOf(String.valueOf(s.split("_")[3])));
                    Measurement footPrint = new Measurement();
                    footPrint.setArea(Util.getPolyLineArea(polyLinesByLayer.get(0)));
                    footPrint.setPolyLine(polyLinesByLayer.get(0));
                    footPrint.setPresentInDxf(true);
                    setBack.setBuildingFootPrint(footPrint);
                    block.getSetBacks().add(setBack);
                    pl.getBlocks().add(block);
                } else {
                    Block block = pl.getBlockByName(s.split("_")[1]);
                    block.setName(s.split("_")[1]);
                    block.setNumber(String.valueOf(s.split("_")[1]));

                    SetBack setBack = new SetBack();
                    setBack.setLevel(Integer.valueOf(String.valueOf(s.split("_")[3])));
                    Measurement footPrint = new Measurement();
                    footPrint.setArea(Util.getPolyLineArea(polyLinesByLayer.get(0)));
                    footPrint.setPolyLine(polyLinesByLayer.get(0));
                    footPrint.setPresentInDxf(true);
                    setBack.setBuildingFootPrint(footPrint);
                    block.getSetBacks().add(setBack);

                }

        }

        if (pl.getBlocks().isEmpty())
            pl.addError(DxfFileConstants.BUILDING_FOOT_PRINT, edcrMessageSource.getMessage(
                    DcrConstants.OBJECTNOTDEFINED, new String[] { DxfFileConstants.BUILDING_FOOT_PRINT }, null));

        for (Block b : pl.getBlocks()) {
            String layerName = BLOCK_NAME_PREFIX + b.getNumber() + "_" + HEIGHT_OF_BUILDING;
            BigDecimal height = Util.getSingleDimensionValueByLayer(doc, layerName, pl);
            b.setHeight(height);
            b.getBuilding().setBuildingHeight(height);
            if (height.compareTo(BigDecimal.valueOf(16)) > 0)
                b.setHighRiseBuilding(true);
        }
    }

    private PlanInformation extractPlanInfo(PlanDetail pl, DXFDocument doc) {
        PlanInformation pi = pl.getPlanInformation();
        Map<String, String> planInfoProperties = Util.getFormatedPlanInfoProperties(doc);
        if (planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME) != null)
            pi.setArchitectInformation(planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME));
        String plotArea = planInfoProperties.get(DxfFileConstants.PLOT_AREA);

        if (plotArea == null) {
            Plot plot = new Plot();
            pl.addError(DxfFileConstants.PLOT_AREA,
                    DxfFileConstants.PLOT_AREA + " is not defined in the Plan Information Layer");
            plot.setPresentInDxf(false);
            pl.setPlot(plot);
        } else {
            Plot plot = new Plot();
            plotArea = plotArea.replaceAll(digitsRegex, "");
            BigDecimal numericValue = getNumericValue(plotArea, pl, DxfFileConstants.PLOT_AREA);
            if (numericValue != null) {
                pi.setPlotArea(numericValue);
                plot.setArea(numericValue);
                if (numericValue.compareTo(ONEHUDREDTWENTYFIVE) <= 0)
                    plot.setSmallPlot(true);
            }
            plot.setPresentInDxf(true);
            pl.setPlot(plot);
        }

        String noOfSeats = planInfoProperties.get(DxfFileConstants.SEATS_SP_RESI);
        if (StringUtils.isNotBlank(noOfSeats)) {
            noOfSeats = noOfSeats.replaceAll("[^\\d.]", "");
            if (getNumericValue(noOfSeats, pl, DxfFileConstants.SEATS_SP_RESI) != null)
                pi.setNoOfSeats(getNumericValue(noOfSeats, pl, DxfFileConstants.SEATS_SP_RESI).intValue());
        }
        String noOfMechanicalParking = planInfoProperties.get(DxfFileConstants.MECHANICAL_PARKING);
        if (StringUtils.isNotBlank(noOfMechanicalParking)) {
            noOfMechanicalParking = noOfMechanicalParking.replaceAll("[^\\d.]", "");
            if (getNumericValue(noOfMechanicalParking, pl, DxfFileConstants.MECHANICAL_PARKING) != null)
                pi.setNoOfMechanicalParking(getNumericValue(noOfMechanicalParking, pl, MECHANICAL_PARKING).intValue());
        }

        String powerUsedHp = planInfoProperties.get(POWER_USED_HP);
        if (StringUtils.isNotBlank(powerUsedHp)) {
            powerUsedHp = powerUsedHp.replaceAll("[^\\d.]", "");
            if (getNumericValue(powerUsedHp, pl, POWER_USED_HP) != null)
                pi.setPowerUsedHp(getNumericValue(powerUsedHp, pl, POWER_USED_HP).intValue());
        }

        String numberOfWorkers = planInfoProperties.get(NO_OF_WORKERS);
        if (StringUtils.isNotBlank(numberOfWorkers)) {
            numberOfWorkers = numberOfWorkers.replaceAll("[^\\d.]", "");
            if (getNumericValue(numberOfWorkers, pl, NO_OF_WORKERS) != null)
                pi.setNumberOfWorkers(getNumericValue(numberOfWorkers, pl, NO_OF_WORKERS).intValue());
        }

        String demolitionArea = planInfoProperties.get(DxfFileConstants.EXISTING_FLOOR_AREA_TO_BE_DEMOLISHED);
        if (StringUtils.isNotBlank(demolitionArea)) {
            demolitionArea = demolitionArea.replaceAll(digitsRegex, "");
            if (getNumericValue(demolitionArea, pl, DxfFileConstants.EXISTING_FLOOR_AREA_TO_BE_DEMOLISHED) != null)
                pi.setDemolitionArea(getNumericValue(demolitionArea, pl, DxfFileConstants.EXISTING_FLOOR_AREA_TO_BE_DEMOLISHED));
        }

        if (planInfoProperties.get(DxfFileConstants.SINGLE_FAMILY_BLDG) != null) {
            String value = planInfoProperties.get(DxfFileConstants.SINGLE_FAMILY_BLDG);
            if (value.equalsIgnoreCase(DcrConstants.YES))
                pi.setSingleFamilyBuilding(true);
            else
                pi.setSingleFamilyBuilding(false);
        }

        if (planInfoProperties.get(DxfFileConstants.CRZ_ZONE) != null) {
            String value = planInfoProperties.get(DxfFileConstants.CRZ_ZONE);
            if (value.equalsIgnoreCase(DcrConstants.YES)) {
                pi.setCrzZoneArea(true);
                pi.setCrzZoneDesc(DcrConstants.YES);
            } else if (value.equalsIgnoreCase(DcrConstants.NO)) {
                pi.setCrzZoneArea(false);
                pi.setCrzZoneDesc(DcrConstants.NO);
            } else
                pl.addError(DxfFileConstants.CRZ_ZONE,
                        DxfFileConstants.CRZ_ZONE + " cannot be accepted , should be either YES/NO.");
        } else
            pl.addError(DxfFileConstants.CRZ_ZONE,
                    DxfFileConstants.CRZ_ZONE + " cannot be accepted , should be either YES/NO.");

        if (planInfoProperties.get(DxfFileConstants.SECURITY_ZONE) != null) {
            String securityZone = planInfoProperties.get(DxfFileConstants.SECURITY_ZONE);
            if (securityZone.equalsIgnoreCase(DcrConstants.YES)) {
                pi.setSecurityZone(true);
                pi.setSecurityZoneDesc(DcrConstants.YES);
            } else if (securityZone.equalsIgnoreCase(DcrConstants.NO)) {
                pi.setSecurityZone(false);
                pi.setSecurityZoneDesc(DcrConstants.NO);
            } else
                pl.addError(DxfFileConstants.SECURITY_ZONE,
                        DxfFileConstants.SECURITY_ZONE + " cannot be accepted , should be either YES/NO.");
        } else
            pl.addError(DxfFileConstants.SECURITY_ZONE,
                    DxfFileConstants.SECURITY_ZONE + " cannot be accepted , should be either YES/NO.");

        // Labels changed check
        if (planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_SIDE_LESS_1M) != null) {
            String openingBelow2mside = planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_SIDE_LESS_1M);
            if (openingBelow2mside.equalsIgnoreCase(DcrConstants.YES)) {
                // pi.setOpeningOnSideBelow2mts(true);
                pi.setOpeningOnSide(true);
                pi.setOpeningOnSideBelow2mtsDesc(DcrConstants.YES);
            } else if (openingBelow2mside.equalsIgnoreCase(DcrConstants.NO))
                // pi.setOpeningOnSideBelow2mts(false);
                pi.setOpeningOnSideBelow2mtsDesc(DcrConstants.NO);
            else
                // pi.setOpeningOnSideBelow2mts(null);
                pi.setOpeningOnSideBelow2mtsDesc(DcrConstants.NA);
        } else
            // pi.setOpeningOnSideBelow2mts(null);
            pi.setOpeningOnSideBelow2mtsDesc(DcrConstants.NA);
        // Labels changed check
        if (planInfoProperties.get(OPENING_ABOVE_2_1_ON_REAR_LESS_1M) != null) {
            String openingAbove2mrear = planInfoProperties.get(OPENING_ABOVE_2_1_ON_REAR_LESS_1M);
            if (openingAbove2mrear.equalsIgnoreCase(DcrConstants.YES))
                // pi.setOpeningOnRearAbove2mts(true);
                pi.setOpeningOnRearAbove2mtsDesc(DcrConstants.YES);
            else if (openingAbove2mrear.equalsIgnoreCase(DcrConstants.NO))
                // pi.setOpeningOnRearAbove2mts(false);
                pi.setOpeningOnRearAbove2mtsDesc(DcrConstants.NO);
            else
                // pi.setOpeningOnRearAbove2mts(null);
                pi.setOpeningOnRearAbove2mtsDesc(DcrConstants.NA);
        } else
            // pi.setOpeningOnRearAbove2mts(null);
            pi.setOpeningOnRearAbove2mtsDesc(DcrConstants.NA);
        if (planInfoProperties.get(OPENING_ABOVE_2_1_ON_SIDE_LESS_1M) != null) {
            String openingAbove2mrear = planInfoProperties.get(OPENING_ABOVE_2_1_ON_SIDE_LESS_1M);
            if (openingAbove2mrear.equalsIgnoreCase(DcrConstants.YES)) {
                // pi.setOpeningOnSideAbove2mts(true);
                pi.setOpeningOnSide(true);
                pi.setOpeningOnSideAbove2mtsDesc(DcrConstants.YES);
            } else if (openingAbove2mrear.equalsIgnoreCase(DcrConstants.NO))
                // pi.setOpeningOnSideAbove2mts(false);
                pi.setOpeningOnSideAbove2mtsDesc(DcrConstants.NO);
            else
                // pi.setOpeningOnSideAbove2mts(null);
                pi.setOpeningOnSideAbove2mtsDesc(DcrConstants.NA);
        } else
            // pi.setOpeningOnSideAbove2mts(null);
            pi.setOpeningOnSideAbove2mtsDesc(DcrConstants.NA);
        // Labels changed check
        if (planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_REAR_LESS_1M) != null) {
            String openingBelow2mrear = planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_REAR_LESS_1M);
            if (openingBelow2mrear.equalsIgnoreCase(DcrConstants.YES))
                // pi.setOpeningOnRearBelow2mts(true);
                pi.setOpeningOnRearBelow2mtsDesc(DcrConstants.YES);
            else if (openingBelow2mrear.equalsIgnoreCase(DcrConstants.NO))
                // pi.setOpeningOnRearBelow2mts(false);
                pi.setOpeningOnRearBelow2mtsDesc(DcrConstants.NO);
            else
                // pi.setOpeningOnRearBelow2mts(null);
                pi.setOpeningOnRearBelow2mtsDesc(DcrConstants.NA);
        } else
            // pi.setOpeningOnRearBelow2mts(null);
            pi.setOpeningOnRearBelow2mtsDesc(DcrConstants.NA);

        // Labels changed check
        if (planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_SIDE) != null) {
            String nocAbutSide = planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_SIDE);
            if (nocAbutSide.equalsIgnoreCase(DcrConstants.YES))
                // pi.setNocToAbutSide(true);
                pi.setNocToAbutSideDesc(DcrConstants.YES);
            else if (nocAbutSide.equalsIgnoreCase(DcrConstants.NO))
                // pi.setNocToAbutSide(false);
                pi.setNocToAbutSideDesc(DcrConstants.NO);
            else
                // pi.setNocToAbutSide(null);
                pi.setNocToAbutSideDesc(DcrConstants.NA);
        } else
            // pi.setNocToAbutSide(null);
            pi.setNocToAbutSideDesc(DcrConstants.NA);
        // Labels changed check
        if (planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_REAR) != null) {
            String nocAbutRear = planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_REAR);
            if (nocAbutRear.equalsIgnoreCase(DcrConstants.YES))
                // pi.setNocToAbutRear(true);
                pi.setNocToAbutRearDesc(DcrConstants.YES);
            else if (nocAbutRear.equalsIgnoreCase(DcrConstants.NO))
                // pi.setNocToAbutRear(false);
                pi.setNocToAbutRearDesc(DcrConstants.NO);
            else
                // pi.setNocToAbutRear(null);
                pi.setNocToAbutRearDesc(DcrConstants.NA);
        } else
            // pi.setNocToAbutRear(null);
            pi.setNocToAbutRearDesc(DcrConstants.NA);

        if (planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME) != null)
            pi.setArchitectInformation(planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME));

        if (planInfoProperties.get(DxfFileConstants.RESURVEY_NO) != null)
            pi.setReSurveyNo(planInfoProperties.get(DxfFileConstants.RESURVEY_NO));

        if (planInfoProperties.get(DxfFileConstants.REVENUE_WARD) != null)
            pi.setRevenueWard(planInfoProperties.get(DxfFileConstants.REVENUE_WARD));

        if (planInfoProperties.get(DxfFileConstants.VILLAGE) != null)
            pi.setVillage(planInfoProperties.get(DxfFileConstants.VILLAGE));

        if (planInfoProperties.get(DxfFileConstants.DESAM) != null)
            pi.setDesam(planInfoProperties.get(DxfFileConstants.DESAM));

        if (!planInfoProperties.isEmpty()) {
            String accessWidth = planInfoProperties.get(DxfFileConstants.ACCESS_WIDTH);
            if (accessWidth == null) {

                Set<String> keySet = planInfoProperties.keySet();
                for (String s : keySet)
                    if (s.contains(DxfFileConstants.ACCESS_WIDTH)) {
                        accessWidth = planInfoProperties.get(s);
                        pl.addError(DxfFileConstants.ACCESS_WIDTH,
                                DxfFileConstants.ACCESS_WIDTH + " is invalid .Text in dxf file is " + s);
                    }

            }

            if (accessWidth == null)
                pl.addError(DxfFileConstants.ACCESS_WIDTH, DxfFileConstants.ACCESS_WIDTH + "  Is not defined");
            else {
                accessWidth = accessWidth.replaceAll(digitsRegex, "");

                pi.setAccessWidth(getNumericValue(accessWidth, pl, DxfFileConstants.ACCESS_WIDTH));

            }
        } else
            pi.setAccessWidth(BigDecimal.ZERO);

        if (planInfoProperties.get(DxfFileConstants.DEPTH_CUTTING) != null) {
            String depthCutting = planInfoProperties.get(DxfFileConstants.DEPTH_CUTTING);
            if (depthCutting.equalsIgnoreCase(DcrConstants.YES)) {
                pi.setDepthCutting(true);
                pi.setDepthCuttingDesc(DcrConstants.YES);
            } else if (depthCutting.equalsIgnoreCase(DcrConstants.NO)) {
                pi.setDepthCutting(false);
                pi.setDepthCuttingDesc(DcrConstants.NO);
            } else
                pl.addError(DxfFileConstants.DEPTH_CUTTING,
                        DxfFileConstants.DEPTH_CUTTING + " cannot be accepted , should be either YES/NO.");
        } else
            pl.addError(DEPTH_CUTTING, prepareMessage(OBJECTNOTDEFINED, DEPTH_CUTTING + " of PLAN_INFO layer"));

        if (planInfoProperties.get(DxfFileConstants.GOVERNMENT_AIDED) != null) {
            String governmentAided = planInfoProperties.get(DxfFileConstants.GOVERNMENT_AIDED);
            if (governmentAided.equalsIgnoreCase(DcrConstants.YES))
                pi.setGovernmentOrAidedSchool(true);
            else if (governmentAided.equalsIgnoreCase(DcrConstants.NO))
                pi.setGovernmentOrAidedSchool(false);
            else
                pl.addError(DxfFileConstants.GOVERNMENT_AIDED,
                        DxfFileConstants.GOVERNMENT_AIDED + " cannot be accepted , should be either YES/NO.");
        } else
            pl.addError(GOVERNMENT_AIDED, prepareMessage(OBJECTNOTDEFINED, GOVERNMENT_AIDED + " of PLAN_INFO layer"));

        if (planInfoProperties.get(DxfFileConstants.PLOT_IN_COMMERCIAL_ZONE) != null) {
            String plotInCommZone = planInfoProperties.get(DxfFileConstants.PLOT_IN_COMMERCIAL_ZONE);
            if (plotInCommZone.equalsIgnoreCase(DcrConstants.YES))
                pi.setPlotInCommercialZone(DcrConstants.YES);
            else if (plotInCommZone.equalsIgnoreCase(DcrConstants.NO))
                pi.setPlotInCommercialZone(DcrConstants.NO);
            else
                pi.setPlotInCommercialZone(DcrConstants.NA);
        } else
            pi.setPlotInCommercialZone(DcrConstants.NA);

        if (planInfoProperties.get(DxfFileConstants.COMMERCIAL_ZONE_BLDG_OPENING_ON_SIDE1) != null) {
            String commZoneBldOpenOnSide1 = planInfoProperties.get(DxfFileConstants.COMMERCIAL_ZONE_BLDG_OPENING_ON_SIDE1);
            if (commZoneBldOpenOnSide1.equalsIgnoreCase(DcrConstants.YES))
                pi.setCommercialZoneBldgOpenOnSide1(DcrConstants.YES);
            else if (commZoneBldOpenOnSide1.equalsIgnoreCase(DcrConstants.NO))
                pi.setCommercialZoneBldgOpenOnSide1(DcrConstants.NO);
            else
                pi.setCommercialZoneBldgOpenOnSide1(DcrConstants.NA);
        } else
            pi.setCommercialZoneBldgOpenOnSide1(DcrConstants.NA);
        if (planInfoProperties.get(DxfFileConstants.COMMERCIAL_ZONE_BLDG_OPENING_ON_SIDE2) != null) {
            String commZoneBldOpenOnSide2 = planInfoProperties.get(DxfFileConstants.COMMERCIAL_ZONE_BLDG_OPENING_ON_SIDE2);
            if (commZoneBldOpenOnSide2.equalsIgnoreCase(DcrConstants.YES))
                pi.setCommercialZoneBldgOpenOnSide2(DcrConstants.YES);
            else if (commZoneBldOpenOnSide2.equalsIgnoreCase(DcrConstants.NO))
                pi.setCommercialZoneBldgOpenOnSide2(DcrConstants.NO);
            else
                pi.setCommercialZoneBldgOpenOnSide2(DcrConstants.NA);
        } else
            pi.setCommercialZoneBldgOpenOnSide2(DcrConstants.NA);

        String noOfBeds = planInfoProperties.get(DxfFileConstants.NO_OF_BEDS);
        if (StringUtils.isNotBlank(noOfBeds)) {
            noOfBeds = noOfBeds.replaceAll(digitsRegex, "");
            if (getNumericValue(noOfBeds, pl, DxfFileConstants.NO_OF_BEDS.toString()) != null)
                pi.setNoOfBeds(BigDecimal.valueOf(Integer.valueOf(noOfBeds)));
        }
        return pi;
    }

}
