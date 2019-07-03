package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.LAYER_COVERAGE;
import static org.egov.edcr.constants.DxfFileConstants.LAYER_COVERAGE_DEDUCT;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.stereotype.Service;

@Service
public class Coverage extends GeneralRule implements RuleService {
    // private static final String OCCUPANCY2 = "OCCUPANCY";

    private static final Logger LOG = Logger.getLogger(Coverage.class);

    private static final String RULE_NAME_KEY = "coverage.rulename";
    private static final String RULE_DESCRIPTION_KEY = "coverage.description";
    private static final String RULE_EXPECTED_KEY = "coverage.expected";
    private static final String RULE_ACTUAL_KEY = "coverage.actual";
    private static final BigDecimal ThirtyFive = BigDecimal.valueOf(35);
    private static final BigDecimal Forty = BigDecimal.valueOf(40);
    private static final BigDecimal FortyFive = BigDecimal.valueOf(45);
    private static final BigDecimal Sixty = BigDecimal.valueOf(60);
    private static final BigDecimal SixtyFive = BigDecimal.valueOf(65);
    private static final BigDecimal Seventy = BigDecimal.valueOf(70);
    private static final BigDecimal SeventyFive = BigDecimal.valueOf(75);
    private static final BigDecimal Eighty = BigDecimal.valueOf(80);
    public static final String RULE_31_1 = "31(1)";

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        for (Block block : planDetail.getBlocks())
            if (block.getCoverage().isEmpty())
                planDetail.addError("coverageArea" + block.getNumber(),
                        "Coverage Area for block " + block.getNumber() + " not Provided");
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        validate(planDetail);
        BigDecimal totalCoverage = BigDecimal.ZERO;
        BigDecimal totalCoverageArea = BigDecimal.ZERO;

        for (Block block : planDetail.getBlocks()) {
            BigDecimal coverageAreaWithoutDeduction = BigDecimal.ZERO;
            BigDecimal coverageDeductionArea = BigDecimal.ZERO;

            for (Measurement coverage : block.getCoverage())
                coverageAreaWithoutDeduction = coverageAreaWithoutDeduction.add(coverage.getArea());
            for (Measurement deduct : block.getCoverageDeductions())
                coverageDeductionArea = coverageDeductionArea.add(deduct.getArea());
            if (block.getBuilding() != null) {
                block.getBuilding().setCoverageArea(coverageAreaWithoutDeduction.subtract(coverageDeductionArea));
                BigDecimal coverage = BigDecimal.ZERO;
                if (planDetail.getPlot().getArea().doubleValue() > 0)
                    coverage = block.getBuilding().getCoverageArea().multiply(BigDecimal.valueOf(100)).divide(
                            planDetail.getPlot().getArea(), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                            DcrConstants.ROUNDMODE_MEASUREMENTS);

                block.getBuilding().setCoverage(coverage);

                totalCoverageArea = totalCoverageArea.add(block.getBuilding().getCoverageArea());
                // totalCoverage =
                // totalCoverage.add(block.getBuilding().getCoverage());
            }

        }

        planDetail.setCoverageArea(totalCoverageArea);

        if (planDetail.getPlot().getPlotBndryArea() != null && planDetail.getPlanInformation().getPlotArea() != null) {
            BigDecimal plotBndryArea = planDetail.getPlot().getPlotBndryArea().setScale(0, RoundingMode.HALF_UP);
            BigDecimal plotArea = planDetail.getPlanInformation().getPlotArea().setScale(0, RoundingMode.HALF_UP);

            BigDecimal area = plotBndryArea.compareTo(plotArea) >= 0 ? plotArea : plotBndryArea;

            if (area.doubleValue() > 0)
                totalCoverage = totalCoverageArea.multiply(BigDecimal.valueOf(100)).divide(area,
                        DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);
            planDetail.setCoverage(totalCoverage);
            if (planDetail.getVirtualBuilding() != null)
                planDetail.getVirtualBuilding().setTotalCoverageArea(totalCoverageArea);

            // for weighted coverage
            if (planDetail.getPlot().getArea().doubleValue() >= 5000) {
                BigDecimal provideCoverage = BigDecimal.ZERO;
                BigDecimal weightedArea = BigDecimal.ZERO;
                BigDecimal weightedCoverage = BigDecimal.ZERO;
                weightedArea = weightedArea.setScale(DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);
                weightedCoverage = weightedCoverage.setScale(DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);
                provideCoverage = provideCoverage.setScale(DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);

                for (Occupancy occ : planDetail.getOccupancies()) {
                    BigDecimal occupancyWiseCoverage = occ.getBuiltUpArea().multiply(getPermissibleCoverage(occ.getType()));
                    weightedArea = weightedArea.add(occupancyWiseCoverage);

                }
                if (planDetail.getVirtualBuilding().getTotalBuitUpArea().doubleValue() > 0)
                    weightedCoverage = weightedArea.divide(planDetail.getVirtualBuilding().getTotalBuitUpArea(), DECIMALDIGITS,
                            ROUNDMODE_MEASUREMENTS);
                if (planDetail.getPlot().getArea().doubleValue() > 0)
                    provideCoverage = planDetail.getCoverageArea().divide(area, DECIMALDIGITS, ROUNDMODE_MEASUREMENTS)
                            .multiply(BigDecimal.valueOf(100));
                // provideCoverage.setScale(2);
                processCoverage(planDetail, "-", provideCoverage.setScale(2, ROUNDMODE_MEASUREMENTS),
                        weightedCoverage.setScale(2, ROUNDMODE_MEASUREMENTS));
            } else {
                boolean exemption = Util.isSmallPlot(planDetail);
                if (!exemption) {
                    OccupancyType mostRestrictiveOccupancy = getMostRestrictiveCoverage(
                            planDetail.getVirtualBuilding().getOccupancies());
                    if (mostRestrictiveOccupancy != null)
                        switch (mostRestrictiveOccupancy) {
                        case OCCUPANCY_B1:
                        case OCCUPANCY_B2:
                        case OCCUPANCY_B3:
                            processCoverage(planDetail, mostRestrictiveOccupancy.getOccupancyTypeVal(), totalCoverage,
                                    ThirtyFive);
                            break;
                        case OCCUPANCY_D:
                        case OCCUPANCY_D1:
                        case OCCUPANCY_I2:
                            processCoverage(planDetail, mostRestrictiveOccupancy.getOccupancyTypeVal(), totalCoverage, Forty);
                            break;
                        case OCCUPANCY_I1:
                            processCoverage(planDetail, mostRestrictiveOccupancy.getOccupancyTypeVal(), totalCoverage, FortyFive);
                            break;

                        case OCCUPANCY_C:
                            processCoverage(planDetail, mostRestrictiveOccupancy.getOccupancyTypeVal(), totalCoverage, Sixty);
                            break;

                        case OCCUPANCY_A1:
                        case OCCUPANCY_A4:
                        case OCCUPANCY_A2:
                        case OCCUPANCY_F3:
                        case OCCUPANCY_G1:
                            processCoverage(planDetail, mostRestrictiveOccupancy.getOccupancyTypeVal(), totalCoverage, SixtyFive);
                            break;
                        case OCCUPANCY_E:
                        case OCCUPANCY_F:
                        case OCCUPANCY_F4:
                            processCoverage(planDetail, mostRestrictiveOccupancy.getOccupancyTypeVal(), totalCoverage, Seventy);
                            break;

                        case OCCUPANCY_G2:
                            processCoverage(planDetail, mostRestrictiveOccupancy.getOccupancyTypeVal(), totalCoverage,
                                    SeventyFive);
                            break;
                        case OCCUPANCY_H:
                            processCoverage(planDetail, mostRestrictiveOccupancy.getOccupancyTypeVal(), totalCoverage, Eighty);
                            break;
                        default:
                            break;
                        }
                }
            }
        }
        return planDetail;
    }

    private BigDecimal getPermissibleCoverage(OccupancyType type) {
        switch (type) {
        case OCCUPANCY_B1:
        case OCCUPANCY_B2:
        case OCCUPANCY_B3:
            return ThirtyFive;

        case OCCUPANCY_D:
        case OCCUPANCY_D1:
        case OCCUPANCY_I2:
            return Forty;

        case OCCUPANCY_I1:
            return FortyFive;

        case OCCUPANCY_C:
            return Sixty;

        case OCCUPANCY_A1:
        case OCCUPANCY_A4:
        case OCCUPANCY_A2:
        case OCCUPANCY_F3:
        case OCCUPANCY_G1:
            return SixtyFive;

        case OCCUPANCY_E:
        case OCCUPANCY_F:
        case OCCUPANCY_F4:
            return Seventy;

        case OCCUPANCY_G2:
            return SeventyFive;

        case OCCUPANCY_H:
            return Eighty;
        default:
            return BigDecimal.ZERO;
        }
    }

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        for (Block block : pl.getBlocks()) {
            List<DXFLWPolyline> polylinesCoverage = Util.getPolyLinesByLayer(doc,
                    String.format(LAYER_COVERAGE, block.getNumber()));
            List<DXFLWPolyline> polylinesCoverageDeduct = Util.getPolyLinesByLayer(doc,
                    String.format(LAYER_COVERAGE_DEDUCT, block.getNumber()));
            for (DXFLWPolyline polyline : polylinesCoverage) {
                Measurement measurement = new Measurement(polyline, false);
                block.getCoverage().add(measurement);
            }
            for (DXFLWPolyline polyline : polylinesCoverageDeduct) {
                Measurement measurement = new Measurement(polyline, false);
                block.getCoverageDeductions().add(measurement);
            }
        }

        return pl;
    }

    private void processCoverage(PlanDetail pl, String occupancy, BigDecimal coverage, BigDecimal upperLimit) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey("Common_Coverage");
        scrutinyDetail.setHeading("Coverage in Percentage");
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, OCCUPANCY);
        scrutinyDetail.addColumnHeading(4, REQUIRED);
        scrutinyDetail.addColumnHeading(5, PROVIDED);
        scrutinyDetail.addColumnHeading(6, STATUS);

        String desc = getLocaleMessage(RULE_DESCRIPTION_KEY, upperLimit.toString());
        String actualResult = getLocaleMessage(RULE_ACTUAL_KEY, coverage.toString());
        String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, upperLimit.toString());
        if (coverage.doubleValue() <= upperLimit.doubleValue()) {
            Map<String, String> details = new HashMap<>();
            details.put(RULE_NO, RULE_31_1);
            details.put(DESCRIPTION, desc);
            details.put(OCCUPANCY, occupancy);
            details.put(REQUIRED, expectedResult);
            details.put(PROVIDED, actualResult);
            details.put(STATUS, Result.Accepted.getResultVal());
            scrutinyDetail.getDetail().add(details);
            pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
            // pl.reportOutput.add(buildResult(desc, actualResult, expectedResult, Result.Accepted));
        } else {
            Map<String, String> details = new HashMap<>();
            details.put(RULE_NO, RULE_31_1);
            details.put(DESCRIPTION, desc);
            details.put(OCCUPANCY, occupancy);
            details.put(REQUIRED, expectedResult);
            details.put(PROVIDED, actualResult);
            details.put(STATUS, Result.Not_Accepted.getResultVal());
            scrutinyDetail.getDetail().add(details);
            pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
            // pl.reportOutput.add(buildResult(desc, actualResult, expectedResult, Result.Not_Accepted));

        }

    }

    protected OccupancyType getMostRestrictiveCoverage(EnumSet<OccupancyType> distinctOccupancyTypes) {

        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B1))
            return OccupancyType.OCCUPANCY_B1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B2))
            return OccupancyType.OCCUPANCY_B2;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B3))
            return OccupancyType.OCCUPANCY_B3;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D))
            return OccupancyType.OCCUPANCY_D;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D1))
            return OccupancyType.OCCUPANCY_D1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I2))
            return OccupancyType.OCCUPANCY_I2;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I1))
            return OccupancyType.OCCUPANCY_I1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_C))
            return OccupancyType.OCCUPANCY_C;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A1))
            return OccupancyType.OCCUPANCY_A1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A4))
            return OccupancyType.OCCUPANCY_A4;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A2))
            return OccupancyType.OCCUPANCY_A2;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F3))
            return OccupancyType.OCCUPANCY_F3;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G1))
            return OccupancyType.OCCUPANCY_G1;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_E))
            return OccupancyType.OCCUPANCY_E;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F))
            return OccupancyType.OCCUPANCY_F;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F4))
            return OccupancyType.OCCUPANCY_F4;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G2))
            return OccupancyType.OCCUPANCY_G2;
        if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_H))
            return OccupancyType.OCCUPANCY_H;

        else
            return null;
    }

}
