package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.COLOUR_CODE_LEACHPIT_TO_PLOT_BNDRY;
import static org.egov.edcr.constants.DxfFileConstants.LAYER_NAME_WASTE_DISPOSAL;
import static org.egov.edcr.utility.DcrConstants.IN_METER;
import static org.egov.edcr.utility.DcrConstants.OBJECTDEFINED_DESC;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED_DESC;
import static org.egov.edcr.utility.DcrConstants.WASTEDISPOSAL;
import static org.egov.edcr.utility.DcrConstants.WASTE_DISPOSAL_DISTANCE_FROMBOUNDARY;
import static org.egov.edcr.utility.DcrConstants.WASTE_DISPOSAL_ERROR_COLOUR_CODE_DISTANCE_FROMBOUNDARY;
import static org.egov.edcr.utility.ParametersConstants.PLOT_AREA;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RoadOutput;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class WasteDisposal extends GeneralRule implements RuleService {
    private static final String SUB_RULE_26A_DESCRIPTION = "Waste Disposal";
    private static final String SUB_RULE_26A = "26(A)";
    private static final String SUB_RULE_104_4_WD = "104(4)";
    private static final String SUB_RULE_104_4_PLOT_DESCRIPTION_WD = "Minimum distance from waste treatment facility like: leach pit,soak pit etc to nearest point on the plot boundary";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // Waste Disposal
        List<DXFLWPolyline> wasterDisposalPolyLines = Util.getPolyLinesByLayer(doc, LAYER_NAME_WASTE_DISPOSAL);
        if (wasterDisposalPolyLines != null && !wasterDisposalPolyLines.isEmpty())
            for (DXFLWPolyline pline : wasterDisposalPolyLines) {
                org.egov.edcr.entity.measurement.WasteDisposal disposal = new org.egov.edcr.entity.measurement.WasteDisposal();
                disposal.setPresentInDxf(true);
                disposal.setPolyLine(pline);
                if (pline.getColor() == 1) {
                    disposal.setType(DcrConstants.EXISTING);
                    pl.getUtility().addWasteDisposal(disposal);
                } else if (pline.getColor() == 2) {
                    disposal.setType(DcrConstants.PROPOSED);
                    pl.getUtility().addWasteDisposal(disposal);
                }
            }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        // waste disposal defined or not
        if (pl != null && pl.getUtility() != null)
            if (pl.getUtility().getLiquidWasteTreatementPlant().isEmpty())
                if (pl.getUtility().getWasteDisposalUnits().isEmpty()) {
                    errors.put(WASTEDISPOSAL,
                            edcrMessageSource.getMessage(OBJECTNOTDEFINED,
                                    new String[] { WASTEDISPOSAL }, LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);
                }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("WASTE_DISPOSAL");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(PLOT_AREA);
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
        scrutinyDetail.setKey("Common_Waste Disposal");
        if (pl.getUtility().getLiquidWasteTreatementPlant().isEmpty())
            if (!pl.getUtility().getWasteDisposalUnits().isEmpty()) {
                pl.reportOutput
                        .add(buildRuleOutputWithSubRule(SUB_RULE_26A_DESCRIPTION, SUB_RULE_26A, SUB_RULE_26A_DESCRIPTION,
                                SUB_RULE_26A_DESCRIPTION,
                                null,
                                null,
                                Result.Accepted, OBJECTDEFINED_DESC));
                setReportOutputDetailsWithoutOccupancy(pl, SUB_RULE_26A, SUB_RULE_26A_DESCRIPTION, "",
                        OBJECTDEFINED_DESC, Result.Accepted.getResultVal());

                if (pl.getUtility().getWells().isEmpty())
                    if (pl.getUtility().getWasteDisposalUnits().size() > 0) {
                        boolean isProposed = pl.getUtility().getWasteDisposalUnits().stream()
                                .anyMatch(wasteDisposal -> wasteDisposal.getType().equalsIgnoreCase(DcrConstants.PROPOSED));
                        if (isProposed)
                            printOutputForProposedWasteDisposal(pl);
                    }

            } else {
                pl.reportOutput.add(buildRuleOutputWithSubRule(SUB_RULE_26A_DESCRIPTION, SUB_RULE_26A, SUB_RULE_26A_DESCRIPTION,
                        SUB_RULE_26A_DESCRIPTION,
                        null,
                        null,
                        Result.Not_Accepted, OBJECTNOTDEFINED_DESC));
                setReportOutputDetailsWithoutOccupancy(pl, SUB_RULE_26A, SUB_RULE_26A_DESCRIPTION, "",
                        OBJECTNOTDEFINED_DESC, Result.Not_Accepted.getResultVal());
            }
        return pl;
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

    private void printOutputForProposedWasteDisposal(PlanDetail pl) {
        String subRule;
        String subRuleDesc;
        boolean valid = false;
        ArrayList<RoadOutput> leachPitToBndryList = new ArrayList();
        BigDecimal minimumDistance;

        for (RoadOutput roadOutput : pl.getUtility().getWellDistance())
            if (checkConditionForLeachPitToBoundary(roadOutput))
                leachPitToBndryList.add(roadOutput);
            else
                continue;

        if (leachPitToBndryList != null && leachPitToBndryList.size() > 0) {
            subRule = SUB_RULE_104_4_WD;
            subRuleDesc = SUB_RULE_104_4_PLOT_DESCRIPTION_WD;
            minimumDistance = BigDecimal.valueOf(1.2);

            RoadOutput roadOutput = leachPitToBndryList.stream()
                    .min(Comparator.comparing(leachToBndry -> leachToBndry.distance)).get();

            printReportOutput(pl, subRule, subRuleDesc, valid, roadOutput, minimumDistance);
        } else {
            HashMap<String, String> errors = new HashMap<>();
            errors.put(WASTE_DISPOSAL_DISTANCE_FROMBOUNDARY + "not defined ",
                    edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                            new String[] { "Distance from the plot boundary to waste disposal" },
                            LocaleContextHolder.getLocale()));
            pl.addErrors(errors);
        }
    }

    private void printReportOutput(PlanDetail pl, String subRule, String subRuleDesc, boolean valid, RoadOutput roadOutput,
            BigDecimal minimumDistance) {
        HashMap<String, String> errors = new HashMap<>();
        if (minimumDistance.compareTo(BigDecimal.ZERO) == 0) {
            errors.put(WASTE_DISPOSAL_DISTANCE_FROMBOUNDARY,
                    prepareMessage(WASTE_DISPOSAL_ERROR_COLOUR_CODE_DISTANCE_FROMBOUNDARY,
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
                                WASTE_DISPOSAL_DISTANCE_FROMBOUNDARY,
                                minimumDistance.toString() + IN_METER,
                                roadOutput.distance + IN_METER,
                                Result.Accepted, null));
                setReportOutputDetailsWithoutOccupancy(pl, subRule, subRuleDesc, minimumDistance.toString() + IN_METER,
                        roadOutput.distance + IN_METER, Result.Accepted.getResultVal());
            } else {
                pl.reportOutput
                        .add(buildRuleOutputWithSubRule(subRule, subRule, subRuleDesc,
                                WASTE_DISPOSAL_DISTANCE_FROMBOUNDARY,
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

}
