package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.LAYER_BIOMETRIC_WASTE_TREATMENT;
import static org.egov.edcr.utility.DcrConstants.BIOMETRIC_WASTE_TREATMENT;
import static org.egov.edcr.utility.DcrConstants.OBJECTDEFINED_DESC;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED_DESC;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.BiometricWasteTreatment;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BiometricWasteManagement extends GeneralRule implements RuleService {
    private static final String SUBRULE_54_4_DESC = "Biomedical Waste Treatment";
    private static final String SUBRULE_54_4 = "54(4)";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // biometric waste treatment
        if (doc.containsDXFLayer(LAYER_BIOMETRIC_WASTE_TREATMENT)) {
            List<DXFLWPolyline> biometricWastePolyLines = Util.getPolyLinesByLayer(doc, LAYER_BIOMETRIC_WASTE_TREATMENT);
            if (biometricWastePolyLines != null && !biometricWastePolyLines.isEmpty())
                for (DXFLWPolyline polyLine : biometricWastePolyLines) {
                    BiometricWasteTreatment biometricWasteTreatment = new BiometricWasteTreatment();
                    biometricWasteTreatment.setPolyLine(polyLine);
                    biometricWasteTreatment.setPresentInDxf(true);
                    pl.getUtility().addBiometricWasteTreatment(biometricWasteTreatment);
                }
        }
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        // biometric waste treatment defined or not
        if (pl != null && pl.getVirtualBuilding() != null && !pl.getVirtualBuilding().getOccupancies().isEmpty())
            for (OccupancyType occupancyType : pl.getVirtualBuilding().getOccupancies())
                if ((occupancyType.equals(OccupancyType.OCCUPANCY_C) || occupancyType.equals(OccupancyType.OCCUPANCY_C1) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_C2) || occupancyType.equals(OccupancyType.OCCUPANCY_C3)) &&
                        pl.getUtility() != null && pl.getUtility().getBiometricWasteTreatment().isEmpty()) {
                    errors.put(BIOMETRIC_WASTE_TREATMENT,
                            edcrMessageSource.getMessage(OBJECTNOTDEFINED,
                                    new String[] { BIOMETRIC_WASTE_TREATMENT }, LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);
                    break;
                }

        return pl;

    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("BIOMETRIC_WASTE_MNGMNT");
        return layers;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add(PLOT_LEVEL_CHECK);
        parameters.add(ParametersConstants.OCCUPANCY);
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
        scrutinyDetail.setKey("Common_Biometric Waste Treatment");
        String subRule = SUBRULE_54_4;
        if (pl != null && pl.getVirtualBuilding() != null && !pl.getVirtualBuilding().getOccupancies().isEmpty())
            for (OccupancyType occupancyType : pl.getVirtualBuilding().getOccupancies())
                if ((occupancyType.equals(OccupancyType.OCCUPANCY_C) || occupancyType.equals(OccupancyType.OCCUPANCY_C1) ||
                        occupancyType.equals(OccupancyType.OCCUPANCY_C2) || occupancyType.equals(OccupancyType.OCCUPANCY_C3)) &&
                        pl.getUtility() != null)
                    if (!pl.getUtility().getBiometricWasteTreatment().isEmpty()) {
                        pl.reportOutput
                                .add(buildRuleOutputWithSubRule(SUBRULE_54_4_DESC, subRule, SUBRULE_54_4_DESC,
                                        BIOMETRIC_WASTE_TREATMENT,
                                        null,
                                        null,
                                        Result.Accepted, OBJECTDEFINED_DESC));
                        setReportOutputDetailsWithoutOccupancy(pl, subRule, SUBRULE_54_4_DESC, "",
                                OBJECTDEFINED_DESC, Result.Accepted.getResultVal());
                        break;
                    } else if (pl.getUtility().getBiometricWasteTreatment().isEmpty()) {
                        pl.reportOutput
                                .add(buildRuleOutputWithSubRule(SUBRULE_54_4_DESC, subRule, SUBRULE_54_4_DESC,
                                        BIOMETRIC_WASTE_TREATMENT,
                                        null,
                                        null,
                                        Result.Not_Accepted, OBJECTNOTDEFINED_DESC));
                        setReportOutputDetailsWithoutOccupancy(pl, subRule, SUBRULE_54_4_DESC, "",
                                OBJECTNOTDEFINED_DESC, Result.Not_Accepted.getResultVal());
                        break;
                    }
        return pl;
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
