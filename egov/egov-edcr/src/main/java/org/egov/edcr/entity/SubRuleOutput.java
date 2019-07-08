package org.egov.edcr.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.utility.RuleReportOutput;

public class SubRuleOutput implements Serializable {
    private static final long serialVersionUID = 1210655669893150338L;
    public String key;
    public List<RuleReportOutput> ruleReportOutputs = new ArrayList();
    public String message;
    public Result result;
    public String ruleDescription;

    @Override
    public String toString() {
        return "SubRuleOutput [key=" + key + ", ruleReportOutputs=" + ruleReportOutputs + ", message=" + message + ", result="
                + result + ", ruleDescription=" + ruleDescription + "]";
    }

    public void add(RuleReportOutput ruleReportOutput) {
        if (ruleReportOutputs == null) {
            ruleReportOutputs = new ArrayList<>();
            ruleReportOutputs.add(ruleReportOutput);
        } else
            ruleReportOutputs.add(ruleReportOutput);

    }

    public List<RuleReportOutput> getRuleReportOutputs() {
        return ruleReportOutputs;
    }

    public void setRuleReportOutputs(List<RuleReportOutput> ruleReportOutputs) {
        this.ruleReportOutputs = ruleReportOutputs;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

}
