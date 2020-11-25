package ca.corefacility.bioinformatics.irida.ria.integration.sequenceFiles;

import java.io.IOException;
import java.nio.file.Path;

import ca.corefacility.bioinformatics.irida.config.services.IridaApiServicesConfig;
import ca.corefacility.bioinformatics.irida.ria.integration.AbstractIridaUIITChromeDriver;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.sequenceFiles.SequenceFilePages;
import ca.corefacility.bioinformatics.irida.ria.integration.utilities.FileUtilities;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiServicesConfig.class })
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/sequenceFiles/SequenceFileView.xml")
@ActiveProfiles("it")
public class SequenceFilePageIT extends AbstractIridaUIITChromeDriver {
	private final FileUtilities fileUtilities = new FileUtilities();

	@Autowired
	@Qualifier("outputFileBaseDirectory")
	private Path outputFileBaseDirectory;

	private static final Logger logger = LoggerFactory.getLogger(SequenceFilePageIT.class);
	/*
	 * FILE ATTRIBUTES
	 */
	private static final String FILE_NAME = "test_file.fastq";
	private static final String FILE_ID = "1";
	private static final String FILE_ENCODING = "Sanger / Illumina 1.9";
	private static final String FILE_CREATED = "Jul 18, 2013, 2:20 PM";
	private static final String FILE_TOTAL_SEQUENCE = "4";
	private static final String FILE_TOTAL_BASES = "937";
	private static final String FILE_MIN_LENGTH = "184";
	private static final String FILE_MAX_LENGTH = "251";
	private static final String FILE_GC_CONTENT = "30";

	private SequenceFilePages page;

	@Before
	public void setUpTest() {
		LoginPage.loginAsManager(driver());
		page = new SequenceFilePages(driver());
	}

	@Test
	public void testSequenceFileFastQCChartsPage() {
		try {
			fileUtilities.copyFileToDirectory(outputFileBaseDirectory,
					"src/test/resources/files/perBaseQualityScoreChart.png");
			fileUtilities.copyFileToDirectory(outputFileBaseDirectory,
					"src/test/resources/files/perSequenceQualityScoreChart.png");
			fileUtilities.copyFileToDirectory(outputFileBaseDirectory,
					"src/test/resources/files/duplicationLevelChart.png");
		} catch(IOException e) {
			logger.error("Cannot copy file. File not found.", e);
		}

		logger.debug("Testing the Sequence File FastQC Charts Page");
		page.goToChartsPage();
		assertTrue(page.isFastQCLinksVisible());
		assertFalse(page.isFastQCNoRunWarningDisplayed());
		assertEquals("Should display three charts", 3, page.getChartCount());
	}

	@Test
	public void testSequenceFileOverrepresentedSequencePage() {
		logger.debug("Testing the Sequence File FastQC Overrepresented Sequences Page");
		page.goToOverrepresentedPage();
		assertEquals("Should display 1 overrepresented sequence", 1, page.getNumberOfOverrepresentedSequences());
		assertTrue("Should display a sequence", page.getOverrepresentedSequence().matches("^[aAtTgGcC]+$"));
		assertTrue("Should display the percentage with a percent sign",
				page.getOverrepresentedSequencePercentage().contains("%"));
		assertEquals("Should display the count", "1", page.getOverrepresentedSequenceCount());
		assertEquals("Should display the source", "No Hit", page.getOverrepresentedSequenceSource());
	}

	@Test
	public void testSequenceFileFastQCDetailsPage() {
		logger.debug("Testing the Sequence File FastQC Details Page");
		page.goToDetailsPage();
		assertTrue(page.isFastQCLinksVisible());
		assertFalse(page.isFastQCNoRunWarningDisplayed());
		testPageChrome();
	}

	@Test
	public void testNoFastQCData() {
		page.goToDetailsPageWithNoData();
		assertEquals("sequenceFile2", page.getPageTitle());
		assertFalse(page.isFastQCLinksVisible());
		assertTrue(page.isFastQCNoRunWarningDisplayed());
	}

	private void testPageChrome() {
		logger.debug("Testing the Sequence File Overrepresented Sequence Page");
		assertEquals("Has the file name as the page title", FILE_NAME, page.getPageTitle());
		assertEquals("Display the file id", FILE_ID, page.getFileId());
		assertEquals("Displays the file created date", FILE_CREATED, page.getFileCreatedDate());
		assertEquals("Displays the file encoding", FILE_ENCODING, page.getFileEncoding());
		assertEquals("Display the total sequence count", FILE_TOTAL_SEQUENCE, page.getTotalSequenceCount());
		assertEquals("Display the total bases count", FILE_TOTAL_BASES, page.getTotalBasesCount());
		assertEquals("Displays the minLength", FILE_MIN_LENGTH, page.getMinLength());
		assertEquals("Displays the maxLength", FILE_MAX_LENGTH, page.getMaxLength());
		assertEquals("Displays the gc content", FILE_GC_CONTENT, page.getGCContent());
	}

}
