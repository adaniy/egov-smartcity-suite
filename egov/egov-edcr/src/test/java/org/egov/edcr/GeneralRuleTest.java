package org.egov.edcr;

import org.apache.log4j.BasicConfigurator;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.feature.GeneralRule;
import org.egov.edcr.utility.PrintUtil;
import org.jfree.util.Log;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeneralRuleTest  {

	//@Test
	public void testExtract() {
		doc=getDxfDocument("Plan_info.dxf");
		pl=generalRule.extract(pl, doc);
		assertTrue(pl.getPlot().getPresentInDxf());
	}
	
	//@Test
	public void testExtractPlanInfo() {
		doc=getDxfDocument("Plan_info.dxf");
		pl=generalRule.extract(pl, doc);
		assertTrue(pl.getPlot().getPresentInDxf());
		assertTrue(pl.getPlanInformation().getPlotArea().doubleValue()==2306.79);
		assertFalse(pl.getPlanInformation().getCrzZoneArea());
		assertFalse(pl.getPlanInformation().getSecurityZone());
		
	}
	
	//@Test
	public void testExtractPlanInfoInvalidInput() {
		doc=getDxfDocument("Plan_info2.dxf");
		pl=generalRule.extract(pl, doc);
		Iterator dxfLayerIterator = doc.getDXFLayerIterator();
		while(dxfLayerIterator.hasNext())
		{
			//System.out.println(((DXFLayer)dxfLayerIterator.next()).getName());	
		}
		assertTrue(pl.getPlot().getPresentInDxf());
		assertTrue(pl.getPlanInformation().getPlotArea().doubleValue()==2306.79);
		assertFalse(pl.getPlanInformation().getCrzZoneArea());
		assertFalse(pl.getPlanInformation().getSecurityZone());
		
	}
	
	//@Test
	public void testExtractSetBack() {
		doc=getDxfDocument("modellayer.dxf");
		pl=generalRule.extract(pl, doc);
		Iterator dxfLayerIterator = doc.getDXFLayerIterator();
		while(dxfLayerIterator.hasNext())
		{
			System.out.println(((DXFLayer)dxfLayerIterator.next()).getName());	
		}
		System.out.println(pl.getBlocks());
		
		assertTrue(pl.getBlocks().size()==1);
		PrintUtil.print(pl.reportOutput);
		System.out.println(pl.getErrors().size());
	}
	
	protected GeneralRule generalRule;
	protected PlanDetail pl;
	protected DXFDocument doc ;
    protected Parser parser = ParserBuilder.createDefaultParser();
	@BeforeClass
	public static void init() {
	BasicConfigurator.configure();
		 	 
	}
 

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}

	@Before
	public void setUp() throws Exception {
		generalRule = new GeneralRule();

		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

		messageSource.setBasename("i18n/messages");
		File file = new File(generalRule.getClass().getClassLoader().getResource("messages/service-message-edcr.properties").getFile());
		FileInputStream stm=new FileInputStream(file);
		Properties commonMessages=new Properties();
		commonMessages.load(stm);
		messageSource.setCommonMessages(commonMessages);
		generalRule.setEdcrMessageSource(messageSource);
		
		pl = new PlanDetail();
		pl.setPlanInformation(new PlanInformation());
		/*DXFDocument doc = getDxfDocument("far_3_block.dxf");
		pl = generalRule.extract(pl, doc);
		 */

	}

	@After
	public void tearDown() throws Exception {
    doc=null;
    generalRule=null;
    pl=null;
    
	}
	 
	protected DXFDocument getDxfDocument(String fileName) {
		//Parser parser = ParserBuilder.createDefaultParser();
		DXFDocument doc=new DXFDocument();
		try {
				File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
				parser.parse(file.getPath(), DXFParser.DEFAULT_ENCODING);
				doc = parser.getDocument();
				
		} catch (ParseException e) {

			Log.error("Error while parsing..................",e);

		}
		catch (Exception e) {
			Log.error("Error while parsing.....................",e);

		}

		return doc;
	}

 
}
