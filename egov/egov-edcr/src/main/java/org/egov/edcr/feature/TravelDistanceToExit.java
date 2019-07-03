package org.egov.edcr.feature;

import static org.egov.edcr.utility.ParametersConstants.FLOOR_COUNT;
import static org.egov.edcr.utility.ParametersConstants.PLOT_AREA;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TravelDistanceToExit extends GeneralRule implements RuleService {

    private static final String SUBRULE_43_2 = "43(2)";
    private static final String SUBRULE_43_2_DESC = "Maximum travel distance to emergency exit";
    public static final BigDecimal VAL_30 = BigDecimal.valueOf(30);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        if (pl != null) {
            String layerName = DxfFileConstants.LAYER_TRAVEL_DIST_TO_EXIT;
            List<BigDecimal> travelDistanceDimensions = Util.getListOfDimensionValueByLayer(doc, layerName);
            if (!travelDistanceDimensions.isEmpty() && travelDistanceDimensions != null)
                pl.setTravelDistancesToExit(travelDistanceDimensions);
        }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null)
            if (pl.getTravelDistancesToExit().isEmpty()) {
                errors.put(DcrConstants.TRAVEL_DIST_EXIT,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { DcrConstants.TRAVEL_DIST_EXIT },
                                LocaleContextHolder.getLocale()));
                pl.addErrors(errors);
            }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("DIST_EXIT");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(FLOOR_COUNT);
        parameters.add(PLOT_AREA);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        Boolean exemption = Boolean.FALSE;
        if (pl != null && pl.getVirtualBuilding() != null && !pl.getVirtualBuilding().getOccupancies().isEmpty()
                && !pl.getBlocks().isEmpty()) {
            boolean numberofFloorsLessOrEqualToThree = true;
            for (Block block : pl.getBlocks())
                if (!block.getCompletelyExisting()) {

                    if (block.getBuilding() != null && block.getBuilding().getFloorsAboveGround() != null &&
                            block.getBuilding().getFloorsAboveGround().compareTo(BigDecimal.valueOf(3)) > 0)
                        numberofFloorsLessOrEqualToThree = false;

                    if (block.getResidentialBuilding().equals(Boolean.TRUE)
                            && numberofFloorsLessOrEqualToThree == true)
                        exemption = Boolean.TRUE;
                }
            if (Util.isSmallPlot(pl))
                exemption = Boolean.TRUE;
        }
        if (!exemption) {
            validate(pl);
            String subRule = SUBRULE_43_2;
            String subRuleDesc = SUBRULE_43_2_DESC;
            scrutinyDetail = new ScrutinyDetail();
            scrutinyDetail.setKey("Common_Travel Distance To Emergency Exits");
            scrutinyDetail.addColumnHeading(1, RULE_NO);
            scrutinyDetail.addColumnHeading(2, REQUIRED);
            scrutinyDetail.addColumnHeading(3, PROVIDED);
            scrutinyDetail.addColumnHeading(4, STATUS);
            scrutinyDetail.setSubHeading(SUBRULE_43_2_DESC);
            if (pl != null)
                for (BigDecimal maximumTravelDistance : pl.getTravelDistancesToExit()) {
                    boolean valid = false;
                    if (maximumTravelDistance.compareTo(VAL_30) <= 0)
                        valid = true;
                    if (valid) {
                        pl.reportOutput
                                .add(buildRuleOutputWithSubRule(subRuleDesc,
                                        subRule,
                                        subRuleDesc,
                                        subRuleDesc,
                                        VAL_30 + DcrConstants.IN_METER,
                                        maximumTravelDistance + DcrConstants.IN_METER, Result.Accepted,
                                        null));
                        setReportOutputDetails(pl, subRule, VAL_30 + DcrConstants.IN_METER,
                                maximumTravelDistance + DcrConstants.IN_METER, Result.Accepted.getResultVal());
                    } else {
                        pl.reportOutput
                                .add(buildRuleOutputWithSubRule(subRuleDesc,
                                        subRule,
                                        subRuleDesc,
                                        subRuleDesc,
                                        VAL_30 + DcrConstants.IN_METER,
                                        maximumTravelDistance + DcrConstants.IN_METER, Result.Not_Accepted,
                                        null));
                        setReportOutputDetails(pl, subRule, VAL_30 + DcrConstants.IN_METER,
                                maximumTravelDistance + DcrConstants.IN_METER, Result.Not_Accepted.getResultVal());

                    }
                }
        }

        return pl;
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String expected, String actual, String status) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

}
