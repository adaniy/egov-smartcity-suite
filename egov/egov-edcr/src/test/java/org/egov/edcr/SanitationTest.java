package org.egov.edcr;

import org.apache.log4j.BasicConfigurator;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.feature.Far;
import org.egov.edcr.feature.GeneralRule;
import org.egov.edcr.feature.Sanitation;
import org.egov.edcr.utility.PrintUtil;
import org.jfree.util.Log;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.mockito.runners.MockitoJUnit44Runner;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnit44Runner.class)

public class SanitationTest {


	private GeneralRule rule;

	@BeforeClass
	public static void init() {
	BasicConfigurator.configure();
		 	 
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		rule = new GeneralRule();

		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

		messageSource.setBasename("i18n/messages");
		File file = new File(rule.getClass().getClassLoader().getResource("messages/service-message-edcr.properties").getFile());
		FileInputStream stm=new FileInputStream(file);
		Properties commonMessages=new Properties();
		commonMessages.load(stm);
		messageSource.setCommonMessages(commonMessages);
		rule.setEdcrMessageSource(messageSource);


	}

	@After
	public void tearDown() throws Exception {
	}

	//@Test
	public final void testExtract() {
		PlanDetail pl = new PlanDetail();
		pl.setPlanInformation(new PlanInformation());
		DXFDocument doc = getDxfDocument("Sanity3.dxf");
		pl = rule.extract(pl, doc);
		Far far = new Far();
		far.setEdcrMessageSource(rule.getEdcrMessageSource());
		far.extract(pl, doc);
		Sanitation sanitation = new Sanitation();
		sanitation.setEdcrMessageSource(rule.getEdcrMessageSource());
		sanitation.extract(pl, doc);
		PrintUtil.print(pl.getReportOutput());
	}

	@Test
	public final void testValidate() {


	}

	//@Test
	public final void testProcess() {
		PlanDetail pl = new PlanDetail();
		pl.setPlanInformation(new PlanInformation());
		DXFDocument doc = getDxfDocument("Sanity3.dxf");
		pl = rule.extract(pl, doc);
		Far far = new Far();
		far.setEdcrMessageSource(rule.getEdcrMessageSource());
		far.extract(pl, doc);
		Sanitation sanitation = new Sanitation();
		sanitation.setEdcrMessageSource(rule.getEdcrMessageSource());
		sanitation.extract(pl, doc);
		PrintUtil.printS(pl.getReportOutput().getScrutinyDetails());
		rule.process(pl);
		far.process(pl);
		sanitation.process(pl);
		//PrintUtil.print(pl);
		//assertTrue(pl.getBlocks().size() == 3);
		assertNotNull(pl.getReportOutput());
	}

	private DXFDocument getDxfDocument(String fileName) {
		Parser parser = ParserBuilder.createDefaultParser();
		DXFDocument doc=new DXFDocument();
		try {
				File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
				parser.parse(file.getPath(), DXFParser.DEFAULT_ENCODING);
				doc = parser.getDocument();
		} catch (ParseException e) {

			Log.error("Error while parsing");

		}
		catch (Exception e) {
			Log.error("Error while parsing");

		}

		return doc;
	}

}
