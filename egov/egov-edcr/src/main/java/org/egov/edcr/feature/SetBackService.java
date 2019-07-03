package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.BLOCK_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.BSMNT_FOOT_PRINT;
import static org.egov.edcr.constants.DxfFileConstants.BSMNT_FRONT_YARD;
import static org.egov.edcr.constants.DxfFileConstants.BSMNT_REAR_YARD;
import static org.egov.edcr.constants.DxfFileConstants.BSMNT_SIDE_YARD_1;
import static org.egov.edcr.constants.DxfFileConstants.BSMNT_SIDE_YARD_2;
import static org.egov.edcr.constants.DxfFileConstants.FRONT_YARD;
import static org.egov.edcr.constants.DxfFileConstants.LEVEL_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.REAR_YARD;
import static org.egov.edcr.constants.DxfFileConstants.SIDE_YARD_1;
import static org.egov.edcr.constants.DxfFileConstants.SIDE_YARD_2;
import static org.egov.edcr.utility.DcrConstants.HEIGHTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.MORETHANONEPOLYLINEDEFINED;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.WRONGHEIGHTDEFINED;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.Yard;
import org.egov.edcr.entity.utility.SetBack;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.service.MinDistance;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SetBackService extends GeneralRule implements RuleService {

    private Logger logger = Logger.getLogger(SetBackService.class);

    @Autowired
    private FrontYardService frontYardService;

    @Autowired
    private SideYardService sideYardService;

    @Autowired
    private RearYardService rearYardService;

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        extractSetBack(pl, doc);
        return pl;
    }

    private void extractSetBack(PlanDetail pl, DXFDocument doc) {
        String yardName;
        // VALIDATION : CHECK NUMBER OF BLOCKS and floors. Check block height provided ?
        // Check whether level defined ? if yes, then check level height is correct format ?
        // check whether for each block setback defined ?
        // side/front/front yard.. Not necessary to define level for all the side.. if any one side define also.. we need to
        // consider
        // Each block combine multiple occupancies to decide the most restrictive occupancy.
        // if height is more than building height in the level. if more than one level, then height is mandatory from 1st level.
        // It should be greater than previous level.
        // they may or may not define yards in that case ..?? throw error ? required only other than level cases.
        // if all levels not defined, then how to using building height ?
        // extract NOC Details and opening above 2.1mt etc.
        for (Block block : pl.getBlocks()) {

            extractBasementFootPrint(doc, block);

            // based on foot prints provided, set back will be decide in general rule.
            for (SetBack setBack : block.getSetBacks())
                if (setBack.getLevel() == -1)
                    extractBasementSetBacks(pl, doc, block, setBack);
                else {
                    yardName = BLOCK_NAME_PREFIX + block.getName() + "_" + LEVEL_NAME_PREFIX + setBack.getLevel() + "_"
                            + FRONT_YARD;
                    setFrontYardDetails(pl, doc, setBack, yardName);
                    yardName = BLOCK_NAME_PREFIX + block.getName() + "_" + LEVEL_NAME_PREFIX + setBack.getLevel() + "_"
                            + REAR_YARD;
                    setRearYardDetails(pl, doc, setBack, yardName);
                    yardName = BLOCK_NAME_PREFIX + block.getName() + "_" + LEVEL_NAME_PREFIX + setBack.getLevel() + "_"
                            + SIDE_YARD_1;
                    setSideYard1Details(pl, doc, setBack, yardName);
                    yardName = BLOCK_NAME_PREFIX + block.getName() + "_" + LEVEL_NAME_PREFIX + setBack.getLevel() + "_"
                            + SIDE_YARD_2;
                    setSideYard2Details(pl, doc, yardName, setBack);
                }
        }
        pl.sortBlockByName();
        pl.sortSetBacksByLevel();

    }

    private void setSideYard2Details(PlanDetail pl, DXFDocument doc, String yardName, SetBack setBack) {
        boolean layerPresent;
        layerPresent = doc.containsDXFLayer(yardName);

        if (layerPresent) {
            Yard yard = getYard(pl, doc, yardName, setBack.getLevel());
            if (yard != null && yard.getPolyLine() != null) {
                setBack.setSideYard2(yard);
                yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, yardName, String.valueOf(setBack.getLevel()), doc));
                setYardHeight(doc, yardName, yard);
            }
        }
    }

    private void setYardHeight(DXFDocument doc, String yardName, Yard yard) {
        String height = Util.getMtextByLayerName(doc, yardName, "");// change this api to get by using layer name and text.
        if (height != null) {
            if (height.contains("="))
                height = height.split("=")[1] != null ? height.split("=")[1].replaceAll("[^\\d.]", "") : "";
            else
                height = height.replaceAll("[^\\d.]", "");

            if (!height.isEmpty())
                yard.setHeight(BigDecimal.valueOf(Double.parseDouble(height)));
        }
    }

    private Yard getYard(PlanDetail pl, DXFDocument doc, String yardName, Integer level) {
        Yard yard = new Yard();
        List<DXFLWPolyline> frontYardLines = Util.getPolyLinesByLayer(doc, yardName);

        // VALIDATE WHETHER ONE SINGLE POLYLINE PRESENT.
        if (frontYardLines != null && frontYardLines.size() > 1)
            pl.addError("", edcrMessageSource.getMessage(MORETHANONEPOLYLINEDEFINED, new String[] { yardName }, null));
        else if (frontYardLines != null && !frontYardLines.isEmpty()) {
            yard.setPolyLine(frontYardLines.get(0));
            yard.setArea(Util.getPolyLineArea(yard.getPolyLine()));

            /*
             * if(yard.getPolyLine().getBounds()!=null && yard.getPolyLine().getBounds().getWidth()>0)
             * yard.setMean(yard.getArea().divide(BigDecimal.valueOf(yard.getPolyLine().getBounds().getWidth()), 5,
             * RoundingMode.HALF_UP)); else yard.setMean(BigDecimal.ZERO); if (logger.isDebugEnabled()) logger.debug(yardName +
             * " Mean " + yard.getMean());
             */
            yard.setPresentInDxf(true);
            yard.setLevel(level);

        } /*
           * else pl.addError("", edcrMessageSource.getMessage(OBJECTNOTDEFINED, new String[] { yardName }, null));
           */

        return yard;

    }

    private void extractBasementSetBacks(PlanDetail pl, DXFDocument doc, Block block, SetBack setBack) {
        if (-1 == setBack.getLevel()) {
            String bsmntYardName = BLOCK_NAME_PREFIX + block.getNumber() + "_" + BSMNT_FRONT_YARD;
            setFrontYardDetails(pl, doc, setBack, bsmntYardName);
            bsmntYardName = BLOCK_NAME_PREFIX + block.getNumber() + "_" + BSMNT_REAR_YARD;
            setRearYardDetails(pl, doc, setBack, bsmntYardName);
            bsmntYardName = BLOCK_NAME_PREFIX + block.getNumber() + "_" + BSMNT_SIDE_YARD_1;
            setSideYard1Details(pl, doc, setBack, bsmntYardName);
            bsmntYardName = BLOCK_NAME_PREFIX + block.getNumber() + "_" + BSMNT_SIDE_YARD_2;
            setSideYard2Details(pl, doc, bsmntYardName, setBack);
        }
    }

    private void setSideYard1Details(PlanDetail pl, DXFDocument doc, SetBack setBack, String yardName) {
        boolean layerPresent;
        layerPresent = doc.containsDXFLayer(yardName);
        if (layerPresent) {
            Yard sideYard1 = getYard(pl, doc, yardName, setBack.getLevel());
            if (sideYard1 != null && sideYard1.getPolyLine() != null) {
                setBack.setSideYard1(sideYard1);
                sideYard1.setMinimumDistance(
                        MinDistance.getYardMinDistance(pl, yardName, String.valueOf(setBack.getLevel()), doc));
                setYardHeight(doc, yardName, sideYard1);
            } /*
               * else yardNotDefined(pl, yardName);
               */
        }
    }

    private void setRearYardDetails(PlanDetail pl, DXFDocument doc, SetBack setBack, String yardName) {
        boolean layerPresent;
        layerPresent = doc.containsDXFLayer(yardName);
        if (layerPresent) {
            Yard rearYard = getYard(pl, doc, yardName, setBack.getLevel());
            if (rearYard != null && rearYard.getPolyLine() != null) {
                setBack.setRearYard(rearYard);
                rearYard.setMinimumDistance(
                        MinDistance.getYardMinDistance(pl, yardName, String.valueOf(setBack.getLevel()), doc));
                setYardHeight(doc, yardName, rearYard);
            }
        }
    }

    private void setFrontYardDetails(PlanDetail pl, DXFDocument doc, SetBack setBack, String yardName) {
        boolean layerPresent = doc.containsDXFLayer(yardName);
        if (layerPresent) {
            Yard frontYard = getYard(pl, doc, yardName, setBack.getLevel());
            if (frontYard != null && frontYard.getPolyLine() != null) {
                setBack.setFrontYard(frontYard);
                frontYard.setMinimumDistance(
                        MinDistance.getYardMinDistance(pl, yardName, String.valueOf(setBack.getLevel()), doc));
                setYardHeight(doc, yardName, frontYard);
            } /*
               * else yardNotDefined(pl, yardName);
               */
        }
    }

    private void extractBasementFootPrint(DXFDocument doc, Block block) {

        String basementFootPrint = BLOCK_NAME_PREFIX + block.getNumber() + "_" + BSMNT_FOOT_PRINT;
        List<DXFLWPolyline> basementPolyline = Util.getPolyLinesByLayer(doc, basementFootPrint);
        if (!basementPolyline.isEmpty()) {
            SetBack setBack = new SetBack();
            Measurement footPrint = new Measurement();
            footPrint.setArea(Util.getPolyLineArea(basementPolyline.get(0)));
            footPrint.setPolyLine(basementPolyline.get(0));
            footPrint.setPresentInDxf(true);
            setBack.setLevel(-1);
            setBack.setBuildingFootPrint(footPrint);
            block.getSetBacks().add(setBack);
        }
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        // Assumption: if height of one level, should be less than next level. this condition not validated.as in each level user
        // can define different height.
        BigDecimal heightOfBuilding = BigDecimal.ZERO;
        for (Block block : pl.getBlocks()) {
            heightOfBuilding = block.getBuilding().getBuildingHeight();
            int i = 0;
            if (!block.getCompletelyExisting())
                for (SetBack setback : block.getSetBacks()) {
                    i++;
                    // if height not defined other than 0 level , then throw error.
                    if (setback.getLevel() == 0) {
                        // for level 0, all the yards are mandatory. Else throw error.
                        if (setback.getFrontYard() == null)
                            errors.put("frontyardNodeDefined",
                                    prepareMessage(OBJECTNOTDEFINED, " Set Back of " + block.getName() + "  at level zero "));
                        if (setback.getRearYard() == null
                                && !pl.getPlanInformation().getNocToAbutRearDesc().equalsIgnoreCase(DcrConstants.YES))
                            errors.put("rearyardNodeDefined",
                                    prepareMessage(OBJECTNOTDEFINED, " Rear Yard of  " + block.getName() + "  at level zero "));
                        if (setback.getSideYard1() == null)
                            errors.put("side1yardNodeDefined", prepareMessage(OBJECTNOTDEFINED,
                                    " Side Yard 1 of block " + block.getName() + " at level zero"));
                        if (setback.getSideYard2() == null
                                && !pl.getPlanInformation().getNocToAbutSideDesc().equalsIgnoreCase(DcrConstants.YES))
                            errors.put("side2yardNodeDefined", prepareMessage(OBJECTNOTDEFINED,
                                    " Side Yard 2 of block " + block.getName() + " at level zero "));
                    } else if (setback.getLevel() > 0) {
                        // height defined in level other than zero must contain height
                        if (setback.getFrontYard() != null && setback.getFrontYard().getHeight() == null)
                            errors.put("frontyardnotDefinedHeight", prepareMessage(HEIGHTNOTDEFINED, "Front Yard ",
                                    block.getName(), setback.getLevel().toString()));
                        if (setback.getRearYard() != null && setback.getRearYard().getHeight() == null)
                            errors.put("rearyardnotDefinedHeight", prepareMessage(HEIGHTNOTDEFINED, "Rear Yard ", block.getName(),
                                    setback.getLevel().toString()));
                        if (setback.getSideYard1() != null && setback.getSideYard1().getHeight() == null)
                            errors.put("side1yardnotDefinedHeight", prepareMessage(HEIGHTNOTDEFINED, "Side Yard 1 ",
                                    block.getName(), setback.getLevel().toString()));
                        if (setback.getSideYard2() != null && setback.getSideYard2().getHeight() == null)
                            errors.put("side2yardnotDefinedHeight", prepareMessage(HEIGHTNOTDEFINED, "Side Yard 2 ",
                                    block.getName(), setback.getLevel().toString()));
                    }

                    // if height of setback greater than building height ?
                    // last level height should match with building height.

                    if (setback.getLevel() > 0 && block.getSetBacks().size() == i) {
                        if (setback.getFrontYard() != null && setback.getFrontYard().getHeight() != null
                                && setback.getFrontYard().getHeight().compareTo(heightOfBuilding) != 0)
                            errors.put("frontyardDefinedWrongHeight", prepareMessage(WRONGHEIGHTDEFINED, "Front Yard ",
                                    block.getName(), setback.getLevel().toString(), heightOfBuilding.toString()));
                        if (setback.getRearYard() != null && setback.getRearYard().getHeight() != null
                                && setback.getRearYard().getHeight().compareTo(heightOfBuilding) != 0)
                            errors.put("rearyardDefinedWrongHeight", prepareMessage(WRONGHEIGHTDEFINED, "Rear Yard ",
                                    block.getName(), setback.getLevel().toString(), heightOfBuilding.toString()));
                        if (setback.getSideYard1() != null && setback.getSideYard1().getHeight() != null
                                && setback.getSideYard1().getHeight().compareTo(heightOfBuilding) != 0)
                            errors.put("side1yardDefinedWrongHeight", prepareMessage(WRONGHEIGHTDEFINED, "Side Yard 1 ",
                                    block.getName(), setback.getLevel().toString(), heightOfBuilding.toString()));
                        if (setback.getSideYard2() != null && setback.getSideYard2().getHeight() != null
                                && setback.getSideYard2().getHeight().compareTo(heightOfBuilding) != 0)
                            errors.put("side2yardDefinedWrongHeight", prepareMessage(WRONGHEIGHTDEFINED, "Side Yard 2 ",
                                    block.getName(), setback.getLevel().toString(), heightOfBuilding.toString()));
                    }
                }
        }
        if (errors.size() > 0)
            pl.addErrors(errors);
        return pl;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        validate(pl);
        frontYardService.processFrontYard(pl);
        sideYardService.processSideYard(pl);
        rearYardService.processRearYard(pl);
        return pl;
    }

}
