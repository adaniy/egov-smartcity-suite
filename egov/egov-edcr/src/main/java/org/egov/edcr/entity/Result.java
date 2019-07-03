package org.egov.edcr.entity;

public enum Result {
    Accepted("Accepted"), NA("N/A"), Not_Accepted("Not Accepted"), Verify("Verify");

    private final String resultVal;

    Result(String result) {
        resultVal = result;
    }

    public String getResultVal() {
        return resultVal;
    }

    @Override
    public String toString() {

        return name().replace("_", "");
    }
}
