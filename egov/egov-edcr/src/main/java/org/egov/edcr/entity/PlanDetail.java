package org.egov.edcr.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Transient;

import org.egov.edcr.entity.measurement.CulDeSacRoad;
import org.egov.edcr.entity.measurement.Lane;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.entity.utility.SetBack;
import org.egov.edcr.entity.utility.Utility;
import org.kabeja.dxf.DXFDocument;

/*All the details extracted from the plan are referred in this object*/
public class PlanDetail implements Serializable {

    private static final long serialVersionUID = -3366713640017914855L;
    public ReportOutput reportOutput = new ReportOutput();
    private Utility utility = new Utility();
    private PlanInformation planInformation;
    private Plot plot;
    private List<Block> blocks = new ArrayList<>();
    private List<AccessoryBlock> accessoryBlocks = new ArrayList<>();
    private VirtualBuilding virtualBuilding;
    private Building building;
    private BigDecimal coverage = BigDecimal.ZERO;
    private BigDecimal coverageArea = BigDecimal.ZERO;
    private BigDecimal far = BigDecimal.ZERO;
    private BigDecimal totalBuiltUpArea;
    private BigDecimal totalFloorArea;
    private Boolean edcrPassed = false;
    private List<ElectricLine> electricLine = new ArrayList<>();
    private List<NonNotifiedRoad> nonNotifiedRoads = new ArrayList<>();
    private List<NotifiedRoad> notifiedRoads = new ArrayList<>();
    // Contains declared and converted occupancies
    private List<Occupancy> occupancies = new ArrayList<>();
    // Contains only declared occupancies
    private List<Occupancy> declaredOccupancies = new ArrayList<>();
    // Mezzanine occupancies
    private List<Occupancy> mezzanineOccupancies = new ArrayList<>();
    private List<CulDeSacRoad> culdeSacRoads = new ArrayList<>();
    private List<Lane> laneRoads = new ArrayList<>();
    private HashMap<String, String> errors = new LinkedHashMap<>();
    private HashMap<String, String> noObjectionCertificates = new HashMap<>();
    private List<BigDecimal> travelDistancesToExit = new ArrayList<>();
    private HashMap<String, String> generalInformation = new HashMap<>();
    private Basement basement;
    private ParkingDetails parkingDetails = new ParkingDetails();
    private List<BigDecimal> canopyDistanceFromPlotBoundary;
    @Transient
    private Boolean inMeters = true;
    @Transient
    public StringBuffer additionsToDxf = new StringBuffer();
    @Transient
    private Boolean lengthFactor = true;
    @Transient
    private Double parkingRequired;
    private transient DXFDocument dxfDocument;
    @Transient
    private String dxfFileName;
    @Transient
    private List<EdcrPdfDetail> edcrPdfDetails;

    public List<BigDecimal> getCanopyDistanceFromPlotBoundary() {
        return canopyDistanceFromPlotBoundary;
    }

    public void setCanopyDistanceFromPlotBoundary(List<BigDecimal> canopyDistanceFromPlotBoundary) {
        this.canopyDistanceFromPlotBoundary = canopyDistanceFromPlotBoundary;
    }

    public List<BigDecimal> getTravelDistancesToExit() {
        return travelDistancesToExit;
    }

    public void setTravelDistancesToExit(List<BigDecimal> travelDistancesToExit) {
        this.travelDistancesToExit = travelDistancesToExit;
    }

    private List<BigDecimal> depthCuttings = new ArrayList<>();

    public List<BigDecimal> getDepthCuttings() {
        return depthCuttings;
    }

    public void setDepthCuttings(List<BigDecimal> depthCuttings) {
        this.depthCuttings = depthCuttings;
    }

    public List<AccessoryBlock> getAccessoryBlocks() {
        return accessoryBlocks;
    }

    public void setAccessoryBlocks(List<AccessoryBlock> accessoryBlocks) {
        this.accessoryBlocks = accessoryBlocks;
    }

    public List<Occupancy> getOccupancies() {
        return occupancies;
    }

    public void setOccupancies(List<Occupancy> occupancies) {
        this.occupancies = occupancies;
    }

    public List<Occupancy> getDeclaredOccupancies() {
        return declaredOccupancies;
    }

    public void setDeclaredOccupancies(List<Occupancy> declaredOccupancies) {
        this.declaredOccupancies = declaredOccupancies;
    }

    public List<Occupancy> getMezzanineOccupancies() {
        return mezzanineOccupancies;
    }

    public void setMezzanineOccupancies(List<Occupancy> mezzanineOccupancies) {
        this.mezzanineOccupancies = mezzanineOccupancies;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public Block getBlockByName(String blockName) {
        for (Block block : getBlocks())
            if (block.getName().equalsIgnoreCase(blockName))
                return block;
        return null;
    }

    public HashMap<String, String> getNoObjectionCertificates() {
        return noObjectionCertificates;
    }

    public void setNoObjectionCertificates(HashMap<String, String> noObjectionCertificates) {
        this.noObjectionCertificates = noObjectionCertificates;
    }

    public List<CulDeSacRoad> getCuldeSacRoads() {
        return culdeSacRoads;
    }

    public void setCuldeSacRoads(List<CulDeSacRoad> culdeSacRoads) {
        this.culdeSacRoads = culdeSacRoads;
    }

    public List<Lane> getLaneRoads() {
        return laneRoads;
    }

    public void setLaneRoads(List<Lane> laneRoads) {
        this.laneRoads = laneRoads;
    }

    public List<ElectricLine> getElectricLine() {
        return electricLine;
    }

    public void setElectricLine(List<ElectricLine> electricLine) {
        this.electricLine = electricLine;
    }

    public Boolean getEdcrPassed() {
        return edcrPassed;
    }

    public void setEdcrPassed(Boolean edcrPassed) {
        this.edcrPassed = edcrPassed;
    }

    public List<NonNotifiedRoad> getNonNotifiedRoads() {
        return nonNotifiedRoads;
    }

    public void setNonNotifiedRoads(List<NonNotifiedRoad> nonNotifiedRoads) {
        this.nonNotifiedRoads = nonNotifiedRoads;
    }

    public List<NotifiedRoad> getNotifiedRoads() {
        return notifiedRoads;
    }

    public void setNotifiedRoads(List<NotifiedRoad> notifiedRoads) {
        this.notifiedRoads = notifiedRoads;
    }

    public HashMap<String, String> getGeneralInformation() {
        return generalInformation;
    }

    public void setGeneralInformation(HashMap<String, String> generalInformation) {
        this.generalInformation = generalInformation;
    }

    public void addGeneralInformation(Map<String, String> generalInformation) {
        if (generalInformation != null)
            getGeneralInformation().entrySet().add((Entry<String, String>) generalInformation);
    }

    public void addErrors(Map<String, String> errors) {
        if (errors != null)
            getErrors().putAll(errors);
    }

    public void addNocs(Map<String, String> nocs) {
        if (noObjectionCertificates != null)
            getNoObjectionCertificates().putAll(nocs);
    }

    public void addNoc(String key, String value) {

        if (noObjectionCertificates != null)
            getNoObjectionCertificates().put(key, value);
    }

    public void addError(String key, String value) {

        if (errors != null)
            getErrors().put(key, value);
    }

    public HashMap<String, String> getErrors() {
        return errors;
    }

    public void setErrors(HashMap<String, String> errors) {
        this.errors = errors;
    }

    public ReportOutput getReportOutput() {
        return reportOutput;
    }

    public void setReportOutput(ReportOutput reportOutput) {
        this.reportOutput = reportOutput;
    }

    public PlanInformation getPlanInformation() {
        return planInformation;
    }

    public void setPlanInformation(PlanInformation planInformation) {
        this.planInformation = planInformation;
    }

    public Plot getPlot() {
        return plot;
    }

    public void setPlot(Plot plot) {
        this.plot = plot;
    }

    public VirtualBuilding getVirtualBuilding() {
        return virtualBuilding;
    }

    public void setVirtualBuilding(VirtualBuilding virtualBuilding) {
        this.virtualBuilding = virtualBuilding;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Utility getUtility() {
        return utility;
    }

    public void setUtility(Utility utility) {
        this.utility = utility;
    }

    public Basement getBasement() {
        return basement;
    }

    public void setBasement(Basement basement) {
        this.basement = basement;
    }

    public BigDecimal getCoverage() {
        return coverage;
    }

    public void setCoverage(BigDecimal coverage) {
        this.coverage = coverage;
    }

    public BigDecimal getFar() {
        return far;
    }

    public void setFar(BigDecimal far) {
        this.far = far;
    }

    public BigDecimal getTotalBuiltUpArea() {
        return totalBuiltUpArea;
    }

    public void setTotalBuiltUpArea(BigDecimal totalBuiltUpArea) {
        this.totalBuiltUpArea = totalBuiltUpArea;
    }

    public BigDecimal getTotalFloorArea() {
        return totalFloorArea;
    }

    public void setTotalFloorArea(BigDecimal totalFloorArea) {
        this.totalFloorArea = totalFloorArea;
    }

    public void sortBlockByName() {
        if (!blocks.isEmpty())
            Collections.sort(blocks, Comparator.comparing(Block::getNumber));
    }

    public void sortSetBacksByLevel() {
        for (Block block : blocks)
            Collections.sort(block.getSetBacks(), Comparator.comparing(SetBack::getLevel));
    }

    public ParkingDetails getParkingDetails() {
        return parkingDetails;
    }

    public void setParkingDetails(ParkingDetails parkingDetails) {
        this.parkingDetails = parkingDetails;
    }

    public Boolean getInMeters() {
        return inMeters;
    }

    public void setInMeters(Boolean inMeters) {
        this.inMeters = inMeters;
    }

    public Boolean getLengthFactor() {
        return lengthFactor;
    }

    public void setLengthFactor(Boolean lengthFactor) {
        this.lengthFactor = lengthFactor;
    }

    public StringBuffer getAdditionsToDxf() {
        return additionsToDxf;
    }

    public void addToAdditionsToDxf(String s) {
        additionsToDxf.append(s);
    }

    public void setAdditionsToDxf(StringBuffer additionsToDxf) {
        this.additionsToDxf = additionsToDxf;
    }

    public BigDecimal getCoverageArea() {
        return coverageArea;
    }

    public void setCoverageArea(BigDecimal coverageArea) {
        this.coverageArea = coverageArea;
    }

    public Double getParkingRequired() {
        return parkingRequired;
    }

    public void setParkingRequired(Double parkingRequired) {
        this.parkingRequired = parkingRequired;
    }

    public DXFDocument getDxfDocument() {
        return dxfDocument;
    }

    public void setDxfDocument(DXFDocument dxfDocument) {
        this.dxfDocument = dxfDocument;
    }

    public String getDxfFileName() {
        return dxfFileName;
    }

    public void setDxfFileName(String dxfFileName) {
        this.dxfFileName = dxfFileName;
    }

    public List<EdcrPdfDetail> getEdcrPdfDetails() {
        return edcrPdfDetails;
    }

    public void setEdcrPdfDetails(List<EdcrPdfDetail> edcrPdfDetails) {
        this.edcrPdfDetails = edcrPdfDetails;
    }
}
