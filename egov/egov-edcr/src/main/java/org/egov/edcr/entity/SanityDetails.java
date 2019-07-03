package org.egov.edcr.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class SanityDetails implements Serializable {
    private static final long serialVersionUID = 9L;
    private List<Measurement> maleWaterClosets = new ArrayList<>();
    private List<Measurement> femaleWaterClosets = new ArrayList<>();
    private List<Measurement> commonWaterClosets = new ArrayList<>();
    private List<Measurement> urinals = new ArrayList<>();
    // Exclusive for Bath and Shower
    private List<Measurement> maleBathRooms = new ArrayList<>();
    private List<Measurement> femaleBathRooms = new ArrayList<>();
    private List<Measurement> commonBathRooms = new ArrayList<>();
    // Room + Bath + Water Closet
    private List<Measurement> maleRoomsWithWaterCloset = new ArrayList<>();
    private List<Measurement> femaleRoomsWithWaterCloset = new ArrayList<>();
    private List<Measurement> commonRoomsWithWaterCloset = new ArrayList<>();

    private List<Measurement> drinkingWater = new ArrayList<>();

    private List<Measurement> totalSpecialWC = new ArrayList<>();

    private int totalSPWC = 0;
    private int totalwashBasins = 0;

    public List<Measurement> getMaleWaterClosets() {
        return maleWaterClosets;
    }

    public void setMaleWaterClosets(List<Measurement> maleWaterClosets) {
        this.maleWaterClosets = maleWaterClosets;
    }

    public List<Measurement> getFemaleWaterClosets() {
        return femaleWaterClosets;
    }

    public void setFemaleWaterClosets(List<Measurement> femaleWaterClosets) {
        this.femaleWaterClosets = femaleWaterClosets;
    }

    public List<Measurement> getUrinals() {
        return urinals;
    }

    public void setUrinals(List<Measurement> urinals) {
        this.urinals = urinals;
    }

    public List<Measurement> getMaleBathRooms() {
        return maleBathRooms;
    }

    public void setMaleBathRooms(List<Measurement> maleBathRooms) {
        this.maleBathRooms = maleBathRooms;
    }

    public List<Measurement> getFemaleBathRooms() {
        return femaleBathRooms;
    }

    public void setFemaleBathRooms(List<Measurement> femaleBathRooms) {
        this.femaleBathRooms = femaleBathRooms;
    }

    public List<Measurement> getMaleRoomsWithWaterCloset() {
        return maleRoomsWithWaterCloset;
    }

    public void setMaleRoomsWithWaterCloset(List<Measurement> maleRoomsWithWaterCloset) {
        this.maleRoomsWithWaterCloset = maleRoomsWithWaterCloset;
    }

    public List<Measurement> getFemaleRoomsWithWaterCloset() {
        return femaleRoomsWithWaterCloset;
    }

    public void setFemaleRoomsWithWaterCloset(List<Measurement> femaleRoomsWithWaterCloset) {
        this.femaleRoomsWithWaterCloset = femaleRoomsWithWaterCloset;
    }

    public List<Measurement> getDrinkingWater() {
        return drinkingWater;
    }

    public void setDrinkingWater(List<Measurement> drinkingWater) {
        this.drinkingWater = drinkingWater;
    }

    public List<Measurement> getTotalSpecialWC() {
        return totalSpecialWC;
    }

    public void setTotalSpecialWC(List<Measurement> totalSpecialWC) {
        this.totalSpecialWC = totalSpecialWC;
    }

    public List<Measurement> getCommonWaterClosets() {
        return commonWaterClosets;
    }

    public void setCommonWaterClosets(List<Measurement> commonWaterClosets) {
        this.commonWaterClosets = commonWaterClosets;
    }

    public int getTotalSPWC() {
        return totalSPWC;
    }

    public void setTotalSPWC(int totalSPWC) {
        this.totalSPWC = totalSPWC;
    }

    public int getTotalwashBasins() {
        return totalwashBasins;
    }

    public void setTotalwashBasins(int totalwashBasins) {
        this.totalwashBasins = totalwashBasins;
    }

    public List<Measurement> getCommonBathRooms() {
        return commonBathRooms;
    }

    public List<Measurement> getCommonRoomsWithWaterCloset() {
        return commonRoomsWithWaterCloset;
    }

    public void setCommonBathRooms(List<Measurement> commonBathRooms) {
        this.commonBathRooms = commonBathRooms;
    }

    public void setCommonRoomsWithWaterCloset(List<Measurement> commonRoomsWithWaterCloset) {
        this.commonRoomsWithWaterCloset = commonRoomsWithWaterCloset;
    }
}