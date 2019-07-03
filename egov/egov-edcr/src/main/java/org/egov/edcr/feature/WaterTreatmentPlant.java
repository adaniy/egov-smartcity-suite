package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.INSITU_WASTE_TREATMENT_PLANT;
import static org.egov.edcr.utility.DcrConstants.OBJECTDEFINED_DESC;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED_DESC;
import static org.egov.edcr.utility.ParametersConstants.BUILT_UP_AREA;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.utility.LiquidWasteTreatementPlant;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class WaterTreatmentPlant extends GeneralRule implements RuleService {
    private static final String SUB_RULE_53_5_DESCRIPTION = "Liquid waste management treatment plant ";
    private static final String SUB_RULE_53_5 = "53(5)";
    private static final BigDecimal TWOTHOUSANDFIVEHUNDER = BigDecimal.valueOf(2500);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // Water treatement plant
        if (doc.containsDXFLayer(INSITU_WASTE_TREATMENT_PLANT)) {
            List<DXFLWPolyline> waterTreatementPlanPolyLines = Util.getPolyLinesByLayer(doc, INSITU_WASTE_TREATMENT_PLANT);
            if (waterTreatementPlanPolyLines != null && !waterTreatementPlanPolyLines.isEmpty())
                for (DXFLWPolyline pline : waterTreatementPlanPolyLines) {
                    LiquidWasteTreatementPlant liquidWaste = new LiquidWasteTreatementPlant();
                    liquidWaste.setPresentInDxf(true);
                    liquidWaste.setPolyLine(pline);
                    pl.getUtility().addLiquidWasteTreatementPlant(liquidWaste);
                }
        }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && pl.getUtility() != null)
            // liquid waste treatment plant defined or not
            if (!pl.getVirtualBuilding().getOccupancies().isEmpty())
                for (OccupancyType occupancyType : pl.getVirtualBuilding().getOccupancies())
                    if (checkOccupancyTypeEqualsToNonConditionalOccupancyTypes(occupancyType)
                            && pl.getUtility().getLiquidWasteTreatementPlant().isEmpty()) {
                        errors.put(SUB_RULE_53_5_DESCRIPTION,
                                edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                        SUB_RULE_53_5_DESCRIPTION }, LocaleContextHolder.getLocale()));
                        pl.addErrors(errors);
                        break;
                    } else if (checkOccupancyTypeEqualsToConditionalOccupancyTypes(occupancyType)
                            && pl.getVirtualBuilding().getTotalBuitUpArea() != null
                            && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(TWOTHOUSANDFIVEHUNDER) > 0
                            && pl.getUtility().getLiquidWasteTreatementPlant().isEmpty()) {
                        errors.put(SUB_RULE_53_5_DESCRIPTION,
                                edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                        SUB_RULE_53_5_DESCRIPTION }, LocaleContextHolder.getLocale()));
                        pl.addErrors(errors);
                        break;
                    }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("INSITU_WASTE_TREATMENT_PLANT");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
        parameters.add(BUILT_UP_AREA);
        return parameters;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.setKey("Common_Water Treatment Plant");
        if (!pl.getVirtualBuilding().getOccupancies().isEmpty())
            for (OccupancyType occupancyType : pl.getVirtualBuilding().getOccupancies())
                if (checkOccupancyTypeEqualsToNonConditionalOccupancyTypes(occupancyType)) {
                    processLiquidWasteTreatment(pl);
                    break;
                } else if (checkOccupancyTypeEqualsToConditionalOccupancyTypes(occupancyType)
                        && pl.getVirtualBuilding().getTotalBuitUpArea() != null
                        && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(TWOTHOUSANDFIVEHUNDER) > 0) {
                    processLiquidWasteTreatment(pl);
                    break;
                }
        return pl;
    }

    private void processLiquidWasteTreatment(PlanDetail pl) {
        if (!pl.getUtility().getLiquidWasteTreatementPlant().isEmpty()) {
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(SUB_RULE_53_5_DESCRIPTION, SUB_RULE_53_5, SUB_RULE_53_5_DESCRIPTION,
                            SUB_RULE_53_5_DESCRIPTION,
                            null,
                            null,
                            Result.Accepted, OBJECTDEFINED_DESC));
            setReportOutputDetailsWithoutOccupancy(pl, SUB_RULE_53_5, SUB_RULE_53_5_DESCRIPTION, "",
                    OBJECTDEFINED_DESC, Result.Accepted.getResultVal());
            return;
        } else {
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(SUB_RULE_53_5_DESCRIPTION, SUB_RULE_53_5, SUB_RULE_53_5_DESCRIPTION,
                            SUB_RULE_53_5_DESCRIPTION,
                            null,
                            null,
                            Result.Not_Accepted, OBJECTNOTDEFINED_DESC));
            setReportOutputDetailsWithoutOccupancy(pl, SUB_RULE_53_5, SUB_RULE_53_5_DESCRIPTION, "",
                    OBJECTNOTDEFINED_DESC, Result.Not_Accepted.getResultVal());
            return;
        }
    }

    private boolean checkOccupancyTypeEqualsToNonConditionalOccupancyTypes(OccupancyType occupancyType) {
        return occupancyType.equals(OccupancyType.OCCUPANCY_B1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_B2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_B3) || occupancyType.equals(OccupancyType.OCCUPANCY_C) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C1) || occupancyType.equals(OccupancyType.OCCUPANCY_C2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C3) || occupancyType.equals(OccupancyType.OCCUPANCY_D) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_D1) || occupancyType.equals(OccupancyType.OCCUPANCY_D2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_G1) || occupancyType.equals(OccupancyType.OCCUPANCY_G2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_H) || occupancyType.equals(OccupancyType.OCCUPANCY_I1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_I2);
    }

    private boolean checkOccupancyTypeEqualsToConditionalOccupancyTypes(OccupancyType occupancyType) {
        return occupancyType.equals(OccupancyType.OCCUPANCY_A1) || occupancyType.equals(OccupancyType.OCCUPANCY_A2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_A3) || occupancyType.equals(OccupancyType.OCCUPANCY_A4) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_A5) || occupancyType.equals(OccupancyType.OCCUPANCY_E) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F) || occupancyType.equals(OccupancyType.OCCUPANCY_F1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3)
                || occupancyType.equals(OccupancyType.OCCUPANCY_F4);
    }

    private void setReportOutputDetailsWithoutOccupancy(PlanDetail pl, String ruleNo, String ruleDesc, String expected,
            String actual, String status) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }
}
