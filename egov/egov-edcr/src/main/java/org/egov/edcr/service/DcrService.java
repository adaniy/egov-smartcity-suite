package org.egov.edcr.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.egov.edcr.autonumber.DcrApplicationNumberGenerator;
import org.egov.edcr.entity.DcrReportOutput;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.EdcrApplicationDetail;
import org.egov.edcr.entity.EdcrPdfDetail;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.admin.master.service.CityService;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DJDataSource;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.Subreport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/*General rule class contains validations which are required for all types of building plans*/
@Service
public class DcrService {
    private Logger LOG = Logger.getLogger(DcrService.class);

    private PlanDetail planDetail;

    @Autowired
    private PlanRuleService planRuleService;

    @Autowired
    private JasperReportService reportService;

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private DcrApplicationNumberGenerator dcrApplicationNumberGenerator;

    @Autowired
    private EdcrApplicationDetailService edcrApplicationDetailService;

    @Value("${edcr.client.subreport}")
    private boolean clientSpecificSubReport;

    @Autowired
    private CityService cityService;
    @Autowired
    private PlanService planService;
    @Autowired
    private PlaninfoService planinfoService;
    @Autowired
    private EdcrPdfDetailService edcrPdfDetailService;

    public PlanDetail getPlanDetail() {
        return planDetail;
    }

    public void setPlanDetail(PlanDetail planDetail) {
        this.planDetail = planDetail;
    }

    @Transactional
    public void saveOutputReport(EdcrApplication edcrApplication, InputStream reportOutputStream, PlanDetail planDetail) {

        List<EdcrApplicationDetail> edcrApplicationDetails = edcrApplicationDetailService
                .fingByDcrApplicationId(edcrApplication.getId());
        final String fileName = edcrApplication.getApplicationNumber() + "-v" + edcrApplicationDetails.size() + ".pdf";

        final FileStoreMapper fileStoreMapper = fileStoreService.store(reportOutputStream, fileName, "application/pdf",
                DcrConstants.FILESTORE_MODULECODE);

        buildDocuments(edcrApplication, null, fileStoreMapper, planDetail);

        PlanInformation planInformation = planDetail.getPlanInformation();

        planinfoService.save(planInformation);
        edcrApplication.getEdcrApplicationDetails().get(0).setPlanInformation(planInformation);
        edcrApplicationDetailService.saveAll(edcrApplication.getEdcrApplicationDetails());
    }

    public void buildDocuments(EdcrApplication edcrApplication, FileStoreMapper dxfFile, FileStoreMapper reportOutput,
            PlanDetail planDetail) {

        if (dxfFile != null) {
            EdcrApplicationDetail edcrApplicationDetail = new EdcrApplicationDetail();

            edcrApplicationDetail.setDxfFileId(dxfFile);
            edcrApplicationDetail.setApplication(edcrApplication);
            for (EdcrApplicationDetail edcrApplicationDetail1 : edcrApplication.getEdcrApplicationDetails())
                edcrApplicationDetail.setPlanDetail(edcrApplicationDetail1.getPlanDetail());
            List<EdcrApplicationDetail> edcrApplicationDetails = new ArrayList<>();
            edcrApplicationDetails.add(edcrApplicationDetail);
            edcrApplication.setSavedEdcrApplicationDetail(edcrApplicationDetail);
            edcrApplication.setEdcrApplicationDetails(edcrApplicationDetails);
        }

        if (reportOutput != null) {
            EdcrApplicationDetail edcrApplicationDetail = edcrApplication.getEdcrApplicationDetails().get(0);

            if (planDetail.getEdcrPassed()) {
                edcrApplicationDetail.setStatus("Accepted");
                edcrApplication.setStatus("Accepted");
            } else {
                edcrApplicationDetail.setStatus("Not Accepted");
                edcrApplication.setStatus("Not Accepted");
            }
            edcrApplicationDetail.setCreatedDate(new Date());
            edcrApplicationDetail.setReportOutputId(reportOutput);
            List<EdcrApplicationDetail> edcrApplicationDetails = new ArrayList<>();
            edcrApplicationDetails.add(edcrApplicationDetail);
            planService.savePlanDetail(planDetail, edcrApplicationDetail);

            if (planDetail.getEdcrPdfDetails() != null && planDetail.getEdcrPdfDetails().size() > 0) {
                for (EdcrPdfDetail edcrPdfDetail : planDetail.getEdcrPdfDetails())
                    edcrPdfDetail.setEdcrApplicationDetail(edcrApplicationDetail);

                edcrPdfDetailService.saveAll(planDetail.getEdcrPdfDetails());
            }

            edcrApplication.setEdcrApplicationDetails(edcrApplicationDetails);
        }
    }

    private Object getRuleBean(String ruleName) {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }

    private String buildQRCodeDetails(final EdcrApplication dcrApplication, boolean reportStatus) {
        StringBuilder qrCodeValue = new StringBuilder();
        qrCodeValue = !org.apache.commons.lang.StringUtils
                .isEmpty(dcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber())
                        ? qrCodeValue.append("DCR Number : ")
                                .append(dcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber()).append("\n")
                        : qrCodeValue.append("DCR Number : ").append("N/A").append("\n");
        qrCodeValue = !org.apache.commons.lang.StringUtils.isEmpty(dcrApplication.getApplicationNumber())
                ? qrCodeValue.append("Application Number : ").append(dcrApplication.getApplicationNumber()).append("\n")
                : qrCodeValue.append("Application Number : ").append("N/A").append("\n");
        qrCodeValue = dcrApplication.getApplicationDate() != null
                ? qrCodeValue.append("Application Date : ").append(dcrApplication.getApplicationDate()).append("\n")
                : qrCodeValue.append("Application Date : ").append("N/A").append("\n");
        qrCodeValue = qrCodeValue.append("Report Status :").append(reportStatus ? "Accepted" : "Not Accepted").append("\n");
        return qrCodeValue.toString();
    }

    public Subreport generateDcrSubReport(final List<DcrReportOutput> dcrReportOutputs) throws Exception {
        FastReportBuilder drb = new FastReportBuilder();

        final Style titleStyle = new Style("titleStyle");
        titleStyle.setFont(Font.ARIAL_MEDIUM_BOLD);
        titleStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        titleStyle.setVerticalAlign(VerticalAlign.BOTTOM);

        final Style columnStyle = reportService.getColumnStyle();
        final Style columnHeaderStyle = reportService.getColumnHeaderStyle();
        drb.setTitle("Building Rule Scrutiny");
        drb.setTitleStyle(titleStyle);
        drb.addColumn("KMBR Rule No.", "key", String.class.getName(), 60, columnStyle, columnHeaderStyle);
        drb.addColumn("Rule description", "description", String.class.getName(), 120, columnStyle, columnHeaderStyle);
        drb.addColumn("Required by Rule", "expectedResult", String.class.getName(), 120, columnStyle, columnHeaderStyle);
        drb.addColumn("Provided as per drawings", "actualResult", String.class.getName(), 125, columnStyle, columnHeaderStyle);
        drb.addColumn("Accepted / Not Accepted", "status", String.class.getName(), 90, columnStyle, columnHeaderStyle);
        drb.setUseFullPageWidth(true);
        drb.setPageSizeAndOrientation(Page.Page_Legal_Landscape());
        new JRBeanCollectionDataSource(dcrReportOutputs);
        final DJDataSource djds = new DJDataSource("subreportds", DJConstants.DATA_SOURCE_ORIGIN_PARAMETER,
                DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE);

        final Subreport subRep = new Subreport();
        subRep.setLayoutManager(new ClassicLayoutManager());
        subRep.setDynamicReport(drb.build());
        subRep.setDatasource(djds);
        subRep.setUseParentReportParameters(true);

        return subRep;
    }
}
