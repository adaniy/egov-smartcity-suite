package org.egov.edcr;

import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.entity.ScrutinyDetail;
import org.egov.edcr.entity.ScrutinyDetail.ColumnHeadingDetail;
import org.egov.edcr.feature.Coverage;
import org.egov.edcr.feature.Far;
import org.egov.edcr.feature.Parking;
import org.egov.edcr.feature.Sanitation;
import org.egov.edcr.service.PlanReportService;
import org.egov.edcr.utility.PrintUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnit44Runner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnit44Runner.class)

public class FarTest extends BaseTest {
	Far feature =new Far();
	//@Test
	public final void testExtract() {
		doc = getDxfDocument("Sanity.dxf");
		pl = generalRule.extract(pl, doc);
		pl=feature.extract(pl, doc);
		assertTrue(pl.getBlocks().size() == 3);
		PrintUtil.print(pl.getReportOutput());
	}

	@Test
	public final void testValidate() {


	}

	//@Test
	public final void testProcess() {
	    doc = getDxfDocument("Sanity.dxf");
		pl = generalRule.extract(pl, doc);
		feature.setEdcrMessageSource(generalRule.getEdcrMessageSource());
		feature.extract(pl, doc);
		feature.process(pl);
		Coverage cov=new Coverage();
		cov.setEdcrMessageSource(generalRule.getEdcrMessageSource());
		pl=cov.extract(pl, doc);
		cov.process(pl);
		PrintUtil.print(pl);
		Parking parking = new Parking();
		parking.setEdcrMessageSource(generalRule.getEdcrMessageSource());
		pl= parking.extract(pl, doc);
		parking.process(pl);
		Sanitation sanitation =new Sanitation();
		sanitation.setEdcrMessageSource(generalRule.getEdcrMessageSource());
		sanitation.extract(pl, doc);
		sanitation.process(pl);
		
		assertTrue(pl.getBlocks().size() == 3);
		assertTrue(pl.getFar().doubleValue() == 2d);
		System.out.println();
		System.out.println(pl.getCoverage());
		assertNotNull(pl.getReportOutput());
		PrintUtil.print(pl.getReportOutput());
		List<ScrutinyDetail> scrutinyDetails = pl.getReportOutput().getScrutinyDetails();
		for(ScrutinyDetail detail:scrutinyDetails)
		{
			System.out.println("Key :"+detail.getKey());
			Map<Integer, ColumnHeadingDetail> heading = detail.getColumnHeading();
			Collection<ColumnHeadingDetail> keySet = heading.values();
			List<Map<String, String>> detail2 = detail.getDetail();
			for(Map p:detail2)
			{
				Iterator iterator = keySet.iterator();
				while(iterator.hasNext())
				{
				    ColumnHeadingDetail innerKey=(ColumnHeadingDetail)iterator.next();
					System.out.println("Inner Key :"+innerKey.name);
					System.out.println("Inner Value"+p.get(innerKey.name));
				}
			}
		}
		EdcrApplication app=new EdcrApplication();
		app.setApplicationDate(new Date());
		app.setApplicantName("Manu");
		app.setPlanInformation(new PlanInformation());
		
		PlanReportService prs=new PlanReportService();
		InputStream generateDynamicReport = prs.generateDynamicReport(pl, doc, app);
		 Path newFilePath = Paths.get("/home/mani/mk.pdf","");
		 File f=newFilePath.toFile();
		 if(f.exists())
		 {
			f.delete(); 
		 }
		 try {
			Files.copy(generateDynamicReport, newFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
	}

	
}
