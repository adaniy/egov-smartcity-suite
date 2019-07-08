package org.egov.edcr.entity.utility;

import java.io.Serializable;

public class RuleReportOutput implements Serializable {

    // public String ruleKey;

    private static final long serialVersionUID = -26191259183372877L;

    public String fieldVerified;

    public String expectedResult;

    public String actualResult;

    public String status;

    /*
     * public String getRuleKey() { return ruleKey; } public void setRuleKey(String ruleKey) { this.ruleKey = ruleKey; }
     */
    public String getFieldVerified() {
        return fieldVerified;
    }

    public void setFieldVerified(String fieldVerified) {
        this.fieldVerified = fieldVerified;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getActualResult() {
        return actualResult;
    }

    public void setActualResult(String actualResult) {
        this.actualResult = actualResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RuleReportOutput [fieldVerified=" + fieldVerified + ", expectedResult=" + expectedResult + ", actualResult="
                + actualResult + ", status=" + status + "]";
    }

}
