package org.egov.edcr.rule;

import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.feature.GeneralRule;
import org.egov.edcr.utility.DcrConstants;
import org.kabeja.dxf.DXFDocument;
import org.springframework.stereotype.Service;

@Service
public class DepthCutting extends GeneralRule implements RuleService {
    private static final String SUBRULE_11_A_DESC = "Maximum depth of cutting from ground level";
    private static final String SUBRULE_11_A = "11(A)";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("PLAN_INFO");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        boolean valid = false;
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey("Common_Depth Cutting");
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        if (pl.getPlanInformation() != null && pl.getPlanInformation().getDepthCutting() != null) {
            if (!pl.getPlanInformation().getDepthCutting())
                valid = true;
            if (valid) {
                pl.reportOutput
                        .add(buildRuleOutputWithSubRule(SUBRULE_11_A_DESC,
                                SUBRULE_11_A,
                                SUBRULE_11_A_DESC,
                                SUBRULE_11_A_DESC,
                                BigDecimal.valueOf(1.5).toString() + DcrConstants.IN_METER,
                                "Less Than Or Equal To 1.5" + DcrConstants.IN_METER, Result.Accepted,
                                null));
                setReportOutputDetails(pl, SUBRULE_11_A, SUBRULE_11_A_DESC,
                        BigDecimal.valueOf(1.5).toString() + DcrConstants.IN_METER,
                        "Less Than Or Equal To 1.5" + DcrConstants.IN_METER, Result.Accepted.getResultVal());
            } else {
                pl.reportOutput
                        .add(buildRuleOutputWithSubRule(SUBRULE_11_A_DESC,
                                SUBRULE_11_A,
                                SUBRULE_11_A_DESC,
                                SUBRULE_11_A_DESC,
                                BigDecimal.valueOf(1.5).toString() + DcrConstants.IN_METER,
                                "More Than 1.5" + DcrConstants.IN_METER,
                                Result.Verify, null));
                setReportOutputDetails(pl, SUBRULE_11_A, SUBRULE_11_A_DESC,
                        BigDecimal.valueOf(1.5).toString() + DcrConstants.IN_METER,
                        "More Than 1.5" + DcrConstants.IN_METER, Result.Verify.getResultVal());
            }
        }

        return pl;
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDescription, String expected, String actual,
            String status) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDescription);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }
}
