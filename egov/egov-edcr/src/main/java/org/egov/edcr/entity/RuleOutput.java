package org.egov.edcr.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RuleOutput implements Serializable {

    private static final long serialVersionUID = 306905677684288013L;
    public String key;
    public List<SubRuleOutput> subRuleOutputs = new ArrayList<>();
    public String message;
    public Result result;
    public String ruleDescription;

    public void add(SubRuleOutput subruleOut) {
        if (subRuleOutputs.isEmpty())
            subRuleOutputs.add(subruleOut);
        else
            subRuleOutputs.add(subruleOut);

    }

    public void addAll(List<SubRuleOutput> subruleOut) {
        if (subRuleOutputs.isEmpty())
            subRuleOutputs.addAll(subruleOut);
        else
            subRuleOutputs.addAll(subruleOut);

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (key == null ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RuleOutput other = (RuleOutput) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RuleOutput [key=" + key + ", subRuleOutputs=" + subRuleOutputs + ", message=" + message + ", result=" + result
                + ", ruleDescription=" + ruleDescription + "]";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<SubRuleOutput> getSubRuleOutputs() {
        return subRuleOutputs;
    }

    public void setSubRuleOutputs(List<SubRuleOutput> subRuleOutputs) {
        this.subRuleOutputs = subRuleOutputs;
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
