package org.egov.edcr.feature;

import static org.egov.edcr.utility.DcrConstants.CANOPY_DISTANCE;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.ParametersConstants.PLOT_LEVEL_CHECK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.ParametersConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PetrolFillingStation extends GeneralRule implements RuleService {
    private static final String SUBRULE_59_10 = "59-(10)";
    private static final String SUBRULE_59_10_DESC = "Minimum distance from canopy to plot boundary";

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        String layer = DxfFileConstants.LAYER_CANOPY;
        List<BigDecimal> canopyDistanceFromPlotBoundary = Util.getListOfDimensionValueByLayer(doc, layer);
        pl.setCanopyDistanceFromPlotBoundary(canopyDistanceFromPlotBoundary);
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        if (pl != null && pl.getVirtualBuilding() != null && !pl.getVirtualBuilding().getOccupancies().isEmpty() &&
                pl.getVirtualBuilding().getOccupancies().contains(OccupancyType.OCCUPANCY_F4) &&
                pl.getVirtualBuilding().getOccupancies().contains(OccupancyType.OCCUPANCY_I2)
                && pl.getCanopyDistanceFromPlotBoundary().isEmpty()) {
            errors.put(CANOPY_DISTANCE,
                    edcrMessageSource.getMessage(OBJECTNOTDEFINED,
                            new String[] { CANOPY_DISTANCE }, LocaleContextHolder.getLocale()));
            pl.addErrors(errors);
        }
        return pl;
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add("DIST_CANOPY");
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
        Boolean valid = false;
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey("Common_Petrol Filling Station");
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        if (pl != null && pl.getVirtualBuilding() != null && !pl.getVirtualBuilding().getOccupancies().isEmpty() &&
                pl.getVirtualBuilding().getOccupancies().contains(OccupancyType.OCCUPANCY_F4) &&
                pl.getVirtualBuilding().getOccupancies().contains(OccupancyType.OCCUPANCY_I2)
                && !pl.getCanopyDistanceFromPlotBoundary().isEmpty()) {
            BigDecimal minimumCanopyDistanceFromPlotBoundary = pl.getCanopyDistanceFromPlotBoundary().get(0);
            for (BigDecimal canopyDistanceFromPlotBoundary : pl.getCanopyDistanceFromPlotBoundary())
                if (canopyDistanceFromPlotBoundary.compareTo(minimumCanopyDistanceFromPlotBoundary) < 0)
                    minimumCanopyDistanceFromPlotBoundary = canopyDistanceFromPlotBoundary;
            minimumCanopyDistanceFromPlotBoundary = BigDecimal.valueOf(
                    Math.round(minimumCanopyDistanceFromPlotBoundary.doubleValue() * Double.valueOf(100)) / Double.valueOf(100));
            if (minimumCanopyDistanceFromPlotBoundary.compareTo(BigDecimal.valueOf(3)) >= 0)
                valid = true;
            if (valid)
                setReportOutputDetails(pl, SUBRULE_59_10, SUBRULE_59_10_DESC, String.valueOf(3),
                        minimumCanopyDistanceFromPlotBoundary.toString(), Result.Accepted.getResultVal());
            else
                setReportOutputDetails(pl, SUBRULE_59_10, SUBRULE_59_10_DESC, String.valueOf(3),
                        minimumCanopyDistanceFromPlotBoundary.toString(), Result.Not_Accepted.getResultVal());
        }
        return pl;
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDescription, String expected, String actual,
            String status) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDescription);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

}
