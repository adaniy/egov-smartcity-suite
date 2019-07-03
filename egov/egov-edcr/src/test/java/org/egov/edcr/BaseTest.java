package org.egov.edcr;

import org.apache.log4j.BasicConfigurator;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.feature.GeneralRule;
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

@RunWith(MockitoJUnit44Runner.class)

public class BaseTest {
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

	@Test
	public final void testVal() {


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
