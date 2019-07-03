package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.COLOUR_CODE_CULDESAC;
import static org.egov.edcr.constants.DxfFileConstants.COLOUR_CODE_LANE;
import static org.egov.edcr.constants.DxfFileConstants.COLOUR_CODE_LEACHPIT_TO_PLOT_BNDRY;
import static org.egov.edcr.constants.DxfFileConstants.COLOUR_CODE_NONNOTIFIEDROAD;
import static org.egov.edcr.constants.DxfFileConstants.COLOUR_CODE_NOTIFIEDROAD;
import static org.egov.edcr.constants.DxfFileConstants.COLOUR_CODE_WELLTOBOUNDARY;
import static org.egov.edcr.constants.DxfFileConstants.COLOUR_CODE_WELLTOLEACHPIT;
import static org.egov.edcr.constants.DxfFileConstants.DIST_WELL;
import static org.egov.edcr.constants.DxfFileConstants.LAYER_NAME_WELL;
import static org.egov.edcr.utility.DcrConstants.IN_METER;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.WELL_DISTANCE_FROMBOUNDARY;
import static org.egov.edcr.utility.DcrConstants.WELL_ERROR_COLOUR_CODE_DISTANCE_FROMROAD;
import static org.egov.edcr.utility.ParametersConstants.COLOR_CODE;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RoadOutput;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.utility.WellUtility;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFMText;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Well extends GeneralRule implements RuleService {

    private static final String MSG_ERROR_WRONG_COLOR = "msg.error.color.well.pro.exst";
    private static final String NOT_DEFINED_IN_PROPER_COLOR_CODE = "Not defined in proper color code";
    private static final String SUB_RULE_104_4_PLOT_DESCRIPTION = "Minimum distance from %s waste treatment facility like: leach pit,soak pit etc to nearest point on the plot boundary";
    private static final String WELL_DISTANCE_FROM_ROAD = "Minimum distance from %s well to road";
    private static final String SUB_RULE_104_1_DESCRIPTION = "Open well: Minimum distance between street boundary and the %s well ";
    private static final String SUB_RULE_104_2_DESCRIPTION = "Minimum distance from %s well to nearest point on plot boundary";
    private static final String SUB_RULE_104_4_DESCRIPTION = "Minimum distance from %s well to nearest point on leach pit, soak pit, refuse pit, earth closet or septic tanks ";

    private static final String SUB_RULE_104_1 = "104(1)";
    private static final String SUB_RULE_104_2 = "104(2)";
    private static final String SUB_RULE_104_4 = "104(4)";

    private static final BigDecimal three = BigDecimal.valueOf(3);
    private static final BigDecimal TWO_MTR = BigDecimal.valueOf(2);
    private static final BigDecimal ONE_ANDHALF_MTR = BigDecimal.valueOf(1.5);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // Well Utility
        if (doc.containsDXFLayer(LAYER_NAME_WELL)) {
            List<DXFCircle> wellCircle = Util.getPolyCircleByLayer(doc, LAYER_NAME_WELL);
            List<DXFLWPolyline> wellPolygon = new ArrayList<>();
            if (wellCircle.isEmpty() || wellCircle == null)
                wellPolygon = Util.getPolyLinesByLayer(doc, LAYER_NAME_WELL);
            if (wellCircle != null && !wellCircle.isEmpty()) {
                for (DXFCircle circle : wellCircle)
                    if (circle.getColor() == 1 || circle.getColor() == 2) {
                        WellUtility well = new WellUtility();
                        well.setPresentInDxf(true);
                        well.setCircle(circle);
                        if (circle.getColor() == 1)
                            well.setType("Existing");
                        else if (circle.getColor() == 2)
                            well.setType("Proposed");
                        pl.getUtility().addWells(well);
                    } else
                        pl.addError(NOT_DEFINED_IN_PROPER_COLOR_CODE,
                                edcrMessageSource.getMessage(MSG_ERROR_WRONG_COLOR, new String[] {
                                        String.format(String.format("%.4f", circle.getLength())) },
                                        LocaleContextHolder.getLocale()));
            } else if (wellPolygon != null && !wellPolygon.isEmpty())
                for (DXFLWPolyline polygon : wellPolygon)
                    if (polygon.getColor() == 1 || polygon.getColor() == 2) {
                        WellUtility well = new WellUtility();
                        well.setPresentInDxf(true);
                        well.setPolygon(polygon);
                        if (polygon.getColor() == 1)
                            well.setType(DcrConstants.EXISTING);
                        else if (polygon.getColor() == 2)
                            well.setType(DcrConstants.PROPOSED);
                        pl.getUtility().addWells(well);
                    } else
                        pl.addError(NOT_DEFINED_IN_PROPER_COLOR_CODE,
                                edcrMessageSource.getMessage(MSG_ERROR_WRONG_COLOR, new String[] {
                                        String.format(String.format("%.4f", polygon.getLength())) },
                                        LocaleContextHolder.getLocale()));

        }

        List<DXFDimension> distanceFromWell = Util.getDimensionsByLayer(doc,
                DIST_WELL);
        if (distanceFromWell != null && !distanceFromWell.isEmpty()) {
            List<RoadOutput> distFrmWellWithColor = extractDistanceWithColourCode(doc, distanceFromWell);
            if (!distFrmWellWithColor.isEmpty())
                pl.getUtility().setWellDistance(distFrmWellWithColor);
        }

        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && pl.getUtility() != null && !pl.getUtility().getWells().isEmpty()) {
            List<String> wellType = pl.getUtility().getWells().stream()
                    .map(org.egov.edcr.entity.utility.WellUtility::getType).collect(Collectors.toList());
            boolean proposedWell = false;
            boolean existingWell = false;
            if (!wellType.isEmpty() && wellType.get(0) != null) {
                proposedWell = wellType.stream().anyMatch(wd -> wd.equalsIgnoreCase(DcrConstants.PROPOSED));
                existingWell = wellType.stream().anyMatch(wd -> wd.equalsIgnoreCase(DcrConstants.EXISTING));
            }

            if (!pl.getUtility().getWasteDisposalUnits().isEmpty()) {
                List<String> wdType = pl.getUtility().getWasteDisposalUnits().stream()
                        .map(org.egov.edcr.entity.measurement.WasteDisposal::getType).collect(Collectors.toList());
                boolean proposedWasteDisposal = false;
                boolean existingWasteDisposal = false;
                if (!wdType.isEmpty() && wdType.get(0) != null) {
                    proposedWasteDisposal = wdType.stream()
                            .anyMatch(wd -> wd.equalsIgnoreCase(DcrConstants.PROPOSED));
                    existingWasteDisposal = wdType.stream()
                            .anyMatch(wd -> wd.equalsIgnoreCase(DcrConstants.EXISTING));
                }

                if (pl.getUtility().getWells().get(0).getType() != null
                        && pl.getUtility().getWasteDisposalUnits().get(0).getType() != null)
                    if (proposedWell && proposedWasteDisposal) {
                        if (pl.getUtility().getWellDistance().stream()
                                .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(7)))) {
                            errors.put(SUB_RULE_104_2_DESCRIPTION,
                                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                            String.format(SUB_RULE_104_2_DESCRIPTION, DcrConstants.PROPOSED) },
                                            LocaleContextHolder.getLocale()));
                            pl.addErrors(errors);
                        }
                        if (pl.getUtility().getWellDistance().stream()
                                .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(8)))) {
                            errors.put(SUB_RULE_104_4_DESCRIPTION,
                                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                            String.format(SUB_RULE_104_4_DESCRIPTION, DcrConstants.PROPOSED) },
                                            LocaleContextHolder.getLocale()));
                            pl.addErrors(errors);
                        }
                        if (pl.getUtility().getWellDistance().stream()
                                .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(9)))) {
                            errors.put(SUB_RULE_104_4_PLOT_DESCRIPTION,
                                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                            String.format(SUB_RULE_104_4_PLOT_DESCRIPTION, proposedWasteDisposal) },
                                            LocaleContextHolder.getLocale()));
                            pl.addErrors(errors);
                        }
                        if (pl.getUtility().getWellDistance().stream()
                                .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(1))
                                        || roadOutput.colourCode.equals(String.valueOf(2))
                                        || roadOutput.colourCode.equals(String.valueOf(5)) ||
                                        roadOutput.colourCode.equals(String.valueOf(6)))) {
                            errors.put(WELL_DISTANCE_FROM_ROAD,
                                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                            String.format(WELL_DISTANCE_FROM_ROAD, DcrConstants.PROPOSED) },
                                            LocaleContextHolder.getLocale()));
                            pl.addErrors(errors);
                        }
                    } else if (proposedWell && existingWasteDisposal) {
                        if (pl.getUtility().getWellDistance().stream()
                                .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(7)))) {
                            errors.put(SUB_RULE_104_2_DESCRIPTION,
                                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                            String.format(SUB_RULE_104_2_DESCRIPTION, DcrConstants.PROPOSED) },
                                            LocaleContextHolder.getLocale()));
                            pl.addErrors(errors);
                        }
                        if (pl.getUtility().getWellDistance().stream()
                                .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(8)))) {
                            errors.put(SUB_RULE_104_4_DESCRIPTION,
                                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                            String.format(SUB_RULE_104_4_DESCRIPTION, DcrConstants.PROPOSED) },
                                            LocaleContextHolder.getLocale()));
                            pl.addErrors(errors);
                        }
                        if (pl.getUtility().getWellDistance().stream()
                                .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(1))
                                        || roadOutput.colourCode.equals(String.valueOf(2))
                                        || roadOutput.colourCode.equals(String.valueOf(5)) ||
                                        roadOutput.colourCode.equals(String.valueOf(6)))) {
                            errors.put(WELL_DISTANCE_FROM_ROAD,
                                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                            String.format(WELL_DISTANCE_FROM_ROAD, DcrConstants.PROPOSED) },
                                            LocaleContextHolder.getLocale()));
                            pl.addErrors(errors);
                        }

                    } else if (existingWell && proposedWasteDisposal) {
                        if (pl.getUtility().getWellDistance().stream()
                                .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(8)))) {
                            errors.put(SUB_RULE_104_4_DESCRIPTION,
                                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                            String.format(SUB_RULE_104_4_DESCRIPTION, DcrConstants.EXISTING) },
                                            LocaleContextHolder.getLocale()));
                            pl.addErrors(errors);
                        }
                        if (pl.getUtility().getWellDistance().stream()
                                .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(9)))) {
                            errors.put(SUB_RULE_104_4_PLOT_DESCRIPTION,
                                    edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                            String.format(SUB_RULE_104_4_PLOT_DESCRIPTION, DcrConstants.PROPOSED) },
                                            LocaleContextHolder.getLocale()));
                            pl.addErrors(errors);
                        }
                    }

            } else if (proposedWell) {
                if (pl.getUtility().getWellDistance().stream()
                        .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(7)))) {
                    errors.put(SUB_RULE_104_2_DESCRIPTION,
                            edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                    String.format(SUB_RULE_104_2_DESCRIPTION, DcrConstants.PROPOSED) },
                                    LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);
                }
                if (pl.getUtility().getWellDistance().stream()
                        .noneMatch(roadOutput -> roadOutput.colourCode.equals(String.valueOf(1))
                                || roadOutput.colourCode.equals(String.valueOf(2))
                                || roadOutput.colourCode.equals(String.valueOf(5)) ||
                                roadOutput.colourCode.equals(String.valueOf(6)))) {
                    errors.put(WELL_DISTANCE_FROM_ROAD,
                            edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                    String.format(WELL_DISTANCE_FROM_ROAD, DcrConstants.PROPOSED) },
                                    LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);
                }
            }
        }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("WELL");
        layers.add("DIST_WELL");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        parameters.add(COLOR_CODE);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.setKey("Common_Well");
        if (!pl.getUtility().getWells().isEmpty()) {

            List<String> wellType = pl.getUtility().getWells().stream()
                    .map(org.egov.edcr.entity.utility.WellUtility::getType).collect(Collectors.toList());
            boolean proposedWell = false;
            boolean existingWell = false;
            if (!wellType.isEmpty() && wellType.get(0) != null) {
                proposedWell = wellType.stream().anyMatch(wd -> wd.equalsIgnoreCase(DcrConstants.PROPOSED));
                existingWell = wellType.stream().anyMatch(wd -> wd.equalsIgnoreCase(DcrConstants.EXISTING));
            }

            if (!pl.getUtility().getWasteDisposalUnits().isEmpty()) {

                List<String> wdType = pl.getUtility().getWasteDisposalUnits().stream()
                        .map(org.egov.edcr.entity.measurement.WasteDisposal::getType).collect(Collectors.toList());
                boolean proposedWasteDisposal = false;
                boolean existingWasteDisposal = false;
                if (!wdType.isEmpty() && wdType.get(0) != null) {
                    proposedWasteDisposal = wdType.stream()
                            .anyMatch(wd -> wd.equalsIgnoreCase(DcrConstants.PROPOSED));
                    existingWasteDisposal = wdType.stream()
                            .anyMatch(wd -> wd.equalsIgnoreCase(DcrConstants.EXISTING));
                }

                if (proposedWell) {
                    if (proposedWasteDisposal)
                        printOutputForProposedWellAndProposedWasteDisposal(pl, DcrConstants.PROPOSED, DcrConstants.PROPOSED);
                    if (existingWasteDisposal)
                        printOutputForProposedWellAndExistingWasteDisposal(pl, DcrConstants.PROPOSED);
                }
                if (existingWell && proposedWasteDisposal)
                    printOutputForExistingWellAndProposedWasteDisposal(pl, DcrConstants.EXISTING, DcrConstants.PROPOSED);
            } else if (proposedWell)
                printOutputForProposedWellWithNoWasteDisposalDefined(pl, DcrConstants.PROPOSED);
        }
        return pl;
    }

    private void printOutputForProposedWellWithNoWasteDisposalDefined(PlanDetail pl, String wellType) {
        String subRule = null;
        String subRuleDesc = null;
        boolean valid = false;
        for (RoadOutput roadOutput : pl.getUtility().getWellDistance()) {

            BigDecimal minimumDistance = BigDecimal.ZERO;
            if (checkConditionForNotifiedNonNotifiedRoad(roadOutput)) {
                minimumDistance = three;
                subRule = SUB_RULE_104_1;
                subRuleDesc = String.format(SUB_RULE_104_1_DESCRIPTION, wellType);
            } else if (checkConditionForCuldesacRoad(roadOutput)) {
                minimumDistance = TWO_MTR;
                subRule = SUB_RULE_104_1;
                subRuleDesc = String.format(SUB_RULE_104_1_DESCRIPTION, wellType);
            } else if (checkConditionForLane(roadOutput)) {
                minimumDistance = ONE_ANDHALF_MTR;
                subRule = SUB_RULE_104_1;
                subRuleDesc = String.format(SUB_RULE_104_1_DESCRIPTION, wellType);
            } else if (checkConditionForBoundary(roadOutput)) {
                subRule = SUB_RULE_104_2;
                subRuleDesc = String.format(SUB_RULE_104_2_DESCRIPTION, wellType);
                minimumDistance = ONE_ANDHALF_MTR;
            } else
                continue;
            printReportOutput(pl, subRule, subRuleDesc, valid, roadOutput, minimumDistance);
        }
    }

    private void printOutputForExistingWellAndProposedWasteDisposal(PlanDetail pl, String wellType, String wdType) {
        String subRule = null;
        String subRuleDesc = null;
        boolean valid = false;
        for (RoadOutput roadOutput : pl.getUtility().getWellDistance()) {

            BigDecimal minimumDistance = BigDecimal.ZERO;
            if (checkConditionForWellToLeachPit(roadOutput)) {
                subRule = SUB_RULE_104_4;
                subRuleDesc = String.format(SUB_RULE_104_4_DESCRIPTION, wellType);
                minimumDistance = BigDecimal.valueOf(7.5);
            } else if (checkConditionForLeachPitToBoundary(roadOutput)) {
                subRule = SUB_RULE_104_4;
                subRuleDesc = String.format(SUB_RULE_104_4_PLOT_DESCRIPTION, wdType);
                minimumDistance = BigDecimal.valueOf(1.2);
            } else
                continue;
            printReportOutput(pl, subRule, subRuleDesc, valid, roadOutput, minimumDistance);

        }
    }

    private void printOutputForProposedWellAndExistingWasteDisposal(PlanDetail pl, String wellType) {
        String subRule = null;
        String subRuleDesc = null;
        boolean valid = false;
        for (RoadOutput roadOutput : pl.getUtility().getWellDistance()) {

            BigDecimal minimumDistance = BigDecimal.ZERO;
            if (checkConditionForNotifiedNonNotifiedRoad(roadOutput)) {
                minimumDistance = three;
                subRule = SUB_RULE_104_1;
                subRuleDesc = String.format(SUB_RULE_104_1_DESCRIPTION, wellType);
            } else if (checkConditionForCuldesacRoad(roadOutput)) {
                minimumDistance = TWO_MTR;
                subRule = SUB_RULE_104_1;
                subRuleDesc = String.format(SUB_RULE_104_1_DESCRIPTION, wellType);
            } else if (checkConditionForLane(roadOutput)) {
                minimumDistance = ONE_ANDHALF_MTR;
                subRule = SUB_RULE_104_1;
                subRuleDesc = String.format(SUB_RULE_104_1_DESCRIPTION, wellType);
            } else if (checkConditionForBoundary(roadOutput)) {
                subRule = SUB_RULE_104_2;
                subRuleDesc = String.format(SUB_RULE_104_2_DESCRIPTION, wellType);
                minimumDistance = ONE_ANDHALF_MTR;
            } else if (checkConditionForWellToLeachPit(roadOutput)) {
                subRule = SUB_RULE_104_4;
                subRuleDesc = String.format(SUB_RULE_104_4_DESCRIPTION, wellType);
                minimumDistance = BigDecimal.valueOf(7.5);
            } else
                continue;
            printReportOutput(pl, subRule, subRuleDesc, valid, roadOutput, minimumDistance);
        }
    }

    private void printOutputForProposedWellAndProposedWasteDisposal(PlanDetail pl, String wellType, String wdType) {
        String subRule = null;
        String subRuleDesc = null;
        boolean valid = false;
        for (RoadOutput roadOutput : pl.getUtility().getWellDistance()) {

            BigDecimal minimumDistance = BigDecimal.ZERO;
            if (checkConditionForNotifiedNonNotifiedRoad(roadOutput)) {
                minimumDistance = three;
                subRule = SUB_RULE_104_1;
                subRuleDesc = String.format(SUB_RULE_104_1_DESCRIPTION, wellType);
            } else if (checkConditionForCuldesacRoad(roadOutput)) {
                minimumDistance = TWO_MTR;
                subRule = SUB_RULE_104_1;
                subRuleDesc = String.format(SUB_RULE_104_1_DESCRIPTION, wellType);
            } else if (checkConditionForLane(roadOutput)) {
                minimumDistance = ONE_ANDHALF_MTR;
                subRule = SUB_RULE_104_1;
                subRuleDesc = String.format(SUB_RULE_104_1_DESCRIPTION, wellType);
            } else if (checkConditionForBoundary(roadOutput)) {
                subRule = SUB_RULE_104_2;
                subRuleDesc = String.format(SUB_RULE_104_2_DESCRIPTION, wellType);
                minimumDistance = ONE_ANDHALF_MTR;
            } else if (checkConditionForWellToLeachPit(roadOutput)) {
                subRule = SUB_RULE_104_4;
                subRuleDesc = String.format(SUB_RULE_104_4_DESCRIPTION, wellType);
                minimumDistance = BigDecimal.valueOf(7.5);
            } else if (checkConditionForLeachPitToBoundary(roadOutput)) {
                subRule = SUB_RULE_104_4;
                subRuleDesc = String.format(SUB_RULE_104_4_PLOT_DESCRIPTION, wdType);
                minimumDistance = BigDecimal.valueOf(1.2);
            } else
                continue;
            printReportOutput(pl, subRule, subRuleDesc, valid, roadOutput, minimumDistance);

        }
    }

    private void printReportOutput(PlanDetail pl, String subRule, String subRuleDesc, boolean valid, RoadOutput roadOutput,
            BigDecimal minimumDistance) {
        HashMap<String, String> errors = new HashMap<>();
        if (minimumDistance.compareTo(BigDecimal.ZERO) == 0) {
            errors.put(WELL_DISTANCE_FROMBOUNDARY,
                    prepareMessage(WELL_ERROR_COLOUR_CODE_DISTANCE_FROMROAD,
                            roadOutput.distance != null ? roadOutput.distance.toString()
                                    : ""));
            pl.addErrors(errors);
        } else {
            if (roadOutput.distance != null &&
                    roadOutput.distance.compareTo(BigDecimal.ZERO) > 0
                    && roadOutput.distance.compareTo(minimumDistance) >= 0)
                valid = true;
            if (valid) {
                pl.reportOutput
                        .add(buildRuleOutputWithSubRule(subRule, subRule, subRuleDesc,
                                WELL_DISTANCE_FROMBOUNDARY,
                                minimumDistance.toString() + IN_METER,
                                roadOutput.distance + IN_METER,
                                Result.Accepted, null));
                setReportOutputDetailsWithoutOccupancy(pl, subRule, subRuleDesc, minimumDistance.toString() + IN_METER,
                        roadOutput.distance + IN_METER, Result.Accepted.getResultVal());
            } else {
                pl.reportOutput
                        .add(buildRuleOutputWithSubRule(subRule, subRule, subRuleDesc,
                                WELL_DISTANCE_FROMBOUNDARY,
                                minimumDistance.toString() + IN_METER,
                                roadOutput.distance + IN_METER,
                                Result.Not_Accepted, null));
                setReportOutputDetailsWithoutOccupancy(pl, subRule, subRuleDesc, minimumDistance.toString() + IN_METER,
                        roadOutput.distance + IN_METER, Result.Not_Accepted.getResultVal());
            }
        }

    }

    private boolean checkConditionForLeachPitToBoundary(RoadOutput roadOutput) {
        return Integer.valueOf(roadOutput.colourCode) == COLOUR_CODE_LEACHPIT_TO_PLOT_BNDRY;
    }

    private boolean checkConditionForWellToLeachPit(RoadOutput roadOutput) {
        return Integer.valueOf(roadOutput.colourCode) == COLOUR_CODE_WELLTOLEACHPIT;
    }

    private boolean checkConditionForBoundary(RoadOutput roadOutput) {
        return Integer.valueOf(roadOutput.colourCode) == COLOUR_CODE_WELLTOBOUNDARY;
    }

    private boolean checkConditionForLane(RoadOutput roadOutput) {
        return Integer.valueOf(roadOutput.colourCode) == COLOUR_CODE_LANE;
    }

    private boolean checkConditionForCuldesacRoad(RoadOutput roadOutput) {
        return Integer.valueOf(roadOutput.colourCode) == COLOUR_CODE_CULDESAC;
    }

    private boolean checkConditionForNotifiedNonNotifiedRoad(RoadOutput roadOutput) {
        return Integer.valueOf(roadOutput.colourCode) == COLOUR_CODE_NOTIFIEDROAD ||
                Integer.valueOf(roadOutput.colourCode) == COLOUR_CODE_NONNOTIFIEDROAD;
    }

    private List<RoadOutput> extractDistanceWithColourCode(DXFDocument doc,
            List<DXFDimension> shortestDistanceCentralLineRoadDimension) {
        List<RoadOutput> shortDistainceFromCenter = new ArrayList<>();

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
                        String text2 = text.getText();
                        if (text2.contains(";"))
                            text2 = text2.split(";")[1].replaceAll("[^\\d.]", "");
                        else
                            text2 = text2.replaceAll("[^\\d.]", "");
                        if (!text2.isEmpty()) {
                            value = BigDecimal.valueOf(Double.parseDouble(text2));
                            RoadOutput roadOutput = new RoadOutput();
                            roadOutput.distance = value;
                            roadOutput.colourCode = String.valueOf(line.getColor());
                            shortDistainceFromCenter.add(roadOutput);
                        }

                    }
                }

            }
        return shortDistainceFromCenter;
    }

    private void setReportOutputDetailsWithoutOccupancy(PlanDetail pl, String ruleNo, String ruleDesc, String expected,
            String actual, String status) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

}
