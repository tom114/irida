package ca.corefacility.bioinformatics.irida.ria.integration.pipelines;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import ca.corefacility.bioinformatics.irida.config.services.IridaApiServicesConfig;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.repositories.analysis.submission.AnalysisSubmissionRepository;
import ca.corefacility.bioinformatics.irida.ria.integration.AbstractIridaUIITChromeDriver;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.pipelines.PipelinesAssemblyPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.pipelines.PipelinesSelectionPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.projects.ProjectSamplesPage;

/**
 * Testing for launching an assembly pipeline.
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiServicesConfig.class })
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/pipelines/AssemblyPipelinePageIT.xml")
public class AssemblyPipelinePageIT extends AbstractIridaUIITChromeDriver {
	private static final Logger logger = LoggerFactory.getLogger(AssemblyPipelinePageIT.class);
	private PipelinesAssemblyPage page;

	@Autowired
	private AnalysisSubmissionRepository analysisSubmissionRepository;

	@Before
	public void setUpTest() {
		page = new PipelinesAssemblyPage(driver());
	}

	@Test
	public void testPageSetup() {
		addSamplesToCartManager();

		logger.info("Checking Assembly Page Setup.");
		assertEquals("Should display the correct number of samples.", 1, page.getNumberOfSamplesDisplayed());
	}

	@Test
	public void testShareResultsWithSamples() {
		addSamplesToCartManager();

		String analysisName = AssemblyPipelinePageIT.class.getName() + ".testShareResultsWithSamples";
		page.setNameForAnalysisPipeline(analysisName);

		assertTrue("Share Results with Samples checkbox should exist", page.existsShareResultsWithSamples());
		page.clickShareResultsWithSamples();
		page.clickLaunchPipelineBtn();
		assertTrue("Message should be displayed once the pipeline finished submitting",
				page.isPipelineSubmittedSuccessMessageShown());

		AnalysisSubmission submission = findAnalysisSubmissionWithName(analysisName + "_sample1");

		assertNotNull("Analysis Submission is null", submission);
		assertTrue("updateSamples should be true", submission.getUpdateSamples());
	}

	@Test
	public void testNoShareResultsWithSamples() {
		addSamplesToCartManager();

		String analysisName = AssemblyPipelinePageIT.class.getName() + ".testNoShareResultsWithSamples";
		page.setNameForAnalysisPipeline(analysisName);

		assertTrue("Share Results with Samples checkbox should exist", page.existsShareResultsWithSamples());
		page.clickLaunchPipelineBtn();
		assertTrue("Message should be displayed once the pipeline finished submitting",
				page.isPipelineSubmittedSuccessMessageShown());

		AnalysisSubmission submission = findAnalysisSubmissionWithName(analysisName + "_sample1");

		assertNotNull("Analysis Submission is null", submission);
		assertFalse("updateSamples should be false", submission.getUpdateSamples());
	}

	@Test
	public void testUserNoShareResultsWithSamples() {
		addSamplesToCartUser();

		String analysisName = AssemblyPipelinePageIT.class.getName() + ".testUserNoShareResultsWithSamples";
		page.setNameForAnalysisPipeline(analysisName);

		assertFalse("Share Results with Samples checkbox should not exist", page.existsShareResultsWithSamples());
		page.clickLaunchPipelineBtn();
		assertTrue("Message should be displayed once the pipeline finished submitting",
				page.isPipelineSubmittedSuccessMessageShown());

		AnalysisSubmission submission = findAnalysisSubmissionWithName(analysisName + "_sample1");

		assertNotNull("Analysis Submission is null", submission);
		assertFalse("updateSamples should be false", submission.getUpdateSamples());
	}

	private void addSamplesToCartUser() {
		LoginPage.loginAsUser(driver());
		addSamplesToCart();
	}

	private void addSamplesToCartManager() {
		LoginPage.loginAsManager(driver());
		addSamplesToCart();
	}

	private void addSamplesToCart() {
		ProjectSamplesPage samplesPage = ProjectSamplesPage.gotToPage(driver(), 1);
		samplesPage.selectSample(0);
		samplesPage.addSelectedSamplesToCart();
		PipelinesSelectionPage.goToAssemblyPipelinePipeline(driver());
	}

	private AnalysisSubmission findAnalysisSubmissionWithName(String name) {
		AnalysisSubmission submission = null;
		for (AnalysisSubmission s : analysisSubmissionRepository.findAll()) {
			if (name.equals(s.getName())) {
				submission = s;
			}
		}

		return submission;
	}

	@Test
	public void testEmailPipelineResult() {
		addSamplesToCartManager();

		String analysisName = AssemblyPipelinePageIT.class.getName() + ".testEmailPipelineResult";
		page.setNameForAnalysisPipeline(analysisName);

		assertTrue("Email Pipeline Result checkbox should exist", page.existsEmailPipelineResult());
		page.clickEmailPipelineResult();
		page.clickLaunchPipelineBtn();
		assertTrue("Message should be displayed once the pipeline finished submitting",
				page.isPipelineSubmittedSuccessMessageShown());

		AnalysisSubmission submission = findAnalysisSubmissionWithName(analysisName + "_sample1");

		assertNotNull("Analysis Submission is null", submission);
		assertTrue("emailPipelineResult should be true", submission.getEmailPipelineResult());
	}
}
