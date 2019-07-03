package org.egov.edcr.rule;

import java.util.List;

import org.egov.edcr.entity.PlanDetail;
import org.kabeja.dxf.DXFDocument;

public interface RuleService {

    PlanDetail extract(PlanDetail pl, DXFDocument doc);

    PlanDetail validate(PlanDetail pl);

    PlanDetail process(PlanDetail pl);

    List<String> getLayerNames();

    List<String> getParameters();

}
