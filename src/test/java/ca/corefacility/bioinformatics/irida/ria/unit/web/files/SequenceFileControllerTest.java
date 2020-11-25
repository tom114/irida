package ca.corefacility.bioinformatics.irida.ria.unit.web.files;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;

import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SingleEndSequenceFile;
import ca.corefacility.bioinformatics.irida.ria.web.files.SequenceFileController;
import ca.corefacility.bioinformatics.irida.service.SequencingObjectService;

/**
 * Unit Tests for @{link SequenceFileController}
 *
 */
public class SequenceFileControllerTest {
	public static final Long FILE_ID = 1L;
	public static final Long OBJECT_ID = 2L;
	public static final String FILE_PATH = "src/test/resources/files/test_file.fastq";
	private static final Logger logger = LoggerFactory.getLogger(SequenceFileControllerTest.class);
	private SequenceFileController controller;

	// Services
	private SequencingObjectService objectService;

	@Before
	public void setUp() {
		objectService = mock(SequencingObjectService.class);
		controller = new SequenceFileController(objectService);

		Path path = Paths.get(FILE_PATH);
		SequenceFile file = new SequenceFile(path);
		file.setId(FILE_ID);
		SingleEndSequenceFile seqObject = new SingleEndSequenceFile(file);
		when(objectService.read(anyLong())).thenReturn(seqObject);
	}

	/**
	 *********************************************************************************************
	 * PAGE TESTS
	 *********************************************************************************************
	 */
	@Test
	public void testGetSequenceFilePage() {
		logger.debug("Testing getSequenceFilePage");
		String response = controller.getSequenceFilePage(OBJECT_ID, FILE_ID);
		assertEquals("Should return the correct page", SequenceFileController.FASTQC_PAGE, response);
	}

	/***********************************************************************************************
	 * AJAX TESTS
	 ***********************************************************************************************/

	@Test
	public void testDownloadSequenceFile() throws IOException {
		logger.debug("Testing downloadSequenceFile");
		MockHttpServletResponse response = new MockHttpServletResponse();

		controller.downloadSequenceFile(OBJECT_ID, FILE_ID, response);
		assertTrue("Response should contain a \"Content-Disposition\" header.",
				response.containsHeader("Content-Disposition"));
		assertEquals("Content-Disposition should include the file name", "attachment; filename=\"test_file.fastq\"",
				response.getHeader("Content-Disposition"));

		Path path = Paths.get(FILE_PATH);
		byte[] origBytes = Files.readAllBytes(path);
		byte[] responseBytes = response.getContentAsByteArray();
		assertArrayEquals("Response contents the correct file content", origBytes, responseBytes);
	}

}
