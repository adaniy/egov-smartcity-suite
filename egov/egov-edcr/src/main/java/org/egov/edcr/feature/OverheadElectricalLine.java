package org.egov.edcr.feature;

import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.ElectricLine;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OverheadElectricalLine extends GeneralRule implements RuleService {

    private static final String SUB_RULE_5_DESCRIPTION = "Overhead Electric line and Voltage";
    private static final String SUB_RULE_5 = "23(5)";

    private static final BigDecimal VERTICAL_DISTANCE_11000 = BigDecimal.valueOf(2.4);
    private static final BigDecimal VERTICAL_DISTANCE_33000 = BigDecimal.valueOf(3.7);
    private static final BigDecimal HORIZONTAL_DISTANCE_33000 = BigDecimal.valueOf(1.85);

    private static final int VOLTAGE_11000 = 11;
    private static final int VOLTAGE_33000 = 33;
    private static final BigDecimal HORIZONTAL_DISTANCE_11000 = BigDecimal.valueOf(1.2);
    private static final String REMARKS = "Remarks";
    private static final String VOLTAGE = "Voltage";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        String layerNamesRegExp = "^" + DxfFileConstants.LAYER_OHEL + "_+\\d";
        List<String> ohelLayerNames = Util.getLayerNamesLike(doc, layerNamesRegExp);
        List<ElectricLine> electricLines = new ArrayList<>();
        for (String layerNames : ohelLayerNames) {
            String[] layerParts = layerNames.split("_", 2);
            List<DXFLWPolyline> polylines = Util.getPolyLinesByLayer(doc, layerNames);
            if (layerParts[1] != null && !layerParts[1].isEmpty() && !polylines.isEmpty()) {
                ElectricLine line = new ElectricLine();
                line.setNumber(layerParts[1]);
                BigDecimal dimension = Util.getSingleDimensionValueByLayer(doc,
                        DxfFileConstants.HORIZ_CLEAR_OHE2 + "_" + layerParts[1], pl);
                if (dimension != null && dimension.compareTo(BigDecimal.ZERO) > 0)
                    line.setHorizontalDistance(dimension);
                BigDecimal dimensionVerticle = Util.getSingleDimensionValueByLayer(doc,
                        DxfFileConstants.VERT_CLEAR_OHE + "_" + layerParts[1], pl);
                if (dimensionVerticle != null && dimensionVerticle.compareTo(BigDecimal.ZERO) > 0)
                    line.setVerticalDistance(dimensionVerticle);
                line.setPresentInDxf(true);
                // change this to use api with 3 params
                String voltage = Util.getMtextByLayerName(doc, "VOLTAGE" + "_" + layerParts[1]);
                if (voltage != null)
                    try {
                        if (voltage.contains("=")) {
                            String[] textSplit = voltage.split("=");
                            if (textSplit[1] != null && !textSplit[1].isEmpty()) {
                                voltage = textSplit[1];
                                voltage = voltage.replaceAll("[^\\d.]", "");
                                BigDecimal volt = BigDecimal.valueOf(Double.parseDouble(voltage));
                                line.setVoltage(volt);
                                line.setPresentInDxf(true);
                            }
                        }
                    } catch (NumberFormatException e) {
                        pl.addError("VOLTAGE",
                                "Voltage value contains non numeric character.Voltage must be Number specified in  KW unit, without the text KW");
                    }
                else
                    pl.addError("VOLTAGE_" + layerParts[1],
                            "Voltage is not mentioned for the " + DxfFileConstants.LAYER_OHEL + "_" + layerParts[1]);
                electricLines.add(line);
            }
        }
        pl.setElectricLine(electricLines);

        return pl;

    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        for (ElectricLine electricalLine : pl.getElectricLine())
            if (electricalLine.getPresentInDxf()) {
                if (electricalLine.getVoltage() == null) {
                    errors.put(DcrConstants.VOLTAGE,
                            edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                    new String[] { DcrConstants.VOLTAGE }, LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);
                }
                if (electricalLine.getVoltage() != null && electricalLine.getHorizontalDistance() == null
                        && electricalLine.getVerticalDistance() == null) {
                    errors.put(DcrConstants.ELECTRICLINE_DISTANCE,
                            edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                    new String[] { DcrConstants.ELECTRICLINE_DISTANCE }, LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);
                }
            }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("OHEL_%");
        layers.add("HORIZ_CLEAR_OHEL_%");
        layers.add("VERT_CLEAR_OHEL_%");
        layers.add("VOLTAGE_%");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        parameters.add(VOLTAGE);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey("Common_OverHead Electric Line");
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.addColumnHeading(6, REMARKS);
        scrutinyDetail.addColumnHeading(7, VOLTAGE);
        for (ElectricLine electricalLine : pl.getElectricLine())
            if (electricalLine.getPresentInDxf())
                if (electricalLine.getVoltage() != null
                        && electricalLine.getVoltage().compareTo(BigDecimal.ZERO) > 0
                        && (electricalLine.getHorizontalDistance() != null
                                || electricalLine.getVerticalDistance() != null)) {
                    boolean horizontalDistancePassed = false;
                    if (electricalLine.getHorizontalDistance() != null) {
                        String expectedResult = "";
                        String actualResult = electricalLine.getHorizontalDistance().toString() + DcrConstants.IN_METER;
                        if (electricalLine.getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) < 0) {
                            expectedResult = HORIZONTAL_DISTANCE_11000.toString() + DcrConstants.IN_METER;
                            if (electricalLine.getHorizontalDistance().compareTo(HORIZONTAL_DISTANCE_11000) >= 0)
                                horizontalDistancePassed = true;

                        } else if (electricalLine.getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) >= 0
                                && electricalLine.getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) <= 0) {
                            expectedResult = HORIZONTAL_DISTANCE_33000.toString() + DcrConstants.IN_METER;
                            if (electricalLine.getHorizontalDistance().compareTo(HORIZONTAL_DISTANCE_33000) >= 0)
                                horizontalDistancePassed = true;
                        } else if (electricalLine.getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) > 0) {
                            Double totalHorizontalOHE = HORIZONTAL_DISTANCE_33000.doubleValue() + 0.3 *
                                    Math.ceil(
                                            electricalLine.getVoltage().subtract(BigDecimal.valueOf(VOLTAGE_33000))
                                                    .divide(BigDecimal.valueOf(VOLTAGE_33000), 2, RoundingMode.HALF_UP)
                                                    .doubleValue());
                            expectedResult = totalHorizontalOHE + DcrConstants.IN_METER;
                            if (electricalLine.getHorizontalDistance()
                                    .compareTo(BigDecimal.valueOf(totalHorizontalOHE)) >= 0)
                                horizontalDistancePassed = true;
                        }
                        if (horizontalDistancePassed) {
                            pl.reportOutput
                                    .add(buildRuleOutputWithSubRule(SUB_RULE_5_DESCRIPTION, SUB_RULE_5,
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE +
                                                    electricalLine.getNumber(),
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(),
                                            expectedResult, actualResult,
                                            Result.Accepted, null));
                            setReportOutputDetails(pl, SUB_RULE_5, DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE +
                                    electricalLine.getNumber(), expectedResult,
                                    actualResult, Result.Accepted.getResultVal(), "",
                                    electricalLine.getVoltage().toString() + DcrConstants.IN_KV);
                        } else {
                            boolean verticalDistancePassed = processVerticalDistance(electricalLine, pl, "", "");
                            pl.reportOutput
                                    .add(buildRuleOutputWithSubRule(SUB_RULE_5_DESCRIPTION, SUB_RULE_5,
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE +
                                                    electricalLine.getNumber(),
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(),
                                            expectedResult, actualResult,
                                            verticalDistancePassed == true ? Result.Verify : Result.Not_Accepted, null));
                            if (verticalDistancePassed)
                                setReportOutputDetails(pl, SUB_RULE_5, DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE +
                                        electricalLine.getNumber(), expectedResult,
                                        actualResult, Result.Verify.getResultVal(),
                                        String.format(DcrConstants.HORIZONTAL_ELINE_DISTANCE_NOC, electricalLine.getNumber()),
                                        electricalLine.getVoltage().toString() + DcrConstants.IN_KV);
                            else
                                setReportOutputDetails(pl, SUB_RULE_5, DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE +
                                        electricalLine.getNumber(), expectedResult,
                                        actualResult, Result.Not_Accepted.getResultVal(), "",
                                        electricalLine.getVoltage().toString() + DcrConstants.IN_KV);

                            // NOC required for horizontal, if horizontal distance condition failed and vertical distance passed.
                            if (verticalDistancePassed) {
                                HashMap<String, String> noc = new HashMap<>();
                                noc.put(DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(),
                                        DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE_NOC);
                                pl.addNocs(noc);
                            }
                        }

                    } else if (electricalLine.getHorizontalDistance() == null && electricalLine.getVerticalDistance() != null) {
                        boolean verticalDistancePassed = processVerticalDistance(electricalLine, pl, String.format(
                                DcrConstants.HORIZONTAL_ELINE_DISTANCE_NOC_HLINE_NOT_DEFINED, electricalLine.getNumber()), "");
                        if (verticalDistancePassed) {
                            HashMap<String, String> noc = new HashMap<>();
                            noc.put(DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(),
                                    DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE_NOC);
                            pl.addNocs(noc);
                        }
                    }
                }
        return pl;
    }

    private boolean processVerticalDistance(ElectricLine electricalLine, PlanDetail planDetail, String remarks1,
            String remarks2) {

        boolean verticalDistancePassed = false;

        if (electricalLine.getVerticalDistance() != null) {
            String actualResult = electricalLine.getVerticalDistance().toString() + DcrConstants.IN_METER;
            String expectedResult = "";

            if (electricalLine.getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) < 0) {

                expectedResult = VERTICAL_DISTANCE_11000.toString() + DcrConstants.IN_METER;
                if (electricalLine.getVerticalDistance().compareTo(VERTICAL_DISTANCE_11000) >= 0)
                    verticalDistancePassed = true;

            } else if (electricalLine.getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) >= 0
                    && electricalLine.getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) <= 0) {

                expectedResult = VERTICAL_DISTANCE_33000.toString() + DcrConstants.IN_METER;
                if (electricalLine.getVerticalDistance().compareTo(VERTICAL_DISTANCE_33000) >= 0)
                    verticalDistancePassed = true;

            } else if (electricalLine.getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) > 0) {

                Double totalVertficalOHE = VERTICAL_DISTANCE_33000.doubleValue() + 0.3 *
                        Math.ceil(
                                electricalLine.getVoltage().subtract(BigDecimal.valueOf(VOLTAGE_33000))
                                        .divide(BigDecimal.valueOf(VOLTAGE_33000), 2, RoundingMode.HALF_UP)
                                        .doubleValue());
                expectedResult = totalVertficalOHE + DcrConstants.IN_METER;
                if (electricalLine.getVerticalDistance()
                        .compareTo(BigDecimal.valueOf(totalVertficalOHE)) >= 0)
                    verticalDistancePassed = true;
            }
            if (verticalDistancePassed) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(SUB_RULE_5_DESCRIPTION, SUB_RULE_5,
                                DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(),
                                DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(),
                                expectedResult, actualResult,
                                Result.Accepted, null));
                setReportOutputDetails(planDetail, SUB_RULE_5,
                        DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(), expectedResult,
                        actualResult, Result.Accepted.getResultVal(), remarks1,
                        electricalLine.getVoltage().toString() + DcrConstants.IN_KV);
            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(SUB_RULE_5_DESCRIPTION, SUB_RULE_5,
                                DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(),
                                DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(),
                                expectedResult, actualResult,
                                Result.Not_Accepted, null));
                setReportOutputDetails(planDetail, SUB_RULE_5,
                        DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE + electricalLine.getNumber(), expectedResult,
                        actualResult, Result.Not_Accepted.getResultVal(), remarks2,
                        electricalLine.getVoltage().toString() + DcrConstants.IN_KV);
            }

        }
        return verticalDistancePassed;
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDesc, String expected, String actual,
            String status, String remarks, String voltage) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        details.put(REMARKS, remarks);
        details.put(VOLTAGE, voltage);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

}
