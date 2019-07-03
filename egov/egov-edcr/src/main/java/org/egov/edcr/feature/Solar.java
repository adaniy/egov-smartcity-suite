package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.SOLAR;
import static org.egov.edcr.utility.DcrConstants.OBJECTDEFINED_DESC;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED_DESC;
import static org.egov.edcr.utility.DcrConstants.RULE109;
import static org.egov.edcr.utility.DcrConstants.SOLAR_SYSTEM;
import static org.egov.edcr.utility.ParametersConstants.BUILT_UP_AREA;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Solar extends GeneralRule implements RuleService {
    private static final String SUB_RULE_109_C_DESCRIPTION = "Solar Assisted Water Heating / Lighting system ";
    private static final String SUB_RULE_109_C = "109(C)";
    private static final BigDecimal FOURHUNDRED = BigDecimal.valueOf(400);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // Solar Water Heating Utility
        if (doc.containsDXFLayer(SOLAR)) {
            List<DXFLWPolyline> solarPolyline = Util.getPolyLinesByLayer(doc, SOLAR);
            if (solarPolyline != null && !solarPolyline.isEmpty())
                for (DXFLWPolyline pline : solarPolyline) {
                    org.egov.edcr.entity.utility.Solar solar = new org.egov.edcr.entity.utility.Solar();
                    solar.setPresentInDxf(true);
                    solar.setPolyLine(pline);
                    pl.getUtility().addSolar(solar);
                }
        }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && pl.getUtility() != null)
            // solar water heating defined or not
            if (!pl.getVirtualBuilding().getOccupancies().isEmpty())
                for (OccupancyType occupancyType : pl.getVirtualBuilding().getOccupancies())
                    if (occupancyType.equals(OccupancyType.OCCUPANCY_A1)
                            && pl.getVirtualBuilding().getTotalBuitUpArea() != null
                            && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(FOURHUNDRED) > 0
                            && pl.getUtility().getSolar().isEmpty()) {
                        errors.put(SOLAR_SYSTEM,
                                edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                        SOLAR_SYSTEM }, LocaleContextHolder.getLocale()));
                        pl.addErrors(errors);
                        break;
                    } else if ((occupancyType.equals(OccupancyType.OCCUPANCY_A4)
                            || occupancyType.equals(OccupancyType.OCCUPANCY_A2)
                            || occupancyType.equals(OccupancyType.OCCUPANCY_F3) ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_A3) || occupancyType.equals(OccupancyType.OCCUPANCY_C) ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_C1) || occupancyType.equals(OccupancyType.OCCUPANCY_C2)
                            ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_C3) || occupancyType.equals(OccupancyType.OCCUPANCY_D) ||
                            occupancyType.equals(OccupancyType.OCCUPANCY_D1) || occupancyType.equals(OccupancyType.OCCUPANCY_D2))
                            && pl.getVirtualBuilding().getTotalBuitUpArea() != null
                            && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(500)) > 0
                            && pl.getUtility().getSolar().isEmpty()) {
                        errors.put(SOLAR_SYSTEM,
                                edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                        SOLAR_SYSTEM }, LocaleContextHolder.getLocale()));
                        pl.addErrors(errors);
                        break;
                    }

        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("SOLAR");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(BUILT_UP_AREA);
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
        scrutinyDetail.setKey("Common_Solar");
        String rule = RULE109;
        String subRule = SUB_RULE_109_C;
        String subRuleDesc = SUB_RULE_109_C_DESCRIPTION;
        if (!pl.getVirtualBuilding().getOccupancies().isEmpty())
            for (OccupancyType occupancyType : pl.getVirtualBuilding().getOccupancies())
                if (occupancyType.equals(OccupancyType.OCCUPANCY_A1)
                        && pl.getVirtualBuilding().getTotalBuitUpArea() != null
                        && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(FOURHUNDRED) > 0) {
                    processSolar(pl, rule, subRule, subRuleDesc);
                    break;
                } else if ((occupancyType.equals(OccupancyType.OCCUPANCY_A4) || occupancyType.equals(OccupancyType.OCCUPANCY_A2)
                        || occupancyType.equals(OccupancyType.OCCUPANCY_F3) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_A3) || occupancyType.equals(OccupancyType.OCCUPANCY_C) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_C1) || occupancyType.equals(OccupancyType.OCCUPANCY_C2) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_C3) || occupancyType.equals(OccupancyType.OCCUPANCY_D) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_D1) || occupancyType.equals(OccupancyType.OCCUPANCY_D2))
                        && pl.getVirtualBuilding().getTotalBuitUpArea() != null
                        && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(500)) > 0) {
                    processSolar(pl, rule, subRule, subRuleDesc);
                    break;
                }
        return pl;
    }

    private void processSolar(PlanDetail pl, String rule, String subRule, String subRuleDesc) {
        if (!pl.getUtility().getSolar().isEmpty()) {
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                            SOLAR,
                            null,
                            null,
                            Result.Accepted, OBJECTDEFINED_DESC));
            setReportOutputDetailsWithoutOccupancy(pl, subRule, subRuleDesc, "", OBJECTDEFINED_DESC,
                    Result.Accepted.getResultVal());
            return;
        } else {
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                            SOLAR,
                            null,
                            null,
                            Result.Not_Accepted, OBJECTNOTDEFINED_DESC));
            setReportOutputDetailsWithoutOccupancy(pl, subRule, subRuleDesc, "", OBJECTNOTDEFINED_DESC,
                    Result.Not_Accepted.getResultVal());
            return;
        }
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
