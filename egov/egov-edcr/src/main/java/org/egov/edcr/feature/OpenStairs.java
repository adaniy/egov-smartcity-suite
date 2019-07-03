package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.OpenStair;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFMText;
import org.kabeja.dxf.helpers.StyledTextParagraph;
import org.springframework.stereotype.Service;

@Service
public class OpenStairs extends GeneralRule implements RuleService {
    private static final Logger LOG = Logger.getLogger(OpenStairs.class);
    private static final String SUB_RULE_24_11 = "24(11)";
    private static final BigDecimal OPENSTAIR_DISTANCE = BigDecimal.valueOf(0.60);

    @Override
    public PlanDetail extract(PlanDetail planDetail, DXFDocument doc) {

        for (Block block : planDetail.getBlocks())
            extractOpenStairs(doc, block, planDetail);
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
            if (block.getBuilding() != null && block.getOpenStairs() != null
                    && block.getOpenStairs().size() > 0) {

                scrutinyDetail = new ScrutinyDetail();
                scrutinyDetail.addColumnHeading(1, RULE_NO);
                scrutinyDetail.addColumnHeading(2, REQUIRED);
                scrutinyDetail.addColumnHeading(3, PROVIDED);
                scrutinyDetail.addColumnHeading(4, STATUS);
                scrutinyDetail.setHeading("Open Stair");
                scrutinyDetail.setKey("Block_" + block.getName() + "_OPEN STAIR");

                for (Measurement measurement : block.getOpenStairs())
                    if (measurement.getMinimumDistance().setScale(2, RoundingMode.HALF_UP).compareTo(OPENSTAIR_DISTANCE) >= 0)
                        setReportOutputDetails(planDetail, SUB_RULE_24_11, String.format(SUB_RULE_24_11, block.getNumber()),
                                OPENSTAIR_DISTANCE.toString(),
                                measurement.getMinimumDistance().toString(), Result.Accepted.getResultVal(), scrutinyDetail);
                    else
                        setReportOutputDetails(planDetail, SUB_RULE_24_11, String.format(SUB_RULE_24_11, block.getNumber()),
                                OPENSTAIR_DISTANCE.toString(),
                                measurement.getMinimumDistance().toString(), Result.Not_Accepted.getResultVal(), scrutinyDetail);
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

    private void extractOpenStairs(DXFDocument doc, Block block, PlanDetail planDetail) {

        String openStairNamePattern = String.format("BLK_%s_OPEN_STAIR", block.getNumber());

        List<String> openStairNames = Util.getLayerNamesLike(doc, openStairNamePattern);

        if (!openStairNames.isEmpty())
            for (String openStairName : openStairNames) {
                String[] stairName = openStairName.split("_");
                if (stairName.length == 4 && stairName[3] != null && !stairName[3].isEmpty()) {
                    List<DXFDimension> lines = Util.getDimensionsByLayer(doc, openStairName);
                    System.out.println("BLOCK " + block.getNumber());
                    if (lines != null)
                        for (Object dxfEntity : lines) {
                            BigDecimal value;
                            DXFDimension line = (DXFDimension) dxfEntity;
                            String dimensionBlock = line.getDimensionBlock();
                            DXFBlock dxfBlock = doc.getDXFBlock(dimensionBlock);
                            Iterator dxfEntitiesIterator = dxfBlock.getDXFEntitiesIterator();
                            while (dxfEntitiesIterator.hasNext()) {
                                DXFEntity e = (DXFEntity) dxfEntitiesIterator.next();
                                if (e.getType().equals(DXFConstants.ENTITY_TYPE_MTEXT)) {
                                    DXFMText text = (DXFMText) e;
                                    String text2 = text.getText();

                                    Iterator styledParagraphIterator = text.getTextDocument().getStyledParagraphIterator();

                                    while (styledParagraphIterator.hasNext()) {
                                        StyledTextParagraph next = (StyledTextParagraph) styledParagraphIterator.next();
                                        text2 = next.getText();
                                    }
                                    if (text2.contains(";")) {
                                        String[] textSplit = text2.split(";");
                                        int length = textSplit.length;

                                        if (length >= 1) {
                                            int index = length - 1;
                                            text2 = textSplit[index];
                                            text2 = text2.replaceAll("[^\\d.]", "");
                                        } else
                                            text2 = text2.replaceAll("[^\\d.]", "");
                                    } else
                                        text2 = text2.replaceAll("[^\\d.]", "");

                                    if (!text2.isEmpty()) {
                                        value = getNumericValue(text2, planDetail, openStairName);
                                        OpenStair openStair = new OpenStair();
                                        openStair.setMinimumDistance(value);
                                        block.getOpenStairs().add(openStair);
                                    }
                                }
                            }

                        }
                }

            }
    }

}
