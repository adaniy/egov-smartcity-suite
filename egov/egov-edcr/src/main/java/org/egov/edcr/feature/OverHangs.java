package org.egov.edcr.feature;

import static org.egov.edcr.utility.DcrConstants.SHADE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.Polygon;
import org.egov.edcr.utility.math.Ray;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.springframework.stereotype.Service;

@Service
public class OverHangs extends GeneralRule implements RuleService {
    private static final Logger LOG = Logger.getLogger(OverHangs.class);
    private static final String SUB_RULE_24_10 = "24(10)";
    final Ray rayCasting = new Ray(new Point(-1.123456789, -1.987654321, 0d));

    @Override
    public PlanDetail extract(PlanDetail planDetail, DXFDocument doc) {
        List<DXFLWPolyline> polyLinesByLayer;

        for (Block b : planDetail.getBlocks()) {
            polyLinesByLayer = Util.getPolyLinesByLayer(doc,
                    DxfFileConstants.BLOCK_NAME_PREFIX + b.getNumber() + "_" + DxfFileConstants.SHADE_OVERHANG);
            if (!polyLinesByLayer.isEmpty()) {
                Measurement shade = new Measurement();
                shade.setPolyLine(polyLinesByLayer.get(0));
                b.getBuilding().setShade(shade);
            }
        }
        return planDetail;
    }

    @Override
    public PlanDetail validate(PlanDetail planDetail) {

        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        List<Block> blocks = planDetail.getBlocks();
        for (Block block : blocks)
            if (block.getBuilding() != null &&
                    block.getBuilding().getShade() != null && block.getBuilding().getShade().getPolyLine() != null
                    && planDetail.getPlot().getPolyLine() != null) {

                scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, REQUIRED);
                scrutinyDetail.addColumnHeading(3, PROVIDED);
                scrutinyDetail.addColumnHeading(4, STATUS);
                scrutinyDetail.setHeading(SHADE);
                scrutinyDetail.setKey("Block_" + block.getName() + "_" + SHADE);

                Polygon plotPolygon = Util.getPolygon(planDetail.getPlot().getPolyLine());

                Iterator shadeIterator = block.getBuilding().getShade().getPolyLine().getVertexIterator();
                Boolean shadeOutSideBoundary = false;

                while (shadeIterator.hasNext()) {
                    DXFVertex dxfVertex = (DXFVertex) shadeIterator.next();
                    Point point = dxfVertex.getPoint();
                    if (!rayCasting.contains(point, plotPolygon))
                        shadeOutSideBoundary = true;

                }

                Map<String, String> details = new HashMap<>();
                details.put(RULE_NO, SUB_RULE_24_10);
                details.put(REQUIRED, DcrConstants.SHADE + " for block " + block.getNumber() + " Should be inside Plot Boundary");

                if (shadeOutSideBoundary) {
                    details.put(PROVIDED, DcrConstants.SHADE + " for block " + block.getNumber() + "  is outside Plot Boundary");
                    details.put(STATUS, Result.Not_Accepted.getResultVal());
                } else {
                    details.put(PROVIDED, DcrConstants.SHADE + " for block " + block.getNumber() + "  is inside Plot Boundary");
                    details.put(STATUS, Result.Accepted.getResultVal());
                }

                scrutinyDetail.getDetail().add(details);
                planDetail.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

            }
        return planDetail;
    }

}
