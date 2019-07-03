package org.egov.edcr.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.EdcrApplicationDetail;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanFeature;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.service.es.EdcrIndexService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.PrintUtil;
import org.egov.infra.custom.CustomImplProvider;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.validation.exception.ValidationError;
import org.egov.infra.validation.exception.ValidationException;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class PlanService {
    private Logger LOG = Logger.getLogger(PlanService.class);
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private PlanFeatureService featureService;
    @Autowired
    private DcrService dcrService;
    @Autowired
    private FileStoreService fileStoreService;
    @Autowired
    private EdcrIndexService edcrIndexService;
    @Autowired
    private CustomImplProvider specificRuleService;

    public PlanDetail process(EdcrApplication dcrApplication, String applicationType) {
        DXFDocument doc = getDxfDocument(dcrApplication);
        Collections.reverse(dcrApplication.getEdcrApplicationDetails());
        PlanDetail planDetail = dcrApplication.getEdcrApplicationDetails().get(0).getPlanDetail();
        planDetail.setDxfFileName(dcrApplication.getDxfFile().getOriginalFilename());
        planDetail.setDxfDocument(doc);
        Map<String, String> cityDetails = specificRuleService.getCityDetails();
        extract(planDetail, doc, cityDetails);

        if (planDetail.getInMeters() && planDetail.getLengthFactor())
            applyRules(planDetail, cityDetails);

        InputStream reportStream = generateReport(planDetail, doc, dcrApplication);
        dcrService.saveOutputReport(dcrApplication, reportStream, planDetail);
        PrintUtil.print(planDetail);
        PrintUtil.print(planDetail.getErrors());
        PrintUtil.print(planDetail.getGeneralInformation());
        PrintUtil.print(planDetail.getReportOutput());
        // savePlanDetail(planDetail);
        edcrIndexService.updateIndexes(dcrApplication, applicationType);

        return planDetail;
    }

    public void savePlanDetail(PlanDetail planDetail, EdcrApplicationDetail detail) {
        if (LOG.isInfoEnabled())
            LOG.info("*************Before serialization******************");
        PrintUtil.print(planDetail);
        File f = new File("plandetail.txt");
        try (FileOutputStream fos = new FileOutputStream(f);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(planDetail);
            detail.setPlanDetailFileStore(
                    fileStoreService.store(f, f.getName(), "text/plain", DcrConstants.APPLICATION_MODULE_TYPE));
            oos.flush();
        } catch (IOException e) {
            LOG.error("Unable to serialize!!!!!!", e);
        }
        if (LOG.isInfoEnabled())
            LOG.info("*************Completed serialization******************");
    }

    private PlanDetail extract(PlanDetail planDetail, DXFDocument doc, Map<String, String> cityDetails) {

        // drawing unit should be defined in meters
        if (doc.getDXFHeader().getVariable("$INSUNITS") != null
                && !doc.getDXFHeader().getVariable("$INSUNITS").getValue("70").equalsIgnoreCase("6")) {

            planDetail.setInMeters(false);
            planDetail.getErrors().put("units not in meters", "The 'Drawing Unit' is not in meters. ");
        }

        // dimension length factor should be 1
        if (doc.getDXFHeader() != null && doc.getDXFHeader().getVariable("$DIMLFAC") != null) {
            BigDecimal dimensionLengthFactor = new BigDecimal(doc.getDXFHeader().getVariable("$DIMLFAC").getValue("40"));
            if (dimensionLengthFactor.compareTo(BigDecimal.ONE) != 0) {
                planDetail.setLengthFactor(false);
                planDetail.getErrors().put("length factor", "The dimension length factor is not 1.");
            }
        }

        if (planDetail.getErrors().size() > 0)
            return planDetail;

        for (PlanFeature ruleClass : featureService.getFeatures()) {
            RuleService rule = null;
            if (ruleClass.getRuleClass() != null)
                rule = (RuleService) specificRuleService.find(ruleClass.getRuleClass(), cityDetails);

            if (rule != null)
                rule.extract(planDetail, doc);
        }
        return planDetail;

    }

    private PlanDetail applyRules(PlanDetail planDetail, Map<String, String> cityDetails) {
        for (PlanFeature ruleClass : featureService.getFeatures()) {
            RuleService rule = (RuleService) specificRuleService.find(ruleClass.getRuleClass(), cityDetails);
            rule.process(planDetail);
        }
        return planDetail;
    }

    private InputStream generateReport(PlanDetail planDetail, DXFDocument doc, EdcrApplication dcrApplication) {

        PlanReportService service = getReportService();
        InputStream reportStream = service.generateReport(planDetail, doc, dcrApplication);

        return reportStream;

    }

    private DXFDocument getDxfDocument(EdcrApplication dcrApplication) {
        Parser parser = ParserBuilder.createDefaultParser();
        try {
            parser.parse(dcrApplication.getSavedDxfFile().getPath(), DXFParser.DEFAULT_ENCODING);
        } catch (ParseException e) {
            LOG.error("Error in gettting default parser", e);
            // throw e;

            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement ele : stackTrace)
                if (ele.toString().toLowerCase().contains("font"))
                    throw new ValidationException(
                            Arrays.asList(new ValidationError("Unsupported font is used", "Unsupported font is used")));

        } catch (NoSuchElementException e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement ele : stackTrace)
                if (ele.toString().toLowerCase().contains("font"))
                    throw new ValidationException(
                            Arrays.asList(new ValidationError("Unsupported font is used", "Unsupported font is used")));
        } catch (Exception e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement ele : stackTrace)
                if (ele.toString().toLowerCase().contains("font"))
                    throw new ValidationException(
                            Arrays.asList(new ValidationError("Unsupported font is used", "Unsupported font is used")));
        }

        // Extract DXF Data
        DXFDocument doc = parser.getDocument();
        return doc;
    }

    private PlanReportService getReportService() {
        Object bean = null;
        String beanName = "PlanReportService";
        PlanReportService service = null;
        try {
            beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
            applicationContext.getBean(beanName);
            bean = applicationContext.getBean(beanName);
            service = (PlanReportService) bean;
            if (service == null)
                LOG.error("No Service Found for " + beanName);
        } catch (BeansException e) {
            LOG.error("No Bean Defined for the Rule " + beanName);
        }
        return service;
    }

}