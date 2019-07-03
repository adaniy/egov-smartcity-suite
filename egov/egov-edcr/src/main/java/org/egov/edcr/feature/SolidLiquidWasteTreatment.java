
package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.SOLID_LIQUID_WASTE_TREATMENT;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.FloorUnit;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.utility.SolidLiqdWasteTrtmnt;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.stereotype.Service;

@Service
public class SolidLiquidWasteTreatment extends GeneralRule implements RuleService {

    public static final String SUBRULE_55_11_DESC = "Collection and disposal of solid and liquid Waste";

    private static final String SUBRULE_55_11 = "55(11)";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // Solid liquid waste treatment
        if (doc.containsDXFLayer(SOLID_LIQUID_WASTE_TREATMENT)) {
            List<DXFLWPolyline> solidLiquidWastePolyLines = Util.getPolyLinesByLayer(doc, SOLID_LIQUID_WASTE_TREATMENT);
            if (!solidLiquidWastePolyLines.isEmpty())
                for (DXFLWPolyline polyLine : solidLiquidWastePolyLines) {
                    SolidLiqdWasteTrtmnt solidLiqdWasteTrtmnt = new SolidLiqdWasteTrtmnt();
                    solidLiqdWasteTrtmnt.setPolyLine(polyLine);
                    solidLiqdWasteTrtmnt.setPresentInDxf(true);
                    pl.getUtility().addSolidLiqdWasteTrtmnt(solidLiqdWasteTrtmnt);
                }
        }
        return pl;

    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("SOLID_LIQUID_WASTE_TREATMENT");
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
        processSolidLiquidWasteTreat(pl);
        return pl;
    }

    private void processSolidLiquidWasteTreat(PlanDetail pl) {
        validate(pl);
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, FIELDVERIFIED);
        scrutinyDetail.addColumnHeading(4, STATUS);
        scrutinyDetail.setKey("Common_Collection and disposal of Solid and Liquid Waste");
        if (pl != null && !pl.getBlocks().isEmpty()) {
            Boolean isFound = false;
            for (Block b : pl.getBlocks())
                for (Floor f : b.getBuilding().getFloors())
                    for (FloorUnit unit : f.getUnits())
                        if (OccupancyType.OCCUPANCY_E.equals(unit.getOccupancy().getType()))
                            isFound = true;
            if (isFound && pl.getUtility().getSolidLiqdWasteTrtmnt().isEmpty()) {

                Map<String, String> details = new HashMap<>();
                details.put(RULE_NO, SUBRULE_55_11);
                details.put(DESCRIPTION, SUBRULE_55_11_DESC);
                /*
                 * Marked as verify. As per rule, This rule applicable for wedding hall. There is no colour code specific to
                 * identify business. For other type of business, this might not mandatory.
                 */
                details.put(FIELDVERIFIED, "Not Defined in plan. Verify whether required for defined Business.");
                details.put(STATUS, Result.Verify.getResultVal());
                scrutinyDetail.getDetail().add(details);
                pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
            } else if (isFound && !pl.getUtility().getSolidLiqdWasteTrtmnt().isEmpty()) {

                Map<String, String> details = new HashMap<>();
                details.put(RULE_NO, SUBRULE_55_11);
                details.put(DESCRIPTION, SUBRULE_55_11_DESC);
                details.put(FIELDVERIFIED, "Defined in the Plan.");
                details.put(STATUS, Result.Accepted.getResultVal());
                scrutinyDetail.getDetail().add(details);
                pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

            }
        }
    }

}
