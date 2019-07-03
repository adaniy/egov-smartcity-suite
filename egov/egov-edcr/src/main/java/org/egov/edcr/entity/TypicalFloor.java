package org.egov.edcr.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TypicalFloor implements Serializable {
    private static final long serialVersionUID = 5L;
    private Integer modelFloorNo;
    private List<Integer> repetitiveFloorNos = new ArrayList<>();

    public Integer getModelFloorNo() {
        return modelFloorNo;
    }

    public List<Integer> getRepetitiveFloorNos() {
        return repetitiveFloorNos;
    }

    public void setModelFloorNo(Integer modelFloorNo) {
        this.modelFloorNo = modelFloorNo;
    }

    public void setRepetitiveFloorNos(List<Integer> repetitiveFloorNos) {
        this.repetitiveFloorNos = repetitiveFloorNos;
    }

    public TypicalFloor(String s) {
        try {
            String[] floorNos = s.split("=")[1].split(",");
            int i = 0;
            while (i < floorNos.length) {
                String no = floorNos[i];
                no = no.replaceAll("[^\\d.]", "");
                if (i == 0)
                    modelFloorNo = Integer.valueOf(no);
                else
                    repetitiveFloorNos.add(Integer.valueOf(no));

                i++;
            }
        } catch (NumberFormatException e) {

        }

    }

}
