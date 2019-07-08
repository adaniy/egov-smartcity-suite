package org.egov.edcr.entity;

import java.io.Serializable;

public class PlanFeature implements Serializable {
    private static final long serialVersionUID = -7368099369965079283L;
    private String name;
    private Class ruleClass;

    public PlanFeature(String string) {
        name = string;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getRuleClass() {
        return ruleClass;
    }

    public void setRuleClass(Class ruleClass) {
        this.ruleClass = ruleClass;
    }

    public PlanFeature(Class ruleClass) {
        super();
        this.ruleClass = ruleClass;
    }

}
