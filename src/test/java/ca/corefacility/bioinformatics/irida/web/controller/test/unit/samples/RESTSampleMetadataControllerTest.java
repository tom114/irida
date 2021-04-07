package ca.corefacility.bioinformatics.irida.web.controller.test.unit.samples;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;

import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.ResourceCollection;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.sample.SampleMetadataResponse;
import ca.corefacility.bioinformatics.irida.web.controller.api.RESTGenericController;
import ca.corefacility.bioinformatics.irida.web.controller.api.samples.RESTSampleMetadataController;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class RESTSampleMetadataControllerTest {

	private RESTSampleMetadataController metadataController;
	private SampleService sampleService;
	private MetadataTemplateService metadataTemplateService;

	@Before
	public void setUp() {
		sampleService = mock(SampleService.class);
		metadataTemplateService = mock(MetadataTemplateService.class);

		metadataController = new RESTSampleMetadataController(sampleService, metadataTemplateService);
	}

	@Test
	public void testReadMultipleMetadata() {
		Sample s1 = new Sample("s1");
		s1.setId(1L);
		Sample s2 = new Sample("s2");
		s2.setId(2L);

		MetadataTemplateField f1 = new MetadataTemplateField("f1", "text");
		MetadataEntry entry1 = new MetadataEntry("val1", "text", f1);
		MetadataEntry entry2 = new MetadataEntry("val1", "text", f1);

		ArrayList<Long> ids = Lists.newArrayList(s1.getId(), s2.getId());

		when(sampleService.readMultiple(ids)).thenReturn(Lists.newArrayList(s1, s2));
		when(sampleService.getMetadataForSample(s1)).thenReturn(Sets.newHashSet(entry1));
		when(sampleService.getMetadataForSample(s2)).thenReturn(Sets.newHashSet(entry2));

		ModelMap modelMap = metadataController.getMultipleSampleMetadata(ids);

		ResourceCollection<SampleMetadataResponse> responses = (ResourceCollection) modelMap.get(
				RESTGenericController.RESOURCE_NAME);

		assertEquals(2, responses.size());
		for (SampleMetadataResponse response : responses) {
			assertEquals(1, response.getMetadata()
					.size());
			assertTrue(response.getMetadata()
					.keySet()
					.contains(f1));
		}

		verify(sampleService).readMultiple(ids);
		verify(sampleService).getMetadataForSample(s1);
		verify(sampleService).getMetadataForSample(s2);
	}

}