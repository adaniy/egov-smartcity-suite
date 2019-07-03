package org.egov.edcr.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SanityHelper implements Serializable {

    private static final long serialVersionUID = 55L;
    public Double maleWc = 0d;
    public Double femaleWc = 0d;
    public Double urinal = 0d;
    public Double maleWash = 0d;
    public Double femaleWash = 0d;
    public Double maleBath = 0d;
    public Double femaleBath = 0d;
    public Double commonBath = 0d;
    public Double abultionTap = 0d;
    public Double requiredSpecialWc = 0d;
    public Double providedSpecialWc = 0d;
    public Set<String> ruleNo = new HashSet<>();
    public String ruleDescription;
    public Double failedAreaSpecialWc = 0d;
    public Double failedDimensionSpecialWc = 0d;

}
