package ca.corefacility.bioinformatics.irida.repositories.sample;

import java.util.List;

import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;

public interface MetadataEntryRepositoryCustom {

	List<MetadataResponse> getMetadataForSampleAndFieldSorted(Project project,
			List<MetadataTemplateField> fields, String searchTerm);
}
