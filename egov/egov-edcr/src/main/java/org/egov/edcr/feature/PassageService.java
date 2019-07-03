package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Passage;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.springframework.stereotype.Service;

@Service
public class PassageService extends GeneralRule implements RuleService {
    private static final String RULE41 = "41";
    private static final String RULE39_6 = "39(6)";
    private static final String PASSAGE_STAIR_MINIMUM_WIDTH = "1.2";
    private static final String RULE39_6_DESCRIPTION = "The minimum passage giving access to stair";
    private static final String RULE_41_DESCRIPTION = "The minimum width of corridors/ verandhas";

    @Override
    public PlanDetail extract(PlanDetail planDetail, DXFDocument doc) {

        List<Block> blocks = planDetail.getBlocks();

        for (Block block : blocks)
            if (block.getBuilding() != null) {
                List<BigDecimal> passagePolylines = Util.getListOfDimensionValueByLayer(doc, DxfFileConstants.PASSAGE);
                List<BigDecimal> passageStairPolylines = Util.getListOfDimensionValueByLayer(doc, DxfFileConstants.PASSAGE_STAIR);

                if (passagePolylines != null && passagePolylines.size() > 0
                        || passageStairPolylines != null && passagePolylines.size() > 0) {
                    Passage passage = new Passage();
                    passage.setPassagePolyLines(passagePolylines);
                    passage.setPassageStairPolyLines(passageStairPolylines);
                    block.getBuilding().setPassage(passage);
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
        for (Block block : planDetail.getBlocks())
            if (block.getBuilding() != null) {

                ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, REQUIRED);
                scrutinyDetail.addColumnHeading(3, PROVIDED);
                scrutinyDetail.addColumnHeading(4, STATUS);
                scrutinyDetail.setKey("Block_" + block.getNumber() + "_" + "Passage");

                ScrutinyDetail scrutinyDetail1 = new ScrutinyDetail();
                scrutinyDetail1.addColumnHeading(1, RULE_NO);
                scrutinyDetail1.addColumnHeading(2, REQUIRED);
                scrutinyDetail1.addColumnHeading(3, PROVIDED);
                scrutinyDetail1.addColumnHeading(4, STATUS);
                scrutinyDetail1.setKey("Block_" + block.getNumber() + "_" + "Passage Stair");

                Passage passage = block.getBuilding().getPassage();

                if (passage != null) {

                    List<BigDecimal> passagePolylines = passage.getPassagePolyLines();
                    List<BigDecimal> passageStairPolylines = passage.getPassageStairPolyLines();

                    if (passagePolylines != null && passagePolylines.size() > 0) {

                        BigDecimal minPassagePolyLine = passagePolylines.stream().reduce(BigDecimal::min).get();

                        BigDecimal minWidth = Util.roundOffTwoDecimal(minPassagePolyLine);

                        if (minWidth.compareTo(BigDecimal.ONE) >= 0)
                            setReportOutputDetails(planDetail, RULE41, RULE_41_DESCRIPTION,
                                    String.valueOf(1), String.valueOf(minWidth), Result.Accepted.getResultVal(),
                                    scrutinyDetail);
                        else
                            setReportOutputDetails(planDetail, RULE41, RULE_41_DESCRIPTION,
                                    String.valueOf(1), String.valueOf(minWidth), Result.Not_Accepted.getResultVal(),
                                    scrutinyDetail);
                    }

                    if (passageStairPolylines != null && passageStairPolylines.size() > 0) {

                        BigDecimal minPassageStairPolyLine = passageStairPolylines.stream().reduce(BigDecimal::min).get();
                        ;

                        BigDecimal minWidth = Util.roundOffTwoDecimal(minPassageStairPolyLine);

                        if (minWidth.compareTo(Util.roundOffTwoDecimal(BigDecimal.valueOf(1.2))) >= 0)
                            setReportOutputDetails(planDetail, RULE39_6, RULE39_6_DESCRIPTION,
                                    PASSAGE_STAIR_MINIMUM_WIDTH, String.valueOf(minWidth), Result.Accepted.getResultVal(),
                                    scrutinyDetail1);
                        else
                            setReportOutputDetails(planDetail, RULE39_6, RULE39_6_DESCRIPTION,
                                    PASSAGE_STAIR_MINIMUM_WIDTH, String.valueOf(minWidth), Result.Not_Accepted.getResultVal(),
                                    scrutinyDetail1);
                    }

                }
            }
        return planDetail;
    }

    private void setReportOutputDetails(PlanDetail pl, String ruleNo, String ruleDesc, String expected, String actual,
            String status, ScrutinyDetail scrutinyDetail) {
        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(REQUIRED, expected);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

    @Override
    public List<String> getLayerNames() {
        List<String> layers = new ArrayList<>();
        layers.add(DxfFileConstants.PASSAGE);
        layers.add(DxfFileConstants.PASSAGE_STAIR);

        return layers;
    }

}
