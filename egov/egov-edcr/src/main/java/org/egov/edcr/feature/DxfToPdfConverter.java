package org.egov.edcr.feature;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.egov.edcr.utility.DcrConstants.FILESTORE_MODULECODE;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.egov.edcr.entity.EdcrPdfDetail;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.kabeja.batik.tools.SAXPDFSerializer;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFInsert;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFMText;
import org.kabeja.dxf.DXFSolid;
import org.kabeja.dxf.DXFText;
import org.kabeja.dxf.DXFVariable;
import org.kabeja.dxf.helpers.StyledTextParagraph;
import org.kabeja.svg.SVGGenerator;
import org.kabeja.xml.SAXSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DxfToPdfConverter extends GeneralRule implements RuleService {

    private static final Logger LOG = Logger.getLogger(DxfToPdfConverter.class);

    private static final String APPLICATION_PDF = "application/pdf";

    private static final String MULTIPLE_LAYER = "Multiple layers is defined with %s";
    private static final String MINIMUM_ONE_LAYER = "Minimum one layer has to be defined for %s layer like %s_1.";
    private static final String LAYER_NOT_DEFINED = "%s is not defined.";
    private static final String NEGATIVE_WIDTH = "Negative width defined in block ";
    private static final String FLOOR_PLAN = "FLOOR_PLAN_" + "\\d";
    private static final String DETAILS_PLAN = "DETAILS_" + "\\d";
    private static final String SITE_PLAN = "SITE_PLAN";
    private static final String SERVICE_PLAN = "SERVICE_PLAN";
    private static final String PARKING_PLAN = "PARKING_PLAN";
    private static final String UNDERLINE_CAPITAL = "\\L";
    private static final String UNDERLINE_SMALL = "\\l";
    // DXFTEXT.VALIGN_TOP = 3 meaning the text is alligned vertical to the top
    private static final int TEXT_VALLIGNMENT_TOP = 3;
    private static final String POWER = "Â";

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private AppConfigValueService appConfigValueService;

    @Override
    public PlanDetail extract(PlanDetail planDetail, DXFDocument doc) {
        return planDetail;
    }

    @Override
    public PlanDetail validate(PlanDetail planDetail) {

        ArrayList<EdcrPdfDetail> edcrPdfDetails = new ArrayList<>();

        // List<String> keys = Arrays.asList(DcrConstants.EDCR_DXF_PDF);

        List<String> keys = Arrays.asList("layerOneToNine");
        List<String> appConfigValues = getAppConfigValues(keys);

        // HashMap<String, String> planDetailErrors = new HashMap<>();

        for (String appConfigValue : appConfigValues) {

            DXFDocument dxfDocument = planDetail.getDxfDocument();
            List<String> layerNameList = Util.getLayerNamesLike(dxfDocument, appConfigValue);

            String replace = appConfigValue.replace("_\\d", "");

            if (isDuplicatePresent(layerNameList)) {
                EdcrPdfDetail edcrPdfDetail = new EdcrPdfDetail();
                edcrPdfDetail.setLayer(replace);
                edcrPdfDetail.setFailureReasons(String.format(MULTIPLE_LAYER, replace));
                /*
                 * planDetailErrors.put("Multiple layer " + replace, String.format(MULTIPLE_LAYER, replace));
                 * planDetail.addErrors(planDetailErrors);
                 */
            }

            if ((appConfigValue.equalsIgnoreCase(FLOOR_PLAN)
                    || appConfigValue.equalsIgnoreCase(DETAILS_PLAN))
                    && layerNameList.size() == 0) {

                EdcrPdfDetail edcrPdfDetail = new EdcrPdfDetail();
                edcrPdfDetail.setLayer(replace);
                edcrPdfDetail.setFailureReasons(String.format(MINIMUM_ONE_LAYER, replace, replace));
                /*
                 * planDetailErrors.put("Minimum layer " + replace, String.format(MINIMUM_ONE_LAYER, replace, replace));
                 * planDetail.addErrors(planDetailErrors);
                 */
                edcrPdfDetails.add(edcrPdfDetail);
            } else {

                if ((appConfigValue.equalsIgnoreCase(SITE_PLAN)
                        || appConfigValue.equalsIgnoreCase(SERVICE_PLAN)
                        || appConfigValue.equalsIgnoreCase("") && planDetail.getParkingRequired() != null
                                && planDetail.getParkingRequired() > 0)
                        && layerNameList.size() == 0) {

                    EdcrPdfDetail edcrPdfDetail = new EdcrPdfDetail();
                    edcrPdfDetail.setLayer(appConfigValue);
                    String layerUndefined = "";

                    if (appConfigValue.equalsIgnoreCase(PARKING_PLAN))
                        layerUndefined = appConfigValue + " should be defined if the number of required parking is 1 or more.";
                    else
                        layerUndefined = String.format(LAYER_NOT_DEFINED, appConfigValue);

                    edcrPdfDetail.setFailureReasons(layerUndefined);
                    /*
                     * planDetailErrors.put(layerUndefined, layerUndefined); planDetail.addErrors(planDetailErrors);
                     */
                    edcrPdfDetails.add(edcrPdfDetail);
                }

                // get a particular layer from the document and enable the layer
                for (String layerName : layerNameList) {

                    EdcrPdfDetail edcrPdfDetail = new EdcrPdfDetail();
                    edcrPdfDetail.setLayer(layerName);

                    DXFLayer dxfLayer = dxfDocument.getDXFLayer(layerName);
                    LOG.info(layerName);

                    Iterator entityTypeIterator = dxfLayer.getDXFEntityTypeIterator();

                    List entities = new ArrayList();

                    while (entityTypeIterator.hasNext()) {
                        Object entity = entityTypeIterator.next();
                        entities.add(entity);
                    }

                    // checking whether layer is present , by default a layer 0 is returned if layer is absent
                    if (dxfLayer != null && !dxfLayer.getName().equalsIgnoreCase("0"))
                        // checking for content in layer
                        if (entities.size() > 0) {
                            HashMap<String, String> entitiesToBeValidated = new HashMap<>();
                            entitiesToBeValidated.put(DXFConstants.ENTITY_TYPE_INSERT, NEGATIVE_WIDTH);

                            List<String> errors = validateEntities(dxfLayer, entitiesToBeValidated);
                            if (!errors.isEmpty())
                                for (String error : errors)
                                    edcrPdfDetail.setFailureReasons(error);
                            /*
                             * planDetailErrors.put(error, error); planDetail.addErrors(planDetailErrors);
                             */
                            edcrPdfDetails.add(edcrPdfDetail);
                        } /*
                           * else { edcrPdfDetail.setFailureReasons("No content"); planDetailErrors.put(layerName +
                           * " No content ", String.format(NO_CONTENT, layerName)); planDetail.addErrors(planDetailErrors); }
                           */
                }
            }

        }
        planDetail.setEdcrPdfDetails(edcrPdfDetails);

        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        validate(planDetail);

        String fileName = planDetail.getDxfFileName();
        LOG.info("*************** Converting " + fileName + " to pdf ***************" + "\n");
        DXFDocument dxfDocument = planDetail.getDxfDocument();

        List<EdcrPdfDetail> edcrPdfDetails = planDetail.getEdcrPdfDetails();
        if (edcrPdfDetails != null && edcrPdfDetails.size() > 0)
            for (EdcrPdfDetail edcrPdfDetail : edcrPdfDetails) {
                StringBuffer standardViolations = new StringBuffer();

                if (StringUtils.isBlank(edcrPdfDetail.getFailureReasons())) {

                    // get all the layerIterator from the document
                    Iterator layerIterator = dxfDocument.getDXFLayerIterator();

                    // iterate the layer and disable all layerIterator
                    while (layerIterator.hasNext()) {
                        DXFLayer layer = (DXFLayer) layerIterator.next();
                        layer.setFlags(1);
                    }

                    DXFLayer dxfLayer = dxfDocument.getDXFLayer(edcrPdfDetail.getLayer());
                    LOG.info(edcrPdfDetail.getLayer());
                    dxfLayer.setFlags(0);

                    List texts = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_TEXT);
                    buildText(texts, standardViolations);

                    List mtexts = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_MTEXT);

                    buildMtext(mtexts, standardViolations);

                    List dimensions = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_DIMENSION);

                    buildDimension(dxfDocument, dimensions, standardViolations);

                    if (standardViolations != null && standardViolations.length() > 0)
                        edcrPdfDetail.setStandardViolations(standardViolations.toString());

                    DXFVariable psltScale = dxfDocument.getDXFHeader().getVariable("$PSLTSCALE");

                    if (psltScale != null) {
                        String psltScaleValue = psltScale.getValue("70");

                        if (!isBlank(psltScaleValue))
                            dxfDocument.getDXFHeader().getVariable("$PSLTSCALE").setValue("70", String.valueOf(0));

                    }

                    File file = convertDxfToPdf(dxfDocument, fileName, edcrPdfDetail.getLayer(), edcrPdfDetail);
                    if (file != null) {
                        FileStoreMapper fileStoreMapper = fileStoreService.store(file, file.getName(), APPLICATION_PDF,
                                FILESTORE_MODULECODE);
                        edcrPdfDetail.setConvertedPdf(fileStoreMapper);
                    }
                }

            }
        return planDetail;
    }

    private File convertDxfToPdf(DXFDocument dxfDocument, String fileName, String layerName, EdcrPdfDetail edcrPdfDetail) {

        File fileOut = new File(layerName + ".pdf");

        if (fileOut != null)
            try {

                LOG.info("---------converting " + fileName + " - " + layerName + " to pdf----------");
                FileOutputStream fout = new FileOutputStream(fileOut);

                SVGGenerator generator = new SVGGenerator();
                SAXSerializer out = new SAXPDFSerializer();
                out.setOutput(fout);
                HashMap map = new HashMap();
                // factor of 3.78 for setting page size
                // A0 landscape = 841mm X 1189mm (w * h)
                map.put("width", String.valueOf(1189 * 3.78));
                map.put("height", String.valueOf(841 * 3.78));
                map.put("margin", String.valueOf(0.5));

                generator.generate(dxfDocument, out, map);
                LOG.info("---------conversion success " + fileName + " - " + layerName + "----------");
                fout.flush();
                fout.close();
                return fileOut.length() > 0 ? fileOut : null;
            } catch (Exception ep) {
                LOG.error("Pdf convertion failed for " + fileName + " - " + layerName + " due to " +
                        ep.getMessage());
                edcrPdfDetail.setFailureReasons(ep.getMessage());
            }

        return null;
    }

    private List<String> getAppConfigValues(List<String> keys) {
        List<String> values = new ArrayList<>();

        for (String key : keys) {
            List<AppConfigValues> appConfigValueList = appConfigValueService.getConfigValuesByModuleAndKey(
                    DcrConstants.APPLICATION_MODULE_TYPE, key);

            for (AppConfigValues appConfigValue : appConfigValueList) {
                String value = appConfigValue.getValue();
                value = value.contains("*") ? value.replace("*", "\\d") : value;
                values.add(value);
            }
        }
        return values;
    }

    private List<String> validateEntities(DXFLayer dxfLayer, HashMap<String, String> entities) {

        StringBuffer errorBuffer = new StringBuffer();
        HashSet<String> blks = new HashSet<>();
        ArrayList<String> errors = new ArrayList<>();

        for (Map.Entry<String, String> entity : entities.entrySet())
            if (entity.getKey().equalsIgnoreCase(DXFConstants.ENTITY_TYPE_INSERT)) {
                List insertEntites = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_INSERT);

                if (insertEntites != null && insertEntites.size() > 0)
                    for (Object o : insertEntites) {
                        DXFInsert insert = (DXFInsert) o;
                        if (insert.getScaleX() < 0 || insert.getScaleY() < 0)
                            blks.add(insert.getBlockID());
                    }

                for (String blk : blks)
                    errorBuffer = errorBuffer.append(blk).append(",");

                String insertError = errorBuffer.toString();
                if (insertError != null && !StringUtils.isBlank(insertError))
                    errors.add(entity.getValue() + insertError.substring(0, insertError.length() - 1) + ".");
            }

        return errors;

    }

    private boolean isDuplicatePresent(List<String> layerList) {
        Set<String> duplicateLayerList = layerList.stream().filter(i -> Collections.frequency(layerList, i) > 1)
                .collect(Collectors.toSet());
        return duplicateLayerList.size() > 0 ? true : false;
    }

    private void buildText(List texts, StringBuffer standardViolations) {

        if (texts != null && texts.size() > 0) {
            long issueCount = 0;
            StringBuffer errorMText = new StringBuffer();
            Iterator iterator = texts.iterator();
            while (iterator.hasNext()) {
                DXFText text = (DXFText) iterator.next();
                boolean underLinePresent = text.getText().contains(UNDERLINE_CAPITAL) || text.getText().contains(UNDERLINE_SMALL);
                if (underLinePresent) {
                    text.setText(text.getText().replace(UNDERLINE_CAPITAL, ""));
                    Iterator styledParagraphIterator = text.getTextDocument().getStyledParagraphIterator();
                    while (styledParagraphIterator.hasNext()) {
                        StyledTextParagraph styledTextParagraph = (StyledTextParagraph) styledParagraphIterator.next();
                        styledTextParagraph.setUnderline(true);
                        styledTextParagraph.setValign(TEXT_VALLIGNMENT_TOP);
                    }
                }

                boolean powerPresent = text.getText().contains(POWER);

                if (powerPresent)
                    text.setText(text.getText().replace(POWER, ""));

                if (text.getText().contains("{")
                        || text.getText().contains("}")) {
                    issueCount++;
                    if (errorMText.toString().split(",").length < 5)
                        if (StringUtils.isNotBlank(text.getText()))
                            errorMText.append(text.getText()).append(",");
                }

            }

            if (issueCount > 0)
                standardViolations.append("Text defined as ")
                        .append(errorMText.toString(), 0, errorMText.toString().length() - 1)
                        .append(issueCount > 5 ? " and " + (issueCount - 5) + " others " : "")
                        .append(" are not as per standards.|");

        }
    }

    private void buildMtext(List mtexts, StringBuffer standardViolations) {

        if (mtexts != null && mtexts.size() > 0) {
            String text = "";

            long issueCount = 0;
            for (Object o : mtexts) {
                DXFMText mText = (DXFMText) o;
                boolean underLinePresent = mText.getText().contains(UNDERLINE_CAPITAL)
                        || mText.getText().contains(UNDERLINE_SMALL);

                Iterator styledParagraphIterator = mText.getTextDocument().getStyledParagraphIterator();
                while (styledParagraphIterator.hasNext()) {
                    StyledTextParagraph styledTextParagraph = (StyledTextParagraph) styledParagraphIterator.next();

                    if (underLinePresent) {
                        styledTextParagraph.setUnderline(true);
                        styledTextParagraph.setValign(TEXT_VALLIGNMENT_TOP);
                    }

                    if (styledTextParagraph.getInsertPoint().getX() == 0)
                        styledTextParagraph.getInsertPoint().setX(mText.getInsertPoint().getX());

                    if (styledTextParagraph.getInsertPoint().getY() == 0)
                        styledTextParagraph.getInsertPoint().setY(mText.getInsertPoint().getY());
                }

                boolean powerPresent = mText.getText().contains(POWER);

                if (powerPresent)
                    mText.setText(mText.getText().replace(POWER, ""));

                if (mText.getText().contains("{")
                        || mText.getText().contains("}")) {
                    issueCount++;
                    if (issueCount == 1)
                        text = mText.getText();
                }
            }

            if (issueCount > 0)
                standardViolations.append("Mtext defined as ")
                        .append(text)
                        .append(issueCount > 5 ? " and " + (issueCount - 5) + " others " : "")
                        .append(" are not as per standards.|");
        }

    }

    private void buildDimension(DXFDocument dxfDocument, List dimensions, StringBuffer standardViolations) {

        if (dimensions != null && dimensions.size() > 0) {
            long issueCount = 0;
            Iterator iterator = dimensions.iterator();
            StringBuffer mText = new StringBuffer();

            while (iterator.hasNext()) {
                DXFDimension dimension = (DXFDimension) iterator.next();
                String dimensionBlock = dimension.getDimensionBlock();
                DXFBlock dxfBlock = dxfDocument.getDXFBlock(dimensionBlock);
                Iterator entitiesIterator = dxfBlock.getDXFEntitiesIterator();
                boolean issuePresent = false;

                while (entitiesIterator.hasNext()) {
                    DXFEntity e = (DXFEntity) entitiesIterator.next();

                    if (e.getType().equalsIgnoreCase(DXFConstants.ENTITY_TYPE_LINE)) {
                        DXFLine dxfLine = (DXFLine) e;
                        if (dxfLine.getLineWeight() > 1) {
                            dxfLine.setLineWeight(1);
                            issuePresent = true;
                        }
                    }

                    if (e.getType().equalsIgnoreCase(DXFConstants.ENTITY_TYPE_SOLID)) {
                        DXFSolid dxfSolid = (DXFSolid) e;
                        if (dxfSolid.getLineWeight() > 1) {
                            dxfSolid.setLineWeight(1);
                            if (issuePresent = false)
                                issuePresent = true;
                        }
                    }

                    if (e.getType().equals(DXFConstants.ENTITY_TYPE_MTEXT)) {
                        DXFMText dxfmText = (DXFMText) e;
                        if (issuePresent) {
                            issueCount++;
                            if (mText.toString().split(",").length < 5)
                                mText.append(dxfmText.getText()).append(",");
                        }
                    }
                }
            }

            if (mText != null && mText.length() > 0)
                standardViolations.append("Line weight defined for " + (issueCount > 5 ? " dimensions " : " dimension "))
                        .append(mText.toString(), 0, mText.toString().length() - 1)
                        .append(issueCount > 5 ? " and " + (issueCount - 5) + " others " : "")
                        .append(" are not as per standards.");

        }
    }

}
