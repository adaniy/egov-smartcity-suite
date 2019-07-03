package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.rule.RuleService;
import org.kabeja.dxf.DXFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import ar.com.fdvs.dj.domain.builders.FastReportBuilder;

@Service
public class GeneralRule implements RuleService {
    public static final String MSG_ERROR_MANDATORY = "msg.error.mandatory.object.not.defined";
    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;
    // Dont use class variable like this .
    protected ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
    public static final String STATUS = "Status";

    public static final String PROVIDED = "Provided";
    public static final String LEVEL = "Level";
    public static final String OCCUPANCY = "Occupancy";
    public static final String FIELDVERIFIED = "Field Verified";
    public static final String REQUIRED = "Required";

    public static final String DESCRIPTION = "Description";

    public static final String RULE_NO = "Rule No";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {

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

    public boolean generateRuleReport(PlanDetail planDetail, FastReportBuilder drb, Map map, boolean status) {
        return true;
    }

    protected RuleOutput buildRuleOutputWithSubRule(String mainRule, String subRule, String ruleDescription,
            String fieldVerified, String expectedResult, String actualResult, Result status, String message) {
        RuleOutput ruleOutput = new RuleOutput();

        if (mainRule != null) {
            ruleOutput.key = mainRule;

            if (subRule != null || fieldVerified != null) {
                SubRuleOutput subRuleOutput = new SubRuleOutput();
                subRuleOutput.key = subRule != null ? subRule : fieldVerified;
                subRuleOutput.result = status;
                subRuleOutput.message = message;
                subRuleOutput.ruleDescription = ruleDescription;

                if (expectedResult != null) {
                    RuleReportOutput ruleReportOutput = new RuleReportOutput();
                    ruleReportOutput.setActualResult(actualResult);
                    ruleReportOutput.setExpectedResult(expectedResult);
                    ruleReportOutput.setFieldVerified(fieldVerified);
                    ruleReportOutput.setStatus(status.toString());
                    subRuleOutput.add(ruleReportOutput);
                }
                ruleOutput.subRuleOutputs.add(subRuleOutput);
            }
        }

        return ruleOutput;
    }

    protected RuleOutput buildRuleOutputWithMainRule(String mainRule, String ruleDescription, Result status,
            String message) {
        RuleOutput ruleOutput = new RuleOutput();
        ruleOutput.key = mainRule;
        ruleOutput.result = status;
        ruleOutput.setMessage(message);
        ruleOutput.ruleDescription = ruleDescription;

        return ruleOutput;
    }

    public String getLocaleMessage(String code, String... args) {
        return edcrMessageSource.getMessage(code, args, LocaleContextHolder.getLocale());

    }

    /**
     * @param strValue
     * @param pl
     * @param fieldName
     * @return
     */
    public BigDecimal getNumericValue(String strValue, PlanDetail pl, String fieldName) {

        try {
            if (!org.apache.commons.lang.StringUtils.isEmpty(strValue))
                return BigDecimal.valueOf(Double.parseDouble(strValue));
        } catch (NumberFormatException e) {
            pl.addError(fieldName,
                    "The value for " + fieldName + " '" + strValue + "' Is Invalid");
        }
        return null;
    }

    public String prepareMessage(String code, String... args) {
        return edcrMessageSource.getMessage(code, args, LocaleContextHolder.getLocale());

    }

    public MessageSource getEdcrMessageSource() {
        return edcrMessageSource;
    }

    public void setEdcrMessageSource(MessageSource edcrMessageSource) {
        this.edcrMessageSource = edcrMessageSource;
    }
}
