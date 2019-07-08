package org.egov.edcr.entity;

public class FireStair extends Stair {

    private static final long serialVersionUID = -7731925286127198978L;
    private boolean generalStair = false;

    public boolean isGeneralStair() {
        return generalStair;
    }

    public void setGeneralStair(boolean generalStair) {
        this.generalStair = generalStair;
    }
}
