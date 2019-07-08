package org.egov.edcr.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportOutput implements Serializable {
    private static final long serialVersionUID = -6478928965806435331L;
    public List<RuleOutput> ruleOutPuts = new ArrayList<>();
    public List<ScrutinyDetail> scrutinyDetails = new ArrayList<>();

    public void add(RuleOutput ruleOut) {
        if (ruleOutPuts == null) {
            ruleOutPuts = new ArrayList<>();
            ruleOutPuts.add(ruleOut);
        } else if (ruleOutPuts.contains(ruleOut))
            ruleOutPuts.get(ruleOutPuts.indexOf(ruleOut)).addAll(ruleOut.getSubRuleOutputs());
        else
            ruleOutPuts.add(ruleOut);

    }

    public List<RuleOutput> getRuleOutPuts() {
        return ruleOutPuts;
    }

    public void setRuleOutPuts(List<RuleOutput> ruleOutPuts) {
        this.ruleOutPuts = ruleOutPuts;
    }

    @Override
    public String toString() {
        return "ReportOutput [ruleOutPuts=" + ruleOutPuts + "]";
    }

    public List<ScrutinyDetail> getScrutinyDetails() {
        return scrutinyDetails;
    }

    public void setScrutinyDetails(List<ScrutinyDetail> scrutinyDetails) {
        this.scrutinyDetails = scrutinyDetails;
    }

}
