package ca.corefacility.bioinformatics.irida.ria.integration.pipelines;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ca.corefacility.bioinformatics.irida.ria.integration.AbstractIridaUIITChromeDriver;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.cart.CartPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.pipelines.LaunchPipelinePage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.projects.ProjectSamplesPage;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import static org.junit.Assert.*;

/**
 * <p>
 * Testing for launching a phylogenomics pipeline.
 * </p>
 *
 */
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/pipelines/PipelinePhylogenomicsView.xml")
public class PipelinesPhylogenomicsPageIT extends AbstractIridaUIITChromeDriver {
	private CartPage cartPage;

	private LaunchPipelinePage page;

	@Before
	public void setUpTest() throws IOException {
		cartPage = new CartPage(driver());

		page = LaunchPipelinePage.init(driver());
		addSamplesToCart();
	}

	@Test
	public void testPageSetup() {
		assertEquals("Launch Page should display  the pipeline name", "SNVPhyl Phylogenomics Pipeline", page.getPipelineName());
		assertTrue("Launch form should be displayed", page.isLaunchFormDisplayed());
		assertTrue("Launch details should be displayed", page.isLaunchDetailsDisplayed());
		assertTrue("Launch parameters should be displayed", page.isLaunchParametersDisplayed());
		assertFalse("Share with samples should not be displayed", page.isShareWithSamplesDisplayed());
		assertTrue("Share with projects should be displayed", page.isShareWithProjectsDisplayed());
		assertTrue("Should be able to select a reference file", page.isReferenceFilesDisplayed());
		assertTrue("Should be able to select sample files", page.isLaunchFilesDisplayed());

		// Test the name input
		page.clearName();
		assertTrue("There should be an error displayed that a name is required", page.isNameErrorDisplayed());
		page.updateName("TEST_NAME");
		assertFalse("Name required warning should be cleared", page.isNameErrorDisplayed());

		// Test email checkbox
		assertEquals("No Email", page.getEmailValue());

		// Make sure the saved pipeline parameter inputs are set up correctly
		assertEquals("Phylogenomics Pipeline should have 8 inputs", 8, page.getNumberOfSavedPipelineParameters());
		assertFalse("Should not be displaying modified parameter alert", page.isModifiedAlertVisible());

		String MIMINUM_COVERAGE_PARAMETER = "minimum-percent-coverage";
		String originalMinimumPercentCoverageValue = page.getSavedParameterValue(MIMINUM_COVERAGE_PARAMETER);
		page.updateSavedParameterValue(MIMINUM_COVERAGE_PARAMETER,"123456");
		assertTrue("Modified parameters alert should be displayed.", page.isModifiedAlertVisible());
		page.updateSavedParameterValue(MIMINUM_COVERAGE_PARAMETER, originalMinimumPercentCoverageValue);
		assertTrue("Modified alert should go way once the value is the same as the original", page.isModifiedAlertVisible());

		// Test saving modified parameters
		String newCoverage = "123";
		String newRepeat = "456";
		page.updateSavedParameterValue(MIMINUM_COVERAGE_PARAMETER, newCoverage);
		String REPEAT_MINIMUM_LENGTH_PARAMETER = "repeat-minimum-length";
		page.updateSavedParameterValue(REPEAT_MINIMUM_LENGTH_PARAMETER, newRepeat);
		assertTrue("Modified parameters alert should be displayed.", page.isModifiedAlertVisible());
		final String newModifiedTemplateName = "FOOBAR";
		page.saveModifiedTemplateAs(newModifiedTemplateName);
		assertEquals("Then newly template should be selected", newModifiedTemplateName, page.getSelectedParametersTemplateName());
	}

	private void addSamplesToCart() {
		LoginPage.loginAsUser(driver());
		ProjectSamplesPage samplesPage = ProjectSamplesPage.gotToPage(driver(), 1);
		samplesPage.selectSample(0);
		samplesPage.selectSample(1);
		samplesPage.addSelectedSamplesToCart();
		cartPage.selectPhylogenomicsPipeline();
	}
}
