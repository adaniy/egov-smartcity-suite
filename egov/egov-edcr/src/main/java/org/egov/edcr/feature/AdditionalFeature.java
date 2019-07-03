package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.kabeja.dxf.DXFDocument;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AdditionalFeature extends GeneralRule implements RuleService {
    private static final Logger LOG = Logger.getLogger(AdditionalFeature.class);

    private static final String SUB_RULE_23_4 = "23(4)";
    private static final String SUB_RULE_23_4_DESCRIPTION = " Plot present in CRZ Zone";
    private static final String SUB_RULE_32_3 = "32(3)";
    private static final String RULE_61 = "61";

    private static final String SUB_RULE_32_3_DESCRIPTION = "Security zone ";
    private static final BigDecimal ten = BigDecimal.valueOf(10);

    @Override
    public PlanDetail extract(PlanDetail planDetail, DXFDocument doc) {
        return planDetail;
    }

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();

        List<Block> blocks = planDetail.getBlocks();

        for (Block block : blocks)
            if (!block.getCompletelyExisting())
                if (block.getBuilding() != null)
                    if (block.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) == 0) {
                        errors.put(String.format(DcrConstants.BLOCK_BUILDING_HEIGHT, block.getNumber()),
                                edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                        new String[] { String.format(DcrConstants.BLOCK_BUILDING_HEIGHT, block.getNumber()) },
                                        LocaleContextHolder.getLocale()));
                        planDetail.addErrors(errors);
                    }

        /*
         * if (planDetail.getPlot() != null && planDetail.getPlot().getPlotBndryArea() != null &&
         * planDetail.getPlanInformation().getPlotArea() != null){ BigDecimal plotBndryArea =
         * planDetail.getPlot().getPlotBndryArea().setScale(0, RoundingMode.UP); BigDecimal plotArea =
         * planDetail.getPlanInformation().getPlotArea().setScale(0, RoundingMode.UP); if (plotBndryArea.compareTo(plotArea) > 0)
         * planDetail.addError("plot boundary greater", String.format(PLOT_BOUNDARY_AREA_GREATER,
         * planDetail.getPlot().getPlotBndryArea(), planDetail.getPlanInformation().getPlotArea())); }
         */
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        validate(planDetail);
        validateSmallPlot(planDetail);
        rule23_4(planDetail);

        rule32_3(planDetail);

        return planDetail;
    }

    private void rule32_3(PlanDetail planDetail) {

        if (planDetail.getPlanInformation().getSecurityZone()) {

            List<Block> blocks = planDetail.getBlocks();

            for (Block block : blocks)
                if (block.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) > 0)
                    if (block.getBuilding().getBuildingHeight().compareTo(ten) <= 0) // TODO: LATER CHECK MAXIMUM HEIGHT OF
                                                                                     // BUILDING
                        // FROM FLOOR
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_3,
                                SUB_RULE_32_3_DESCRIPTION + " for block " + block.getNumber(), DcrConstants.SECURITY_ZONE, null,
                                null,
                                Result.Verify, DcrConstants.SECURITY_ZONE + DcrConstants.OBJECTDEFINED_DESC));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_3,
                                SUB_RULE_32_3_DESCRIPTION + " for block" + block.getNumber(),
                                DcrConstants.SECURITY_ZONE, ten.toString() + DcrConstants.IN_METER,
                                block.getBuilding().getBuildingHeight() + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
        }

    }

    private void rule23_4(PlanDetail planDetail) {
        if (planDetail.getPlanInformation().getCrzZoneArea())
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_23_4, SUB_RULE_23_4_DESCRIPTION,
                            DcrConstants.CRZZONE,
                            null,
                            null,
                            Result.Verify, DcrConstants.CRZZONE + DcrConstants.OBJECTDEFINED_DESC));
    }

    private void validateSmallPlot(PlanDetail pl) {
        for (Block block : pl.getBlocks())
            if (pl.getPlot() != null && pl.getPlot().getSmallPlot() && block != null && block.getBuilding() != null
                    && block.getResidentialOrCommercialBuilding()) {
                boolean isAccepted;
                ScrutinyDetail scrutinyDetail = getNewScrutinyDetail(
                        "Block_" + block.getNumber() + "_" + "Number of Floors Allowed");
                if (block.getBuilding().getFloorsAboveGround().doubleValue() > 3)
                    isAccepted = false;
                else
                    isAccepted = true;
                Map<String, String> details = new HashMap<>();
                details.put(RULE_NO, RULE_61);
                details.put(DESCRIPTION, DcrConstants.SMALL_PLOT_VIOLATION);
                details.put(REQUIRED, "<= 3");
                details.put(PROVIDED, String.valueOf(block.getBuilding().getFloorsAboveGround()));
                details.put(STATUS, isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
                scrutinyDetail.getDetail().add(details);
                pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
            }
    }

    private ScrutinyDetail getNewScrutinyDetail(String key) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.setKey(key);
        return scrutinyDetail;
    }

}
