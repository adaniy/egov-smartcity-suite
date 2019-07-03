package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.RECYCLING_WASTE_WATER;
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
import org.egov.edcr.entity.utility.WasteWaterRecyclePlant;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RecycleWasteWater extends GeneralRule implements RuleService {
    private static final BigDecimal ONETHOUSANDFIVEHUNDER = BigDecimal.valueOf(1500);
    private static final String SUB_RULE_53_6_DESCRIPTION = "Recycling and reuse of waste water generated facility ";
    private static final String SUB_RULE_53_6 = "53(6)";
    private static final BigDecimal TWOTHOUSANDFIVEHUNDER = BigDecimal.valueOf(2500);

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // Recycling waste water
        if (doc.containsDXFLayer(RECYCLING_WASTE_WATER)) {
            List<DXFLWPolyline> recycleWasteWaterPolyline = Util.getPolyLinesByLayer(doc, RECYCLING_WASTE_WATER);
            if (recycleWasteWaterPolyline != null && !recycleWasteWaterPolyline.isEmpty())
                for (DXFLWPolyline pline : recycleWasteWaterPolyline) {
                    WasteWaterRecyclePlant waterWaterRecyclePlant = new WasteWaterRecyclePlant();
                    waterWaterRecyclePlant.setPresentInDxf(true);
                    waterWaterRecyclePlant.setPolyLine(pline);
                    pl.getUtility().addWasteWaterRecyclePlant(waterWaterRecyclePlant);
                }
        }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && pl.getUtility() != null)
            // waste water recycle plant defined or not
            if (!pl.getVirtualBuilding().getOccupancies().isEmpty())
                for (OccupancyType occupancyType : pl.getVirtualBuilding().getOccupancies())
                    if (checkOccupancyTypeEqualsToNonConditionalOccupancyTypes(occupancyType)
                            && pl.getUtility().getWasteWaterRecyclePlant().isEmpty()) {
                        errors.put(SUB_RULE_53_6_DESCRIPTION,
                                edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                        SUB_RULE_53_6_DESCRIPTION }, LocaleContextHolder.getLocale()));
                        pl.addErrors(errors);
                        break;
                    } else if (checkOccupancyTypeEqualsToConditionalOccupancyTypes(occupancyType)
                            && pl.getVirtualBuilding().getTotalBuitUpArea() != null
                            && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(ONETHOUSANDFIVEHUNDER) > 0
                            && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(TWOTHOUSANDFIVEHUNDER) < 0
                            && pl.getUtility().getWasteWaterRecyclePlant().isEmpty()) {
                        errors.put(SUB_RULE_53_6_DESCRIPTION,
                                edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] {
                                        SUB_RULE_53_6_DESCRIPTION }, LocaleContextHolder.getLocale()));
                        break;
                    }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("RECYCLING_WASTE_WATER");
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
        scrutinyDetail.setKey("Common_Waste Water Recycle Plant");
        if (!pl.getVirtualBuilding().getOccupancies().isEmpty())
            for (OccupancyType occupancyType : pl.getVirtualBuilding().getOccupancies())
                if (checkOccupancyTypeEqualsToNonConditionalOccupancyTypes(occupancyType)) {
                    processWasteWaterRecyclePlant(pl);
                    break;
                } else if (checkOccupancyTypeEqualsToConditionalOccupancyTypes(occupancyType)
                        && pl.getVirtualBuilding().getTotalBuitUpArea() != null
                        && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(ONETHOUSANDFIVEHUNDER) > 0
                        && pl.getVirtualBuilding().getTotalBuitUpArea().compareTo(TWOTHOUSANDFIVEHUNDER) < 0) {
                    processWasteWaterRecyclePlant(pl);
                    break;
                }

        return pl;
    }

    private void processWasteWaterRecyclePlant(PlanDetail pl) {
        if (!pl.getUtility().getWasteWaterRecyclePlant().isEmpty()) {
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(SUB_RULE_53_6_DESCRIPTION, SUB_RULE_53_6, SUB_RULE_53_6_DESCRIPTION,
                            SUB_RULE_53_6_DESCRIPTION,
                            null,
                            null,
                            Result.Accepted, OBJECTDEFINED_DESC));
            setReportOutputDetailsWithoutOccupancy(pl, SUB_RULE_53_6, SUB_RULE_53_6_DESCRIPTION, "",
                    OBJECTDEFINED_DESC, Result.Accepted.getResultVal());
            return;
        } else {
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(SUB_RULE_53_6_DESCRIPTION, SUB_RULE_53_6, SUB_RULE_53_6_DESCRIPTION,
                            SUB_RULE_53_6_DESCRIPTION,
                            null,
                            null,
                            Result.Not_Accepted, OBJECTNOTDEFINED_DESC));
            setReportOutputDetailsWithoutOccupancy(pl, SUB_RULE_53_6, SUB_RULE_53_6_DESCRIPTION, "",
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
